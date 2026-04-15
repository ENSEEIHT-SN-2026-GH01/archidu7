# Parser LL(1) SHDL

Branche de travail d'Alexis Briend pour l'axe **parser LL(1)** du projet long
TOB (simulateur SHDL, N7 1SN, 2025-2026). Parser descendant LL(1) du langage
SHDL : source → tokens → arbre AST (`parser.ll1.ast.Module`).

## Prérequis

- **JDK 25** (requis — l'interface JavaFX d'Arthur utilise les flexible
  constructor bodies JEP 492). Tester avec `java --version`.
- Bash (scripts de build).
- JUnit 4.13.2 + Hamcrest 1.3 fournis dans `lib/`.

## Build & tests

    ./build.sh          # compile le parser
    ./build.sh test     # compile parser + tests
    ./run.sh            # lance toute la batterie JUnit (107 tests)

## Arborescence

    parser/ll1/
      ast/          # classes AST immuables (Module, Signal, Assignment, Fsm, ...)
      grammar/      # Grammar SHDL, FirstSet, FollowSet, Ll1ConflictChecker
      parser/       # Parser descendant LL(1), interface Lexer, ParsingException
      token/        # Token, TokenType (46 terminaux SHDL)
    tests/parser/ll1/ # 107 tests JUnit
    docs/             # specs, plans, bilan sprint 1, livrables perso

## API publique

Point d'entrée unique, tout le reste est interne :

```java
import parser.ll1.parser.Parser;
import parser.ll1.parser.Lexer;
import parser.ll1.ast.Module;

Module ast = Parser.parseFrom(shdlSource, lexer);   // lexer = impl. d'Erwan
```

Lève `parser.ll1.parser.ParsingException` en cas d'erreur (méthodes
`getLine()`, `getColumn()`, `getErrorCode()` exploitables pour affichage UI).

---

## Pour Erwan (lexer SHDL)

Le parser consomme une `List<Token>` via l'interface :

    package parser.ll1.parser;
    public interface Lexer {
        List<Token> tokenize(String source);
    }

`Token` (dans `parser.ll1.token`) est immuable : `(TokenType, String lexeme,
int line, int column)`. `TokenType` énumère les 46 terminaux SHDL (`MODULE`,
`END`, `IDENTIFIER`, `INTEGER`, `BITFIELD`, `EQ`, `PLUS`, `STAR`, `ARROW`,
`WHEN`, `ON`, `FSM`, `SYNCHRONOUS`, `AMPERSAND`, `SLASH`, ...). La liste
complète est dans `parser/ll1/token/TokenType.java`.

Le parser tolère l'absence d'un `EOF` terminal (il en ajoute un si besoin) et
s'attend à des lignes/colonnes 1-indexed.

Exemple de branchement :

```java
Lexer lexerErwan = new LexerShdl();  // implémentation côté interpretation
Module ast = Parser.parseFrom(sourceShdl, lexerErwan);
```

Un lexer minimal de dépannage (`SimpleLexer`) existe sur `demo/mvp-sprint1`
dans `mvp/` pour pouvoir tester le pipeline sans la vraie implémentation — il
ne couvre ni `->` ni les bitfields.

---

## Pour Mati (simulateur)

Le parser produit un `parser.ll1.ast.Module`, racine de l'AST SHDL. Toutes les
classes du package `parser.ll1.ast` sont immuables et navigables via des
getters.

Points d'accès principaux :
- `Module.getName()`, `getParams()` (liste de `Signal`), `getInstances()`
  (liste de `Instance`).
- `Instance` est un type somme : `Assignment`, `TriState`, `MemoryPoint`,
  `ModuleInstance`, `Fsm`, `MapNode`.
- `Assignment.getTarget()` retourne un `SignalCompound`,
  `getExprCompound()` une `List<SumOfTerms>`.
- Une expression booléenne est un `SumOfTerms` = liste de `Term` (OU logique),
  chaque `Term` = liste de `Factor` (ET logique).
- `Factor.getKind()` ∈ `{SIGNAL, NEG_SIGNAL, LITERAL_0, LITERAL_1,
  PAREN_SUB_EXPR, INTEGER, BITFIELD}`.

Deux façons de parcourir l'AST :

**1. `instanceof` + getters** (exemple concret dans `mvp/Interpreteur.java`
sur `demo/mvp-sprint1`, construction d'un circuit Lien-based à partir d'un
`Module`) :

```java
for (Instance inst : module.getInstances()) {
    if (inst instanceof Assignment a) {
        String cible = a.getTarget().getSignals().get(0).getName();
        for (SumOfTerms sot : a.getExprCompound()) {
            for (Term t : sot.getTerms()) {
                for (Factor f : t.getFactors()) {
                    // instancier And / Or / Non sur des Lien selon f.getKind()
                }
            }
        }
    }
}
```

**2. Pattern Visitor** via `parser.ll1.ast.Visitor` / `DefaultVisitor` — plus
adapté si la logique de traduction devient complexe. Exemple de traversée
complète dans `tests/parser/ll1/ast/DefaultVisitorTraversalTest.java`.

Le sous-ensemble effectivement traduit par `mvp/Interpreteur.java` couvre
pour l'instant `Module` + `Assignment` d'expressions booléennes sur
identifiants. `Fsm`, `MapNode`, `ModuleInstance`, `MemoryPoint` sont présents
dans l'AST mais pas encore consommés côté circuit.

---

## Pour Arthur (UI JavaFX)

Le parser est appelable avec un point d'entrée unique :

```java
Module ast = Parser.parseFrom(sourceShdl, lexer);
```

Comportement :
- `Parser` est **non thread-safe** et à usage unique (`parse()` ne peut être
  appelé qu'une fois). `parseFrom(...)` recrée une instance interne à chaque
  appel — aucune mise en cache nécessaire côté UI.
- En cas de syntaxe invalide : lève `parser.ll1.parser.ParsingException` avec
  `getLine()`, `getColumn()`, `getErrorCode()` (enum `ErrorCode`, 8 types
  dont `EMPTY_FILE`, `UNEXPECTED_TOKEN`, `TRAILING_TOKENS`, ...), et un
  message lisible via `getMessage()`. De quoi surligner la position fautive
  dans `EditeurTexte` ou afficher un popup.
- En cas de succès : `Module` racine, avec `getName()` exploitable pour
  remplir `ListeModulePrincipale`. SHDL multi-modules non supporté au
  sprint 1 (un seul `module ... end module` par source).

Exemple d'enchaînement :

```java
try {
    Module ast = Parser.parseFrom(editeurTexte.getTexte(), lexerShdl);
    // ast consommable par Mati pour construire le circuit
} catch (ParsingException e) {
    // e.getLine() / e.getColumn() -> position dans EditeurTexte
    // e.getErrorCode()            -> catégorie d'erreur
}
```

Aucune dépendance JavaFX côté parser : rien n'empêche de le lancer en tâche
de fond si le parsing de fichiers longs devient bloquant (peu probable aux
tailles SHDL actuelles).

---

## Statut sprint 1

- Grammaire LL(1) SHDL complète (module, assignment, memory point, FSM, map).
- 107 tests verts.
- Conflit LL(1) connu : `TERM_REST` (wildcard FSM `* -> sN` + `when`) —
  documenté par `Ll1ConflictTest`, neutralisé en pratique puisque FSM est
  hors scope démo sprint 1 (cf. `docs/sprint1/2026-04-13-bilan-sprint1.md`).

## Branches liées

- `main` : tronc équipe (artefacts partagés, `livrables/sujets.pdf`, etc.).
- `demo/mvp-sprint1` : démo JavaFX intégrant ce parser + simulateur + UI.
- `origin/simulation` (Mati) : simulateur Lien-based de référence.
- `origin/interpretation` (Erwan) : lexer regex/automates.

## Sprint 2 (indicatif)

- Retirer le conflit `TERM_REST` (factoriser la wildcard FSM).
- Récupération d'erreurs (multi-erreurs par parsing, meilleurs messages).
- Support `Fsm` / `MapNode` / `ModuleInstance` côté interpréteur (hors parser).
