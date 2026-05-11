# Parser LL(1) Table-Driven — SHDL

Driver de parsing LL(1) pour le langage SHDL. Produit un Concrete Syntax Tree (CST).

## Compilation et tests

```bash
bash build.sh test   # compile + teste
bash run.sh          # execute les suites JUnit
```

Si des classes ont ete supprimees ou renommees, nettoyer le cache avant :

```bash
rm -rf bin/* && bash build.sh test
```

## API publique

```java
// Point d'entree unique
CstNode root = CstParser.parse(String source);
```

`CstParser.parse(source)` :
1. Tokenise `source` via le lexer du projet (`parser.lexer.Lexer`)
2. Filtre les trivia (whitespace, lineTerminator, commentaires)
3. Ajoute une sentinelle EOF a `source.length()`
4. Leve `ParsingException` si la source n'est pas syntaxiquement valide

`ParsingException` expose :
- `offset()` : position dans la source ou l'erreur a ete detectee
- `expected()` : token attendu (null si absence d'entree dans la table)
- `actual()` : lexeme effectivement trouve
- `context()` : NonTerminal innermost au moment de l'erreur
- `contextPath()` : chemin complet ex. `"Module > Instance > Operation"` (null si racine)

## Dependance amont

Le lexer est fourni par le package `parser.lexer` (code amont du projet) :
- `Lexer` : tokenise une source SHDL
- `Lexem<Token>` : lexeme avec token, texte et offset
- `Token` : enum de tous les types de tokens SHDL

**Ne jamais modifier `parser/lexer/`, `parser/automate/`, `parser/regex/`, `util/`.**

## Navigation dans le CST

`CstNode` est une interface sealed avec deux implementations :
- `CstLeaf` : terminal consomme (token + lexeme source)
- `CstInternal` : non-terminal developpe (NT + production + enfants)

Methodes de navigation (disponibles sur tout `CstNode`) :

```java
Optional<CstNode> first(Symbol s)    // premier enfant direct avec ce symbole
List<CstNode>     allOf(Symbol s)    // tous les enfants directs avec ce symbole
boolean           has(Symbol s)      // existence d'un enfant direct
int               startOffset()      // offset inclus du premier caractere
int               endOffset()        // offset exclus du dernier caractere
```

Exemple — recuperer le nom du module :

```java
CstNode root   = CstParser.parse(source);
CstNode module = root.first(NonTerminal.Module).orElseThrow();
CstNode name   = module.first(new Terminal(Token.Identifiant)).orElseThrow();
String  text   = ((CstLeaf) name).lexem().getText();
```

## Debugging avec CstDumper

```java
String dump = CstDumper.dump(root);
System.out.print(dump);
```

Exemple de sortie pour `"module m (a) i = .0 end module"` :

```
Start @0..30 [Start -> Module]
+-- Module @0..30 [Module -> ModuleKW Identifiant LeftPar Param ...]
    +-- ModuleKW("module") @0..6
    +-- Identifiant("m") @7..8
    ...
    +-- Param @10..11 [Param -> Signal]
    |   +-- Signal @10..11 [Signal -> Identifiant Signal_Subset_Opt]
    |       +-- Identifiant("a") @10..11
    |       +-- Signal_Subset_Opt @11..11 [Signal_Subset_Opt -> epsilon]  (vide)
    ...
```

Les noeuds epsilon (productions vides) sont marques `(vide)` avec `startOffset == endOffset`.

## Limitations connues

- La grammaire est en forme LL(1) stricte et est figee (voir `GrammarFreezeTest`).
  Ne pas modifier `Grammar.SHDL` sans mettre a jour tous les tests dependants.
- `LiteralValue` ne supporte que `BitField` (ex. `.0`, `.1`) ; `NaturalInteger`
  n'est pas un `Factor` valide dans une expression.
- `MemoryAssignment` supporte un seul `Set_Or_Reset` par instance.
- Les appels memoire (`$nom(...)`) utilisent `Dollar Identifiant ModuleCall`.

## Ecarts assumes vs `shdl_grammar_LL1_v2.txt`

1. **Factor parens** : la spec a `Factor ::= RightPar SOT LeftPar` (typo, parens
   inversees). Notre grammaire utilise `LeftPar SOT RightPar`. Sans correction,
   `(a + b)` serait rejete.
2. **NTs morts retires** : `SignalCompound` et `Concat_Signal_Star` sont definis
   dans la spec mais reference par aucune autre production (inaccessibles depuis
   l'axiome). On les omet pour eviter de polluer la grammaire.
3. **Trivia filtres** : whitespace, lineTerminator et commentaires sont retires
   *avant* parsing, donc absents du CST. Les offsets restent corrects. CST
   "quasi-strict" : suffisant pour la simulation, insuffisant pour un outil de
   refactoring qui voudrait preserver le formatage source.
4. **Casse uniforme** : `Comma_Opt` au lieu de `Comma_opt` (la spec a une
   incoherence interne sur la casse de `_Opt`).
5. **MemAssignOp** : la spec ecrit `'::='` ; le lexer matche `:=` (typo amont).
