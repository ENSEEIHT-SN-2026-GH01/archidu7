# Spec — Parser LL(1) SHDL (SCRUM-23)

**Auteur** : Alexis Briend
**Date** : 2026-04-12 (fin sprint 1)
**Ticket** : SCRUM-23 (parent SCRUM-9 "Interpretation langage / Modélisation")
**Sous-tâches originales** : SCRUM-43 (Firsts), SCRUM-44 (Follow), SCRUM-45 (Build syntax tree) — assignées à Erwan par erreur, reprises dans cette spec.

## 1. Objectif et scope

Implémenter un parser syntaxique **LL(1) avec lookahead 2 local sur `Instance`** pour le langage SHDL. Le parser prend une `List<Token>` produite par le lexer d'Erwan (`parser/automate/AutomateDeterministe`) et produit un AST (`Module` à la racine) consommable par l'analyseur sémantique, le simulateur (Mati) et la future visualisation.

### Dans le scope

- Grammaire LL(1) formelle dérivée de la grammaire PEG SHDL de référence
- Calcul dynamique des ensembles First et Follow
- Détecteur de conflits LL(1) (avec auto-tests sur grammaires négatives)
- Parser en descente récursive utilisant First sets pour le choix des alternatives
- AST immutable typé, interface `Node` + classes classiques
- Pattern Visitor générique avec retour typé
- Gestion d'erreurs fail-fast avec messages riches (ligne, colonne, contexte grammatical, snippet source, suggestions)
- Suite de tests JUnit 4 exhaustive (couverture > 90% lignes, 100% non-terminaux, 100% cellules LL(1), 100% types d'erreur, tests anti-régression sur grammaires négatives)

### Hors scope

- Lexer : à la charge d'Erwan (`parser/regex/` + `parser/automate/`)
- Analyseur sémantique : phase ultérieure (équipotentielles, types, résolution sous-modules)
- Simulateur : Mati (`origin/simulation`)
- UI : Arthur/Chaptal
- Commentaires SHDL (`//`, `#`) : gérés/ignorés par le lexer, invisibles pour le parser
- Mode panic recovery : hors scope sprint 1, TODO explicite ajouté
- Pretty-printer et round-trip AST ↔ texte : sprint 2
- **Préservation des commentaires dans l'AST** (AST "lossy") : sprint 2 si UI le demande. Coût : ajouter `List<Comment> trivia` à chaque `Node`. Décision d'architecture assumée.
- **Gradle-ification du parser** : reportée sprint 2 (décision Q2 du brainstorming). Le code vit à la racine comme celui d'Erwan, builde via `javac` manuel pour l'intégration de ce soir.

## 2. Architecture

### 2.1 Emplacement

Package `parser.ll1`, dossier `parser/ll1/` à la racine, à côté de `parser/regex/` et `parser/automate/` d'Erwan. Alignement total avec sa convention (packages déclarés, pas de records, pas de sealed, Java classique).

### 2.2 Arborescence

```
parser/ll1/
├── token/
│   ├── TokenType.java          enum avec MODULE, END, IDENTIFIER, INTEGER, BITFIELD, ...
│   └── Token.java              classe valeur (type, value, line, column) avec getters
│
├── grammar/
│   ├── Symbol.java             interface — Terminal ou NonTerminal
│   ├── Terminal.java           wrapper TokenType
│   ├── NonTerminal.java        enum MODULE, INSTANCE, SIGNAL, FACTOR, etc.
│   ├── Production.java         (NonTerminal head, List<Symbol> body)
│   ├── Grammar.java            Grammar.SHDL = liste ordonnée des productions
│   ├── FirstSet.java           calcul algorithme du point fixe, Map<NonTerminal, Set<TokenType>>
│   ├── FollowSet.java          calcul algorithme du point fixe
│   └── Ll1ConflictChecker.java détecte conflits FIRST/FIRST, FIRST/FOLLOW, récursion gauche
│
├── ast/
│   ├── Node.java               interface avec Position, accept(Visitor)
│   ├── Position.java           classe (int line, int column)
│   ├── Visitor.java            interface Visitor<R> avec visit(Module), visit(Assignment), ...
│   ├── DefaultVisitor.java     abstract class avec traversée par défaut des enfants
│   ├── Module.java
│   ├── Instance.java           interface marqueur pour ModuleInstance, TriState, Assignment, MemoryPoint, Fsm, MapNode
│   ├── Assignment.java         classe implements Instance
│   ├── TriState.java           classe implements Instance
│   ├── MemoryPoint.java        classe implements Instance
│   ├── ModuleInstance.java     classe implements Instance
│   ├── Fsm.java, FsmHeader.java, FsmRule.java
│   ├── MapNode.java, MapEntry.java    (MapNode plutôt que Map pour éviter collision avec java.util.Map)
│   ├── Signal.java             classe (name + optional hi/lo indices)
│   ├── SignalCompound.java     classe (liste de Signal concaténés avec &)
│   ├── SumOfTerms.java, Term.java, Factor.java
│   └── BitField.java
│
├── parser/
│   ├── Parser.java             public Module parse(), descente récursive
│   ├── ParsingException.java   extends RuntimeException, avec ErrorCode enum
│   └── ErrorCode.java          enum UNEXPECTED_TOKEN, EOF_UNEXPECTED, DEPTH_EXCEEDED, ...
```

Tests dans `tests/parser/ll1/` en arbre miroir (voir section 7).

### 2.3 API publique

```java
// Construction principale — lance IllegalArgumentException si tokens == null
Parser parser = new Parser(tokens);                    // sans source brute (pas de snippets)
Parser parser = new Parser(tokens, shdlText);          // avec source brute pour snippets
Module ast = parser.parse();                           // ParsingException si erreur
// parse() 2ᵉ appel → IllegalStateException

// Factory haut-niveau (lexer + parser) — utile pour Mati et l'UI
Module ast = Parser.parseFrom(String shdlText, Lexer lexer);

// Accès à la grammaire et aux ensembles (pour debug / UI / tests)
Grammar.SHDL                                           // singleton immutable
Grammar.SHDL.first(NonTerminal.FACTOR)                 // Set<TokenType>
Grammar.SHDL.follow(NonTerminal.INSTANCE_LIST)
new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts() // liste (vide si OK)
```

**Contrats** :
- `tokens` non-null (checké par `Objects.requireNonNull`)
- `shdlText` peut être `null` → snippets source absents des messages d'erreur
- Le parser **n'appelle pas** `Ll1ConflictChecker` au démarrage (évite coût à chaque instanciation) — la cohérence est vérifiée **par les tests** via `GrammarFreezeTest` + `Ll1ConflictTest`

## 3. Grammaire LL(1)

### 3.1 Grammaire PEG source

Source normative : `shdl_grammar.peg` du frontend existant. Reproduite pour référence à la fin de ce document.

### 3.2 Transformations appliquées

**Factorisations gauches et éliminations de récursion** :

```
Module           → MODULE IDENTIFIER LPAREN ParamList RPAREN InstanceList END MODULE

ParamList        → Param ParamListRest
ParamListRest    → separ Param ParamListRest | ε
separ            → COMMA | COLON

ArgList          → Arg ArgListRest
ArgListRest      → separ Arg ArgListRest | ε

Param            → Signal
Arg              → SignalOrLiteral ArgRest
SignalOrLiteral  → Signal | INTEGER | BITFIELD
ArgRest          → AMPERSAND SignalOrLiteral ArgRest | ε

Signal           → IDENTIFIER SignalTail
SignalTail       → LBRACKET INTEGER SignalAfterInt | ε
SignalAfterInt   → RBRACKET | DOTDOT INTEGER RBRACKET | COLON INTEGER RBRACKET

SignalCompound      → Signal SignalCompoundRest
SignalCompoundRest  → AMPERSAND Signal SignalCompoundRest | ε

SumOfTermsCompound       → SumOfTerms SumOfTermsCompoundRest
SumOfTermsCompoundRest   → AMPERSAND SumOfTerms SumOfTermsCompoundRest | ε

SumOfTerms       → Term SumOfTermsRest
SumOfTermsRest   → PLUS Term SumOfTermsRest | ε

Term             → Factor TermRest
TermRest         → STAR Factor TermRest | ε

Factor           → LPAREN SumOfTerms RPAREN
                 | INTEGER                      (validation sémantique : 0 ou 1)
                 | BITFIELD
                 | SLASH Signal
                 | Signal

InstanceList     → Instance InstanceListRest
InstanceListRest → Instance InstanceListRest | ε       (ε accepté si suivant = END)

Instance         → (dispatch par lookahead, voir §3.3)

AssignOrTri      → SignalCompound EQ SumOfTermsCompound TriStateTail
TriStateTail     → OUTPUT ENABLED WHEN SumOfTerms | ε

MemoryPoint      → SignalCompound ASSIGN SumOfTermsCompound ON SumOfTerms 
                   OptComma SetReset WHEN SumOfTerms MemTail OptSemi
MemTail          → OptComma ENABLED WHEN SumOfTerms | ε
SetReset         → SET | RESET
OptComma         → COMMA | ε
OptSemi          → SEMICOLON | ε

ModuleInstance   → IDENTIFIER LPAREN ArgList RPAREN
                 | DOLLAR IDENTIFIER LPAREN ArgList RPAREN

Map              → MAP SignalCompound ARROW SignalCompound MapValueList END MAP
MapValueList     → MapValue MapValueList | ε
MapValue         → BITFIELD ARROW BITFIELD

Fsm              → FsmKeyword FsmHeader FsmRuleList END FsmKeyword
FsmKeyword       → FSM | STATEMACHINE
FsmHeader        → ASYNCHRONOUS
                 | SYNCHRONOUS ON SumOfTerms OptComma IDENTIFIER WHEN SumOfTerms
                 | IDENTIFIER WHEN SumOfTerms OptComma SYNCHRONOUS ON SumOfTerms
FsmRuleList      → FsmRule FsmRuleList | ε
FsmRule          → FsmRuleLeft ARROW IDENTIFIER FsmRuleRest
FsmRuleRest      → WHEN SumOfTerms OptSemiOrComma | ε
OptSemiOrComma   → SEMICOLON | COMMA | ε
FsmRuleLeft      → STAR | StateNameList
StateNameList    → IDENTIFIER StateNameListRest
StateNameListRest → COMMA IDENTIFIER StateNameListRest | ε
```

### 3.3 Disambiguation de `Instance` (lookahead 2 local)

`Instance` est le seul point où LL(1) strict ne suffit pas. Le choix entre `ModuleInstance`, `AssignOrTri`, `MemoryPoint`, `Fsm`, `Map` se fait par inspection des deux premiers tokens :

| peek(0) | peek(1) | Règle choisie |
|---------|---------|---------------|
| `IDENTIFIER` | `LPAREN` | `ModuleInstance` (standard) |
| `IDENTIFIER` | autre | Parser `SignalCompound`, puis : `EQ` → `AssignOrTri`, `ASSIGN` → `MemoryPoint` |
| `DOLLAR` | — | `ModuleInstance` (prédéfini) |
| `FSM` ou `STATEMACHINE` | — | `Fsm` |
| `MAP` | — | `Map` |

**Justification** : extension LL(k=2) documentée dans le livre *Compilation : analyse lexicale et syntaxique* de Legendre/Schwarzentruber. La factorisation pure LL(1) nécessiterait un non-terminal `InstanceAfterIdent` fusionnant toutes les suites possibles après `IDENTIFIER`, ce qui obscurcit la lisibilité sans bénéfice pratique. Trade-off assumé et documenté.

### 3.4 Écarts conscients vs la PEG

1. **`DotDot`** : PEG autorise `".."` ou `":"`. On conserve les deux via `SignalAfterInt` (règle avec `DOTDOT` ou `COLON`). Pas de rupture de compat.
2. **Règle `Signal` dupliquée** dans la PEG (lignes 21-22 et 23-24 identiques) : une seule production conservée.
3. **`LiteralValue`** intégrée directement dans `Factor` et `SignalOrLiteral` (évite un non-terminal inutile).
4. **`ModuleName` et `StateName` en PEG** autorisent un chiffre initial (`[a-zA-Z0-9_]+`), mais le token `IDENTIFIER` du lexer ne le permet pas. **Restriction consciente** : tous les noms passent par `IDENTIFIER`. À confirmer avec Erwan (`QUESTIONS_EQUIPE.md`).
5. **`Factor` avec INTEGER** : la PEG a `'0'` ou `'1'` littéraux. Notre token `INTEGER` couvre `[0-9]+`. **Validation sémantique** au parser : si `Factor` reçoit un `INTEGER` dont la valeur ∉ {"0", "1"}, `ParsingException(BIT_OUT_OF_RANGE)`.

## 4. Algorithmes

### 4.1 Calcul de First (point fixe)

```
First(t) = {t}               pour tout terminal t
First(ε) = {ε}
First(X) = ⋃ First(α)        pour toute production X → α
First(Y α) = si ε ∉ First(Y) : First(Y)
             sinon          : (First(Y) \ {ε}) ∪ First(α)

Répéter jusqu'à stabilisation.
```

### 4.2 Calcul de Follow (point fixe)

```
Follow(S) = {EOF}            pour l'axiome S (= Module)
Pour chaque production A → α X β :
  Follow(X) ⊇ First(β) \ {ε}
  Si ε ∈ First(β) ou β est vide :
    Follow(X) ⊇ Follow(A)

Répéter jusqu'à stabilisation.
```

### 4.3 Détection des conflits LL(1)

Pour chaque non-terminal `A` avec productions `A → α₁ | α₂ | ... | αₙ` :

- **FIRST/FIRST** : `First(αᵢ) ∩ First(αⱼ) ≠ ∅` pour `i ≠ j`
- **FIRST/FOLLOW** : si `ε ∈ First(αᵢ)`, alors `First(αⱼ) ∩ Follow(A) ≠ ∅` pour tout `j ≠ i`

Le checker retourne `List<Ll1Conflict>` avec type, non-terminal concerné, productions et tokens en conflit.

### 4.4 Parseur en descente récursive

Une méthode privée `parseX()` par non-terminal. Chaque méthode :

1. Pousse son nom sur `grammarStack` via `enterRule`
2. Consulte `peek(0)` (et `peek(1)` pour `Instance`) pour choisir la production
3. Consomme les terminaux via `consume(expected)`
4. Récurse sur les non-terminaux
5. Construit et retourne le nœud AST
6. Dépile via `finally` dans `enterRule`

## 5. AST

### 5.1 Immutabilité

- Tous les champs `private final`, set par constructeur uniquement
- `Objects.requireNonNull(champ, "nom")` dans chaque constructeur
- `List.copyOf(enfants)` pour toute liste d'enfants (immutable + non-null)
- Tests de reflection vérifient `final` sur tous les champs
- Tests vérifient que `getList().add(...)` throw `UnsupportedOperationException`

**Annotations futures** (types inférés, équipotentielles) : via `IdentityHashMap<Node, Annotation>` **externes**. Documenté dans Javadoc de `Node`. Aucun champ non-final ne sera ajouté aux classes AST.

### 5.2 Interface Node et Visitor

```java
public interface Node {
    Position getPosition();
    <R> R accept(Visitor<R> v);
}

public interface Visitor<R> {
    R visit(Module m);
    R visit(Assignment a);
    R visit(TriState t);
    R visit(MemoryPoint m);
    R visit(ModuleInstance mi);
    R visit(Fsm f);
    R visit(MapNode m);
    R visit(SumOfTerms s);
    R visit(Term t);
    R visit(Factor f);
    R visit(Signal s);
    R visit(SignalCompound sc);
    R visit(BitField b);
    // ... un par classe AST concrète
}

public abstract class DefaultVisitor<R> implements Visitor<R> {
    // Valeur retournée par défaut (null), overridable pour R primitif wrappé
    protected R defaultResult() { return null; }
    // Traversée par défaut : visite chaque enfant, retourne defaultResult()
    // Les sous-classes overrident les méthodes qui les intéressent
}
```

### 5.3 Position source

Chaque `Node` porte `Position(line, column)` correspondant au premier token consommé pour le construire. Utilisé par `ParsingException`, les futures analyses sémantiques, et l'UI pour surligner les erreurs dans l'éditeur.

## 6. Gestion d'erreurs

### 6.1 Classe ParsingException

```java
public class ParsingException extends RuntimeException {
    public enum ErrorCode {
        UNEXPECTED_TOKEN,
        EOF_UNEXPECTED,
        DEPTH_EXCEEDED,
        EMPTY_FILE,
        BIT_OUT_OF_RANGE,
        TRAILING_TOKENS,
        EMPTY_PARAM_LIST,
        EMPTY_INSTANCE_LIST,
        // Note : double-parse = IllegalStateException (erreur d'API, pas erreur de parse)
        // Note : grammaire non-LL(1) détectée = GrammarDefinitionException séparée (erreur de développeur, pas de l'utilisateur SHDL)
    }

    private final ErrorCode code;
    private final int line, column;
    private final Set<TokenType> expected;   // LinkedHashSet pour ordre déterministe
    private final TokenType actual;
    private final List<String> grammarContext;  // snapshot immédiat dans le constructeur
    private final String sourceSnippet;       // ligne source avec caret "^"
    private final String suggestion;          // optionnel, via Levenshtein sur mots-clés
    // + constructeur qui snapshot immédiatement context via List.copyOf(stack)
    // + getMessage() formaté
    // + getters pour tests
}
```

### 6.2 Format du message d'erreur

```
Ligne 3, colonne 12 : attendu [EQ, ASSIGN], reçu INTEGER('5')
  3 | c = a * 5b
              ^
Contexte : Module > InstanceList > Instance > AssignOrTri > SignalCompound
Suggestion : peut-être vouliez-vous dire « module » ?
```

### 6.3 Garde-fous robustesse

| Cas | Comportement |
|-----|-------------|
| `List<Token>` vide ou seul EOF | `ParsingException(EMPTY_FILE)` |
| Pas de token EOF en fin de liste | EOF synthétique ajouté par le constructeur du Parser |
| `parse()` appelé 2× | `IllegalStateException("parse() déjà appelé")` (erreur d'API, pas erreur SHDL) |
| Tokens restants après `end module` | `ParsingException(TRAILING_TOKENS)` |
| EOF au milieu d'une règle | `ParsingException(EOF_UNEXPECTED)` |
| Récursion > `MAX_DEPTH` (= 64) | `ParsingException(DEPTH_EXCEEDED)` |
| `Factor` avec INTEGER ∉ {0, 1} | `ParsingException(BIT_OUT_OF_RANGE)` |
| `ParamList` vide | `ParsingException(EMPTY_PARAM_LIST)` |
| `InstanceList` vide | `ParsingException(EMPTY_INSTANCE_LIST)` |

### 6.4 Parser non thread-safe

Documenté en Javadoc. Un `Parser` est construit et consommé séquentiellement par un seul thread.

## 7. Plan de tests (JUnit 4)

### 7.1 Organisation

```
tests/parser/ll1/
├── token/
│   └── TokenTest.java
├── grammar/
│   ├── GrammarFreezeTest.java       hash stable des productions
│   ├── FirstSetTest.java            valeurs hardcodées
│   ├── FollowSetTest.java           valeurs hardcodées
│   └── Ll1ConflictTest.java         positifs (SHDL) + négatifs (grammaires sciemment KO)
├── ast/
│   ├── AstImmutabilityTest.java     reflection (final) + unmodifiableList
│   ├── VisitorTest.java             DefaultVisitor + exhaustivité
│   └── PositionTest.java            chaque nœud a une Position cohérente
├── parser/
│   ├── ParserSignalTest.java
│   ├── ParserExpressionTest.java    @Parameterized — priorités, parenthésage
│   ├── ParserAssignmentTest.java    @Parameterized — positifs + négatifs
│   ├── ParserTriStateTest.java
│   ├── ParserMemoryPointTest.java
│   ├── ParserModuleInstanceTest.java (incluant test lookahead 2 discriminant)
│   ├── ParserFsmTest.java
│   ├── ParserMapTest.java
│   ├── ParserFullModuleTest.java    ET, BasculeD, FsmSynchrone, DecodeurBCD
│   └── ParserErrorTest.java         ErrorCode, line, column, actual pour chaque cas
└── fixtures/
    ├── ShdlFixtures.java            builders AST avec validation
    └── TokenFixtures.java           builders Token
```

### 7.2 Tests critiques (preuves de solidité)

**a) Ll1ConflictTest sur grammaires négatives** (prouve que le checker marche) :

```java
@Test
public void checkerDetecteConflitFirstFirst() {
    // A → a B | a C
    Grammar badGrammar = Grammar.of(
        prod(A, a, B), prod(A, a, C), ...);
    List<Ll1Conflict> conflicts = new Ll1ConflictChecker(badGrammar).findAllConflicts();
    assertFalse(conflicts.isEmpty());
    assertEquals(Ll1Conflict.Type.FIRST_FIRST, conflicts.get(0).getType());
}

@Test
public void checkerDetecteRecursionGauche() { ... }

@Test
public void checkerDetecteConflitFirstFollow() { ... }

@Test
public void grammaireShdlEstSansConflit() {
    assertTrue(new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts().isEmpty());
}
```

**b) ParserErrorTest avec assertions fines** (jamais `@Test(expected=)`) :

```java
@Test
public void erreurSurFactorAvec5() {
    try {
        new Parser(TokenFixtures.expr("5"), null).parse();
        fail("Attendu ParsingException");
    } catch (ParsingException e) {
        assertEquals(ErrorCode.BIT_OUT_OF_RANGE, e.getCode());
        assertEquals(1, e.getLine());
        assertEquals(INTEGER, e.getActual());
    }
}
```

**c) ParserPositionTest** :

```java
@Test
public void moduleAsLaPositionDuMotMODULE() {
    Module m = parse("module ET(a : b) b = a end module");
    assertEquals(new Position(1, 1), m.getPosition());
}
```

**d) AstImmutabilityTest par reflection** + test UnsupportedOperationException sur les listes :

```java
@Test
public void tousLesChampsSontFinal() throws Exception {
    for (Class<?> cls : findAllNodeSubclasses()) {
        for (Field f : cls.getDeclaredFields()) {
            assertTrue(cls.getSimpleName() + "." + f.getName() + " n'est pas final",
                Modifier.isFinal(f.getModifiers()));
        }
    }
}

@Test(expected = UnsupportedOperationException.class)
public void listesAstSontImmutables() {
    Module m = parseSimple();
    m.getInstances().add(null);  // doit throw
}
```

### 7.3 Modules E2E

Quatre programmes SHDL complets sont tokenisés à la main (via fixtures), parsés, et comparés à un AST attendu construit via les builders :

1. **ET** : `module ET(a, b : c) c = a * b end module`
2. **BasculeD** : bascule D avec clock, reset, enable
3. **FsmSynchrone** : FSM synchrone 2 états
4. **DecodeurBCD** : `map` BCD → 7 segments

Ces tests sont la preuve concrète que le parser marche sur du SHDL réel.

### 7.4 Couverture visée

| Métrique | Objectif |
|----------|----------|
| Lignes du package `parser.ll1` | > 90% |
| Non-terminaux | 100% (chacun dans ≥ 1 test positif et ≥ 1 négatif) |
| Cellules table LL(1) non-vides | 100% |
| Types d'ErrorCode | 100% |

Total estimé : **60-80 tests**, temps d'exécution < 2 secondes.

### 7.5 Ordre TDD

1. `TokenTest` — valide `Token`
2. `GrammarFreezeTest` — gèle `Grammar.SHDL` avec un hash stable
3. `FirstSetTest` (tests rouges) → implémenter `FirstSet` → verts
4. `FollowSetTest` (tests rouges) → implémenter `FollowSet` → verts
5. `Ll1ConflictTest` (grammaires négatives d'abord) → implémenter `Ll1ConflictChecker` → verts
6. `AstImmutabilityTest` par reflection — force la discipline dans les classes AST au fur et à mesure
7. `VisitorTest`, `PositionTest`
8. Tests parser par non-terminal, du plus simple au plus complexe
9. Tests de robustesse (erreurs, profondeur, vide, EOF) en fin

## 8. Intégration dans le projet

### 8.1 Ce soir (fin sprint 1)

- Code LL(1) complet dans `parser/ll1/` sur branche `feature/skeleton-javafx` (ou nouvelle `feature/ll1-parser`)
- Pas de modification des autres branches
- Pas de Gradle-ification (cf. décision Q2)
- Merge en `feature/integration` à la fin

### 8.2 Contrats externes

- **Lexer (Erwan)** : fournit `List<Token>` avec EOF terminal, ignore commentaires et whitespace
- **Analyseur sémantique (futur)** : consomme `Module` via Visitor
- **Simulateur (Mati)** : consomme `Module` via Visitor spécialisé `ModuleToSimulatorVisitor`

### 8.3 Questions ouvertes (QUESTIONS_EQUIPE.md)

- Valider le contrat Token avec Erwan
- Confirmer que le lexer émet `DOLLAR` + `IDENTIFIER` séparément pour `$nand`
- Confirmer alignement strict sur `IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*`
- Version Java cible du projet (records, sealed)

## 9. Risques et mitigations

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Grammaire LL(1) a un conflit non détecté à la main | Moyenne | Élevé | `Ll1ConflictChecker` auto-testé sur grammaires négatives |
| Valeurs First/Follow hardcodées fausses | Moyenne | Moyen | Comparaison croisée algorithme dynamique vs hardcodé, les 2 doivent coïncider |
| AST trop rigide pour annotations futures | Faible | Moyen | `Map<Node, ?>` externes avec `IdentityHashMap` |
| `StackOverflowError` sur entrée pathologique | Moyenne | Élevé | `MAX_DEPTH = 64` + `ParsingException(DEPTH_EXCEEDED)` |
| Lexer d'Erwan non aligné sur nos tokens | Moyenne | Élevé | Contrat Token documenté + `QUESTIONS_EQUIPE.md` |
| Erwan rejette le style de code | Moyenne | Élevé | Alignement strict sur ses conventions (packages, classes, JUnit 4, RuntimeException). Revue conjointe dès que possible. |
| Token class dupliquée côté lexer (Erwan) et parser (nous) | Moyenne | Élevé | Le parser définit `Token` dans `parser.ll1.token`. Erwan l'importe. Si refus, créer un package commun `parser.token` au sprint 2. |
| Branche `feature/integration` absente | Certaine | Faible | Créer en fin de soirée lors du merge sprint 1 |

## 10. Annexe — PEG de référence (extrait)

Reproduit depuis `frontend/src/lib/shdl/shdl_grammar.peg` de la version Vue existante. Source normative pour la sémantique SHDL.

```
Module = "module" ModuleName "(" ParamList ")" InstanceList "end" "module"
Signal = SignalName "[" NaturalInteger "]" 
       | SignalName "[" NaturalInteger DotDot NaturalInteger "]"
       | SignalName
SignalCompound = Signal ("&" Signal)*
SignalOrLiteralCompound = (Signal | LiteralValue) ("&" (Signal | LiteralValue))*
DotDot = ".." | ":"
ParamList = Param separ ParamList | Param
ArgList = Arg separ ArgList | Arg
InstanceList = Instance InstanceList | Instance
Instance = ModuleInstance | TriState | Assignment | MemoryPoint | Fsm | Map
Assignment = SignalCompound "=" SumOfTermsCompound
TriState = SignalCompound "=" SumOfTermsCompound "output" "enabled" "when" SumOfTerms
MemoryPoint = SignalCompound ":=" SumOfTermsCompound "on" SumOfTerms ","? 
              ("reset"|"set") "when" SumOfTerms [","? "enabled" "when" SumOfTerms] ";"?
ModuleInstance = ModuleName "(" ArgList ")" | "$" ModuleName "(" ArgList ")"
Map = "map" SignalCompound "->" SignalCompound MapValue* "end" "map"
Fsm = ("fsm"|"statemachine") FsmHeader FsmRules "end" ("fsm"|"statemachine")
SumOfTerms = Term ("+" Term)*
Term = Factor ("*" Factor)*
Factor = "(" SumOfTerms ")" | "0" | "1" | BitField | Signal | "/" Signal
separ = "," | ":"
BitField = "\"" [01]+ "\""
```

---

**Fin de spec.** À relire par Alexis avant implémentation.
