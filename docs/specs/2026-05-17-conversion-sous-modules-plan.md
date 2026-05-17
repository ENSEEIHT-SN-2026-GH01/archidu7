# Plan d'implémentation — Conversion des appels de sous-modules

> **Pour les workers agentiques :** exécution via subagent-driven-development.
> Étapes en cases à cocher (`- [ ]`). Spec de référence :
> `docs/specs/2026-05-17-conversion-sous-modules-design.md`.

**Objectif :** étendre `parser/conversion/` pour traduire les appels de modules
SHDL (`$sub(... : ...)`) en `AppelModule` dans les `Branchements` du `Module` IR.

**Architecture :** `Module.Entrees`/`Sorties` peuplés depuis la signature (découpe
sur `:`) ; `ModuleResolver` résout les modules appelés en multi-fichiers
(mémoïsation + détection de cycle) ; `ModuleCallBuilder` traduit un `ModuleCall`
en `AppelModule`.

**Stack :** Java 25, build `bash build.sh` / `bash build.sh test`, JUnit 4.13.2.

---

## Task 1 : `ConversionException` — motifs d'erreur

**Files :**
- Modify : `parser/conversion/ConversionException.java`

- [ ] **Étape 1 :** ajouter à l'enum `Reason` : `MODULE_NOT_FOUND`,
  `MODULE_CALL_CYCLE`, `MODULE_CALL_INVALID_ARG`, `MODULE_BAD_SEPARATORS`,
  `MODULE_ARITY_MISMATCH`, `DUPLICATE_MODULE_DEFINITION`.
- [ ] **Étape 2 :** ne PAS retirer `MODULE_CALL_NOT_SUPPORTED` (encore utilisé jusqu'au Task 5).
- [ ] **Étape 3 :** `bash build.sh` → vert. Commit `feat(conversion): motifs d'erreur appels de modules`.

---

## Task 2 : `Module.Entrees`/`Sorties` depuis la signature

**Files :**
- Modify : `parser/conversion/Names.java`, `parser/conversion/ModuleBuilder.java`
- Test : `tests/parser/conversion/NamesTest.java`, `tests/parser/conversion/ModuleBuilderTest.java`

- [ ] **Étape 1 :** dans `Names`, ajouter `Descripteur descriptorOf(CstNode signalNode)` :
  via `signalRef` obtenir `(nom, Subset)` ; scalaire → `new Descripteur(nom)` ;
  vecteur → `new Descripteur(nom, subset.minIndex(), subset.maxIndex())`.
  Importer `erwan.Descripteur`.
- [ ] **Étape 2 :** dans `ModuleBuilder`, créer `record Signature(List<Descripteur> entrees, List<Descripteur> sorties)`
  et `private static Signature buildSignature(CstInternal mod)` : parcourt
  `Param Separ_Param_Star`, lit le token de chaque `Separ` (`Comma`/`Colon`) ;
  les params avant l'unique `Colon` → `entrees`, après → `sorties` ; aucun
  `Colon` → tous dans `entrees`, `sorties` vide ; plus d'un `Colon` →
  `MODULE_BAD_SEPARATORS`. Remplace `validateParams`.
- [ ] **Étape 3 :** dans `ModuleBuilder.build`, appeler `buildSignature` et
  construire `new Module(moduleName, plan, sig.entrees(), sig.sorties(), Collections.emptyList())`.
- [ ] **Étape 4 :** tests `NamesTest` : `descriptorOf` sur `a`, `a[3]`, `a[3..0]`
  (vérifier `Nom()`, `nbSignaux()`, `indiceDebut/Fin`).
- [ ] **Étape 5 :** tests `ModuleBuilderTest` : `module fa(a,b,cin : s,cout)` →
  `Entrees` = [a,b,cin], `Sorties` = [s,cout] (ordre + noms) ; `module mux(a,b,sel)` →
  `Sorties` vide ; signature à deux `:` → `MODULE_BAD_SEPARATORS`.
- [ ] **Étape 6 :** `bash build.sh test` + suite conversion → vert. Commit
  `feat(conversion): peupler Module.Entrees/Sorties depuis la signature`.

---

## Task 3 : `ModuleResolver` + résolution multi-fichiers

**Files :**
- Create : `parser/conversion/ModuleResolver.java`
- Modify : `parser/conversion/ModuleBuilder.java`, `parser/conversion/Conversion.java`
- Test : `tests/parser/conversion/ModuleResolverTest.java`, mise à jour des appelants de `ModuleBuilder.build`

- [ ] **Étape 1 :** changer la signature `ModuleBuilder.build(CstNode moduleNode, ModuleResolver resolver)`.
  Pour ce task, `resolver` n'est pas encore utilisé dans `build` (les appels
  restent rejetés via `MODULE_CALL_NOT_SUPPORTED`) — il est seulement transmis.
- [ ] **Étape 2 :** créer `ModuleResolver` :
  - constructeur `ModuleResolver(Collection<CstNode> fichiers)` : pour chaque CST,
    descendre `Start → Module`, extraire le nom (`Identifiant` après `ModuleKW`),
    remplir `Map<String,CstNode>` ; nom déjà présent → `DUPLICATE_MODULE_DEFINITION`.
  - `Module resolve(String nom)` : cache `Map<String,Module>` (hit → renvoie) ;
    `Set<String>` pile de résolution (nom déjà présent → `MODULE_CALL_CYCLE`) ;
    nom absent de la table CST → `MODULE_NOT_FOUND` ; sinon empile, appelle
    `ModuleBuilder.build(cst, this)`, dépile, met en cache, renvoie.
  - `String mainName()` ou expose le nom du premier fichier fourni.
- [ ] **Étape 3 :** réécrire `Conversion` :
  - `convert(CstNode tree)` → `convert(tree, java.util.List.of())`.
  - `convert(CstNode main, Collection<CstNode> library)` : construit la liste
    `main` + `library`, instancie `ModuleResolver`, renvoie `resolver.resolve(nomDeMain)`.
- [ ] **Étape 4 :** mettre à jour tout appelant direct de `ModuleBuilder.build`
  (notamment le helper `build(...)` de `ModuleBuilderTest`) pour fournir un
  `ModuleResolver` contenant le CST du module testé.
- [ ] **Étape 5 :** tests `ModuleResolverTest` : indexation et `resolve` d'un
  module simple ; deux fichiers même nom → `DUPLICATE_MODULE_DEFINITION` ;
  `resolve` d'un nom absent → `MODULE_NOT_FOUND` ; mémoïsation (deux `resolve`
  du même nom renvoient la même instance).
- [ ] **Étape 6 :** `bash build.sh test` + suite complète → vert. Commit
  `feat(conversion): ModuleResolver + API Conversion multi-fichiers`.

---

## Task 4 : `ModuleCallBuilder` — `ModuleCall` → `AppelModule`

**Files :**
- Create : `parser/conversion/ModuleCallBuilder.java`
- Test : `tests/parser/conversion/ModuleCallBuilderTest.java`

- [ ] **Étape 1 :** créer `ModuleCallBuilder` avec
  `static AppelModule build(CstNode moduleCallNode, erwan.Module called)`.
- [ ] **Étape 2 :** parcourir `ModuleCall → '(' Arg Separ_Arg_Star ')'` : collecter
  la liste ordonnée des `Arg` et, pour chaque séparateur, le token sous `Separ`.
- [ ] **Étape 3 :** localiser l'unique `Colon` ; nombre de `Colon` ≠ 1 →
  `MODULE_BAD_SEPARATORS`. Args avant → entrées, après → sorties.
- [ ] **Étape 4 :** chaque `Arg` doit être un `Signal` nu : si `Signal_Or_Litteral_Value`
  est un `LiteralValue`, ou si `Concat_Signal_Or_Litteral_Value_Star` est non vide →
  `MODULE_CALL_INVALID_ARG`. Sinon `Names.descriptorOf(signal)` → `Descripteur`.
- [ ] **Étape 5 :** validation d'arité : `DE.size() != called.Entrees.size()` ou
  `DS.size() != called.Sorties.size()` ou un `nbSignaux()` divergent →
  `MODULE_ARITY_MISMATCH`.
- [ ] **Étape 6 :** renvoyer `new AppelModule(called, DE, DS)`.
- [ ] **Étape 7 :** tests `ModuleCallBuilderTest` avec un `erwan.Module` construit
  à la main : découpe `DE`/`DS` correcte ; descripteur scalaire et vecteur ;
  argument littéral → `MODULE_CALL_INVALID_ARG` ; zéro/deux `:` →
  `MODULE_BAD_SEPARATORS` ; arité fausse → `MODULE_ARITY_MISMATCH`.
- [ ] **Étape 8 :** `bash build.sh test` → vert. Commit
  `feat(conversion): ModuleCallBuilder (ModuleCall → AppelModule)`.

---

## Task 5 : `ModuleBuilder` — câbler les appels

**Files :**
- Modify : `parser/conversion/ModuleBuilder.java`, `parser/conversion/ConversionException.java`
- Test : `tests/parser/conversion/ModuleBuilderTest.java`, `tests/parser/conversion/ConversionErrorsTest.java`

- [ ] **Étape 1 :** dans `buildInstance`, détecter une `Instance` contenant un
  nœud `ModuleCall` (forme `Dollar Identifiant ModuleCall`, ou `Identifiant Operation`
  avec `Operation → ModuleCall`). Vérifier la forme réelle contre un dump du CST.
- [ ] **Étape 2 :** pour un appel : extraire le nom du module appelé (l'`Identifiant`
  adjacent au `ModuleCall`), appeler `resolver.resolve(nom)`, puis
  `ModuleCallBuilder.build(moduleCallNode, moduleResolu)` → `AppelModule`.
- [ ] **Étape 3 :** `build` accumule les `AppelModule` dans une `List<AppelModule> branchements`
  (à côté du `List<Erwan> plan`) et construit
  `new Module(nom, plan, entrees, sorties, branchements)`.
- [ ] **Étape 4 :** retirer `MODULE_CALL_NOT_SUPPORTED` de l'enum `Reason` et tout
  code le levant.
- [ ] **Étape 5 :** mettre à jour `ConversionErrorsTest` : retirer les cas
  attendant `MODULE_CALL_NOT_SUPPORTED`.
- [ ] **Étape 6 :** tests `ModuleBuilderTest` (via `Conversion.convert(main, library)`) :
  un module appelant un sous-module → `Branchements` de taille 1, `AppelModule`
  bien formé ; appel d'un module inconnu → `MODULE_NOT_FOUND` ; cycle
  `A`↔`B` → `MODULE_CALL_CYCLE`.
- [ ] **Étape 7 :** `bash build.sh test` + suite complète → vert. Commit
  `feat(conversion): ModuleBuilder convertit les appels en AppelModule`.

---

## Task 6 : Test d'intégration multi-fichiers

**Files :**
- Test : `tests/parser/conversion/ConversionIntegrationTest.java`

- [ ] **Étape 1 :** ajouter un test : deux sources SHDL en chaîne (`String`), un
  module `top` qui fait `$fa(...)` vers un module `fa` défini séparément. Parser
  chacune (`CstParser.parse`), appeler `Conversion.convert(topCst, List.of(faCst))`.
- [ ] **Étape 2 :** vérifier : `Module.Branchements` taille 1 ; l'`AppelModule`
  pointe vers un `Module` de nom `fa` ; `DE`/`DS` de tailles attendues ;
  `Entrees`/`Sorties` du module `top` et du sous-module corrects.
- [ ] **Étape 3 :** `bash build.sh test` + suite complète → vert. Commit
  `test(conversion): intégration appel de sous-module multi-fichiers`.

---

## Revue finale

Après les 6 tasks : revue de code globale, `bash build.sh test` complet. Vérifier
que `EndToEndSimulationTest` passe toujours — si la simulation autonome d'un
module passe par `FileSimulateur(erwan.Module)`, router via `FileSimulateur(m.Plan)`.
