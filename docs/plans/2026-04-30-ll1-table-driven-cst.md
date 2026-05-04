# Plan d'implémentation — Parser LL(1) table-driven SHDL, sortie CST

> **Pour workers agentiques :** SOUS-SKILL REQUISE : `superpowers:subagent-driven-development` (recommandé) ou `superpowers:executing-plans` pour implémenter tâche par tâche. Cases `- [ ]` pour le suivi.

**Goal :** Implémenter un parser LL(1) **table-driven** transformant une `String` source SHDL en un **CST strict** (arbre de dérivation), selon la spec `docs/specs/2026-04-30-ll1-table-driven-cst-design.md`. Aucun AST construit dans cette branche — la transformation aval est hors scope.

**Architecture :** Pipeline `String → Lexer → List<Token> → CstParser (drivé par ParsingTable) → CstNode`. Grammaire formelle (`Grammar.SHDL`) comme source unique de vérité, table M[NT, T] générée dynamiquement via FIRST/FOLLOW existants. CST strict (cascades `*_Star` non linéarisées). `CstInternal` stocke la `Production` complète. Helpers minimum : `first`/`allOf`/`has`. Positions calculées automatiquement sur tous les nœuds.

**Tech Stack :** Java 25 (sealed + records + pattern matching switch), JUnit 4, build via `javac` manuel à la racine. Packages `parser.ll1.tabledriven.*`. Pas de Gradle.

**Branche :** `feature/parser-ll1-table-driven` (déjà créée depuis `feature/parser-ll1`). Le code RD existant (`parser/ll1/parser/Parser.java`) reste **intact** pendant le développement, **sans aucun import** depuis le nouveau code.

**Convention de test :** chaque étape suit `Write failing test → Run (fail) → Implement → Run (pass) → Commit`. Discipline test-first par couche : tous les tests d'une couche sont écrits **avant** l'implémentation de cette couche. TDD strict en intra-couche pour les sous-tâches.

**Commande de test unique (à adapter par suite) :**
```bash
javac -cp ".:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore tests.parser.ll1.tabledriven.<Suite>
```

**Convention de commit :** `<type>(<scope>): <message>` où `<type>` ∈ `feat|fix|test|refactor|docs|chore` et `<scope>` désigne la couche (`grammar`, `lexer`, `table`, `cst`, `parser`, `dumper`, `setup`).

---

## Task 0 : Setup arborescence et sanity check

**Files :**
- Create: `parser/ll1/tabledriven/package-info.java`
- Create: `tests/parser/ll1/tabledriven/package-info.java`
- Create: `tests/parser/ll1/tabledriven/SanityTest.java`

- [ ] **Step 1 : Vérifier que JUnit est déjà présent** (réutilisé du sprint 1)

```bash
ls lib/junit-4.13.2.jar lib/hamcrest-core-1.3.jar
```
Attendu : les 2 jars présents (sinon, les récupérer comme dans le plan sprint 1, Task 0).

- [ ] **Step 2 : Vérifier que la branche est correcte**
```bash
git branch --show-current
```
Attendu : `feature/parser-ll1-table-driven`.

- [ ] **Step 3 : Vérifier l'absence d'import depuis le RD**
```bash
grep -r "import parser.ll1.parser\." parser/ll1/tabledriven/ tests/parser/ll1/tabledriven/ 2>/dev/null
```
Attendu : aucun résultat (la couche table-driven ne dépend pas du RD).
Vérifier ça à chaque commit, ajouter au protocole de review.

- [ ] **Step 4 : Créer l'arborescence**
```bash
mkdir -p parser/ll1/tabledriven/{lexer,table,cst}
mkdir -p tests/parser/ll1/tabledriven/{lexer,table,cst,parser,fixtures,integration}
```

- [ ] **Step 5 : Écrire les `package-info.java`**

`parser/ll1/tabledriven/package-info.java` :
```java
/**
 * Parser LL(1) table-driven pour SHDL. Sortie : CST strict.
 * Voir docs/specs/2026-04-30-ll1-table-driven-cst-design.md.
 */
package parser.ll1.tabledriven;
```

`tests/parser/ll1/tabledriven/package-info.java` :
```java
package tests.parser.ll1.tabledriven;
```

- [ ] **Step 6 : Sanity test**

`tests/parser/ll1/tabledriven/SanityTest.java` :
```java
package tests.parser.ll1.tabledriven;

import org.junit.Test;
import static org.junit.Assert.*;

public class SanityTest {
    @Test public void vrai() { assertTrue(true); }
}
```

Compiler + lancer :
```bash
javac -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d out \
  parser/ll1/tabledriven/package-info.java \
  tests/parser/ll1/tabledriven/package-info.java \
  tests/parser/ll1/tabledriven/SanityTest.java
java -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore tests.parser.ll1.tabledriven.SanityTest
```
Attendu : `OK (1 test)`.

- [ ] **Step 7 : Commit**
```bash
git add parser/ll1/tabledriven/ tests/parser/ll1/tabledriven/
git commit -m "chore(setup): arborescence tabledriven + sanity"
```

---

## Task 1 : Mise à jour de `Grammar.SHDL`

**Objectif :** Aligner `Grammar.SHDL` (et `NonTerminal`, `Terminal` au besoin) sur la grammaire LL(1) cible définie dans `shdl_grammar_LL1.txt`.

**Files :**
- Modify: `parser/ll1/grammar/Grammar.java` (productions de `Grammar.SHDL`)
- Modify: `parser/ll1/grammar/NonTerminal.java` (enum mis à jour)
- Modify: `parser/ll1/token/TokenType.java` (enum mis à jour si nécessaire)
- Create: `tests/parser/ll1/tabledriven/grammar/GrammarFreezeTest.java`
- Create: `tests/parser/ll1/tabledriven/grammar/GrammarStructureTest.java`
- Create: `tests/parser/ll1/tabledriven/grammar/Ll1ConflictTest.java`

**Stratégie :** test-first. Les 3 tests sont écrits AVANT toute modif de `Grammar.java`.

- [ ] **Step 1 : Lire la grammaire cible**

Ouvrir `/home/alexis/Téléchargements/temp/shdl_grammar_LL1.txt` (ou la version à jour transmise) et lister :
- Tous les non-terminaux (à mettre dans `NonTerminal` enum)
- Tous les terminaux (à mettre dans `TokenType` enum)
- Les productions exactes
- Les bugs identifiés à corriger côté Java :
  - L52 `Factor ::= RightPar SumOfTerms LeftPar` → écrire `LeftPar SumOfTerms RightPar`
  - `SignalCompound` (L16-18) : omettre (non référencé)

- [ ] **Step 2 : Écrire `GrammarFreezeTest` (test rouge)**

`tests/parser/ll1/tabledriven/grammar/GrammarFreezeTest.java` :
```java
package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.Grammar;
import static org.junit.Assert.*;

public class GrammarFreezeTest {
    @Test
    public void texte_grammaire_fige() {
        String expected = """
            Start ::= Module
            Module ::= ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW
            Instance_Plus ::= Instance Instance_Star
            Instance_Star ::= Instance Instance_Star
            Instance_Star ::= ε
            ...
            """;  // texte complet exact attendu, non tronqué
        assertEquals(expected.strip(), Grammar.SHDL.toBnf().strip());
    }
}
```
Lancer : doit échouer (ou parce que `toBnf()` n'existe pas encore, ou parce que le texte ne matche pas l'ancienne grammaire).

- [ ] **Step 3 : Écrire `GrammarStructureTest` (test rouge)**

```java
package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import static org.junit.Assert.*;

public class GrammarStructureTest {
    @Test public void start_est_axiome() {
        assertEquals(NonTerminal.Start, Grammar.SHDL.start());
    }
    @Test public void nombre_total_productions() {
        // Compter exactement le nombre de productions attendues (à figer)
        assertEquals(<N>, Grammar.SHDL.productions().size());
    }
    @Test public void tous_non_terminaux_definis() {
        for (NonTerminal nt : NonTerminal.values()) {
            assertTrue("NT " + nt + " sans production",
                Grammar.SHDL.productions().stream().anyMatch(p -> p.lhs() == nt));
        }
    }
}
```

- [ ] **Step 4 : Écrire `Ll1ConflictTest` (test rouge)**

```java
package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.Ll1ConflictChecker;
import static org.junit.Assert.*;

public class Ll1ConflictTest {
    @Test public void aucun_conflit_sur_grammaire_cible() {
        var conflits = Ll1ConflictChecker.check(Grammar.SHDL);
        assertTrue("Conflits LL(1) détectés : " + conflits, conflits.isEmpty());
    }
}
```

- [ ] **Step 5 : Lancer les tests, confirmer rouge**
```bash
# compile + run uniquement les tests grammar
```
Attendu : 3 fails (ou erreurs de compilation).

- [ ] **Step 6 : Mettre à jour `NonTerminal.java`**

Adapter l'enum pour matcher exactement les non-terminaux de la grammaire cible. Garder uniquement ceux utilisés. Renommer / ajouter / supprimer comme nécessaire.

- [ ] **Step 7 : Mettre à jour `TokenType.java`**

Adapter pour matcher les terminaux. Liste cible (à confirmer depuis grammar file) :
```
MODULE_KW, END_KW, ON_KW, WHEN_KW, SET_KW, RESET_KW, ENABLED_KW,
LEFT_PAR, RIGHT_PAR, LEFT_SQUARE_BRACK, RIGHT_SQUARE_BRACK,
COMMA, COLON, SEMICOLON, POINT_POINT, DOLLAR,
ASSIGN_OP, MEM_ASSIGN_OP, OR_OP, AND_OP, CONCAT_OP, NOT_OP,
IDENTIFIANT, BIT_FIELD, NATURAL_INTEGER,
EOF
```

- [ ] **Step 8 : Mettre à jour `Grammar.java` (productions)**

Réécrire `Grammar.SHDL` avec les productions cibles. Format :
```java
public static final Grammar SHDL = new GrammarBuilder()
    .start(NonTerminal.Start)
    .add(Start, Module)
    .add(Module, ModuleKW, Identifiant, LeftPar, Param, Separ_Param_Star, RightPar, Instance_Plus, EndKW, ModuleKW)
    .add(Instance_Plus, Instance, Instance_Star)
    .add(Instance_Star, Instance, Instance_Star)
    .addEpsilon(Instance_Star)
    // ... toutes les productions
    .build();
```

Si `GrammarBuilder` n'a pas l'API `addEpsilon` ou similaire, l'ajouter (mais cohérent avec l'existant — vérifier d'abord).

- [ ] **Step 9 : Lancer les tests, confirmer vert**

Attendu : `GrammarFreezeTest`, `GrammarStructureTest`, `Ll1ConflictTest` tous verts. Si `Ll1ConflictTest` rouge, c'est qu'il y a un conflit dans la grammaire cible — investiguer (probablement bug grammaire à signaler en amont).

- [ ] **Step 10 : Vérifier que les anciens tests grammar (non-régression)**

Les anciens `FirstSetTest`, `FollowSetTest`, `GrammarFreezeTest` (s'il existe sous l'ancien nom) doivent soit (a) être supprimés (ils testaient l'ancienne grammaire), soit (b) être migrés. Décision : **supprimer les anciens** s'ils figent l'ancienne grammaire. Garder `FirstSetTest` / `FollowSetTest` qui testent les algorithmes sur grammaires synthétiques (indépendants de la grammaire concrète).

```bash
# Identifier les tests qui dépendent de la grammaire concrète
grep -l "Grammar.SHDL" tests/parser/ll1/grammar/
```
Pour chaque résultat : décider migration ou suppression.

- [ ] **Step 11 : Commit**
```bash
git add parser/ll1/grammar/ parser/ll1/token/TokenType.java tests/parser/ll1/tabledriven/grammar/
git commit -m "feat(grammar): mise a jour Grammar.SHDL avec grammaire LL(1) cible"
```

---

## Task 2 : Tests algorithmes Grammar (FIRST, FOLLOW)

**Objectif :** S'assurer que les tests existants `FirstSetTest`/`FollowSetTest` (qui testent les algorithmes, pas la grammaire concrète) passent toujours, et compléter avec des tests sur la nouvelle grammaire.

**Files :**
- Inspect: `tests/parser/ll1/grammar/FirstSetTest.java`
- Inspect: `tests/parser/ll1/grammar/FollowSetTest.java`
- Possibly create: `tests/parser/ll1/tabledriven/grammar/FirstSetShdlTest.java`
- Possibly create: `tests/parser/ll1/tabledriven/grammar/FollowSetShdlTest.java`

- [ ] **Step 1 : Lancer les tests existants**

Si `FirstSetTest`/`FollowSetTest` testent uniquement les algorithmes sur grammaires de toy : verts → on les garde tels quels.

Si certains testent FIRST/FOLLOW sur l'ancienne `Grammar.SHDL` : ils sont rouges → migrer ou supprimer.

- [ ] **Step 2 : Ajouter quelques cas FIRST/FOLLOW caractéristiques sur la grammaire cible**

Pour avoir une régression bruyante si la grammaire évolue silencieusement. Exemples (à adapter) :

`tests/parser/ll1/tabledriven/grammar/FirstSetShdlTest.java` :
```java
package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.FirstSet;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.token.TokenType;
import java.util.Set;
import static org.junit.Assert.*;

public class FirstSetShdlTest {
    private final FirstSet first = FirstSet.compute(Grammar.SHDL);

    @Test public void first_module_contient_module_kw() {
        assertTrue(first.of(NonTerminal.Module).contains(TokenType.MODULE_KW));
    }
    @Test public void first_factor_contient_4_terminaux() {
        var f = first.of(NonTerminal.Factor);
        assertTrue(f.contains(TokenType.LEFT_PAR));
        assertTrue(f.contains(TokenType.BIT_FIELD));
        assertTrue(f.contains(TokenType.NOT_OP));
        assertTrue(f.contains(TokenType.IDENTIFIANT));
    }
    // ... 5-10 cas représentatifs
}
```

`FollowSetShdlTest.java` similaire.

- [ ] **Step 3 : Lancer, vert, commit**
```bash
git add tests/parser/ll1/tabledriven/grammar/
git commit -m "test(grammar): FIRST/FOLLOW caracteristiques sur grammaire cible"
```

---

## Task 3 : `Token` (record) et `TokenType` finalisé

**Files :**
- Refactor: `parser/ll1/token/Token.java` (passer en record offset-only)
- Possibly modify: `parser/ll1/token/TokenType.java` (déjà fait Task 1, vérifier)
- Create: `tests/parser/ll1/tabledriven/lexer/TokenTest.java`

**Note :** ce refactor de `Token` est cassant pour le parser RD (qui utilise `Token(type, value, line, col, offset)`). On accepte cette casse — le RD ne sera plus testé/lancé pendant cette branche. À la fin, on supprime le RD ou on le laisse non compilable jusqu'à suppression.

**Mitigation alternative** : conserver l'ancien `Token` à 5 champs et créer un nouveau `Token2` ou `Tok` pour le table-driven. Décidé : **non**, on assume la casse, on simplifie au passage. Si le RD doit être préservé, on le déplace dans une sous-package isolée.

- [ ] **Step 1 : Décision casse RD**

Vérifier l'état du RD :
```bash
ls parser/ll1/parser/
grep -l "new Token(" parser/ll1/parser/ tests/parser/ll1/parser/
```

Décision proposée : on déplace temporairement le RD hors compilation en renommant `parser/ll1/parser/Parser.java` → `parser/ll1/parser/Parser.java.legacy` (ne sera plus compilé par le find pattern `*.java`). Idem pour ses tests et `Lexer.java`. Tout préserver, juste hors build.

```bash
git mv parser/ll1/parser/Parser.java parser/ll1/parser/Parser.java.legacy
git mv parser/ll1/parser/Lexer.java parser/ll1/parser/Lexer.java.legacy
git mv parser/ll1/parser/ParsingException.java parser/ll1/parser/ParsingException.java.legacy
git mv parser/ll1/parser/ErrorCode.java parser/ll1/parser/ErrorCode.java.legacy
# tests RD aussi
for f in tests/parser/ll1/parser/*.java; do git mv "$f" "$f.legacy"; done
# tests AST aussi (utilisent l'ancien AST/Token)
for f in tests/parser/ll1/ast/*.java; do git mv "$f" "$f.legacy"; done
# package-info OK à laisser ou bouger selon
```

Commit intermédiaire : `chore(legacy): mise hors build des sources RD du sprint 1`.

- [ ] **Step 2 : Écrire `TokenTest`**

```java
package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class TokenTest {
    @Test public void offset_et_end_avec_valeur() {
        var t = new Token(TokenType.IDENTIFIANT, "abc", 10);
        assertEquals(10, t.offset());
        assertEquals(13, t.end());
        assertEquals("abc", t.value());
    }
    @Test public void end_avec_valeur_null() {
        var t = new Token(TokenType.LEFT_PAR, null, 5);
        assertEquals(5, t.offset());
        assertEquals(5, t.end());  // pas de longueur si value=null
    }
    @Test public void egalite_records() {
        var a = new Token(TokenType.IDENTIFIANT, "x", 0);
        var b = new Token(TokenType.IDENTIFIANT, "x", 0);
        assertEquals(a, b);
    }
    @Test public void differents_offsets_pas_egaux() {
        assertNotEquals(
            new Token(TokenType.IDENTIFIANT, "x", 0),
            new Token(TokenType.IDENTIFIANT, "x", 1)
        );
    }
}
```

Lancer : rouge (Token a encore l'ancienne signature).

- [ ] **Step 3 : Réécrire `Token` en record**

```java
package parser.ll1.token;

import java.util.Objects;

public record Token(TokenType type, String value, int offset) {
    public Token {
        Objects.requireNonNull(type, "type");
        if (offset < 0) throw new IllegalArgumentException("offset < 0");
    }
    public int end() {
        return offset + (value == null ? 0 : value.length());
    }
}
```

- [ ] **Step 4 : Vert, commit**
```bash
javac ... && java ... TokenTest
git add parser/ll1/token/Token.java tests/parser/ll1/tabledriven/lexer/TokenTest.java
git commit -m "feat(token): record Token offset-only"
```

---

## Task 4 : `KeywordTable` et `LexerException`

**Files :**
- Create: `parser/ll1/tabledriven/lexer/KeywordTable.java`
- Create: `parser/ll1/tabledriven/lexer/LexerException.java`
- Create: `tests/parser/ll1/tabledriven/lexer/KeywordTableTest.java`

- [ ] **Step 1 : Test rouge `KeywordTableTest`**

```java
package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.KeywordTable;
import parser.ll1.token.TokenType;
import java.util.Optional;
import static org.junit.Assert.*;

public class KeywordTableTest {
    @Test public void module_reconnu() {
        assertEquals(Optional.of(TokenType.MODULE_KW), KeywordTable.lookup("module"));
    }
    @Test public void end_reconnu() {
        assertEquals(Optional.of(TokenType.END_KW), KeywordTable.lookup("end"));
    }
    @Test public void identifiant_quelconque_pas_reconnu() {
        assertEquals(Optional.empty(), KeywordTable.lookup("modulo"));
        assertEquals(Optional.empty(), KeywordTable.lookup("foo"));
    }
    @Test public void casse_sensible() {
        assertEquals(Optional.empty(), KeywordTable.lookup("Module"));
        assertEquals(Optional.empty(), KeywordTable.lookup("END"));
    }
    @Test public void couvre_tous_les_keywords_attendus() {
        // 7 keywords
        for (var kw : java.util.List.of("module","end","on","when","set","reset","enabled")) {
            assertTrue(kw + " manquant", KeywordTable.lookup(kw).isPresent());
        }
    }
}
```

- [ ] **Step 2 : Implémenter `KeywordTable`**

```java
package parser.ll1.tabledriven.lexer;

import parser.ll1.token.TokenType;
import java.util.Map;
import java.util.Optional;

public final class KeywordTable {
    private static final Map<String, TokenType> KEYWORDS = Map.of(
        "module",  TokenType.MODULE_KW,
        "end",     TokenType.END_KW,
        "on",      TokenType.ON_KW,
        "when",    TokenType.WHEN_KW,
        "set",     TokenType.SET_KW,
        "reset",   TokenType.RESET_KW,
        "enabled", TokenType.ENABLED_KW
    );
    private KeywordTable() {}
    public static Optional<TokenType> lookup(String lexeme) {
        return Optional.ofNullable(KEYWORDS.get(lexeme));
    }
}
```

- [ ] **Step 3 : Implémenter `LexerException` (squelette)**

```java
package parser.ll1.tabledriven.lexer;

public class LexerException extends RuntimeException {
    private final int offset;
    public LexerException(String message, int offset) {
        super("Erreur lexicale a l'offset " + offset + " : " + message);
        this.offset = offset;
    }
    public LexerException(String message, int offset, Throwable cause) {
        super("Erreur lexicale a l'offset " + offset + " : " + message, cause);
        this.offset = offset;
    }
    public int offset() { return offset; }
}
```

- [ ] **Step 4 : Vert, commit**
```bash
git add parser/ll1/tabledriven/lexer/KeywordTable.java parser/ll1/tabledriven/lexer/LexerException.java \
        tests/parser/ll1/tabledriven/lexer/KeywordTableTest.java
git commit -m "feat(lexer): KeywordTable + LexerException"
```

---

## Task 5 : `ShdlLexer` complet

**Files :**
- Create: `parser/ll1/tabledriven/lexer/ShdlLexer.java`
- Create: `tests/parser/ll1/tabledriven/lexer/ShdlLexerTest.java`
- Create: `tests/parser/ll1/tabledriven/lexer/WhitespaceCommentSkipTest.java`
- Create: `tests/parser/ll1/tabledriven/lexer/LexerEdgeCasesTest.java`

**Stratégie :** test-first, mais en plusieurs sous-batches parce que la couche est dense.

### 5.A — Tokenisation par type de token (5-12 tokens isolés)

- [ ] **Step 5.A.1 : Test rouge — tokenisation de chaque token isolé**

`ShdlLexerTest.java` (extrait, à compléter pour chaque type) :
```java
package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class ShdlLexerTest {
    @Test public void identifiant_simple() {
        var tokens = ShdlLexer.tokenize("foo");
        assertEquals(2, tokens.size());  // foo + EOF
        assertEquals(TokenType.IDENTIFIANT, tokens.get(0).type());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(0, tokens.get(0).offset());
        assertEquals(TokenType.EOF, tokens.get(1).type());
    }
    @Test public void keyword_module() {
        var tokens = ShdlLexer.tokenize("module");
        assertEquals(TokenType.MODULE_KW, tokens.get(0).type());
        assertNull(tokens.get(0).value());
    }
    @Test public void bitfield() {
        var tokens = ShdlLexer.tokenize(".0101");
        assertEquals(TokenType.BIT_FIELD, tokens.get(0).type());
        assertEquals(".0101", tokens.get(0).value());
    }
    @Test public void natural_integer() {
        var tokens = ShdlLexer.tokenize("42");
        assertEquals(TokenType.NATURAL_INTEGER, tokens.get(0).type());
        assertEquals("42", tokens.get(0).value());
    }
    @Test public void operateurs_simples() {
        // tester chaque opérateur isolément : =, ::=, +, *, &, /, .., :, ,, ;, $, (, ), [, ]
        for (var pair : List.of(
            List.of("=", TokenType.ASSIGN_OP),
            List.of("::=", TokenType.MEM_ASSIGN_OP),
            List.of("+", TokenType.OR_OP),
            // ...
        )) {
            var tokens = ShdlLexer.tokenize((String) pair.get(0));
            assertEquals(pair.get(0).toString(),
                (TokenType) pair.get(1), tokens.get(0).type());
        }
    }
    @Test public void source_vide() {
        var tokens = ShdlLexer.tokenize("");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
        assertEquals(0, tokens.get(0).offset());
    }
}
```

- [ ] **Step 5.A.2 : Implémentation minimale de `ShdlLexer`**

```java
package parser.ll1.tabledriven.lexer;

import parser.automate.AutomateDeterministe;
import parser.automate.LexingException;
import parser.regex.Builder;
import parser.regex.Regex;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import util.Pair;
import java.util.*;

public final class ShdlLexer {
    private static final AutomateDeterministe<TokenType> AUTOMATE = buildAutomate();

    private static AutomateDeterministe<TokenType> buildAutomate() {
        List<Pair<Regex, TokenType>> rules = new LinkedList<>();
        // ordre : pas important grâce à reclassif keywords post-lex et longest match
        // PAS de keywords ici (reclassif post-lex)
        addRule(rules, "[a-zA-Z_][a-zA-Z0-9_]*",   TokenType.IDENTIFIANT);
        addRule(rules, "\\.[0-1]+",                 TokenType.BIT_FIELD);
        addRule(rules, "[0-9]+",                    TokenType.NATURAL_INTEGER);
        addRule(rules, "::=",                       TokenType.MEM_ASSIGN_OP);
        addRule(rules, "=",                         TokenType.ASSIGN_OP);
        addRule(rules, "\\+",                       TokenType.OR_OP);
        addRule(rules, "\\*",                       TokenType.AND_OP);
        addRule(rules, "&",                         TokenType.CONCAT_OP);
        addRule(rules, "/",                         TokenType.NOT_OP);
        addRule(rules, "\\.\\.",                    TokenType.POINT_POINT);
        addRule(rules, ":",                         TokenType.COLON);
        addRule(rules, ",",                         TokenType.COMMA);
        addRule(rules, ";",                         TokenType.SEMICOLON);
        addRule(rules, "\\$",                       TokenType.DOLLAR);
        addRule(rules, "\\(",                       TokenType.LEFT_PAR);
        addRule(rules, "\\)",                       TokenType.RIGHT_PAR);
        addRule(rules, "\\[",                       TokenType.LEFT_SQUARE_BRACK);
        addRule(rules, "\\]",                       TokenType.RIGHT_SQUARE_BRACK);
        try {
            return AutomateDeterministe.fromList(rules);
        } catch (LexingException e) {
            throw new IllegalStateException("Construction DFA echoue", e);
        }
    }

    private static void addRule(List<Pair<Regex, TokenType>> rules, String regex, TokenType t) {
        rules.add(Pair.pair(Builder.parseRegex(regex), t));
    }

    private ShdlLexer() {}

    public static List<Token> tokenize(String source) {
        Objects.requireNonNull(source, "source");
        List<Token> tokens = new ArrayList<>();
        int offset = 0;
        while (offset < source.length()) {
            // skip whitespace + comments
            int skipped = skipTrivia(source, offset);
            if (skipped > 0) { offset += skipped; continue; }
            if (offset >= source.length()) break;

            String reste = source.substring(offset);
            Pair<TokenType, Integer> p;
            try {
                p = AUTOMATE.exec1(reste);
            } catch (LexingException e) {
                throw new LexerException(
                    "lexeme non reconnu : " + previewChar(reste),
                    offset, e
                );
            }
            int len = p.snd();
            String lexeme = source.substring(offset, offset + len);
            TokenType type = p.fst();

            // reclassif keyword si Identifiant
            if (type == TokenType.IDENTIFIANT) {
                var kw = KeywordTable.lookup(lexeme);
                if (kw.isPresent()) { type = kw.get(); }
            }

            // value : null pour keywords/operateurs sans contenu utile, lexeme pour les 3 types valeur
            String value = needsValue(type) ? lexeme : null;
            tokens.add(new Token(type, value, offset));
            offset += len;
        }
        tokens.add(new Token(TokenType.EOF, null, source.length()));
        return tokens;
    }

    private static boolean needsValue(TokenType t) {
        return t == TokenType.IDENTIFIANT
            || t == TokenType.BIT_FIELD
            || t == TokenType.NATURAL_INTEGER;
    }

    private static String previewChar(String s) {
        return s.isEmpty() ? "<vide>" : "'" + s.charAt(0) + "'";
    }

    /** Avance de la longueur des whitespace + commentaires consécutifs. Retourne 0 si rien à skipper. */
    private static int skipTrivia(String source, int from) {
        int i = from;
        boolean progressed = true;
        while (progressed && i < source.length()) {
            progressed = false;
            // whitespace
            while (i < source.length() && isWs(source.charAt(i))) { i++; progressed = true; }
            // commentaire // ...
            if (i + 1 < source.length() && source.charAt(i) == '/' && source.charAt(i+1) == '/') {
                while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') i++;
                progressed = true;
            }
            // commentaire # ...
            if (i < source.length() && source.charAt(i) == '#') {
                while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') i++;
                progressed = true;
            }
        }
        return i - from;
    }

    private static boolean isWs(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
}
```

- [ ] **Step 5.A.3 : Vert, commit**
```bash
git add parser/ll1/tabledriven/lexer/ShdlLexer.java tests/parser/ll1/tabledriven/lexer/ShdlLexerTest.java
git commit -m "feat(lexer): ShdlLexer tokenisation par type"
```

### 5.B — Skip whitespace et commentaires

- [ ] **Step 5.B.1 : Tests rouges**

`tests/parser/ll1/tabledriven/lexer/WhitespaceCommentSkipTest.java` :
```java
package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class WhitespaceCommentSkipTest {
    @Test public void espaces_entre_tokens() {
        var tokens = ShdlLexer.tokenize("foo  bar");
        // foo + bar + EOF
        assertEquals(3, tokens.size());
        assertEquals(0, tokens.get(0).offset());
        assertEquals(5, tokens.get(1).offset());  // foo + 2 espaces = 5
    }
    @Test public void retours_ligne_et_tabs() {
        var tokens = ShdlLexer.tokenize("foo\n\tbar");
        assertEquals(3, tokens.size());
        assertEquals(5, tokens.get(1).offset());
    }
    @Test public void commentaire_double_slash() {
        var tokens = ShdlLexer.tokenize("foo // commentaire\nbar");
        assertEquals(3, tokens.size());  // foo + bar + EOF
        assertEquals(TokenType.IDENTIFIANT, tokens.get(0).type());
        assertEquals(TokenType.IDENTIFIANT, tokens.get(1).type());
    }
    @Test public void commentaire_diese() {
        var tokens = ShdlLexer.tokenize("foo # commentaire\nbar");
        assertEquals(3, tokens.size());
    }
    @Test public void commentaire_jusqu_a_eof() {
        var tokens = ShdlLexer.tokenize("foo // jusqu'a la fin");
        assertEquals(2, tokens.size());  // foo + EOF
    }
    @Test public void source_only_whitespace() {
        var tokens = ShdlLexer.tokenize("   \n\t  ");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }
    @Test public void source_only_comment() {
        var tokens = ShdlLexer.tokenize("// rien d'autre");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }
}
```

- [ ] **Step 5.B.2 : Si tests échouent, corriger `skipTrivia`. Si verts d'emblée, commit séparé pour la traçabilité.**

```bash
git add tests/parser/ll1/tabledriven/lexer/WhitespaceCommentSkipTest.java
git commit -m "test(lexer): skip whitespace + commentaires"
```

### 5.C — Edge cases

- [ ] **Step 5.C.1 : Tests rouges**

`tests/parser/ll1/tabledriven/lexer/LexerEdgeCasesTest.java` :
```java
package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.LexerException;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class LexerEdgeCasesTest {
    @Test public void lexeme_invalide() {
        try {
            ShdlLexer.tokenize("@");
            fail("LexerException attendue");
        } catch (LexerException e) {
            assertEquals(0, e.offset());
        }
    }
    @Test public void offset_correct_apres_plusieurs_tokens() {
        var tokens = ShdlLexer.tokenize("a b c");
        assertEquals(0, tokens.get(0).offset());  // a
        assertEquals(2, tokens.get(1).offset());  // b
        assertEquals(4, tokens.get(2).offset());  // c
    }
    @Test public void mots_cles_seuls_dans_identifiants() {
        // "modulo" doit rester IDENTIFIANT, pas se faire tronquer en "module" + "o"
        // -> longest match doit gagner
        var tokens = ShdlLexer.tokenize("modulo");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.IDENTIFIANT, tokens.get(0).type());
        assertEquals("modulo", tokens.get(0).value());
    }
    @Test public void token_eof_offset_egal_taille_source() {
        var tokens = ShdlLexer.tokenize("foo");
        assertEquals(3, tokens.get(tokens.size() - 1).offset());
    }
    @Test public void mem_assign_pas_decompose() {
        var tokens = ShdlLexer.tokenize("::=");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.MEM_ASSIGN_OP, tokens.get(0).type());
    }
}
```

- [ ] **Step 5.C.2 : Vert, commit**
```bash
git add tests/parser/ll1/tabledriven/lexer/LexerEdgeCasesTest.java
git commit -m "test(lexer): edge cases (lexeme invalide, longest match, EOF offset)"
```

---

## Task 6 : `ParsingTable` et `TableBuilder`

**Files :**
- Create: `parser/ll1/tabledriven/table/ParsingTable.java`
- Create: `parser/ll1/tabledriven/table/TableBuilder.java`
- Create: `tests/parser/ll1/tabledriven/table/ParsingTableTest.java`
- Create: `tests/parser/ll1/tabledriven/table/TableBuilderTest.java`

- [ ] **Step 1 : Tests rouges**

`ParsingTableTest.java` :
```java
package tests.parser.ll1.tabledriven.table;

import org.junit.Test;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.token.TokenType;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.*;

public class ParsingTableTest {
    @Test public void lookup_present() {
        // construire une mini table avec une entrée
        var prod = /* production stub */;
        var key = new ParsingTable.TableKey(NonTerminal.Module, TokenType.MODULE_KW);
        var table = new ParsingTable(Map.of(key, prod));
        assertEquals(Optional.of(prod), table.lookup(NonTerminal.Module, TokenType.MODULE_KW));
    }
    @Test public void lookup_absent_renvoie_empty() {
        var table = new ParsingTable(Map.of());
        assertEquals(Optional.empty(), table.lookup(NonTerminal.Module, TokenType.MODULE_KW));
    }
}
```

`TableBuilderTest.java` :
```java
package tests.parser.ll1.tabledriven.table;

import org.junit.Test;
import parser.ll1.grammar.Grammar;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.tabledriven.table.TableBuilder;
import static org.junit.Assert.*;

public class TableBuilderTest {
    @Test public void grammaire_shdl_construit_sans_conflit() {
        ParsingTable t = TableBuilder.build(Grammar.SHDL);
        assertNotNull(t);
        // sanity : au moins une entrée pour Module sur MODULE_KW
        assertTrue(t.lookup(parser.ll1.grammar.NonTerminal.Module,
            parser.ll1.token.TokenType.MODULE_KW).isPresent());
    }
    @Test public void cellule_dupliquee_leve_exception() {
        // construire une grammaire artificielle non-LL(1)
        // (à coder via un GrammarBuilder de test si possible, sinon mock)
        // S -> a | a   <-- conflit FIRST/FIRST
        // Attendu : IllegalStateException
    }
    @Test public void mini_grammaire_table_attendue() {
        // S -> a | b ; toy 2 productions
        // table = {(S, a) -> P1, (S, b) -> P2}
        // ...
    }
}
```

- [ ] **Step 2 : Implémenter `ParsingTable`**

```java
package parser.ll1.tabledriven.table;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.token.TokenType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ParsingTable(Map<TableKey, Production> entries) {
    public ParsingTable {
        Objects.requireNonNull(entries);
        entries = Map.copyOf(entries);  // immutable
    }
    public Optional<Production> lookup(NonTerminal nt, TokenType t) {
        return Optional.ofNullable(entries.get(new TableKey(nt, t)));
    }
    public record TableKey(NonTerminal nt, TokenType t) {
        public TableKey {
            Objects.requireNonNull(nt); Objects.requireNonNull(t);
        }
    }
}
```

- [ ] **Step 3 : Implémenter `TableBuilder`**

```java
package parser.ll1.tabledriven.table;

import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.*;

public final class TableBuilder {
    private TableBuilder() {}
    public static ParsingTable build(Grammar g) {
        FirstSet first = FirstSet.compute(g);
        FollowSet follow = FollowSet.compute(g, first);
        Map<ParsingTable.TableKey, Production> table = new HashMap<>();
        for (Production p : g.productions()) {
            Set<TokenType> firstAlpha = first.ofSequence(p.rhs());
            for (TokenType a : firstAlpha) {
                if (a == TokenType.EOF) continue;  // EOF handled via FOLLOW
                put(table, new ParsingTable.TableKey(p.lhs(), a), p);
            }
            if (firstAlpha.contains(/* epsilon marker */)) {
                for (TokenType b : follow.of(p.lhs())) {
                    put(table, new ParsingTable.TableKey(p.lhs(), b), p);
                }
            }
        }
        return new ParsingTable(table);
    }
    private static void put(Map<ParsingTable.TableKey, Production> table,
                            ParsingTable.TableKey k, Production p) {
        var existing = table.put(k, p);
        if (existing != null && !existing.equals(p)) {
            throw new IllegalStateException("Conflit table " + k +
                " : " + existing + " vs " + p);
        }
    }
}
```

**Note** : la sentinelle ε et l'API exacte `FirstSet.ofSequence` doivent matcher l'API existante. Adapter selon `parser/ll1/grammar/FirstSet.java`.

- [ ] **Step 4 : Vert, commit**
```bash
git add parser/ll1/tabledriven/table/ tests/parser/ll1/tabledriven/table/
git commit -m "feat(table): ParsingTable + TableBuilder"
```

---

## Task 7 : `CstNode`, `CstInternal`, `CstLeaf` + helpers

**Files :**
- Create: `parser/ll1/tabledriven/cst/CstNode.java`
- Create: `parser/ll1/tabledriven/cst/CstInternal.java`
- Create: `parser/ll1/tabledriven/cst/CstLeaf.java`
- Create: `tests/parser/ll1/tabledriven/cst/CstNavigationTest.java`
- Create: `tests/parser/ll1/tabledriven/cst/CstStructureTest.java`

- [ ] **Step 1 : Tests rouges**

`CstStructureTest.java` :
```java
package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.cst.*;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class CstStructureTest {
    @Test public void leaf_offsets_proviennent_du_token() {
        var leaf = new CstLeaf(/* terminal */, new Token(TokenType.IDENTIFIANT, "abc", 5));
        assertEquals(5, leaf.startOffset());
        assertEquals(8, leaf.endOffset());
    }
    @Test public void internal_offsets_calcules() {
        var t1 = new CstLeaf(/*...*/, new Token(TokenType.IDENTIFIANT, "a", 0));
        var t2 = new CstLeaf(/*...*/, new Token(TokenType.IDENTIFIANT, "b", 4));
        var nt = new CstInternal(NonTerminal.Module, /* prod */, List.of(t1, t2));
        assertEquals(0, nt.startOffset());
        assertEquals(5, nt.endOffset());
    }
    @Test public void internal_epsilon_offset_du_curseur() {
        var nt = CstInternal.epsilon(NonTerminal.Instance_Star, /* prod */, 10);
        assertEquals(10, nt.startOffset());
        assertEquals(10, nt.endOffset());
    }
}
```

`CstNavigationTest.java` :
```java
package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.cst.*;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.*;

public class CstNavigationTest {
    @Test public void first_terminal_present() {
        // CstInternal avec un Leaf IDENTIFIANT et un Leaf LEFT_PAR
        var leaf1 = new CstLeaf(/*...*/, new Token(TokenType.IDENTIFIANT, "x", 0));
        var leaf2 = new CstLeaf(/*...*/, new Token(TokenType.LEFT_PAR, null, 1));
        var node = new CstInternal(NonTerminal.Module, /* prod */, List.of(leaf1, leaf2));
        var found = node.first(/* Symbol pour LEFT_PAR */);
        assertTrue(found.isPresent());
        assertEquals(TokenType.LEFT_PAR, ((CstLeaf) found.get()).token().type());
    }
    @Test public void allOf_retourne_tous_matches() {
        // 3 enfants Param, ils doivent tous être retournés
        // ...
    }
    @Test public void has_terminal_oui_non() { /* ... */ }
    @Test public void leaf_first_toujours_empty() {
        var leaf = new CstLeaf(/*...*/, new Token(TokenType.IDENTIFIANT, "x", 0));
        assertEquals(Optional.empty(), leaf.first(/* anything */));
        assertTrue(leaf.allOf(/* anything */).isEmpty());
        assertFalse(leaf.has(/* anything */));
    }
}
```

- [ ] **Step 2 : Implémenter `CstNode` et records**

```java
package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.Symbol;
import java.util.List;
import java.util.Optional;

public sealed interface CstNode permits CstInternal, CstLeaf {
    int startOffset();
    int endOffset();
    Symbol symbol();

    Optional<CstNode> first(Symbol s);
    List<CstNode> allOf(Symbol s);
    boolean has(Symbol s);
}
```

```java
package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Symbol;
import java.util.*;
import java.util.stream.Collectors;

public record CstInternal(
    NonTerminal nt,
    Production rule,
    List<CstNode> children,
    int startOffset,
    int endOffset
) implements CstNode {
    public CstInternal {
        Objects.requireNonNull(nt);
        Objects.requireNonNull(rule);
        children = List.copyOf(children);
    }

    /** Constructeur principal : positions calculées depuis les enfants. */
    public static CstInternal of(NonTerminal nt, Production rule, List<CstNode> children) {
        Objects.requireNonNull(children);
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Use epsilon() for empty children");
        }
        int start = children.get(0).startOffset();
        int end   = children.get(children.size() - 1).endOffset();
        return new CstInternal(nt, rule, children, start, end);
    }

    /** Constructeur ε : offset = position du curseur courant dans le flux. */
    public static CstInternal epsilon(NonTerminal nt, Production rule, int cursorOffset) {
        return new CstInternal(nt, rule, List.of(), cursorOffset, cursorOffset);
    }

    @Override public Symbol symbol() { return nt; }

    @Override public Optional<CstNode> first(Symbol s) {
        return children.stream().filter(c -> c.symbol().equals(s)).findFirst();
    }
    @Override public List<CstNode> allOf(Symbol s) {
        return children.stream().filter(c -> c.symbol().equals(s)).collect(Collectors.toList());
    }
    @Override public boolean has(Symbol s) {
        return children.stream().anyMatch(c -> c.symbol().equals(s));
    }
}
```

```java
package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;
import parser.ll1.token.Token;
import java.util.*;

public record CstLeaf(Terminal t, Token token) implements CstNode {
    public CstLeaf {
        Objects.requireNonNull(t);
        Objects.requireNonNull(token);
    }
    @Override public int startOffset() { return token.offset(); }
    @Override public int endOffset()   { return token.end(); }
    @Override public Symbol symbol()   { return t; }
    @Override public Optional<CstNode> first(Symbol s) { return Optional.empty(); }
    @Override public List<CstNode> allOf(Symbol s)     { return List.of(); }
    @Override public boolean has(Symbol s)             { return false; }
}
```

- [ ] **Step 3 : Vert, commit**
```bash
git add parser/ll1/tabledriven/cst/ tests/parser/ll1/tabledriven/cst/
git commit -m "feat(cst): CstNode sealed + CstInternal/CstLeaf + helpers first/allOf/has"
```

---

## Task 8 : `CstParser` (driver à pile)

**Files :**
- Create: `parser/ll1/tabledriven/CstParser.java`
- Create: `parser/ll1/tabledriven/ParsingException.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserModuleTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserSignalTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserExpressionTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserAssignmentTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserMemoryAssignmentTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserModuleCallTest.java`
- Create: `tests/parser/ll1/tabledriven/parser/CstParserErrorTest.java`

**Stratégie :** test-first par construct. Pour chaque construct, on écrit le test puis on s'assure que le parser passe. Le parser lui-même est driver générique table-driven, donc une fois qu'il marche pour Module il marche en principe pour tout le reste — sauf bug ou edge case grammatical.

### 8.A — Squelette parser + Module simplissime

- [ ] **Step 8.A.1 : `ParsingException`**

```java
package parser.ll1.tabledriven;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

public class ParsingException extends RuntimeException {
    private final int offset;
    private final TokenType expected;
    private final Token actual;
    private final NonTerminal context;

    public ParsingException(String message, int offset, TokenType expected,
                            Token actual, NonTerminal context) {
        super(format(message, offset, expected, actual, context));
        this.offset = offset;
        this.expected = expected;
        this.actual = actual;
        this.context = context;
    }
    private static String format(String msg, int offset, TokenType expected,
                                  Token actual, NonTerminal ctx) {
        return "Erreur syntaxique a l'offset " + offset
            + " : " + msg
            + (expected != null ? " (attendu " + expected + ")" : "")
            + (actual != null ? " (trouve " + actual.type() +
                (actual.value() != null ? "(\"" + actual.value() + "\")" : "") + ")" : "")
            + (ctx != null ? " [contexte " + ctx + "]" : "");
    }
    public int offset() { return offset; }
    public TokenType expected() { return expected; }
    public Token actual() { return actual; }
    public NonTerminal context() { return context; }
}
```

- [ ] **Step 8.A.2 : Test rouge — `module foo () i = .0 end module`**

`tests/parser/ll1/tabledriven/parser/CstParserModuleTest.java` :
```java
package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import static org.junit.Assert.*;

public class CstParserModuleTest {
    @Test public void module_minimal() {
        // Note : le module exact dépend de la grammaire ; ici un cas qui doit passer
        String src = "module foo (a) i = .0 end module";
        CstNode root = CstParser.parse(src);
        assertTrue(root instanceof CstInternal);
        assertEquals(NonTerminal.Start, ((CstInternal) root).nt());
        // racine = Start, qui contient Module
        var module = ((CstInternal) root).first(NonTerminal.Module);
        assertTrue(module.isPresent());
    }
}
```

- [ ] **Step 8.A.3 : Implémenter `CstParser` (driver complet)**

```java
package parser.ll1.tabledriven;

import parser.ll1.grammar.*;
import parser.ll1.tabledriven.cst.*;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.tabledriven.table.TableBuilder;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.*;

public final class CstParser {
    private static final ParsingTable TABLE = TableBuilder.build(Grammar.SHDL);

    private CstParser() {}

    public static CstNode parse(String source) {
        return parseTokens(ShdlLexer.tokenize(source));
    }

    public static CstNode parseTokens(List<Token> tokens) {
        Deque<Symbol> stack = new ArrayDeque<>();
        // Pile inversée : EOF en bas, START en haut
        stack.push(/* EOF terminal */);
        stack.push(Grammar.SHDL.start());

        // Pile parallèle de "constructions en cours" :
        // chaque entrée = (CstInternal en construction, nb d'enfants attendus, liste enfants accumulés)
        Deque<Frame> frames = new ArrayDeque<>();
        // Frame racine factice pour collecter le Start
        Frame rootFrame = new Frame(null, null, 1, new ArrayList<>());
        frames.push(rootFrame);

        int cursor = 0;
        while (!stack.isEmpty()) {
            Symbol top = stack.pop();
            Token current = tokens.get(cursor);

            if (top instanceof Terminal terminalTop) {
                if (terminalTop.matches(current.type())) {
                    var leaf = new CstLeaf(terminalTop, current);
                    attach(frames, leaf);
                    if (current.type() == TokenType.EOF) {
                        // fin
                        break;
                    }
                    cursor++;
                } else {
                    throw new ParsingException(
                        "token inattendu", current.offset(),
                        terminalTop.tokenType(), current, /* context si dispo */ null);
                }
            } else if (top instanceof NonTerminal ntTop) {
                Optional<Production> opt = TABLE.lookup(ntTop, current.type());
                if (opt.isEmpty()) {
                    throw new ParsingException(
                        "aucune production applicable", current.offset(),
                        null, current, ntTop);
                }
                Production prod = opt.get();
                // ouvrir un frame
                frames.push(new Frame(ntTop, prod, prod.rhs().size(), new ArrayList<>()));
                // pousser RHS en ordre inverse
                for (int i = prod.rhs().size() - 1; i >= 0; i--) {
                    Symbol s = prod.rhs().get(i);
                    if (!s.isEpsilon()) stack.push(s);
                }
                if (prod.isEpsilon()) {
                    // fermer immédiatement
                    var frame = frames.pop();
                    var node = CstInternal.epsilon(ntTop, prod, current.offset());
                    attach(frames, node);
                }
            }

            // fermer les frames complets
            while (!frames.isEmpty() && frames.peek().isComplete()) {
                Frame f = frames.pop();
                if (f.nt == null) break;  // root frame
                CstNode node = CstInternal.of(f.nt, f.production, f.children);
                attach(frames, node);
            }
        }

        if (rootFrame.children.size() != 1) {
            throw new IllegalStateException("Parsing incoherent : root pas unique");
        }
        return rootFrame.children.get(0);
    }

    private static void attach(Deque<Frame> frames, CstNode node) {
        Frame top = frames.peek();
        if (top == null) throw new IllegalStateException("No frame to attach");
        top.children.add(node);
    }

    private static class Frame {
        final NonTerminal nt;
        final Production production;
        final int expectedChildren;
        final List<CstNode> children;
        Frame(NonTerminal nt, Production p, int n, List<CstNode> ch) {
            this.nt = nt; this.production = p; this.expectedChildren = n; this.children = ch;
        }
        boolean isComplete() { return children.size() >= expectedChildren; }
    }
}
```

**Note importante :** la mécanique exacte de `Symbol`, `Terminal.matches(TokenType)`, `Production.isEpsilon()`, etc., doit s'aligner sur l'API existante de `parser/ll1/grammar/`. Adapter au besoin.

- [ ] **Step 8.A.4 : Vert, commit**
```bash
git add parser/ll1/tabledriven/CstParser.java parser/ll1/tabledriven/ParsingException.java \
        tests/parser/ll1/tabledriven/parser/CstParserModuleTest.java
git commit -m "feat(parser): CstParser driver a pile + ParsingException + premier test Module"
```

### 8.B — Tests par construct (ajout incrémental)

Pour chaque construct ci-dessous : écrire test rouge, vérifier que le parser passe, sinon ajuster CstParser ou la grammaire et re-tester.

- [ ] **Step 8.B.1 : `CstParserSignalTest`** — Signal sans range, avec index, avec range `..`, avec range `:`
- [ ] **Step 8.B.2 : `CstParserExpressionTest`** — `a + b`, `a * b`, `/a`, `(a + b) * c`, `a & b & c`, mélanges
- [ ] **Step 8.B.3 : `CstParserAssignmentTest`** — `c = a + b`, `c[3..0] = ...`, concat en RHS
- [ ] **Step 8.B.4 : `CstParserMemoryAssignmentTest`** — `c ::= a on b , set when c enabled when d ;`
- [ ] **Step 8.B.5 : `CstParserModuleCallTest`** — `fullAdder(a, b: s, co)`, `$fullAdder(a, b: s, co)`
- [ ] **Step 8.B.6 : `CstParserErrorTest`** — token inattendu, RightPar manquant, EndKW manquant, expression mal formée

Chaque test fichier suit le même pattern : entrée source SHDL → `CstParser.parse(src)` → assertions sur la structure du CST. Comparaison via :
- accès direct `cst.first(NT)` / `allOf(NT)` (rapide pour assertions ciblées)
- comparaison golden-master via `CstDumper` (Task 9) une fois ce dernier prêt

Commit après chaque sous-tâche : `test(parser): <construct>`.

---

## Task 9 : `CstDumper`

**Files :**
- Create: `parser/ll1/tabledriven/cst/CstDumper.java`
- Create: `tests/parser/ll1/tabledriven/cst/CstDumperTest.java`

- [ ] **Step 1 : Test rouge**

```java
package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstDumper;
import static org.junit.Assert.*;

public class CstDumperTest {
    @Test public void dump_d_un_module_simple() {
        var cst = CstParser.parse("module foo (a) i = .0 end module");
        String dump = CstDumper.dump(cst);
        // On ne fixe pas le format exact ici (golden master), on vérifie qu'il contient
        // les éléments structurants
        assertTrue(dump.contains("Module"));
        assertTrue(dump.contains("ModuleKW"));
        assertTrue(dump.contains("Identifiant(\"foo\")"));
        assertTrue(dump.contains("@"));  // les positions
    }
    @Test public void dump_indente_par_niveau() {
        var cst = CstParser.parse("module foo (a) i = .0 end module");
        String dump = CstDumper.dump(cst);
        // au moins une ligne avec "│" ou indentation 4 espaces
        assertTrue(dump.contains("├─") || dump.contains("    "));
    }
    @Test public void dump_epsilon_marque() {
        var cst = CstParser.parse("module foo () end module");  // sans param ni instance
        String dump = CstDumper.dump(cst);
        // ε ou (vide) visible
        assertTrue(dump.contains("ε") || dump.contains("(vide)"));
    }
}
```

- [ ] **Step 2 : Implémenter `CstDumper`**

```java
package parser.ll1.tabledriven.cst;

public final class CstDumper {
    private CstDumper() {}
    public static String dump(CstNode root) {
        var sb = new StringBuilder();
        dumpNode(root, "", true, sb);
        return sb.toString();
    }
    private static void dumpNode(CstNode n, String prefix, boolean isLast, StringBuilder sb) {
        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");
        if (n instanceof CstLeaf leaf) {
            sb.append(leaf.t())
              .append("(\"").append(leaf.token().value() == null ? "" : leaf.token().value()).append("\")")
              .append(" @").append(leaf.startOffset()).append("..").append(leaf.endOffset())
              .append("\n");
        } else if (n instanceof CstInternal in) {
            sb.append(in.nt())
              .append(" @").append(in.startOffset()).append("..").append(in.endOffset())
              .append(" [").append(in.rule().toString()).append("]");
            if (in.children().isEmpty()) sb.append("  (ε)");
            sb.append("\n");
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            for (int i = 0; i < in.children().size(); i++) {
                dumpNode(in.children().get(i), childPrefix,
                    i == in.children().size() - 1, sb);
            }
        }
    }
}
```

- [ ] **Step 3 : Vert, commit**
```bash
git add parser/ll1/tabledriven/cst/CstDumper.java tests/parser/ll1/tabledriven/cst/CstDumperTest.java
git commit -m "feat(cst): CstDumper indente avec positions et productions"
```

---

## Task 10 : Tests d'intégration end-to-end

**Files :**
- Create: `tests/parser/ll1/tabledriven/integration/EndToEndTest.java`
- Create: `tests/parser/ll1/tabledriven/integration/fixtures/` (fichiers .shdl + .cst.txt golden)

- [ ] **Step 1 : Préparer 3-4 modules SHDL de complexités variées**

Exemples (à figer dans des fichiers ou strings) :
- `mod_simple.shdl` : `module foo () end module`
- `mod_assignment.shdl` : `module et (a, b: c) c = a * b end module`
- `mod_memory.shdl` : `module flipflop (d, clk: q) q ::= d on clk , set when reset enabled when /enable ; end module`
- `mod_complet.shdl` : module avec params, instances multiples, mélange assignments + memory + module calls

- [ ] **Step 2 : Pour chaque module, parse + dump → fichier `.cst.txt` golden**

```java
@Test public void mod_simple() {
    String src = readFixture("mod_simple.shdl");
    String expected = readFixture("mod_simple.cst.txt");
    CstNode actual = CstParser.parse(src);
    assertEquals(expected.strip(), CstDumper.dump(actual).strip());
}
```

Première exécution : générer le golden manuellement à partir d'une exécution validée à la main, écrire le `.cst.txt`, puis test passe.

- [ ] **Step 3 : Cas d'erreur**

```java
@Test public void erreur_offset_correct() {
    try {
        CstParser.parse("module foo () bar end module");  // 'bar' ne peut pas commencer une instance
        fail();
    } catch (ParsingException e) {
        // l'offset doit pointer vers le début de 'bar'
        assertEquals(14, e.offset());
    }
}
```

- [ ] **Step 4 : Vert, commit**
```bash
git add tests/parser/ll1/tabledriven/integration/
git commit -m "test(integration): end-to-end parsing 4 modules SHDL"
```

---

## Task 11 : Vérifications finales et hygiène

**Files :**
- Possibly create: `parser/ll1/tabledriven/README.md`

- [ ] **Step 1 : Vérifier zéro dépendance vers le RD legacy**
```bash
grep -rn "import parser\.ll1\.parser\." parser/ll1/tabledriven/ tests/parser/ll1/tabledriven/
grep -rn "parser\.ll1\.parser\." parser/ll1/tabledriven/ tests/parser/ll1/tabledriven/
```
Attendu : aucun résultat.

- [ ] **Step 2 : Vérifier que tous les tests passent**
```bash
javac -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d out \
  $(find parser/ll1/grammar parser/ll1/token parser/ll1/tabledriven tests/parser/ll1/tabledriven \
    util parser/automate parser/regex -name '*.java' -not -name '*.legacy')
# Lancer chaque suite via JUnit Suite ou un par un
java -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore tests.parser.ll1.tabledriven.SanityTest
# ... pour chaque test class
```

Compter les tests : doit être ≥ ~80-100 tests verts.

- [ ] **Step 3 : README minimaliste**

Créer `parser/ll1/tabledriven/README.md` :
```markdown
# Parser SHDL table-driven LL(1)

API publique : `parser.ll1.tabledriven.CstParser.parse(String) -> CstNode`.

Pipeline : `String → ShdlLexer → List<Token> → CstParser (drivé par table) → CstNode`.

Voir `docs/specs/2026-04-30-ll1-table-driven-cst-design.md` pour les décisions de design.

## Lancement des tests
```
(commande javac + JUnit)
```
```

- [ ] **Step 4 : Récap des fichiers créés**
```bash
git diff --stat origin/feature/parser-ll1-table-driven docs/
git log --oneline feature/parser-ll1..HEAD
```

- [ ] **Step 5 : Commit final**
```bash
git add parser/ll1/tabledriven/README.md
git commit -m "docs(parser): README tabledriven + recap"
```

---

## Conditions d'acceptation

- ✅ Toutes les tâches 0-11 cochées
- ✅ Tous les tests verts (estimation : 80-100 tests)
- ✅ Aucun import depuis `parser.ll1.parser.*` (RD legacy)
- ✅ `Ll1ConflictChecker` ne signale aucun conflit sur `Grammar.SHDL`
- ✅ Le pipeline `String → CstNode` fonctionne sur 4 modules SHDL d'intégration
- ✅ Les exceptions `ParsingException` et `LexerException` ont l'offset correct
- ✅ `CstDumper.dump()` produit une trace lisible incluant productions et offsets
- ✅ Pas de push remote sans demande explicite

## Hors scope (rappel)

- Suppression définitive du RD legacy (`.legacy` files) — décision sprint suivant
- AST et transformer CST → AST — autre branche, hors scope
- Migration Gradle, intégration UI — autres axes

---

**Fin de plan.**
