# Parser LL(1) SHDL — Table-Driven

Branche dédiée à l'axe **parser LL(1) table-driven** du projet long TOB
(simulateur SHDL, N7 1SN, 2025-2026).

Source SHDL → tokens → **CST** (Concrete Syntax Tree) via une table d'analyse
M[NT, T] construite à partir de FIRST/FOLLOW.

## Prérequis

- **JDK 25**
- Bash
- JUnit 4.13.2 + Hamcrest 1.3 (fournis dans `lib/`)

## Build & tests

    ./build.sh test     # compile parser + tests
    ./run.sh            # lance la batterie JUnit (151 tests)

Si des classes ont été supprimées ou renommées :

    rm -rf bin/* && ./build.sh test

## Arborescence

    parser/
      lexer/                       # lexer regex/automate (code amont, ne pas modifier)
      automate/  regex/  util/     # idem
      ll1/
        grammar/                   # Grammar.SHDL, FirstSet, FollowSet, Ll1ConflictChecker
        tabledriven/
          table/                   # ParsingTable + TableBuilder (algo Aho/Sethi/Ullman)
          cst/                     # CstNode sealed (CstInternal, CstLeaf), CstDumper
          CstParser.java           # driver à pile LL(1)
          ParsingException.java
    tests/parser/ll1/              # 151 tests JUnit
    docs/                          # specs, plans, rapports

## API publique

```java
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;

CstNode root = CstParser.parse(shdlSource);
```

Lève `parser.ll1.tabledriven.ParsingException` en cas d'erreur. L'exception
expose `offset()`, `expected()`, `actual()`, `context()` et `contextPath()`.

Un `CstNode` est soit un `CstInternal` (non-terminal développé) soit un
`CstLeaf` (terminal consommé). Navigation via `first(Symbol)`, `allOf(Symbol)`,
`has(Symbol)`, et `startOffset()` / `endOffset()`.

Pour une vue lisible :

```java
import parser.ll1.tabledriven.cst.CstDumper;
System.out.print(CstDumper.dump(root));
```

## Caractéristiques

- **Grammaire LL(1)** figée par `GrammarFreezeTest`, vérifiée sans conflit par
  `Ll1ConflictChecker` (FIRST/FIRST, FIRST/FOLLOW, récursion gauche).
- **Table M[NT, T]** construite une seule fois au chargement, lookup O(1).
- **Driver à pile** : générique, sans switch ni récursion par non-terminal.
- **CST quasi-strict** : reflète exactement la dérivation grammaticale ; les
  trivia (whitespace, lineTerminator, commentaires) sont filtrés avant parsing.
- **Tests** : 151 cas couvrant grammaire, table, parser par construct,
  invariants, intégration end-to-end (9 modules).

## Documentation détaillée

- `parser/ll1/tabledriven/README.md` : API détaillée, navigation CST, dumper,
  écarts assumés vs grammaire amont.
- `docs/specs/2026-04-30-ll1-table-driven-cst-design.md` : spec complète.
- `docs/plans/2026-04-30-ll1-table-driven-cst.md` : plan d'implémentation.
