# Conversion CST → IR : appels de sous-modules

**Date :** 2026-05-17 — **Auteur :** Alexis (`abd7852`) — **Branche :** `feature/parser-ll1-table-driven`

## Objectif

Étendre `parser/conversion/` pour traduire les **appels de modules** SHDL
(`$fulladder(a, b, cin : s, cout)`) en IR `erwan`. Aujourd'hui tout appel est
rejeté (`MODULE_CALL_NOT_SUPPORTED`). Après cette extension, un circuit qui
instancie un sous-module se convertit en un `erwan.Module` dont les
`Branchements` contiennent les `AppelModule` correspondants.

## Contexte

- **Entrée figée — le CST.** La grammaire LL(1) gère déjà les appels :
  - `Instance ::= Identifiant Operation | Dollar Identifiant ModuleCall`
  - `Operation ::= ModuleCall | Signal_Subset_Opt Assignment`
  - `ModuleCall ::= '(' Arg Separ_Arg_Star ')'`
  - `Separ_Arg_Star ::= Separ Arg Separ_Arg_Star | ε` ; `Separ ::= Comma | Colon`
  - `Arg ::= SignalOrLiteralCompound` ; `Signal_Or_Litteral_Value ::= Signal | LiteralValue`
  - `Module ::= ModuleKW Identifiant '(' Param Separ_Param_Star ')' Instance_Plus EndKW ModuleKW`
- **Sortie figée — l'IR de Mati** (`erwan/`, code coéquipier intouchable) :
  - `Module(String Nom, List<Erwan> Plan, List<Descripteur> Entrees, List<Descripteur> Sorties, List<AppelModule> Branchements)` — constructeur 5-arg, pose `Nom`.
  - `AppelModule(Module module, List<Descripteur> DE, List<Descripteur> DS)` — `module` est le sous-module appelé, `DE`/`DS` les descripteurs des signaux fournis en entrée/sortie par le circuit appelant.
  - `Descripteur(String Nom)` (signal scalaire) et `Descripteur(String Nom, int d, int f)` (vecteur, `d ≤ f`).
- **Contrat du simulateur.** `FileSimulateur` (constructeur `FileSimulateur(erwan.Module)`) apparie **positionnellement** `A.DE.get(i)` ↔ `A.module.Entrees.get(i)` et vérifie l'égalité des tailles (`FileSimulateur.java:29-40`). La conversion doit donc produire des `Module.Entrees`/`Sorties` **ordonnés**, et des `AppelModule` cohérents en arité.
- Un fichier `.shdl` contient **exactement un module** (`Start ::= Module`). Un appel `$sub` vise donc un module défini dans un autre fichier : la résolution est inter-fichiers.

## Architecture

### Type interne `Signature`

`record Signature(List<Descripteur> entrees, List<Descripteur> sorties)`.
Produit par l'analyse de la liste de paramètres du `Module`.

### `Module.Entrees` / `Module.Sorties` — depuis la signature, découpées sur `:`

La liste de paramètres `Param Separ_Param_Star` est découpée sur le séparateur
`Colon` : les paramètres **avant** le `:` deviennent `Entrees`, ceux **après**
deviennent `Sorties`, dans l'ordre d'écriture.

- `module fa(a, b, cin : s, cout)` → `Entrees=[a,b,cin]`, `Sorties=[s,cout]`.
- `module mux(a, b, sel)` (aucun `:`) → `Entrees=[a,b,sel]`, `Sorties=[]`.
  Le module reste simulable seul (`FileSimulateur(List<Erwan>)` dérive les E/S
  du `Plan`) mais n'expose pas d'interface de sortie : il n'est pas appelable
  comme sous-module producteur. Aucune fixture S1 existante n'est cassée.

Chaque paramètre `Signal` devient un `Descripteur` : scalaire `a` →
`new Descripteur("a")` ; vecteur `a[3..0]` → `new Descripteur("a", minIndex, maxIndex)`
(`Descripteur` indexe en ordre croissant, comme `LITTERANGE`).

**Décision assumée :** le `:` de la signature porte la sémantique
entrées/sorties. La grammaire autorise déjà `Separ` (donc `:`) dans la liste de
paramètres — le langage n'est pas modifié, on lui donne un sens. C'est
symétrique avec la syntaxe d'appel et fidèle au SHDL de référence.

### `ModuleResolver` — résolution multi-fichiers

`parser/conversion/ModuleResolver.java`. Reçoit une `Collection<CstNode>` (un
CST par fichier). À la construction : extrait le nom de chaque module
(`Identifiant` suivant `ModuleKW`) → `Map<String, CstNode>`. Deux fichiers
définissant le même nom → `DUPLICATE_MODULE_DEFINITION`.

`Module resolve(String nom)` :
- nom déjà dans le cache `Map<String,Module>` → le renvoie (mémoïsation, gère les DAG) ;
- nom présent dans la pile de résolution courante → `MODULE_CALL_CYCLE` ;
- nom absent de la table des CST → `MODULE_NOT_FOUND` ;
- sinon : empile le nom, `m = ModuleBuilder.build(cst, this)`, dépile, met en cache, renvoie `m`.

`ModuleResolver` et `ModuleBuilder` sont mutuellement récursifs : `resolve` →
`build` → (sur chaque `$appel`) `resolve`.

### `ModuleCallBuilder` — un `ModuleCall` → un `AppelModule`

`parser/conversion/ModuleCallBuilder.java`. Reçoit le nœud CST `ModuleCall` et
le `Module` appelé (déjà résolu). Produit l'`AppelModule` :

- Parcourt `Arg` + `Separ_Arg_Star` ; pour chaque séparateur, lit le token sous
  `Separ` (`Comma` ou `Colon`).
- Le `Colon` unique marque la frontière : args avant → `DE`, args après → `DS`.
  Nombre de `Colon` ≠ 1 → `MODULE_BAD_SEPARATORS`.
- Chaque `Arg` doit être un `Signal` nu (scalaire ou sous-ensemble vecteur).
  `LiteralValue` ou concaténation (`Concat_..._Star` non vide) → `MODULE_CALL_INVALID_ARG`.
- Chaque `Signal` → `Descripteur` (mêmes règles que les paramètres).
- Validation d'arité : `DE.size()` = `module.Entrees.size()`, `DS.size()` =
  `module.Sorties.size()`, et `nbSignaux()` égaux descripteur par descripteur →
  sinon `MODULE_ARITY_MISMATCH`.
- Renvoie `new AppelModule(module, DE, DS)`.

### `ModuleBuilder` — modifications

- `build(CstNode moduleNode, ModuleResolver resolver)` (nouvelle signature).
- Construit la `Signature` → peuple `Module.Entrees`/`Sorties`.
- `buildInstance` cesse de rejeter les appels : une `Instance` contenant un
  nœud `ModuleCall` (forme `$Identifiant ModuleCall`, ou `Identifiant Operation`
  avec `Operation → ModuleCall`) → extrait le nom du module appelé (l'`Identifiant`
  adjacent au `ModuleCall`), `resolver.resolve(nom)`, puis `ModuleCallBuilder`
  → `AppelModule` collecté dans une liste `branchements`.
- `build` renvoie `new Module(nom, plan, entrees, sorties, branchements)`.

### `Conversion` — API

- `convert(CstNode tree)` — conservée ; équivaut à `convert(tree, List.of())`.
  Un module sans appel se convertit ; un `$appel` non résolu y lève
  `MODULE_NOT_FOUND` (comportement correct).
- `convert(CstNode main, Collection<CstNode> library)` — neuve : construit un
  `ModuleResolver` sur `main` + `library`, renvoie `resolver.resolve(nomDeMain)`.

### Flux

```
fichiers .shdl ──parse──▶ CstNode×N
                              │
                              ▼  Conversion.convert(main, library)
                        ModuleResolver  (table nom→CST, cache, pile)
                              │ resolve(nomMain)
                              ▼
                        ModuleBuilder.build(cst, resolver)
                          ├─ Signature → Entrees/Sorties
                          ├─ Instances assignation → Plan (List<Erwan>)
                          └─ Instances $appel :
                               resolver.resolve(sousModule)  ──▶ Module
                               ModuleCallBuilder(moduleCall, Module) ──▶ AppelModule
                              ▼
                  new Module(nom, Plan, Entrees, Sorties, Branchements)
```

## Validation

Nouveaux motifs `ConversionException.Reason` :

- `MODULE_NOT_FOUND` — `$appel` vers un module absent de la bibliothèque.
- `MODULE_CALL_CYCLE` — cycle d'appels (`A` appelle `B` appelle `A`).
- `MODULE_CALL_INVALID_ARG` — argument d'appel littéral ou concaténé.
- `MODULE_BAD_SEPARATORS` — nombre de `:` ≠ 1 dans un appel, ou > 1 dans une signature.
- `MODULE_ARITY_MISMATCH` — arité ou largeur d'un descripteur d'appel ≠ interface du module appelé.
- `DUPLICATE_MODULE_DEFINITION` — deux fichiers définissent le même nom de module.

L'ancien `MODULE_CALL_NOT_SUPPORTED` disparaît.

## Hors périmètre (restent rejetés)

Argument d'appel littéral (`.0101`) ou concaténé (`a & b`), expression/formule
en argument, appel imbriqué comme argument (`$m($n(...) : x)`). Affectation
mémoire. Concaténation et littéraux en RHS d'assignation (inchangé).

**Limitations assumées :**
- Un module sans `:` dans sa signature n'a pas de sorties déclarées : appelable
  seulement si l'appel ne lui demande aucune sortie.
- La vérification que les sorties déclarées sont effectivement assignées dans le
  corps, et les entrées effectivement lues, n'est pas faite (cf. `cst-conversion-design` §5.6).

## Tests

- `NamesTest` : `descriptorOf` scalaire / `[i]` / `[d..f]`.
- `ModuleBuilderTest` : signature avec/sans `:` → `Entrees`/`Sorties` corrects et ordonnés ; signature à deux `:` → `MODULE_BAD_SEPARATORS`.
- `ModuleResolverTest` : indexation, mémoïsation, `DUPLICATE_MODULE_DEFINITION`, `MODULE_NOT_FOUND`, `MODULE_CALL_CYCLE`.
- `ModuleCallBuilderTest` : découpe `DE`/`DS` sur `:`, descripteurs scalaires/vecteurs, `MODULE_CALL_INVALID_ARG`, `MODULE_BAD_SEPARATORS`, `MODULE_ARITY_MISMATCH`.
- `ConversionIntegrationTest` : circuit deux fichiers (un module appelant un sous-module) → `convert` → `Branchements` non vide, `AppelModule` bien formé.
