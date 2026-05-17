# Plan d'implémentation — Conversion vecteurs

> **Pour les workers agentiques :** exécution via subagent-driven-development.
> Étapes en cases à cocher (`- [ ]`). Spec de référence :
> `docs/specs/2026-05-17-conversion-vecteurs-design.md`.

**Objectif :** étendre `parser/conversion/` pour traduire les vecteurs SHDL en IR `erwan`.

**Architecture :** type interne `Bus` (= `List<Erwan>`, scalaire = largeur 1) traversant
`ExpressionBuilder` ; `Names` expose un `Subset` au lieu de rejeter les vecteurs ;
`ModuleBuilder` gère les LHS vecteurs via `ARANGE`.

**Stack :** Java 25, build `bash build.sh` / `bash build.sh test`, JUnit 4.13.2.

---

## Task 1 : Phase 1 — resynchroniser le cœur `erwan/`+`simulateur/` sur `378ca23`

**Contexte :** le worktree `feature` a importé `simulateur/` depuis l'ancien `0554592` ;
`simulateur/ModuleSimulateur.java` (typo bloquant) est à la racine et casse le build.
Sur `origin/simulation@378ca23` Mati l'a déplacé dans `PlusTard/` (hors build).

**Files :**
- Modify : `erwan/`, `simulateur/` (resync), `build.sh` si besoin

- [ ] **Étape 1 :** `git checkout origin/simulation -- erwan` (déjà identique, idempotent).
- [ ] **Étape 2 :** resynchroniser `simulateur/` sur `378ca23` : ne garder que le **cœur**
  (`erwan/`+`simulateur/` hors `affichage/`, `Exp/`, `PlusTard/`, `nouv/`). Supprimer du
  worktree les fichiers racine périmés (`simulateur/Experience*.java`,
  `simulateur/ModuleSimulateur.java`, etc.) qui sont sous `Exp/`/`PlusTard/` sur `378ca23`,
  et ne pas importer `Exp/`/`PlusTard/`/`nouv/`.
- [ ] **Étape 3 :** vérifier que `build.sh` exclut bien `simulateur/affichage/*`
  (déjà le cas) et ne ramasse pas `Exp/`/`PlusTard/` (absents → OK).
- [ ] **Étape 4 :** `bash build.sh` → attendu : `>> Build OK (N classes)`, exit 0.
- [ ] **Étape 5 :** `bash build.sh test` → compilation tests OK.
- [ ] **Étape 6 :** commit `build: resync coeur erwan/+simulateur/ sur simulation@378ca23`.

---

## Task 2 : `Subset`, motif `VECTOR_WIDTH_MISMATCH`, extraction dans `Names`

**Files :**
- Create : `parser/conversion/Subset.java`
- Modify : `parser/conversion/ConversionException.java`, `parser/conversion/Names.java`
- Test : `tests/parser/conversion/NamesTest.java`

- [ ] **Étape 1 :** ajouter `VECTOR_WIDTH_MISMATCH` à l'enum `Reason`
  (`ConversionException.java`).
- [ ] **Étape 2 :** créer `Subset` :

```java
package parser.conversion;

/** Sous-ensemble optionnel d'un signal : scalaire, index unique, ou plage. */
public record Subset(boolean isVector, int hi, int lo) {
    public static final Subset SCALAR = new Subset(false, 0, 0);
    public static Subset single(int i) { return new Subset(true, i, i); }
    public static Subset range(int hi, int lo) { return new Subset(true, hi, lo); }
    public int width() { return isVector ? Math.abs(hi - lo) + 1 : 1; }
}
```

- [ ] **Étape 3 :** dans `Names`, ajouter (sans supprimer les méthodes scalaires
  existantes) une méthode `Subset subsetOf(CstNode signalSubsetOpt)` : nœud `ε` →
  `Subset.SCALAR` ; sinon lire `NaturalInteger` (= `hi`) et `Range_Opt` (`ε` →
  `single(hi)` ; sinon second `NaturalInteger` = `lo` → `range(hi, lo)`).
  Lever `MALFORMED_CST` si la structure ne correspond pas.
- [ ] **Étape 4 :** ajouter `record SignalRef(String nom, Subset subset)` (fichier ou
  nested) et `Names.signalRef(CstNode signalNT)` qui extrait l'`Identifiant` et le
  `Subset` d'un nœud `Signal`.
- [ ] **Étape 5 :** écrire les tests `NamesTest` : `signalRef` sur `a`, `a[3]`,
  `a[3..0]`, `a[3:0]` ; vérifier `nom`, `isVector`, `hi`, `lo`, `width()`.
- [ ] **Étape 6 :** `bash build.sh test` puis lancer `NamesTest` → vert.
- [ ] **Étape 7 :** commit `feat(conversion): Subset + extraction vecteur dans Names`.

---

## Task 3 : type `Bus` + `ExpressionBuilder` vectorisé

**Files :**
- Create : `parser/conversion/Bus.java`
- Modify : `parser/conversion/ExpressionBuilder.java`, `parser/conversion/ModuleBuilder.java`
- Test : `tests/parser/conversion/ExpressionBuilderTest.java`

- [ ] **Étape 1 :** créer `Bus` :

```java
package parser.conversion;

import java.util.List;
import erwan.Erwan;

/** Faisceau de signaux ; un scalaire est un Bus de largeur 1. */
public record Bus(List<Erwan> bits) {
    public Bus { bits = List.copyOf(bits); }
    public int width() { return bits.size(); }
    public static Bus scalar(Erwan e) { return new Bus(List.of(e)); }
}
```

- [ ] **Étape 2 :** `ExpressionBuilder.build` et les `buildSOTC/buildSOT/buildTerm/
  buildFactor` renvoient désormais `Bus`.
  - `buildFactor` `Signal` : via `Names.signalRef` → scalaire `Bus.scalar(LITTERAL(nom))` ;
    `a[i]` → `Bus.scalar(LITTERAL(nom,i))` ; `a[d..f]` → `new Bus(LITTERANGE(nom,d,f))`.
  - `NotOp` : largeur 1 → `Bus.scalar(NOT(bit))` ; largeur >1 → `new Bus(NOTR(bus.bits()))`.
  - `buildTerm` : opérandes même largeur (sinon `VECTOR_WIDTH_MISMATCH`) ;
    W=1 → `Bus.scalar(AND(bits))` ; W>1 → `new Bus(ANDR(listeDeListes))`.
  - `buildSOT` : idem avec `OR`/`ORR`.
  - `LiteralValue` en RHS : conserver le rejet `LITERAL_IN_RHS_NOT_SUPPORTED`.
  - `Concat_SumOfTerms_Star` non vide : conserver `CONCAT_NOT_SUPPORTED`.
- [ ] **Étape 3 :** adapter le site d'appel dans `ModuleBuilder.buildInstance` :
  `ExpressionBuilder.build` renvoie un `Bus` ; pour ce task, le LHS reste scalaire —
  exiger `rhs.width() == 1` (sinon `VECTOR_WIDTH_MISMATCH`) et utiliser
  `rhs.bits().get(0)` dans `AFFECTATION`. Le LHS vecteur est traité au Task 4 —
  conserver pour l'instant le rejet `extractScalarFromIdAndSubset`.
- [ ] **Étape 4 :** mettre à jour `ExpressionBuilderTest` : les cas scalaires existants
  passent par `Bus` largeur 1 ; ajouter `a[3..0]` seul, `/a[3..0]` (NOTR),
  `a[3..0]*b[3..0]` (ANDR), `a[3..0]+b[3..0]` (ORR), et un mismatch
  `a[3..0]+b[1..0]` → `VECTOR_WIDTH_MISMATCH`.
- [ ] **Étape 5 :** `bash build.sh test` + tests conversion → vert.
- [ ] **Étape 6 :** commit `feat(conversion): Bus + ExpressionBuilder vectorisé`.

---

## Task 4 : `ModuleBuilder` — LHS vecteurs

**Files :**
- Modify : `parser/conversion/ModuleBuilder.java`, `parser/conversion/Names.java`
- Test : `tests/parser/conversion/ModuleBuilderTest.java`

- [ ] **Étape 1 :** `buildInstance` renvoie `List<Erwan>` (ajoutés au plan via `addAll`).
- [ ] **Étape 2 :** extraire le LHS via `Names.signalRef` (Identifiant + `Signal_Subset_Opt`).
  - LHS scalaire → `rhs.width()==1` requis → `[AFFECTATION(nom, rhs.bit0)]`.
  - LHS `s[i]` → `rhs.width()==1` requis → `[AFFECTATION(nom, i, rhs.bit0)]`.
  - LHS `s[d..f]` → `rhs.width()==abs(d-f)+1` requis (sinon `VECTOR_WIDTH_MISMATCH`) →
    `ARANGE(nom, d, f, rhs.bits())`.
- [ ] **Étape 3 :** la déduplication LHS (`DUPLICATE_LHS`) porte sur le nom de base ;
  conserver le comportement, clé = `nom` (+ plage si vecteur).
- [ ] **Étape 4 :** tests `ModuleBuilderTest` : LHS `s[3..0] = a[3..0]+b[3..0]` → plan de
  4 `Erwan` ; LHS `s[2]` ; mismatch LHS/RHS → `VECTOR_WIDTH_MISMATCH`.
- [ ] **Étape 5 :** `bash build.sh test` → vert.
- [ ] **Étape 6 :** commit `feat(conversion): ModuleBuilder gère les LHS vecteurs`.

---

## Task 5 : paramètres vecteurs, nettoyage, test d'intégration

**Files :**
- Modify : `parser/conversion/ModuleBuilder.java` (`validateParams`),
  `parser/conversion/Names.java` (retrait code mort)
- Test : `tests/parser/conversion/ConversionErrorsTest.java`,
  `tests/parser/conversion/ConversionIntegrationTest.java`

- [ ] **Étape 1 :** `validateParams` accepte les paramètres vecteurs (`a[0..3]`) :
  remplacer l'appel rejetant par `Names.signalRef` ; ne plus lever
  `VECTOR_SUBSET_NOT_SUPPORTED`.
- [ ] **Étape 2 :** supprimer les méthodes scalaires devenues mortes dans `Names`
  (`extractScalarFromSignalNT`, `extractScalarFromIdAndSubset`) si plus aucun appelant.
- [ ] **Étape 3 :** mettre à jour `ConversionErrorsTest` : retirer les cas attendant
  `VECTOR_SUBSET_NOT_SUPPORTED` ; ajouter les cas `VECTOR_WIDTH_MISMATCH`.
- [ ] **Étape 4 :** ajouter à `ConversionIntegrationTest` une fixture circuit sur bus
  4 bits (ex. ET/OU bit à bit) : parse → `Conversion.convert` → plan non vide,
  largeurs correctes.
- [ ] **Étape 5 :** `bash build.sh test` + suite complète conversion → vert.
- [ ] **Étape 6 :** commit `feat(conversion): paramètres vecteurs + tests d'intégration`.

---

## Revue finale

Après les 5 tasks : revue de code globale de l'implémentation, puis
`bash build.sh test` complet.
