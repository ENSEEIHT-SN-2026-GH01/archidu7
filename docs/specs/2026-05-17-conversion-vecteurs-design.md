# Conversion CST → IR : extension aux vecteurs de signaux

**Date :** 2026-05-17 — **Auteur :** Alexis (`abd7852`) — **Branche :** `feature/parser-ll1-table-driven`

## Objectif

Étendre la couche `parser/conversion/` pour traduire les **vecteurs de signaux** SHDL
(`a[3..0]`, `a[3]`) en IR `erwan.Erwan`/`erwan.Module`. Aujourd'hui tout vecteur est
rejeté (`VECTOR_SUBSET_NOT_SUPPORTED`). Après cette extension, un circuit combinatoire
sur bus se convertit et se simule de bout en bout.

## Contexte

- **Entrée figée — le CST.** La grammaire LL(1) gère déjà les vecteurs :
  - `Signal ::= Identifiant Signal_Subset_Opt`
  - `Signal_Subset_Opt ::= '[' NaturalInteger Range_Opt ']' | ε`
  - `Range_Opt ::= DotDot NaturalInteger | ε` ; `DotDot ::= '..' | ':'`
- **Sortie figée — l'IR de Mati** (`erwan/`, code coéquipier intouchable). Primitives vecteurs déjà disponibles :
  - `List<Erwan> LITTERANGE(String nom, int debut, int fin)` — bits nommés `nom[i]`
  - `List<Erwan> ARANGE(String nom, int debut, int fin, List<Erwan> entrees)` — affectation vectorisée
  - `List<Erwan> ANDR/ORR(List<List<Erwan>> entrees)` — opérations bit à bit
  - `List<Erwan> NOTR(List<Erwan> entrees)`
  - `Erwan LITTERAL(String nom)` / `LITTERAL(String nom, int i)` ; `AFFECTATION(...)`
- La conversion est une **traduction mécanique** prise en étau entre ces deux contrats.

## Architecture

### Type interne `Bus`

`parser/conversion/Bus.java` — `record Bus(List<Erwan> bits)`. Largeur = `bits.size()`.
Un scalaire est un `Bus` de largeur 1. `ExpressionBuilder` renvoie un `Bus`
(au lieu d'un `Erwan`). L'IR raisonne déjà en `List<Erwan>` ; rendre la conversion
« native liste » s'aligne dessus et évite un double chemin scalaire/vecteur.

### Sous-ensemble d'un signal — `Subset`

`Names.java` cesse de rejeter les vecteurs et expose la plage du signal :
`record Subset(boolean isVector, int hi, int lo)`.
- scalaire `a` → `isVector=false` ; largeur 1 ; nom IR `a`
- `a[3]`   → `isVector=true, hi=lo=3` ; largeur 1 ; nom IR `a[3]`
- `a[3..0]`→ `isVector=true, hi=3, lo=0` ; largeur 4
- largeur = `isVector ? abs(hi-lo)+1 : 1`

`Names` fournit `SignalRef signalRef(CstNode signal)` → `(String nom, Subset subset)`,
et la même extraction pour le couple `(Identifiant, Signal_Subset_Opt)` du LHS.

### Flux de données

`ExpressionBuilder` (RHS) :
- **Factor `Signal`** : scalaire → `Bus([LITTERAL(nom)])` ; `a[i]` → `Bus([LITTERAL(nom,i)])` ;
  `a[d..f]` → `Bus(LITTERANGE(nom,d,f))`.
- **Factor `( SOT )`** : récursion, renvoie le `Bus` interne.
- **NotOp** : largeur 1 → `Erwan.NOT` ; largeur >1 → `Erwan.NOTR(bus.bits)`.
- **Term (AND)** : tous les opérandes au même `width` W. W=1 → `Erwan.AND` ;
  W>1 → `Erwan.ANDR(List<List<Erwan>>)`.
- **SumOfTerms (OR)** : idem avec `Erwan.OR`/`Erwan.ORR`.

`ModuleBuilder.buildInstance` (LHS) — renvoie désormais `List<Erwan>` (ajoutés au plan) :
- LHS scalaire `s`     → RHS largeur 1 → `[AFFECTATION("s", rhs.bit0)]`
- LHS `s[i]`           → RHS largeur 1 → `[AFFECTATION("s", i, rhs.bit0)]`
- LHS `s[d..f]`        → RHS largeur `abs(d-f)+1` → `ARANGE("s", lo, hi, rhs.bits)`
  avec `lo=min(d,f)`, `hi=max(d,f)` (`ARANGE` exige `IndiceDebut ≤ IndiceFin`).

`validateParams` accepte les paramètres vecteurs (`a[0..3]`) sans les rejeter.

## Validation

Nouveau motif `ConversionException.Reason.VECTOR_WIDTH_MISMATCH`, levé quand :
- deux opérandes d'un `+` ou `*` ont des largeurs différentes ;
- la largeur du LHS ≠ largeur du RHS.

Le zip des bits est **positionnel, par indice croissant**. `LITTERANGE` et
`ARANGE` (IR de Mati) bouclent `for i=debut; i<=fin` : un bus est toujours
matérialisé en indices croissants. Toute la chaîne (référence → opérations →
affectation) est donc cohérente en indice croissant : `s[i]` reçoit le calcul
portant sur les `Numero == i` des opérandes.

## Hors périmètre (restent rejetés)

Concaténation `&` (`CONCAT_NOT_SUPPORTED`), littéral en RHS
(`LITERAL_IN_RHS_NOT_SUPPORTED`), appels de modules (`MODULE_CALL_NOT_SUPPORTED`),
affectation mémoire (`MEMORY_ASSIGNMENT_NOT_SUPPORTED`).

**Limitations assumées :**
- Un identifiant nu `a` est toujours scalaire. Référencer un bus exige une plage explicite.
- **Bus inversé non exprimable.** `LITTERANGE`/`ARANGE` n'indexent qu'en ordre
  croissant ; `a[3..0]` et `a[0..3]` produisent le même bus. Un câblage
  délibérément inversé est hors de portée de l'IR actuelle.
- Les `Descripteur` d'E/S de `Module` restent vides — déjà le cas pour les scalaires,
  c'est un chantier distinct (câblage E/S).

## Tests

- `NamesTest` : extraction de `Subset` scalaire / `[i]` / `[d..f]`.
- `ExpressionBuilderTest` : facteur bus, `NOTR`, `ANDR`, `ORR`, mismatch de largeur.
- `ModuleBuilderTest` : LHS `s[d..f]` → `ARANGE`, LHS `s[i]`, mismatch LHS/RHS.
- `ConversionErrorsTest` : les rejets vecteurs disparaissent ; ajout des cas `VECTOR_WIDTH_MISMATCH`.
- Test d'intégration : un circuit sur bus 4 bits parse → convert → (simulation si possible).
