# Spec — Conversion CST → Module (S1 : combinatoire scalaire)

**Auteur** : Alexis Briend
**Date** : 2026-05-06 (sprint 2, fin)
**Branche cible** : `feature/parser-ll1-table-driven`
**Mandat** : Erwan, par Discord — *« passer de l'arbre de dérivation au circuit […] prendre la grammaire et faire tous les cas, et pour chacun des cas, réduire le minimum au minimum vital pour Mati. et renvoyer une erreur si c'est pas valide »*.

## 1. Contexte

Le parser LL(1) table-driven (spec du 2026-04-30) produit un CST conforme à `Grammar.SHDL`. Erwan a posé sur `origin/interpretation` :

- un stub `parser/conversion/Conversion.java` (36 lignes, à remplir) ;
- un IR sémantique `simulateur.Erwan.Erwan` + enum `Operation` ;
- une classe `simulateur.Module` qui agrège un `List<Erwan> Plan` et trois listes d'I/O ;
- des bridges (`Descripteur`, `AppelModule`, `Branchement`).

Mati a posé en aval `simulateur.FileSimulateur(List<Erwan>)` qui consomme le `Plan` et produit un graphe de `Composant`/`Connecteur`/`Lien` simulable.

La présente spec couvre **uniquement** l'étage CST → `Module`. La simulation et l'UI sont hors scope.

## 2. Scope (S1)

### 2.1 Dans le scope (accepté)

Modules SHDL composés exclusivement d'assignations **combinatoires scalaires** :

- `Module → ModuleKW Identifiant LeftPar Param (Separ Param)* RightPar Instance+ EndKW ModuleKW`
- `Param → Signal` avec `Signal_Subset_Opt = ε`
- `Instance → Identifiant SignalAssignment` avec `Signal_Subset_Opt = ε` sur le LHS
- `SignalAssignment → AssignOp SumOfTermsCompound` avec `Concat_SumOfTerms_Star = ε`
- `SumOfTerms → Term (OrOp Term)*`
- `Term → Factor (AndOp Factor)*`
- `Factor → LeftPar SumOfTerms RightPar | NotOp Signal | Signal` avec `Signal_Subset_Opt = ε`

### 2.2 Hors scope (rejeté → `ConversionException`)

Tout le reste est syntaxiquement valide mais sémantiquement refusé en S1 :

- `Signal_Subset_Opt ≠ ε` quel que soit l'emplacement (LHS, RHS, Param). Couvre `[N]`, `[N..M]`, `[N:M]`.
- `Concat_SumOfTerms_Star ≠ ε` (`||` dans un `SignalAssignment`).
- `MemoryAssignment` (`:=`).
- `Operation → ModuleCall` et `Instance → Dollar Identifiant ModuleCall` (`$nom(...)`).
- `Factor → LiteralValue` (`.0` / `.1`) — refusé en S1, ouvert en S2 quand Mati clarifiera la sémantique des constantes côté `FileSimulateur`.
- Double assignation scalaire : deux `Instance` du même `Identifiant` LHS.

### 2.3 Justification

Le pivot est `List<Erwan> → FileSimulateur`. `FileSimulateur.recupSignal()` ne traite aujourd'hui que `LITTERAL/NOT/AND/OR`. Tout ce qui dépasse ce sous-ensemble lèverait soit une `RuntimeException` sans contexte côté Mati, soit produirait un comportement silencieux incorrect (cf. note sur les constantes en RHS, §5.4). Le scope S1 est exactement la fenêtre où un round-trip parse → convert → simulate produit un résultat correct vérifiable.

## 3. API publique

```java
package parser.conversion;

public class Conversion {
    public static Module convert(CstNode tree) throws ConversionException;
}
```

`tree` est attendu de symbole `Start` (sortie directe de `CstParser.parse`). La descente vers `Module` est faite par `Conversion`.

`ConversionException` (voir §6) remonte sans modification depuis n'importe quel niveau.

## 4. Architecture

### 4.1 Découpage

```
parser/conversion/
├── Conversion.java          point d'entrée public, descente Start → Module
├── ModuleBuilder.java       Module / Instance / Param + déduplication LHS
├── ExpressionBuilder.java   SumOfTerms / Term / Factor / Signal
├── Names.java               extraction scalaire (Identifiant + check SS_OPT vide)
└── ConversionException.java exception riche
```

Cinq fichiers, ~250-350 lignes au total. Aucun couplage circulaire :

- `Conversion` → `ModuleBuilder`
- `ModuleBuilder` → `ExpressionBuilder` + `Names`
- `ExpressionBuilder` → `Names`
- `Names` et `ConversionException` sont terminaux

### 4.2 Responsabilités

**`Conversion`** : valide la racine, `first(Module).orElseThrow(ConversionException)`, délègue à `ModuleBuilder.build(moduleNode)`. ~30 lignes.

**`ModuleBuilder`** :
1. Itère `Param` + `Separ_Param_Star` → produit la liste des noms de paramètres (utilisée uniquement pour valider la scalarité, **pas** stockée dans `Module.Entrees`, voir §5.1).
2. Itère `Instance_Plus` + `Instance_Star` à plat (récursion à droite aplatie).
3. Pour chaque `Instance` : extrait le LHS (Identifiant + Signal_Subset_Opt), vérifie scalarité via `Names`, vérifie non-redondance contre les LHS déjà vus, délègue le RHS à `ExpressionBuilder`, émet `Erwan.AFFECTATION(lhsName, rhsErwan)`.
4. Renvoie `new Module(plan, [], [], [])` — listes d'I/O laissées vides (voir §5.1).

**`ExpressionBuilder`** : visite récursive aplatie de `SumOfTermsCompound → SumOfTerms → Term → Factor`. Émet `Erwan` selon les règles d'aplatissement n-aire et de skip d'un seul opérande (voir §5.2 et §5.3).

**`Names`** : helpers statiques :
- `extractScalarFromSignalNT(CstNode signalNT) → String` : prend un nœud `Signal`, vérifie que `Signal_Subset_Opt = ε`, renvoie le texte de l'`Identifiant`. Lève `ConversionException` sinon.
- `extractScalarFromIdAndSubset(CstNode idLeaf, CstNode subsetOpt) → String` : variante pour le LHS d'`Instance` où l'`Identifiant` et le `Signal_Subset_Opt` sont des frères directs sous `Operation`, pas sous un `Signal`.

**`ConversionException`** : voir §6.

### 4.3 Flux de données

```
CstNode (Start)
    │
    ▼  Conversion.convert
CstNode (Module)
    │
    ▼  ModuleBuilder.build
    │      │
    │      ├─ paramètres : List<String>
    │      ├─ pour chaque Instance :
    │      │     LHS String  +  ExpressionBuilder.build(SOTC) → Erwan
    │      │                 ─────────────┐
    │      │                              ▼
    │      │                   Erwan.AFFECTATION(LHS, RHS)
    │      └─ List<Erwan> plan
    ▼
new Module(plan, [], [], [])
```

## 5. Règles sémantiques précises

### 5.1 Champs d'I/O de `Module` laissés vides en S1

`FileSimulateur(List<Erwan>)` ne lit que le `Plan` ; il calcule lui-même les ensembles d'entrées/sorties par différence sur les noms apparaissant en LHS vs en RHS. Les champs `Module.Entrees`, `Module.Sorties`, `Module.Branchements` sont donc **du code mort** vis-à-vis de Mati en S1. Les remplir mal serait un bug silencieux non détectable via le pipeline actuel ; les remplir bien dupliquerait la logique de Mati. ⇒ ils sont initialisés à `Collections.emptyList()`. Ouvert en S2 si un consommateur (GUI Chaptal ?) en a besoin.

### 5.2 Aplatissement n-aire

Les factories `Erwan.AND(List<Erwan>)` et `Erwan.OR(List<Erwan>)` sont n-aires. La grammaire produit un CST en récursion à droite (`Or_Operand_Star → OrOp Term Or_Operand_Star | ε`). Le visiteur **aplatit** :

- `a + b + c` → `OR([a, b, c])`, **pas** `OR(a, OR(b, c))`.
- `a * b * c` → `AND([a, b, c])`.

Précédence : `a + b * c` reste `OR(a, AND(b, c))` (la grammaire l'impose déjà).

### 5.3 Skip de wrapper à un seul opérande

Si `Or_Operand_Star = ε`, le `SumOfTerms` se réduit à un `Term` ; on émet le résultat de la visite du `Term` directement, **pas** un `OR([term])`. Idem pour `And_Operand_Star = ε` : `Term` se réduit à un `Factor`, on émet le `Factor` directement. Évite de produire des `Erwan` AND/OR à 1 entrée que `simulateur.And`/`Or` n'accepte pas.

### 5.4 Constantes en RHS — refusées en S1

`c <= .1` produirait `AFFECTATION("c", LITTERAL("1"))` (ce que fait `Erwan.CONSTANTE`), et `FileSimulateur` traiterait alors `"1"` comme une entrée nommée — bug silencieux côté Mati. En S1, `Factor → LiteralValue` lève `ConversionException`. Réservé à S2 (avec correction Mati).

### 5.5 Double assignation — refusée

`c <= a; c <= b` est syntaxiquement valide. `FileSimulateur.construction()` traite la 2ᵉ assignation de manière non-spécifiée (le `Dico.existe()` court-circuite la 2ᵉ visite, mais l'union des entrées subsiste — le résultat est un mélange). En S1, `ModuleBuilder` maintient un `Set<String>` des LHS déjà vus, et lève `ConversionException` à la 2ᵉ apparition.

### 5.6 Référence à un signal non-déclaré — non détectée en S1

Un Factor `LITTERAL("toto")` où `toto` n'est ni paramètre ni LHS précédent n'est **pas** détecté par Conversion en S1. `FileSimulateur` le traitera comme une entrée flottante. Documenté comme limitation S1, à ajouter en S2 si la table de symboles s'avère utile.

### 5.7 Récursion à droite des `*_Star`

Tous les `*_Star` dans la grammaire sont des récursions à droite (`X_Star → ε | tok X X_Star`). Le visiteur les aplatit en boucle locale, jamais par récursion sur la JVM. Patron unique partagé entre `Separ_Param_Star`, `Instance_Star`, `Or_Operand_Star`, `And_Operand_Star`. Évite les stack overflows et homogénéise le code.

## 6. Erreurs

### 6.1 `ConversionException`

Hérite de `RuntimeException` (cohérent avec `ParsingException`). Champs immutables :

- `int offset()` — offset source du nœud CST déclencheur (`startOffset()` du CST node).
- `String nodeKind()` — symbole du nœud déclencheur (NT name ou Token name).
- `Reason reason()` — enum énumérant les rejets S1 :
  - `VECTOR_SUBSET_NOT_SUPPORTED` (LHS, RHS ou Param avec `[...]`)
  - `CONCAT_NOT_SUPPORTED` (`||`)
  - `MEMORY_ASSIGNMENT_NOT_SUPPORTED` (`:=`)
  - `MODULE_CALL_NOT_SUPPORTED` (LHS `Dollar` ou RHS `Operation → ModuleCall`)
  - `LITERAL_IN_RHS_NOT_SUPPORTED` (`.0`/`.1` en Factor)
  - `DUPLICATE_LHS` (assignation multiple du même Identifiant)
  - `MALFORMED_CST` (cas qui ne devraient jamais arriver si CST vient de `CstParser` valide)
- `String message()` — phrase humaine, ex. `"Vecteur non supporté en S1 (paramètre 'a[0..3]', offset 27)"`.

L'éditeur Chaptal pourra surligner via `offset()` comme pour `ParsingException`. Aucun `try/catch` interne dans `Conversion` — tout remonte.

### 6.2 Pas de NPE silencieux

Aucun `orElseThrow()` sans `ConversionException` typé. Aucun `children.get(0)` sans guard préalable. Tout chemin "ne devrait pas arriver" lève `MALFORMED_CST` plutôt qu'un NPE.

## 7. Tests (T1)

Trois couches, JUnit 4 (cohérent avec l'existant), dans `tests/parser/conversion/`.

### 7.1 Tests unitaires `ExpressionBuilder` (~12 tests)

Un test par "shape" d'expression scalaire :
- `a` → `LITTERAL("a")`
- `!a` → `NOT(LITTERAL("a"))`
- `a * b` → `AND([a, b])`
- `a + b` → `OR([a, b])` (vérifie `Op == OR`, sentinelle anti-régression sur le bug `Erwan.OR()`)
- `a * b * c` → `AND([a, b, c])` (n-aire)
- `a + b + c` → `OR([a, b, c])` (n-aire)
- `a + b * c` → `OR(a, AND(b, c))` (précédence)
- `a * (b + c)` → `AND(a, OR(b, c))` (parens)
- `(a)` → `LITTERAL("a")` (parens dégénérées)
- `!a * !b` → `AND(NOT(a), NOT(b))`
- `a` réutilisé → `LITTERAL` distincts mais `Nom` identique
- arbre profond (≥4 niveaux de parens) → ne stack-overflow pas

### 7.2 Tests unitaires `ModuleBuilder` (~6 tests)

- module à 1 paramètre 1 instance → `Plan.size() == 1`, `AFFECTATION("c", ...)` correct
- module à 2 instances → ordre préservé
- LHS dupliqué → `DUPLICATE_LHS`
- paramètre vectoriel → `VECTOR_SUBSET_NOT_SUPPORTED`
- LHS vectoriel → `VECTOR_SUBSET_NOT_SUPPORTED`
- paramètre scalaire jamais référencé en RHS → toléré (warning hors scope)

### 7.3 Tests d'erreur (~8 tests)

Un test négatif par production hors S1 :
- `c <= a || b` → `CONCAT_NOT_SUPPORTED`
- `c := a on clk` → `MEMORY_ASSIGNMENT_NOT_SUPPORTED`
- `$add(a, b, c)` → `MODULE_CALL_NOT_SUPPORTED`
- `c <= .1` → `LITERAL_IN_RHS_NOT_SUPPORTED`
- `c[0] <= a` → `VECTOR_SUBSET_NOT_SUPPORTED`
- `c <= a[3]` → `VECTOR_SUBSET_NOT_SUPPORTED`
- chaque erreur expose `offset()` correct (vérifié par calcul manuel)
- pas de NPE pour aucune source SHDL parseable

### 7.4 Smoke test e2e (1 test, **écrit en premier**)

`module et(a, b) c <= a * b end module` :
1. `CstParser.parse(source)` → CST
2. `Conversion.convert(cst)` → `Module`
3. `new FileSimulateur(module.Plan)` ne plante pas
4. Vérifie une ligne de table de vérité (les détails d'API de `FileSimulateur` à arbitrer au moment du test).

Si ce smoke test plante côté Mati (boucle, NPE), la spec sera replanifiée avec lui avant d'écrire les ~25 autres tests.

### 7.5 Audit fixtures existantes

Les 9 modules SHDL utilisés par `CstParserMultiInstanceTest` sont audités fixture par fixture **avant** rédaction des tests d'intégration. Pour chaque fixture :
- Si S1-valide → test positif `Module` non-null + sanity sur la taille du `Plan`.
- Si S1-invalide → test négatif `ConversionException` avec la bonne `Reason`.

Le décompte final n'est pas garanti `9/9`. La spec ne promet **pas** "9 tests d'intégration positifs".

### 7.6 Test sentinelle sur `Erwan.OR()`

Test JUnit dans `tests/simulateur/Erwan/` (fichier neuf à ajouter par accord avec Erwan, ou ajouté dans `TestErwan.java`) qui vérifie `Erwan.OR(...).Op == Operation.OR`. Empêche la régression du bug fixé en pré-requis.

## 8. Pré-requis (hors livrable)

À régler **avant** d'écrire la moindre ligne de Conversion. Tous nécessitent l'accord d'Erwan.

1. **Bug `Erwan.OR()` ligne 145** : `return new Erwan(Operation.AND, ...)` → `Operation.OR`.
2. **`Module` ctor 2 ne compile pas** : `this(...)` après deux boucles `for`. À refondre côté Erwan (méthode privée `splitBranchements` + ctor 2 qui appelle `this(...)` en tête, **ou** suppression du ctor 2 si non utilisé).
3. **Trancher "scalaire = SS_OPT vide strict, partout"** : confirmation écrite d'Erwan.

Une fois ces trois points faits côté Erwan et merge `origin/interpretation` réalisé dans `feature/parser-ll1-table-driven`, on lance la suite parser (151 tests) pour vérifier qu'aucune régression n'est introduite par les modifications cosmétiques d'Erwan sur `CstInternal.java` / `CstLeaf.java`.

## 9. Stratégie de merge

`git merge origin/interpretation` dans `feature/parser-ll1-table-driven`.

Conflits attendus uniquement sur `parser/ll1/tabledriven/cst/CstInternal.java` et `CstLeaf.java` — modifications cosmétiques de formatage côté Erwan (vérifié). Stratégie : conserver la version locale (formatage canonique de la branche), résoudre manuellement, lancer les 151 tests parser. Si rouge, replanifier avant Conversion.

## 10. Ordre d'implémentation

1. Pré-requis Erwan (§8) — bloquant.
2. Merge + run suite parser — vert obligatoire.
3. Smoke test e2e (§7.4) — premier code écrit, vert obligatoire.
4. `ConversionException` + `Names` (TDD).
5. `ExpressionBuilder` (TDD, tests §7.1 d'abord).
6. `ModuleBuilder` (TDD, tests §7.2 d'abord).
7. `Conversion` (driver public).
8. Tests d'erreur §7.3.
9. Audit fixtures §7.5 + tests d'intégration correspondants.
10. Test sentinelle §7.6.

Commits petits et fréquents (un par étape TDD : test rouge → implémentation → test vert → commit).

## 11. Non-objectifs (S2 et au-delà)

- Vectoriel (extension `recupSignal` côté Mati + extension de `ExpressionBuilder` pour `ARANGE/ANDR/ORR/NOTR`).
- Concat `||` (nouvelle factory IR + extension Mati).
- `MemoryAssignment` (nouvelle factory IR + classe `Bascule` côté Mati + horloge + traitement séquentiel dans `FileSimulateur`).
- `ModuleCall` (TODO côté Erwan, bouchons `APPELMODULE`/`MODULE`).
- Table de symboles + détection signaux non-déclarés.
- Remplissage de `Module.Entrees` / `Sorties` / `Branchements`.
- Constantes en RHS (`.0` / `.1`).
