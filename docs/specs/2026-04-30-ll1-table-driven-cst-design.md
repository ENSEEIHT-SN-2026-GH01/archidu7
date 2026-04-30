# Spec — Parser LL(1) table-driven SHDL, sortie CST

**Auteur** : Alexis Briend
**Date** : 2026-04-30 (sprint 2)
**Ticket** : refonte de l'axe interprétation, suite à une revue d'architecture du premier parser RD.
**Branche cible** : `feature/parser-ll1-table-driven` (créée depuis `feature/parser-ll1`)

## 1. Contexte et motivation

Le premier parser SHDL (sprint 1, branche `feature/parser-ll1`) est un parser à descente récursive écrit à la main. Il fonctionne (107 tests verts), mais une revue d'architecture a fait remonter un point structurel : on a une grammaire LL(1) formelle (`Grammar.SHDL`) avec FIRST/FOLLOW/Ll1ConflictChecker — mais le parser ne la consulte jamais à l'exécution. La grammaire formelle ne sert que de documentation passive.

Cette refonte vise à corriger ça : parser **table-driven** qui consomme directement la grammaire et la table M[NT, T] générée dynamiquement. La grammaire formelle devient la source de vérité unique.

Nous avons aussi fait le choix de scope que la sortie de ce parser soit l'**arbre de dérivation strict (CST)**, sans transformation en AST dans cette branche. La transformation CST → représentation utilisable par le simulateur sera réalisée en aval, hors de cette spec.

La grammaire de référence est la version LL(1) formalisée disponible dans `shdl_grammar_LL1.txt` (version stable pour ~2 semaines).

## 2. Objectif et scope

Implémenter un parser LL(1) **table-driven** pour le langage SHDL. Le parser prend une `String` source, la tokenise, puis produit un **CST strict** (arbre de dérivation reflétant la grammaire formelle, sans linéarisation des `*_Star`).

### Dans le scope

- Mise à jour de `Grammar.SHDL` avec la grammaire LL(1) cible (production-pour-production depuis `shdl_grammar_LL1.txt`)
- Lexer SHDL : wrapper sur `AutomateDeterministe<TokenType>` (`parser/automate/`), avec :
  - skip whitespace/commentaires en pré-traitement
  - reclassification post-lex des keywords (le moteur d'automate refuse l'ambiguïté keyword/identifiant)
  - production de `Token(TokenType, String value, int offset)` — offset-only, pas de ligne/colonne
- Génération dynamique de la table M[NT, T] depuis `Grammar.SHDL` via les `FirstSet`/`FollowSet` existants
- Driver de parsing à pile : lookup table, prédiction, construction du CST en parallèle
- Représentation du CST : sealed `CstNode`, records `CstInternal` et `CstLeaf`
- Un seul helper de navigation : `first(Symbol) / allOf(Symbol) / has(Symbol)` sur `CstNode`
- Calcul automatique de `startOffset()` / `endOffset()` sur les nœuds internes
- Suite de tests JUnit 4 exhaustive, organisée par couche (Grammar → Lexer → TableBuilder → CstParser → CstHelper)
- Discipline : tests d'une couche écrits **avant** l'implémentation de cette couche

### Hors scope

- **AST** : construit hors scope, en aval, à partir du CST produit par cette branche. Aucune classe `ast/*` créée ici.
- **Transformation CST → AST** : hors scope.
- **Simulateur, UI, sauvegarde** : autres axes.
- **Préservation des commentaires** : skippés par le lexer, jamais visibles.
- **Récupération d'erreur (panic recovery)** : on reste fail-fast à la première erreur, avec message contextualisé.
- **Réutilisation de code du parser RD** : zéro. Mindset 100 % table-driven.
- **Migration Gradle** : reste hors périmètre, le code vit à la racine selon la convention de l'équipe.

## 3. Architecture

### 3.1 Pipeline

```
String source
   ↓ (Lexer SHDL)
List<Token>
   ↓ (CstParser, drivé par ParsingTable)
CstNode (= CstInternal racine sur "Module")
```

`CstParser.parse(String)` est l'unique point d'entrée public.

### 3.2 Emplacement

Package `parser.ll1.tabledriven`, sous-dossier `parser/ll1/tabledriven/` à côté du parser RD (qui reste intact pendant le développement). Le code RD (`parser/ll1/parser/Parser.java`, ~444 LoC) est isolé. Aucun import croisé entre les deux pendant le développement.

À l'issue du sprint, quand le nouveau parser est validé, le RD pourra être supprimé (décision en sprint suivant, hors scope de cette spec).

### 3.3 Arborescence cible

```
parser/ll1/
├── token/
│   ├── TokenType.java          enum (mis à jour si nécessaire pour aligner sur la grammaire LL(1) cible)
│   └── Token.java              record (TokenType, String value, int offset)
│
├── grammar/                     RÉUTILISÉ TEL QUEL après mise à jour de Grammar.SHDL
│   ├── Grammar.java             (mis à jour : Grammar.SHDL = grammaire LL(1) cible)
│   ├── Production.java
│   ├── NonTerminal.java
│   ├── Terminal.java
│   ├── Symbol.java
│   ├── FirstSet.java
│   ├── FollowSet.java
│   ├── Ll1ConflictChecker.java
│   └── Ll1Conflict.java
│
├── parser/                      ANCIEN RD, INTACT pendant le sprint
│   └── Parser.java
│
└── tabledriven/                 NOUVEAU
    ├── lexer/
    │   ├── ShdlLexer.java       wrapper sur AutomateDeterministe<TokenType>
    │   ├── LexerException.java
    │   └── KeywordTable.java    Map<String, TokenType> pour reclassif post-lex
    ├── table/
    │   ├── ParsingTable.java    record (Map<Pair<NonTerminal, Terminal>, Production>)
    │   └── TableBuilder.java    static build(Grammar) → ParsingTable
    ├── cst/
    │   ├── CstNode.java         sealed interface
    │   ├── CstInternal.java     record (NonTerminal nt, Production rule, List<CstNode> children, int startOffset, int endOffset)
    │   ├── CstLeaf.java         record (Terminal t, Token token)
    │   └── CstDumper.java       sérialisation textuelle indentée (debug)
    ├── CstParser.java           driver à pile, API publique parse(String)
    └── ParsingException.java    erreur de parsing avec offset
```

### 3.4 Réutilisations explicites

- **`parser/ll1/grammar/*`** : tout. Les algorithmes FirstSet/FollowSet/Ll1ConflictChecker sont déjà implémentés et testés. Seul `Grammar.SHDL` change (productions mises à jour).
- **`parser/automate/AutomateDeterministe`** (branche `origin/interpretation`) : utilisé par `ShdlLexer` via `exec1`.
- **`parser/regex/*`** (branche `origin/interpretation`) : utilisé pour construire les regexes des tokens.

### 3.5 Décisions clés (et pourquoi)

| Décision | Choix | Pourquoi |
|---|---|---|
| Approche parsing | Table-driven | Le parser RD ne consomme pas la grammaire formelle à l'exécution. Table-driven fait de Grammar.SHDL la source unique de vérité. |
| Sortie | CST strict (B1) | Choix de scope : on livre le CST brut, la transformation aval est hors périmètre. |
| AST | Aucun | Hors scope, construit en aval. |
| Forme du CST | Strict (cascades `*_Star` non linéarisées) | Choix : reflet exact de la dérivation, pas de linéarisation. |
| Stockage `CstInternal` | NonTerminal lhs + Production complète (A2) | Permet le dispatch propre dans le transformer aval (sinon, lecture par index ou pattern matching sur les enfants — fragile). |
| Helpers de navigation | Un seul : `first/allOf/has` | Minimum d'outillage côté CST, les navigations spécifiques sont composées par les consommateurs. |
| Positions sur internes | Calculées automatiquement | ~5 lignes côté implémentation, utile pour les futures erreurs sémantiques en aval. |
| Position sur Token | Offset-only | Simplifie le lexer ; conversion line/col à l'affichage via helper utilitaire. |
| Lexer | Construit sur `exec1` du moteur d'automate | Pas de duplication. `exec` a un bug (offset cumulé) qu'on évite. |
| Keywords | Reclassifiés post-lex via `Map<String, TokenType>` | Le moteur d'automate refuse l'ambiguïté keyword/identifiant ("non deterministic"). Pattern standard "keywords as identifiers". |
| Whitespace + commentaires | Skip dans le wrapper, jamais en tokens | Le moteur d'automate ne supporte pas la négation `[^...]` pour les commentaires. |
| Génération de la table | Dynamique au démarrage | Réutilise FirstSet/FollowSet existants, cohérent avec "Grammar.SHDL = source de vérité". |
| Java | 25, sealed + records | Disponible depuis montée pour code Arthur. Code idiomatique moderne. |
| Tests | Test-first par couche, TDD strict en intra-couche | Compromis entre rigueur et pragmatisme. |

## 4. Composants

### 4.1 `Token` (record)

```java
public record Token(TokenType type, String value, int offset) {
    public int end() {
        return offset + (value == null ? 0 : value.length());
    }
}
```

`value` est non-null pour `IDENTIFIANT`, `BIT_FIELD`, `NATURAL_INTEGER` ; null pour les keywords/opérateurs/ponctuation (l'info est dans `type`).

### 4.2 `TokenType` (enum)

Liste exhaustive (à finaliser depuis la grammaire LL(1) cible) :

```
MODULE_KW, END_KW, ON_KW, WHEN_KW, SET_KW, RESET_KW, ENABLED_KW,
LEFT_PAR, RIGHT_PAR, LEFT_SQUARE_BRACK, RIGHT_SQUARE_BRACK,
COMMA, COLON, SEMICOLON, POINT_POINT, DOLLAR,
ASSIGN_OP, MEM_ASSIGN_OP, OR_OP, AND_OP, CONCAT_OP, NOT_OP,
IDENTIFIANT, BIT_FIELD, NATURAL_INTEGER,
EOF
```

`EOF` : sentinelle ajoutée par le lexer en fin de flux pour faciliter la fin de parsing.

### 4.3 `ShdlLexer`

```java
public final class ShdlLexer {
    public static List<Token> tokenize(String source) throws LexerException;
}
```

Algorithme :

1. Construction unique (à la première invocation, cached) d'un `AutomateDeterministe<TokenType>` à partir d'une `List<Pair<Regex, TokenType>>` contenant uniquement :
   - Opérateurs/ponctuations (regex de littéral échappé)
   - `IDENTIFIANT` : `[a-zA-Z_][a-zA-Z0-9_]*`
   - `BIT_FIELD` : `\.[0-1]+`
   - `NATURAL_INTEGER` : `[0-9]+`
   - **Pas** de keywords (reclassif post-lex)
   - **Pas** de whitespace (skip pré-traitement)
   - **Pas** de commentaires (skip pré-traitement)
2. Boucle sur le source :
   - Skip whitespace (`[ \t\r\n]+`)
   - Skip commentaire (`//...EOL` ou `#...EOL`)
   - Si fin du source : ajouter `Token(EOF, null, source.length())`, sortir
   - Appeler `automate.exec1(source.substring(currentOffset))` → `(type, length)`
   - Slice le lexème : `source.substring(currentOffset, currentOffset + length)`
   - Si `type == IDENTIFIANT` et lexème ∈ `KeywordTable` : reclassifier en keyword
   - Sinon : créer `Token(type, lexèmeOuNull, currentOffset)` (`null` si type sans valeur intéressante)
   - Avancer `currentOffset += length`
3. Erreur si `exec1` lève `LexingException` ou si le source est vide à un endroit inattendu : `LexerException` avec offset.

### 4.4 `KeywordTable`

```java
public static final Map<String, TokenType> KEYWORDS = Map.of(
    "module",  MODULE_KW,
    "end",     END_KW,
    "on",      ON_KW,
    "when",    WHEN_KW,
    "set",     SET_KW,
    "reset",   RESET_KW,
    "enabled", ENABLED_KW
);
```

### 4.5 `Grammar.SHDL`

À mettre à jour pour matcher exactement les productions de `shdl_grammar_LL1.txt` (dernière version stable), production-par-production. Tous les non-terminaux et terminaux nécessaires ajoutés. `Ll1ConflictChecker` doit confirmer "no conflict" sur la nouvelle grammaire — sinon investigation grammaticale avant de poursuivre.

Bugs déjà identifiés dans le fichier qu'on **corrige dans `Grammar.SHDL` côté Java** :
- Factor L52 : `LeftPar SumOfTerms RightPar` (ordre correct, pas inversé)
- `SignalCompound` (L16-18) : on l'omet car non référencé
- Le reste verbatim

### 4.6 `ParsingTable`

```java
public record ParsingTable(Map<TableKey, Production> entries) {
    public record TableKey(NonTerminal nt, Terminal t) {}
    public Optional<Production> lookup(NonTerminal nt, Terminal t) { ... }
}
```

### 4.7 `TableBuilder`

```java
public final class TableBuilder {
    public static ParsingTable build(Grammar g) { ... }
}
```

Algorithme classique :
- Pour chaque production `A → α` dans `g` :
  - Pour chaque `a ∈ FIRST(α) \ {ε}` : `table[A, a] = A → α`
  - Si `ε ∈ FIRST(α)` : pour chaque `b ∈ FOLLOW(A)` : `table[A, b] = A → α`
- Si une cellule reçoit deux productions différentes : la grammaire n'est pas LL(1) → `IllegalStateException` (mais `Ll1ConflictChecker` doit avoir détecté en amont, donc impossible si la spec est respectée).

### 4.8 `CstNode`, `CstInternal`, `CstLeaf`

```java
public sealed interface CstNode permits CstInternal, CstLeaf {
    int startOffset();
    int endOffset();
    
    // helper unique #1
    Optional<CstNode> first(Symbol s);
    List<CstNode> allOf(Symbol s);
    boolean has(Symbol s);
}

public record CstInternal(
    NonTerminal nt,
    Production rule,
    List<CstNode> children,
    int startOffset,
    int endOffset
) implements CstNode {
    // implémentations de first/allOf/has parcourent children
}

public record CstLeaf(Terminal t, Token token) implements CstNode {
    public int startOffset() { return token.offset(); }
    public int endOffset() { return token.end(); }
    // first/allOf/has triviaux (un Leaf n'a pas d'enfants)
}
```

`startOffset` / `endOffset` sur `CstInternal` calculés à la construction :
- `startOffset` = offset du premier `CstLeaf` descendant (ou `-1` si l'arbre est purement ε, cas dégénéré)
- `endOffset` = endOffset du dernier `CstLeaf` descendant

Cas ε (production réduite à `ε`) : `CstInternal` avec children vides ; `startOffset` = `endOffset` = offset du token courant au moment de la prédiction (= position d'insertion du ε dans le flux).

### 4.9 `CstParser`

```java
public final class CstParser {
    public static CstNode parse(String source) throws LexerException, ParsingException;
    public static CstNode parseTokens(List<Token> tokens) throws ParsingException;  // pour tests isolés
}
```

Algorithme à pile classique :
- Pile initiale : `[EOF, START]` (`START = NonTerminal.Start`)
- Curseur sur les tokens (commence à 0)
- Boucle :
  - Top de la pile = `X`, token courant = `t`
  - Si `X` est un Terminal : il doit matcher `t.type`. Sinon → `ParsingException`. Sinon, pop `X`, créer un `CstLeaf(X, t)`, attacher au parent, avancer le curseur.
  - Si `X` est un NonTerminal : `lookup(X, t.type)`. Sinon → `ParsingException`. Sinon, pop `X`, créer un `CstInternal(X, production, [], -1, -1)` (placeholder), attacher au parent, push les symboles RHS de la production en ordre inverse sur la pile.
  - Si `X = EOF` et `t.type = EOF` : succès, return racine.
- Maintenance d'une pile parallèle de `CstInternal` "en construction" pour attacher les enfants au bon parent.
- À la fermeture d'un `CstInternal` (= quand tous ses enfants sont collectés) : recalculer `startOffset` / `endOffset` depuis les enfants.

### 4.10 `ParsingException`

```java
public final class ParsingException extends RuntimeException {
    private final int offset;
    private final TokenType expected;  // peut être null pour erreurs plus complexes
    private final Token actual;
    private final NonTerminal context;
    // message formaté avec ces infos
}
```

Format du message :
```
Erreur syntaxique à l'offset 47 : attendu RIGHT_PAR, trouvé IDENTIFIANT("foo")
  contexte : règle ModuleCall en cours
```

Pas de ligne/colonne dans le message — `Position.fromOffset(source, offset)` à la couche présentation fera la conversion si l'UI en a besoin.

### 4.11 `CstDumper`

```java
public final class CstDumper {
    public static String dump(CstNode root);
}
```

Sortie indentée :
```
Module @0..47 [Module → ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW]
├── ModuleKW("module") @0..6
├── Identifiant("foo") @7..10
├── LeftPar @11..12
├── Param @12..13 [Param → Signal]
│   └── Signal @12..13 [Signal → Identifiant Signal_Subset_Opt]
│       ├── Identifiant("a") @12..13
│       └── Signal_Subset_Opt @13..13 [Signal_Subset_Opt → ε]  (vide)
...
```

Utile pour debug et tests golden-master.

## 4.12 Modifications nécessaires côté code amont (`parser/regex/`, `parser/automate/`)

**Aucune.** Le code amont est utilisable tel quel pour le besoin via `exec1`. Bugs et limitations identifiés (audit) sont contournables côté wrapper :

- `exec` bugué : non utilisé
- Pas de priorité keyword/Identifiant : reclassif post-lex
- Pas de négation `[^...]` : skip whitespace+commentaires en pré-traitement
- `fromList` mute son argument : passer une copie défensive
- `System.out.println` polluant : ignoré

Améliorations qualité optionnelles à signaler en amont sans bloquer cette branche (voir audit).

## 5. Tests

Organisation par couche, **test-first par couche**.

### 5.1 Couche `Grammar` (mise à jour)

Tests existants (FirstSet, FollowSet, Ll1ConflictChecker) doivent **continuer à passer** après mise à jour de `Grammar.SHDL`. Tests à ajouter :

- `GrammarFreezeTest` : épingle le texte exact de la grammaire LL(1) cible. Tout changement → test rouge bruyant.
- `GrammarStructureTest` : nombre de productions, présence de chaque non-terminal attendu.
- `Ll1ConflictTest` : `assertNoConflict(Grammar.SHDL)`.

### 5.2 Couche `Lexer`

Fichiers : `ShdlLexerTest.java`, `KeywordReclassificationTest.java`, `WhitespaceCommentSkipTest.java`, `LexerEdgeCasesTest.java`.

Cas couverts :
- Tokenisation de chaque type isolé (un par un)
- Tokenisation d'un flux complet (cas représentatifs : module simple, signal avec range, expression `a*b+c`)
- Reclassification keyword/identifiant (`module` vs `modulo`)
- Skip whitespace varié (`\t\r\n` mixés)
- Skip commentaires `//` et `#`
- Edge cases : source vide, source avec uniquement whitespace, EOF correct, lexème non reconnu (`@` par exemple)
- `Token.offset()` correct sur chaque token d'un flux multi-token
- `Token.end()` correct

### 5.3 Couche `TableBuilder`

Fichiers : `TableBuilderTest.java`.

Cas :
- Sur une grammaire mini fabriquée (`S → a b | c`), table calculée == table attendue
- Sur `Grammar.SHDL` complète : la table couvre toutes les paires (NT, T) attendues, aucune cellule à deux productions
- Conflit explicite (production injectée provoquant un conflit) → `IllegalStateException`

### 5.4 Couche `CstParser`

Fichiers : `CstParserModuleTest.java`, `CstParserSignalTest.java`, `CstParserExpressionTest.java`, `CstParserAssignmentTest.java`, `CstParserMemoryAssignmentTest.java`, `CstParserModuleCallTest.java`, `CstParserErrorTest.java`.

Cas par construct :
- Module vide : `module foo () end module` → CstNode racine = Module avec children attendus
- Module avec params : `module foo (a, b, c) ... end module`
- Signal sans range : `a`
- Signal avec index : `a[3]`
- Signal avec range : `a[3..0]` et `a[3:0]`
- Expression : `a + b * c`, `/a`, `(a + b) * c`, `a & b & c`
- Assignment : `c = a + b`
- MemoryAssignment : `c ::= a on b , set when c enabled when d ;`
- ModuleCall : `fullAdder(a, b: s, co)` et `$fullAdder(a, b: s, co)`
- Module complet (toutes constructions ensemble)

Cas d'erreur :
- Token inattendu en début de Module
- Manquement d'un token attendu (RightPar, EndKW)
- Token inattendu au milieu d'une expression
- Vérification que `ParsingException.offset` pointe vers le bon endroit

Comparaison des CST attendus : via `CstDumper.dump(actual)` vs string golden, ou via égalité structurelle `equals` sur les records.

### 5.5 Couche `CstHelper`

Fichier : `CstNavigationTest.java`.

Cas :
- `first(NT)` retourne le bon enfant ou `Optional.empty()`
- `allOf(NT)` retourne tous les enfants matchant
- `has(T)` retourne `true`/`false` correctement
- Comportements sur `CstLeaf` (toujours empty/false)

### 5.6 Couche `CstDumper`

Fichier : `CstDumperTest.java`.

Cas :
- Dump d'un CST simple → format attendu
- Indentation correcte sur arbre profond
- Position et nom de production affichés
- Cas ε (children vide)

### 5.7 Tests d'intégration

Fichier : `EndToEndTest.java`.

Cas :
- Source SHDL réel (3-4 modules de complexités variées) → `parse(source)` → CST attendu (golden-master via CstDumper)
- Source SHDL invalide → `ParsingException` avec offset attendu

### 5.8 Couverture cible

- 100 % des productions de `Grammar.SHDL` exercées au moins une fois en parsing
- 100 % des branches d'erreur déclenchées au moins une fois
- 100 % des combinaisons `(NT, T)` valides de la table testées via parsing
- > 90 % couverture lignes (mesurée si JaCoCo dispo, sinon estimée)

## 6. Discipline d'implémentation

1. **Test-first par couche** : tests de la couche écrits et tous rouges avant d'écrire le code de la couche.
2. **TDD strict en intra-couche** : test rouge → impl minimale → vert → commit. Pas de batching.
3. **Subagent-driven-development** : implémentation déléguée à un agent par tâche, avec review spec puis review qualité après chaque tâche.
4. **Pas de réutilisation du parser RD** : zéro import. Vérification grep avant chaque commit.
5. **Commits fréquents et atomiques** : un commit = une couche/sous-tâche cohérente.
6. **Pas de push sans demande explicite à l'utilisateur**.

## 7. Risques et mitigations

| Risque | Probabilité | Mitigation |
|---|---|---|
| La grammaire évolue avant fin sprint | Moyenne | `GrammarFreezeTest` épingle, on rebase si update |
| Bug subtil dans `AutomateDeterministe` non détecté par audit | Faible | Tests Lexer exhaustifs sur cas réels, intégration end-to-end |
| Conflit LL(1) découvert dans la grammaire à l'usage | Faible | `Ll1ConflictChecker` en garde-fou, investigation grammaticale le cas échéant |
| Le contrat CST évolue (passage à B2 linéarisé en aval) | Moyenne | Un helper `flatten` cascade peut être ajouté en 5 lignes ; impact limité |
| Mati nécessite des positions ligne/col à l'affichage | Moyenne | Helper `Position.fromOffset(source, offset)` à part, pas dans Token |
| Tests trop volumineux à écrire dans le sprint | Moyenne | Priorité aux couches Lexer + CstParser + intégration ; CstDumper et CstHelper plus légers |

## 8. Ce que cette spec **ne** couvre pas

- Plan d'exécution détaillé tâche-par-tâche (cf. plan dérivé via `superpowers:writing-plans`)
- Estimations en heures
- Critères de merge final vers `main`
- Suppression du parser RD (sprint suivant)

---

**Fin de spec.**
