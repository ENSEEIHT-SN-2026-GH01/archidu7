# Parser LL(1) SHDL — Table-Driven + Conversion

Branche dédiée aux axes **parser LL(1) table-driven** et **conversion CST → IR
simulateur** du projet long TOB (simulateur SHDL, N7 1SN, 2025-2026).

Pipeline : Source SHDL → tokens → **CST** (table d'analyse M[NT, T]) →
**`simulateur.Module`** prêt à être passé à `FileSimulateur`.

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
import simulateur.Module;
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
  parmi `VECTOR_SUBSET_NOT_SUPPORTED`, `CONCAT_NOT_SUPPORTED`,
  `MEMORY_ASSIGNMENT_NOT_SUPPORTED`, `MODULE_CALL_NOT_SUPPORTED`,
  `LITERAL_IN_RHS_NOT_SUPPORTED`, `DUPLICATE_LHS`, `MALFORMED_CST`.

**Périmètre S1 du convertisseur** : combinatoire scalaire stricte (params
scalaires, affectations `=`, opérateurs `+ * /`). Hors-scope (vecteurs, `&`,
`:=`, appel de module, littéraux dans RHS) → `ConversionException` typée.

## Arborescence

    parser/
      lexer/  automate/  regex/  util/   # code amont (Erwan)
      ll1/
        grammar/                          # Grammar.SHDL, FirstSet, FollowSet
        tabledriven/
          table/  cst/  CstParser.java    # driver LL(1) à pile
      conversion/                         # CST → simulateur.Module
        Conversion.java                   # point d'entrée: convert(CstNode) -> Module
        ConversionException.java
        Names.java  ExpressionBuilder.java  ModuleBuilder.java
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
