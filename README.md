# Parser LL(1) SHDL — Table-Driven + Conversion

Branche dédiée aux axes **parser LL(1) table-driven** et **conversion CST → IR
simulateur** du projet long TOB (simulateur SHDL, N7 1SN, 2025-2026).

Pipeline : Source SHDL → tokens → **CST** (table d'analyse M[NT, T]) →
**`erwan.Module`** prêt à être passé à `FileSimulateur`.

## Prérequis

- **JDK 25**
- Bash
- JUnit 4.13.2 + Hamcrest 1.3 (fournis dans `lib/`)

## Build & tests

    ./build.sh test     # compile parser + conversion + simulateur (hors affichage) + tests
    ./run.sh            # batterie JUnit (193 tests)

Si des classes ont été supprimées ou renommées :

    rm -rf bin/* && ./build.sh test

## Pipeline complet (point d'entrée pour le bundle JavaFX)

```java
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import erwan.Module;
import simulateur.FileSimulateur;

CstNode cst   = CstParser.parse(shdlSource);          // peut lever ParsingException
Module module = Conversion.convert(cst);              // peut lever ConversionException
FileSimulateur sim = new FileSimulateur(module.Plan); // circuit prêt à simuler
```

`FileSimulateur` expose ensuite `getEntrees(i, j) : BouttonEntree` et
`getSorties(i, j) : Connecteur` (1-indexed). `BouttonEntree.set(Etat)` propage,
`Connecteur.getValeur()` lit la sortie.

**Exceptions à attraper côté UI** :
- `ParsingException` (parser) — offset, expected, actual, context, contextPath.
- `ConversionException` (conversion) — `offset()`, `nodeKind()`, `reason()`
  parmi `VECTOR_WIDTH_MISMATCH`, `CONCAT_NOT_SUPPORTED`,
  `MEMORY_ASSIGNMENT_NOT_SUPPORTED`, `LITERAL_IN_RHS_NOT_SUPPORTED`,
  `DUPLICATE_LHS`, `MALFORMED_CST`, `MODULE_NOT_FOUND`, `MODULE_CALL_CYCLE`,
  `MODULE_CALL_INVALID_ARG`, `MODULE_BAD_SEPARATORS`, `MODULE_ARITY_MISMATCH`,
  `DUPLICATE_MODULE_DEFINITION`.

## Convention — gestion des exceptions

Décision de groupe : **propager les exceptions, ne pas les écrire sur le
terminal** (`printStackTrace` / `System.err`). Une exception avalée et logguée
est invisible pour l'utilisateur ; en la laissant remonter (`throws`)
jusqu'à la couche UI, l'application peut l'afficher directement. Les
`ParsingException` / `ConversionException` portent déjà tout ce qu'il faut
pour ça (offset, `reason()`, message).

**Périmètre du convertisseur** : combinatoire scalaire (params scalaires,
affectations `=`, opérateurs `+ * /`) **et appel de sous-modules** (voir
ci-dessous). Hors-scope (vecteurs, `&`, `:=`, littéraux dans RHS) →
`ConversionException` typée.

## Appel de sous-modules

Un module peut en appeler un autre : `$nom(entrées… : sorties…)` dans le corps
SHDL. La conversion résout ces appels **par nom, en mémoire** — elle ne lit
**aucun fichier**.

```java
// CST principal + CST des modules bibliothèque, tous déjà parsés
Module m = Conversion.convert(cstPrincipal, List.of(cstFa, cstAnd));
```

Répartition des responsabilités — à lire avant de coder quoi que ce soit côté
sous-modules :

| Étape | Qui | Quoi |
|-------|-----|------|
| Localiser le `.shdl` d'un sous-module sur le disque, le lire | **appelant** (GUI) | I/O fichier |
| Parser chaque source en CST | **appelant** | `CstParser.parse` |
| Indexer par nom, résoudre `$call`, mémoïser, détecter cycles/absences, construire l'IR | **conversion** ✔ fait | `Conversion.convert(main, library)` |

→ **La conversion ne parcourt pas le disque.** On lui passe le CST principal en
1ᵉʳ argument, les CST de la bibliothèque ensuite ; elle apparie les `$call` aux
définitions par leur nom. Le chargement disque (lister `modules/`, lire les
`.shdl`) reste entièrement à la charge de l'appelant.

Erreurs typées, toutes avec l'offset du site d'appel : `MODULE_NOT_FOUND`
(nom absent de la bibliothèque fournie), `MODULE_CALL_CYCLE`,
`MODULE_ARITY_MISMATCH`, `MODULE_BAD_SEPARATORS`, `MODULE_CALL_INVALID_ARG`,
`DUPLICATE_MODULE_DEFINITION`.

**État** : côté conversion, l'appel de sous-modules est **terminé et testé**.
Le câblage côté `FileSimulateur(Module)` est encore en chantier (Arthur,
branche `appel_module`) — le test e2e `subModuleCall_halfAdder_tableauVerite`
est `@Ignore` en attendant.

## Arborescence

    parser/
      lexer/  automate/  regex/  util/   # code amont (Erwan)
      ll1/
        grammar/                          # Grammar.SHDL, FirstSet, FollowSet
        tabledriven/
          table/  cst/  CstParser.java    # driver LL(1) à pile
      conversion/                         # CST → erwan.Module
        Conversion.java                   # point d'entrée: convert(main, library) -> Module
        ModuleResolver.java               # index nom→CST, résolution + mémoïsation
        ModuleBuilder.java  ModuleCallBuilder.java   # corps + appels $call
        ConversionException.java
        Names.java  ExpressionBuilder.java  Subset.java  Bus.java
    simulateur/                           # IR Erwan + circuit Mati
    tests/                                # 193 tests JUnit
    docs/                                 # specs, plans

## CST — navigation rapide

`CstInternal` (non-terminal) ou `CstLeaf` (terminal). Méthodes :
`first(Symbol)`, `allOf(Symbol)`, `has(Symbol)`, `startOffset()`, `endOffset()`.

```java
import parser.ll1.tabledriven.cst.CstDumper;
System.out.print(CstDumper.dump(cst));   // vue lisible
```

## Caractéristiques

- **Grammaire LL(1)** figée par `GrammarFreezeTest`, sans conflit
  (FIRST/FIRST, FIRST/FOLLOW, récursion gauche).
- **Table M[NT, T]** construite une seule fois, lookup O(1).
- **Driver à pile** générique, sans switch par non-terminal.
- **CST quasi-strict** : trivia filtrés (whitespace, commentaires).
- **Conversion S1** : rejets typés hors-scope, e2e xor (4 lignes de table de
  vérité) vérifié via `FileSimulateur`.

## Documentation détaillée

- `parser/ll1/tabledriven/README.md` — API CST détaillée.
- `docs/specs/2026-04-30-ll1-table-driven-cst-design.md` — spec parser.
- `docs/specs/2026-05-06-cst-conversion-design.md` — spec conversion.
- `docs/plans/` — plans d'implémentation correspondants.
