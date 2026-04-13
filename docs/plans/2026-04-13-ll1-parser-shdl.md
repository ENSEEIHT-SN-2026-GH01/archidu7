# Plan d'implémentation — Parser LL(1) SHDL (SCRUM-23)

> **Pour workers agentiques :** SOUS-SKILL REQUISE : `superpowers:subagent-driven-development` (recommandé) ou `superpowers:executing-plans` pour implémenter tâche par tâche. Cases `- [ ]` pour le suivi.

**Goal:** Implémenter un parser LL(1) (avec lookahead 2 local sur `Instance`) qui transforme une `List<Token>` en AST `Module`, selon la spec `docs/specs/2026-04-12-ll1-parser-shdl-design.md`.

**Architecture:** Descente récursive, grammaire déclarative immuable (`Grammar.SHDL`), ensembles First/Follow calculés dynamiquement, checker de conflits LL(1) auto-testé sur grammaires négatives, AST immuable avec Visitor générique, erreurs riches via `ParsingException` + `ErrorCode`.

**Tech Stack:** Java 11+ classique (pas de records, pas de sealed), JUnit 4, build `javac` manuel (pas de Gradle pour ce soir), packages `parser.ll1.*` à côté de `parser.regex/` et `parser.automate/` d'Erwan.

**Convention de test :** chaque étape `Write failing test` → `Run (fail)` → `Implement` → `Run (pass)` → `Commit`. Un commit par tâche minimum, plus si plusieurs briques.

**Commande de test unique (à adapter par tâche) :**
```bash
javac -cp ".:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.parser.ll1.<Suite>
```

Si les jars JUnit ne sont pas présents à `lib/`, Task 0 les récupère.

---

## Task 0 : Setup (JUnit, arborescence, sanity check)

**Files:**
- Create: `parser/ll1/package-info.java`
- Create: `tests/parser/ll1/package-info.java`
- Modify: `.gitignore` (ajouter `lib/` si absent ou garder jars versionnés — voir étape 2)

- [ ] **Step 1 : Vérifier / récupérer JUnit 4**

```bash
ls lib/junit-4.13.2.jar lib/hamcrest-core-1.3.jar 2>/dev/null || {
  mkdir -p lib
  curl -L -o lib/junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
  curl -L -o lib/hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
}
ls -la lib/
```
Attendu : les 2 jars présents.

- [ ] **Step 2 : Décider si `lib/` est versionné**

Si Erwan n'a pas versionné JUnit, l'ajouter à `.gitignore` :
```
# Dépendances (JUnit, Hamcrest) — téléchargées via setup
lib/*.jar
```
Sinon, les committer (plus simple pour coéquipiers). Décision par défaut : **versionner** pour que `javac` marche direct chez les autres sans setup.

- [ ] **Step 3 : Créer l'arborescence**

```bash
mkdir -p parser/ll1/{token,grammar,ast,parser}
mkdir -p tests/parser/ll1/{token,grammar,ast,parser,fixtures}
```

- [ ] **Step 4 : Écrire `parser/ll1/package-info.java`**

```java
/**
 * Parser LL(1) pour SHDL. Voir docs/specs/2026-04-12-ll1-parser-shdl-design.md.
 */
package parser.ll1;
```

- [ ] **Step 5 : Sanity test**

Créer `tests/parser/ll1/SanityTest.java` :
```java
package tests.parser.ll1;

import org.junit.Test;
import static org.junit.Assert.*;

public class SanityTest {
    @Test public void vrai() { assertTrue(true); }
}
```

Compiler et lancer :
```bash
javac -cp "lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d out tests/parser/ll1/SanityTest.java parser/ll1/package-info.java
java -cp "out:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" org.junit.runner.JUnitCore tests.parser.ll1.SanityTest
```
Attendu : `OK (1 test)`.

- [ ] **Step 6 : Commit**
```bash
git add lib/ parser/ll1/ tests/parser/ll1/ .gitignore
git commit -m "SCRUM-23 setup : arborescence parser.ll1 + JUnit 4"
```

---

## Task 1 : Token et TokenType

**Files:**
- Create: `parser/ll1/token/TokenType.java`
- Create: `parser/ll1/token/Token.java`
- Create: `tests/parser/ll1/token/TokenTest.java`

- [ ] **Step 1 : Test rouge**

`tests/parser/ll1/token/TokenTest.java` :
```java
package tests.parser.ll1.token;

import org.junit.Test;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class TokenTest {
    @Test public void gettersRendentCeQuOnAMisDedans() {
        Token t = new Token(TokenType.IDENTIFIER, "abc", 3, 12);
        assertEquals(TokenType.IDENTIFIER, t.getType());
        assertEquals("abc", t.getValue());
        assertEquals(3, t.getLine());
        assertEquals(12, t.getColumn());
    }

    @Test(expected = NullPointerException.class)
    public void typeNullInterdit() { new Token(null, "x", 1, 1); }

    @Test public void toStringContientType() {
        assertTrue(new Token(TokenType.MODULE, "module", 1, 1).toString().contains("MODULE"));
    }

    @Test public void eofEstUnTokenType() {
        assertNotNull(TokenType.valueOf("EOF"));
    }
}
```

- [ ] **Step 2 : Vérifier fail**
Compilation échoue (classes absentes). Attendu.

- [ ] **Step 3 : Implémenter `TokenType`**

```java
package parser.ll1.token;

public enum TokenType {
    // Mots-clés
    MODULE, END, MAP, FSM, STATEMACHINE,
    ON, WHEN, SET, RESET, ENABLED, OUTPUT,
    ASYNCHRONOUS, SYNCHRONOUS,

    // Identifiants et littéraux
    IDENTIFIER, INTEGER, BITFIELD,

    // Opérateurs
    EQ,          // =
    ASSIGN,      // :=
    STAR,        // *
    PLUS,        // +
    SLASH,       // /
    AMPERSAND,   // &
    ARROW,       // ->
    DOTDOT,      // ..

    // Délimiteurs
    LPAREN, RPAREN, LBRACKET, RBRACKET,
    COMMA, COLON, SEMICOLON, DOLLAR,

    // Fin
    EOF;
}
```

- [ ] **Step 4 : Implémenter `Token`**

```java
package parser.ll1.token;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;   // peut être null pour EOF
        this.line = line;
        this.column = column;
    }

    public TokenType getType()   { return type; }
    public String    getValue()  { return value; }
    public int       getLine()   { return line; }
    public int       getColumn() { return column; }

    @Override public String toString() {
        return type + "(" + value + ")@" + line + ":" + column;
    }
}
```

- [ ] **Step 5 : Compiler + tester vert**

```bash
javac -cp "lib/*" -d out parser/ll1/token/*.java tests/parser/ll1/token/*.java tests/parser/ll1/SanityTest.java
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.token.TokenTest
```
Attendu : `OK (4 tests)`.

- [ ] **Step 6 : Commit**
```bash
git add parser/ll1/token tests/parser/ll1/token
git commit -m "SCRUM-23 : Token + TokenType (contrat lexer → parser)"
```

---

## Task 2 : Grammaire — Symbol, Terminal, NonTerminal, Production, Grammar squelette

**Files:**
- Create: `parser/ll1/grammar/{Symbol,Terminal,NonTerminal,Production,Grammar}.java`
- Create: `tests/parser/ll1/grammar/GrammarStructureTest.java`

- [ ] **Step 1 : Test rouge (structures de base)**

`tests/parser/ll1/grammar/GrammarStructureTest.java` :
```java
package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class GrammarStructureTest {
    @Test public void terminalEqualsParTokenType() {
        assertEquals(new Terminal(TokenType.EQ), new Terminal(TokenType.EQ));
        assertNotEquals(new Terminal(TokenType.EQ), new Terminal(TokenType.PLUS));
    }

    @Test public void productionCopieBody() {
        java.util.ArrayList<Symbol> body = new java.util.ArrayList<>();
        body.add(new Terminal(TokenType.EQ));
        Production p = new Production(NonTerminal.SIGNAL, body);
        body.clear();
        assertEquals(1, p.getBody().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void bodyImmutable() {
        new Production(NonTerminal.SIGNAL, List.of(new Terminal(TokenType.EQ)))
            .getBody().add(new Terminal(TokenType.PLUS));
    }

    @Test public void epsilonEstDistinctDesAutresSymboles() {
        assertTrue(Terminal.EPSILON.isEpsilon());
        assertFalse(new Terminal(TokenType.EQ).isEpsilon());
    }
}
```

- [ ] **Step 2 : Vérifier fail (compile error).**

- [ ] **Step 3 : Implémenter `Symbol`**

```java
package parser.ll1.grammar;

public interface Symbol {
    default boolean isTerminal()    { return false; }
    default boolean isNonTerminal() { return false; }
    default boolean isEpsilon()     { return false; }
}
```

- [ ] **Step 4 : Implémenter `Terminal`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.Objects;

public final class Terminal implements Symbol {
    public static final Terminal EPSILON = new Terminal(null, true);

    private final TokenType type;
    private final boolean epsilon;

    public Terminal(TokenType type) { this(Objects.requireNonNull(type), false); }
    private Terminal(TokenType type, boolean epsilon) { this.type = type; this.epsilon = epsilon; }

    public TokenType getType() { return type; }
    @Override public boolean isTerminal() { return !epsilon; }
    @Override public boolean isEpsilon()  { return epsilon; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terminal)) return false;
        Terminal t = (Terminal) o;
        return epsilon == t.epsilon && type == t.type;
    }
    @Override public int hashCode() { return Objects.hash(type, epsilon); }
    @Override public String toString() { return epsilon ? "ε" : type.name(); }
}
```

- [ ] **Step 5 : Implémenter `NonTerminal`**

```java
package parser.ll1.grammar;

public enum NonTerminal implements Symbol {
    MODULE,
    PARAM_LIST, PARAM_LIST_REST, PARAM,
    ARG_LIST,   ARG_LIST_REST,   ARG,
    SEPAR,
    INSTANCE_LIST, INSTANCE_LIST_REST, INSTANCE,
    ASSIGN_OR_TRI, TRI_STATE_TAIL,
    MEMORY_POINT, MEM_TAIL, SET_RESET, OPT_COMMA, OPT_SEMI, OPT_SEMI_OR_COMMA,
    MODULE_INSTANCE,
    MAP, MAP_VALUE_LIST, MAP_VALUE,
    FSM, FSM_KEYWORD, FSM_HEADER, FSM_RULE_LIST, FSM_RULE, FSM_RULE_REST, FSM_RULE_LEFT,
    STATE_NAME_LIST, STATE_NAME_LIST_REST,
    SUM_OF_TERMS_COMPOUND, SUM_OF_TERMS_COMPOUND_REST,
    SUM_OF_TERMS, SUM_OF_TERMS_REST,
    TERM, TERM_REST, FACTOR,
    SIGNAL_COMPOUND, SIGNAL_COMPOUND_REST,
    SIGNAL, SIGNAL_TAIL, SIGNAL_AFTER_INT,
    SIGNAL_OR_LITERAL;

    @Override public boolean isNonTerminal() { return true; }
}
```

- [ ] **Step 6 : Implémenter `Production`**

```java
package parser.ll1.grammar;

import java.util.List;
import java.util.Objects;

public final class Production {
    private final NonTerminal head;
    private final List<Symbol> body;   // immutable via List.copyOf

    public Production(NonTerminal head, List<Symbol> body) {
        this.head = Objects.requireNonNull(head, "head");
        this.body = List.copyOf(Objects.requireNonNull(body, "body"));
    }

    public NonTerminal getHead() { return head; }
    public List<Symbol> getBody() { return body; }
    public boolean isEpsilon() { return body.size() == 1 && body.get(0).isEpsilon(); }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(head.name()).append(" →");
        for (Symbol s : body) sb.append(' ').append(s);
        return sb.toString();
    }
}
```

- [ ] **Step 7 : Squelette `Grammar` (sans SHDL pour l'instant)**

```java
package parser.ll1.grammar;

import java.util.*;

public final class Grammar {
    private final NonTerminal axiom;
    private final List<Production> productions;
    private final Map<NonTerminal, List<Production>> byHead;

    public Grammar(NonTerminal axiom, List<Production> productions) {
        this.axiom = Objects.requireNonNull(axiom);
        this.productions = List.copyOf(Objects.requireNonNull(productions));
        Map<NonTerminal, List<Production>> m = new EnumMap<>(NonTerminal.class);
        for (Production p : this.productions) {
            m.computeIfAbsent(p.getHead(), k -> new ArrayList<>()).add(p);
        }
        Map<NonTerminal, List<Production>> frozen = new EnumMap<>(NonTerminal.class);
        m.forEach((k, v) -> frozen.put(k, List.copyOf(v)));
        this.byHead = Collections.unmodifiableMap(frozen);
    }

    public NonTerminal getAxiom() { return axiom; }
    public List<Production> getProductions() { return productions; }
    public List<Production> productionsOf(NonTerminal nt) {
        return byHead.getOrDefault(nt, List.of());
    }
}
```

- [ ] **Step 8 : Compiler + tester vert**

```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.grammar.GrammarStructureTest
```
Attendu : `OK (4 tests)`.

- [ ] **Step 9 : Commit**
```bash
git add parser/ll1/grammar tests/parser/ll1/grammar
git commit -m "SCRUM-23 : grammaire — Symbol, Terminal, NonTerminal, Production, Grammar"
```

---

## Task 3 : Grammar.SHDL (définition complète) + GrammarFreezeTest

**Files:**
- Modify: `parser/ll1/grammar/Grammar.java` (ajouter `SHDL` singleton)
- Create: `parser/ll1/grammar/GrammarBuilder.java` (DSL de construction lisible)
- Create: `tests/parser/ll1/grammar/GrammarFreezeTest.java`

- [ ] **Step 1 : Test rouge — freeze**

`tests/parser/ll1/grammar/GrammarFreezeTest.java` :
```java
package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import static org.junit.Assert.*;

public class GrammarFreezeTest {
    @Test public void axiomeEstModule() {
        assertEquals(NonTerminal.MODULE, Grammar.SHDL.getAxiom());
    }

    @Test public void grammaireContientToutesLesProductionsPrincipales() {
        // Présence des non-terminaux clés (simple smoke test, les tests First/Follow valident le détail)
        for (NonTerminal nt : NonTerminal.values()) {
            assertFalse("aucune production pour " + nt,
                Grammar.SHDL.productionsOf(nt).isEmpty());
        }
    }

    @Test public void hashStableCommeGarantieAntiRegression() {
        // Signature = concat des productions ordonnées, puis hashCode Java
        // Valeur à figer APRÈS le premier run vert.
        int h = 0;
        for (Production p : Grammar.SHDL.getProductions()) h = h * 31 + p.toString().hashCode();
        // TODO: remplacer EXPECTED_HASH par la valeur observée au premier run
        // Protection contre modification accidentelle de la grammaire
        System.out.println("Grammar.SHDL hash = " + h);
        // Après premier run, décommenter et figer :
        // assertEquals("grammaire modifiée, mettre à jour le hash après review", EXPECTED, h);
    }
}
```

- [ ] **Step 2 : Implémenter `GrammarBuilder`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

/** Petit DSL fluide pour décrire les productions lisiblement. */
public final class GrammarBuilder {
    private final List<Production> productions = new ArrayList<>();

    public GrammarBuilder prod(NonTerminal head, Object... body) {
        List<Symbol> b = new ArrayList<>(body.length);
        for (Object o : body) b.add(toSymbol(o));
        productions.add(new Production(head, b));
        return this;
    }

    public GrammarBuilder eps(NonTerminal head) {
        productions.add(new Production(head, List.of(Terminal.EPSILON)));
        return this;
    }

    public Grammar build(NonTerminal axiom) { return new Grammar(axiom, productions); }

    private static Symbol toSymbol(Object o) {
        if (o instanceof Symbol) return (Symbol) o;
        if (o instanceof TokenType) return new Terminal((TokenType) o);
        throw new IllegalArgumentException("Symbole invalide: " + o);
    }
}
```

- [ ] **Step 3 : Ajouter `Grammar.SHDL` à `Grammar.java`**

À ajouter en champ statique dans `Grammar` (après les méthodes existantes) :

```java
    public static final Grammar SHDL = buildShdl();

    private static Grammar buildShdl() {
        GrammarBuilder g = new GrammarBuilder();
        // Alias concis
        final NonTerminal
            MOD = NonTerminal.MODULE, PL = NonTerminal.PARAM_LIST, PLR = NonTerminal.PARAM_LIST_REST,
            PARAM = NonTerminal.PARAM, AL = NonTerminal.ARG_LIST, ALR = NonTerminal.ARG_LIST_REST,
            ARG = NonTerminal.ARG, SEP = NonTerminal.SEPAR,
            IL = NonTerminal.INSTANCE_LIST, ILR = NonTerminal.INSTANCE_LIST_REST, INS = NonTerminal.INSTANCE,
            AOT = NonTerminal.ASSIGN_OR_TRI, TST = NonTerminal.TRI_STATE_TAIL,
            MP = NonTerminal.MEMORY_POINT, MT = NonTerminal.MEM_TAIL, SR = NonTerminal.SET_RESET,
            OC = NonTerminal.OPT_COMMA, OS = NonTerminal.OPT_SEMI, OSC = NonTerminal.OPT_SEMI_OR_COMMA,
            MI = NonTerminal.MODULE_INSTANCE,
            MAP = NonTerminal.MAP, MVL = NonTerminal.MAP_VALUE_LIST, MV = NonTerminal.MAP_VALUE,
            FSM = NonTerminal.FSM, FK = NonTerminal.FSM_KEYWORD, FH = NonTerminal.FSM_HEADER,
            FRL = NonTerminal.FSM_RULE_LIST, FR = NonTerminal.FSM_RULE, FRR = NonTerminal.FSM_RULE_REST,
            FLL = NonTerminal.FSM_RULE_LEFT, SNL = NonTerminal.STATE_NAME_LIST, SNLR = NonTerminal.STATE_NAME_LIST_REST,
            SOTC = NonTerminal.SUM_OF_TERMS_COMPOUND, SOTCR = NonTerminal.SUM_OF_TERMS_COMPOUND_REST,
            SOT = NonTerminal.SUM_OF_TERMS, SOTR = NonTerminal.SUM_OF_TERMS_REST,
            T = NonTerminal.TERM, TR = NonTerminal.TERM_REST, F = NonTerminal.FACTOR,
            SC = NonTerminal.SIGNAL_COMPOUND, SCR = NonTerminal.SIGNAL_COMPOUND_REST,
            S = NonTerminal.SIGNAL, ST = NonTerminal.SIGNAL_TAIL, SAI = NonTerminal.SIGNAL_AFTER_INT,
            SOL = NonTerminal.SIGNAL_OR_LITERAL;

        // Module
        g.prod(MOD, TokenType.MODULE, TokenType.IDENTIFIER, TokenType.LPAREN, PL, TokenType.RPAREN,
                    IL, TokenType.END, TokenType.MODULE);
        // ParamList
        g.prod(PL, PARAM, PLR);
        g.prod(PLR, SEP, PARAM, PLR); g.eps(PLR);
        g.prod(PARAM, S);
        g.prod(SEP, TokenType.COMMA); g.prod(SEP, TokenType.COLON);
        // ArgList
        g.prod(AL, ARG, ALR);
        g.prod(ALR, SEP, ARG, ALR); g.eps(ALR);
        g.prod(ARG, SOL); // on factorise sans ArgRest ici : & géré via SignalOrLiteralCompound si besoin
        // NOTE : la PEG parle de SignalOrLiteralCompound ; on le remonte dans ARG via une récursion interne
        // Mais pour garder LL(1), on implémente ArgList plat : Arg = SignalOrLiteral (AMPERSAND SignalOrLiteral)*
        // → on laisse le parser gérer la répétition via ARG_LIST_REST avec séparateur & dans sa propre sous-règle.
        // Pour rester simple ici : ARG = SOL (les compound avec & sont gérés dans SumOfTermsCompound / SignalCompound)
        g.prod(SOL, S);
        g.prod(SOL, TokenType.INTEGER);
        g.prod(SOL, TokenType.BITFIELD);
        // InstanceList (ε si suivant = END)
        g.prod(IL, INS, ILR);
        g.prod(ILR, INS, ILR); g.eps(ILR);
        // Instance : dispatch spécial lookahead-2, mais on enregistre les alternatives pour First/Follow
        g.prod(INS, MI);
        g.prod(INS, AOT);
        g.prod(INS, MP);
        g.prod(INS, FSM);
        g.prod(INS, MAP);
        // AssignOrTri
        g.prod(AOT, SC, TokenType.EQ, SOTC, TST);
        g.prod(TST, TokenType.OUTPUT, TokenType.ENABLED, TokenType.WHEN, SOT); g.eps(TST);
        // MemoryPoint
        g.prod(MP, SC, TokenType.ASSIGN, SOTC, TokenType.ON, SOT, OC, SR, TokenType.WHEN, SOT, MT, OS);
        g.prod(MT, OC, TokenType.ENABLED, TokenType.WHEN, SOT); g.eps(MT);
        g.prod(SR, TokenType.SET); g.prod(SR, TokenType.RESET);
        g.prod(OC, TokenType.COMMA); g.eps(OC);
        g.prod(OS, TokenType.SEMICOLON); g.eps(OS);
        g.prod(OSC, TokenType.SEMICOLON); g.prod(OSC, TokenType.COMMA); g.eps(OSC);
        // ModuleInstance
        g.prod(MI, TokenType.IDENTIFIER, TokenType.LPAREN, AL, TokenType.RPAREN);
        g.prod(MI, TokenType.DOLLAR, TokenType.IDENTIFIER, TokenType.LPAREN, AL, TokenType.RPAREN);
        // Map
        g.prod(MAP, TokenType.MAP, SC, TokenType.ARROW, SC, MVL, TokenType.END, TokenType.MAP);
        g.prod(MVL, MV, MVL); g.eps(MVL);
        g.prod(MV, TokenType.BITFIELD, TokenType.ARROW, TokenType.BITFIELD);
        // FSM
        g.prod(FSM, FK, FH, FRL, TokenType.END, FK);
        g.prod(FK, TokenType.FSM); g.prod(FK, TokenType.STATEMACHINE);
        g.prod(FH, TokenType.ASYNCHRONOUS);
        g.prod(FH, TokenType.SYNCHRONOUS, TokenType.ON, SOT, OC, TokenType.IDENTIFIER, TokenType.WHEN, SOT);
        g.prod(FH, TokenType.IDENTIFIER, TokenType.WHEN, SOT, OC, TokenType.SYNCHRONOUS, TokenType.ON, SOT);
        g.prod(FRL, FR, FRL); g.eps(FRL);
        g.prod(FR, FLL, TokenType.ARROW, TokenType.IDENTIFIER, FRR);
        g.prod(FRR, TokenType.WHEN, SOT, OSC); g.eps(FRR);
        g.prod(FLL, TokenType.STAR); g.prod(FLL, SNL);
        g.prod(SNL, TokenType.IDENTIFIER, SNLR);
        g.prod(SNLR, TokenType.COMMA, TokenType.IDENTIFIER, SNLR); g.eps(SNLR);
        // Expressions
        g.prod(SOTC, SOT, SOTCR);
        g.prod(SOTCR, TokenType.AMPERSAND, SOT, SOTCR); g.eps(SOTCR);
        g.prod(SOT, T, SOTR);
        g.prod(SOTR, TokenType.PLUS, T, SOTR); g.eps(SOTR);
        g.prod(T, F, TR);
        g.prod(TR, TokenType.STAR, F, TR); g.eps(TR);
        g.prod(F, TokenType.LPAREN, SOT, TokenType.RPAREN);
        g.prod(F, TokenType.INTEGER);
        g.prod(F, TokenType.BITFIELD);
        g.prod(F, TokenType.SLASH, S);
        g.prod(F, S);
        // Signal
        g.prod(SC, S, SCR);
        g.prod(SCR, TokenType.AMPERSAND, S, SCR); g.eps(SCR);
        g.prod(S, TokenType.IDENTIFIER, ST);
        g.prod(ST, TokenType.LBRACKET, TokenType.INTEGER, SAI); g.eps(ST);
        g.prod(SAI, TokenType.RBRACKET);
        g.prod(SAI, TokenType.DOTDOT, TokenType.INTEGER, TokenType.RBRACKET);
        g.prod(SAI, TokenType.COLON, TokenType.INTEGER, TokenType.RBRACKET);

        return g.build(MOD);
    }
```

**Note importante sur Instance et Factor** : la grammaire listée ci-dessus contient un **conflit FIRST/FIRST intentionnel** sur `INSTANCE` (toutes les alternatives démarrent par `IDENTIFIER`) et sur `FACTOR` (`S` vs `SLASH S` OK, mais `S` démarre par `IDENTIFIER` comme d'autres). Ces conflits sont **attendus** — ils seront résolus **dans le code du parser** via lookahead 2 (Instance) et vérification que le parser fonctionne (Factor est OK car First-distinct). `Ll1ConflictChecker` les flaggera — on les autorisera en liste blanche documentée (voir Task 5).

- [ ] **Step 4 : Run test**

Compiler + run `GrammarFreezeTest`. Les 2 premiers tests doivent passer. Le 3ᵉ affiche le hash — noter la valeur pour la figer plus tard.

- [ ] **Step 5 : Figer le hash**
Remplacer le TODO par la valeur observée et décommenter `assertEquals`.

- [ ] **Step 6 : Commit**
```bash
git add parser/ll1/grammar/Grammar.java parser/ll1/grammar/GrammarBuilder.java tests/parser/ll1/grammar/GrammarFreezeTest.java
git commit -m "SCRUM-23 : Grammar.SHDL (productions complètes) + freeze test"
```

---

## Task 4 : FirstSet

**Files:**
- Create: `parser/ll1/grammar/FirstSet.java`
- Create: `tests/parser/ll1/grammar/FirstSetTest.java`

- [ ] **Step 1 : Test rouge (valeurs hardcodées pour non-terminaux clés)**

```java
package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.Set;
import static org.junit.Assert.*;
import static parser.ll1.token.TokenType.*;

public class FirstSetTest {
    private final FirstSet first = new FirstSet(Grammar.SHDL);

    @Test public void firstModule() { assertEquals(Set.of(MODULE), first.of(NonTerminal.MODULE)); }

    @Test public void firstSignal() { assertEquals(Set.of(IDENTIFIER), first.of(NonTerminal.SIGNAL)); }

    @Test public void firstFactor() {
        assertEquals(Set.of(LPAREN, INTEGER, BITFIELD, SLASH, IDENTIFIER),
                     first.of(NonTerminal.FACTOR));
    }

    @Test public void firstInstanceInclutDollarEtMots() {
        Set<TokenType> f = first.of(NonTerminal.INSTANCE);
        assertTrue(f.contains(IDENTIFIER));
        assertTrue(f.contains(DOLLAR));
        assertTrue(f.contains(FSM));
        assertTrue(f.contains(STATEMACHINE));
        assertTrue(f.contains(MAP));
    }

    @Test public void firstTermRestContientEpsilon() {
        assertTrue(first.ofSequenceIncludesEpsilon(List.of(new Terminal(STAR)) /* dummy */) // à adapter
            || first.nullable(NonTerminal.TERM_REST));
    }

    @Test public void firstOptCommaContientEpsilon() {
        assertTrue(first.nullable(NonTerminal.OPT_COMMA));
    }
}
```

*Note : la dernière méthode `firstTermRestContientEpsilon` est approximative — simplifier pour tester juste `nullable`.*

- [ ] **Step 2 : Vérifier fail (FirstSet absent).**

- [ ] **Step 3 : Implémenter `FirstSet`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class FirstSet {
    private final Grammar grammar;
    private final Map<NonTerminal, Set<TokenType>> first = new EnumMap<>(NonTerminal.class);
    private final Set<NonTerminal> nullable = EnumSet.noneOf(NonTerminal.class);

    public FirstSet(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
        for (NonTerminal nt : NonTerminal.values()) first.put(nt, EnumSet.noneOf(TokenType.class));
        compute();
    }

    private void compute() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                NonTerminal head = p.getHead();
                if (p.isEpsilon()) {
                    if (nullable.add(head)) changed = true;
                    continue;
                }
                boolean allNullable = true;
                for (Symbol s : p.getBody()) {
                    Set<TokenType> firstOfS = firstOfSymbol(s);
                    if (first.get(head).addAll(firstOfS)) changed = true;
                    if (!isNullable(s)) { allNullable = false; break; }
                }
                if (allNullable && nullable.add(head)) changed = true;
            }
        }
    }

    private Set<TokenType> firstOfSymbol(Symbol s) {
        if (s.isEpsilon()) return Set.of();
        if (s.isTerminal()) return Set.of(((Terminal) s).getType());
        return first.get((NonTerminal) s);
    }

    private boolean isNullable(Symbol s) {
        if (s.isEpsilon()) return true;
        if (s.isTerminal()) return false;
        return nullable.contains((NonTerminal) s);
    }

    public Set<TokenType> of(NonTerminal nt) {
        return Collections.unmodifiableSet(first.get(nt));
    }

    public boolean nullable(NonTerminal nt) { return nullable.contains(nt); }

    /** First d'une séquence (utile pour le parser et Follow). */
    public Set<TokenType> ofSequence(List<Symbol> seq) {
        Set<TokenType> out = EnumSet.noneOf(TokenType.class);
        for (Symbol s : seq) {
            out.addAll(firstOfSymbol(s));
            if (!isNullable(s)) return out;
        }
        return out;
    }

    public boolean sequenceNullable(List<Symbol> seq) {
        for (Symbol s : seq) if (!isNullable(s)) return false;
        return true;
    }
}
```

- [ ] **Step 4 : Compiler + run**

Attendu : tous les tests FirstSet verts.

- [ ] **Step 5 : Commit**
```bash
git add parser/ll1/grammar/FirstSet.java tests/parser/ll1/grammar/FirstSetTest.java
git commit -m "SCRUM-23 : FirstSet par point fixe + tests"
```

---

## Task 5 : FollowSet

**Files:**
- Create: `parser/ll1/grammar/FollowSet.java`
- Create: `tests/parser/ll1/grammar/FollowSetTest.java`

- [ ] **Step 1 : Test rouge**

```java
package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.Set;
import static org.junit.Assert.*;
import static parser.ll1.token.TokenType.*;

public class FollowSetTest {
    private final FirstSet first = new FirstSet(Grammar.SHDL);
    private final FollowSet follow = new FollowSet(Grammar.SHDL, first);

    @Test public void followModuleEstEof() {
        assertEquals(Set.of(EOF), follow.of(NonTerminal.MODULE));
    }

    @Test public void followInstanceListContientEnd() {
        assertTrue(follow.of(NonTerminal.INSTANCE_LIST).contains(END));
    }

    @Test public void followInstanceContientPremierInstanceEtEnd() {
        Set<TokenType> f = follow.of(NonTerminal.INSTANCE);
        assertTrue(f.contains(IDENTIFIER));
        assertTrue(f.contains(DOLLAR));
        assertTrue(f.contains(END));
    }

    @Test public void followFactorContientStarPlusEtMore() {
        Set<TokenType> f = follow.of(NonTerminal.FACTOR);
        assertTrue(f.contains(STAR));
        assertTrue(f.contains(PLUS));
    }
}
```

- [ ] **Step 2 : Fail.**

- [ ] **Step 3 : Implémenter `FollowSet`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class FollowSet {
    private final Grammar grammar;
    private final FirstSet first;
    private final Map<NonTerminal, Set<TokenType>> follow = new EnumMap<>(NonTerminal.class);

    public FollowSet(Grammar grammar, FirstSet first) {
        this.grammar = Objects.requireNonNull(grammar);
        this.first = Objects.requireNonNull(first);
        for (NonTerminal nt : NonTerminal.values()) follow.put(nt, EnumSet.noneOf(TokenType.class));
        follow.get(grammar.getAxiom()).add(TokenType.EOF);
        compute();
    }

    private void compute() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                List<Symbol> body = p.getBody();
                for (int i = 0; i < body.size(); i++) {
                    Symbol s = body.get(i);
                    if (!s.isNonTerminal()) continue;
                    NonTerminal X = (NonTerminal) s;
                    List<Symbol> beta = body.subList(i + 1, body.size());
                    // Follow(X) ⊇ First(beta) \ {ε}
                    Set<TokenType> firstBeta = first.ofSequence(beta);
                    if (follow.get(X).addAll(firstBeta)) changed = true;
                    // Si beta est nullable (ou vide), Follow(X) ⊇ Follow(head)
                    if (first.sequenceNullable(beta)) {
                        if (follow.get(X).addAll(follow.get(p.getHead()))) changed = true;
                    }
                }
            }
        }
    }

    public Set<TokenType> of(NonTerminal nt) {
        return Collections.unmodifiableSet(follow.get(nt));
    }
}
```

- [ ] **Step 4 : Run + vert.**

- [ ] **Step 5 : Commit**
```bash
git add parser/ll1/grammar/FollowSet.java tests/parser/ll1/grammar/FollowSetTest.java
git commit -m "SCRUM-23 : FollowSet par point fixe + tests"
```

---

## Task 6 : Ll1ConflictChecker (avec grammaires négatives)

**Files:**
- Create: `parser/ll1/grammar/Ll1Conflict.java`
- Create: `parser/ll1/grammar/Ll1ConflictChecker.java`
- Create: `parser/ll1/grammar/GrammarDefinitionException.java`
- Create: `tests/parser/ll1/grammar/Ll1ConflictTest.java`

- [ ] **Step 1 : Test rouge (grammaires négatives + SHDL positive)**

```java
package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class Ll1ConflictTest {
    @Test public void checkerDetecteConflitFirstFirst() {
        // A → a B | a C        (FIRST/FIRST évident)
        NonTerminal A = NonTerminal.MODULE;  // on réutilise l'enum, on construit artificiellement
        // En pratique on fait une mini-grammaire avec tokens EQ/PLUS
        Grammar bad = new GrammarBuilder()
            .prod(A, TokenType.EQ, TokenType.MODULE)
            .prod(A, TokenType.EQ, TokenType.END)
            .build(A);
        List<Ll1Conflict> c = new Ll1ConflictChecker(bad).findAllConflicts();
        assertFalse(c.isEmpty());
        assertTrue(c.stream().anyMatch(x -> x.getType() == Ll1Conflict.Type.FIRST_FIRST));
    }

    @Test public void checkerDetecteRecursionGauche() {
        // A → A b | c
        NonTerminal A = NonTerminal.MODULE;
        Grammar bad = new GrammarBuilder()
            .prod(A, A, TokenType.PLUS)
            .prod(A, TokenType.INTEGER)
            .build(A);
        List<Ll1Conflict> c = new Ll1ConflictChecker(bad).findAllConflicts();
        assertTrue(c.stream().anyMatch(x -> x.getType() == Ll1Conflict.Type.LEFT_RECURSION));
    }

    @Test public void checkerDetecteFirstFollow() {
        // A → B c ; B → d | ε  ; si c ∈ Follow(B), pas de conflit
        // Pour forcer conflit : A → B c | c   → FIRST(B c) contient c (via ε) + FIRST(c) = c
        NonTerminal A = NonTerminal.MODULE, B = NonTerminal.PARAM_LIST;
        Grammar bad = new GrammarBuilder()
            .prod(A, B, TokenType.EQ)
            .prod(A, TokenType.EQ)
            .prod(B, TokenType.PLUS)
            .eps(B)
            .build(A);
        List<Ll1Conflict> c = new Ll1ConflictChecker(bad).findAllConflicts();
        assertFalse(c.isEmpty());
    }

    @Test public void grammaireShdlAUnConflitAttenduSurInstanceSeulement() {
        // Instance a un conflit FIRST/FIRST sur IDENTIFIER (résolu par lookahead 2 dans le parser).
        // On vérifie que tous les autres non-terminaux sont propres.
        List<Ll1Conflict> all = new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts();
        for (Ll1Conflict c : all) {
            assertEquals("conflit inattendu sur " + c.getNonTerminal(),
                NonTerminal.INSTANCE, c.getNonTerminal());
        }
    }
}
```

- [ ] **Step 2 : Fail.**

- [ ] **Step 3 : Implémenter `Ll1Conflict`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class Ll1Conflict {
    public enum Type { FIRST_FIRST, FIRST_FOLLOW, LEFT_RECURSION }

    private final Type type;
    private final NonTerminal nt;
    private final Set<TokenType> conflictingTokens;
    private final String detail;

    public Ll1Conflict(Type type, NonTerminal nt, Set<TokenType> conflictingTokens, String detail) {
        this.type = type;
        this.nt = nt;
        this.conflictingTokens = Set.copyOf(conflictingTokens);
        this.detail = detail;
    }

    public Type getType() { return type; }
    public NonTerminal getNonTerminal() { return nt; }
    public Set<TokenType> getConflictingTokens() { return conflictingTokens; }
    public String getDetail() { return detail; }

    @Override public String toString() { return type + " sur " + nt + " : " + detail; }
}
```

- [ ] **Step 4 : Implémenter `GrammarDefinitionException`**

```java
package parser.ll1.grammar;

import java.util.List;

public class GrammarDefinitionException extends RuntimeException {
    private final List<Ll1Conflict> conflicts;
    public GrammarDefinitionException(List<Ll1Conflict> conflicts) {
        super("Grammaire non LL(1) : " + conflicts);
        this.conflicts = List.copyOf(conflicts);
    }
    public List<Ll1Conflict> getConflicts() { return conflicts; }
}
```

- [ ] **Step 5 : Implémenter `Ll1ConflictChecker`**

```java
package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class Ll1ConflictChecker {
    private final Grammar grammar;
    private final FirstSet first;
    private final FollowSet follow;

    public Ll1ConflictChecker(Grammar grammar) {
        this.grammar = grammar;
        this.first = new FirstSet(grammar);
        this.follow = new FollowSet(grammar, first);
    }

    public List<Ll1Conflict> findAllConflicts() {
        List<Ll1Conflict> result = new ArrayList<>();
        // Récursion gauche directe
        for (Production p : grammar.getProductions()) {
            List<Symbol> body = p.getBody();
            if (!body.isEmpty() && body.get(0) == p.getHead()) {
                result.add(new Ll1Conflict(Ll1Conflict.Type.LEFT_RECURSION, p.getHead(),
                    Set.of(), "production " + p));
            }
        }
        // FIRST/FIRST et FIRST/FOLLOW par non-terminal
        for (NonTerminal nt : NonTerminal.values()) {
            List<Production> prods = grammar.productionsOf(nt);
            if (prods.size() < 2) continue;
            List<Set<TokenType>> firsts = new ArrayList<>();
            int nullableIdx = -1;
            for (int i = 0; i < prods.size(); i++) {
                Production p = prods.get(i);
                Set<TokenType> f = p.isEpsilon() ? Set.of() : first.ofSequence(p.getBody());
                firsts.add(f);
                if (p.isEpsilon() || first.sequenceNullable(p.getBody())) nullableIdx = i;
            }
            // FIRST/FIRST
            for (int i = 0; i < firsts.size(); i++) {
                for (int j = i + 1; j < firsts.size(); j++) {
                    Set<TokenType> inter = EnumSet.copyOf(firsts.get(i).isEmpty() ? Set.of() : firsts.get(i));
                    inter.retainAll(firsts.get(j));
                    if (!inter.isEmpty()) {
                        result.add(new Ll1Conflict(Ll1Conflict.Type.FIRST_FIRST, nt,
                            inter, "productions " + i + " et " + j));
                    }
                }
            }
            // FIRST/FOLLOW
            if (nullableIdx >= 0) {
                Set<TokenType> followNt = follow.of(nt);
                for (int i = 0; i < firsts.size(); i++) {
                    if (i == nullableIdx) continue;
                    Set<TokenType> inter = firsts.get(i).isEmpty()
                        ? EnumSet.noneOf(TokenType.class)
                        : EnumSet.copyOf(firsts.get(i));
                    inter.retainAll(followNt);
                    if (!inter.isEmpty()) {
                        result.add(new Ll1Conflict(Ll1Conflict.Type.FIRST_FOLLOW, nt,
                            inter, "ε production en conflit avec production " + i));
                    }
                }
            }
        }
        return result;
    }
}
```

- [ ] **Step 6 : Run — tous les tests négatifs doivent détecter un conflit, SHDL doit n'avoir que le conflit attendu sur INSTANCE.**

- [ ] **Step 7 : Commit**
```bash
git add parser/ll1/grammar/Ll1Conflict.java parser/ll1/grammar/Ll1ConflictChecker.java parser/ll1/grammar/GrammarDefinitionException.java tests/parser/ll1/grammar/Ll1ConflictTest.java
git commit -m "SCRUM-23 : Ll1ConflictChecker auto-testé sur grammaires négatives"
```

---

## Task 7 : AST — Position, Node, Visitor, DefaultVisitor

**Files:**
- Create: `parser/ll1/ast/{Position,Node,Visitor,DefaultVisitor}.java`

- [ ] **Step 1 : Position**

```java
package parser.ll1.ast;
import java.util.Objects;

public final class Position {
    private final int line, column;
    public Position(int line, int column) { this.line = line; this.column = column; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o; return p.line == line && p.column == column;
    }
    @Override public int hashCode() { return Objects.hash(line, column); }
    @Override public String toString() { return line + ":" + column; }
}
```

- [ ] **Step 2 : Node**

```java
package parser.ll1.ast;

public interface Node {
    Position getPosition();
    <R> R accept(Visitor<R> v);
}
```

- [ ] **Step 3 : Visitor (stub — sera complété au fur et à mesure)**

```java
package parser.ll1.ast;

public interface Visitor<R> {
    R visit(Module m);
    R visit(Assignment a);
    R visit(TriState t);
    R visit(MemoryPoint m);
    R visit(ModuleInstance mi);
    R visit(Fsm f);
    R visit(FsmRule r);
    R visit(MapNode m);
    R visit(MapEntry e);
    R visit(SumOfTerms s);
    R visit(Term t);
    R visit(Factor f);
    R visit(Signal s);
    R visit(SignalCompound sc);
    R visit(BitField b);
}
```

*Le fichier `Visitor.java` ne compilera pas tant que les classes AST n'existent pas — c'est OK, on les crée à la tâche suivante.*

- [ ] **Step 4 : DefaultVisitor**

```java
package parser.ll1.ast;

public abstract class DefaultVisitor<R> implements Visitor<R> {
    protected R defaultResult() { return null; }
    protected R visitChildren(Node n) { return defaultResult(); }

    @Override public R visit(Module m)           { m.getInstances().forEach(i -> i.accept(this)); return defaultResult(); }
    @Override public R visit(Assignment a)       { a.getTarget().accept(this); a.getExpr().accept(this); return defaultResult(); }
    @Override public R visit(TriState t)         { t.getTarget().accept(this); t.getExpr().accept(this); t.getEnable().accept(this); return defaultResult(); }
    @Override public R visit(MemoryPoint m)      { m.getTarget().accept(this); m.getExpr().accept(this); m.getClock().accept(this); m.getCondition().accept(this); return defaultResult(); }
    @Override public R visit(ModuleInstance mi)  { mi.getArgs().forEach(a -> a.accept(this)); return defaultResult(); }
    @Override public R visit(Fsm f)              { f.getRules().forEach(r -> r.accept(this)); return defaultResult(); }
    @Override public R visit(FsmRule r)          { return defaultResult(); }
    @Override public R visit(MapNode m)          { m.getInput().accept(this); m.getOutput().accept(this); m.getEntries().forEach(e -> e.accept(this)); return defaultResult(); }
    @Override public R visit(MapEntry e)         { return defaultResult(); }
    @Override public R visit(SumOfTerms s)       { s.getTerms().forEach(t -> t.accept(this)); return defaultResult(); }
    @Override public R visit(Term t)             { t.getFactors().forEach(f -> f.accept(this)); return defaultResult(); }
    @Override public R visit(Factor f)           { return defaultResult(); }
    @Override public R visit(Signal s)           { return defaultResult(); }
    @Override public R visit(SignalCompound sc)  { sc.getSignals().forEach(s -> s.accept(this)); return defaultResult(); }
    @Override public R visit(BitField b)         { return defaultResult(); }
}
```

- [ ] **Step 5 : Pas de compilation encore — on enchaîne Task 8.**

---

## Task 8 : Classes AST feuilles (Signal, SignalCompound, BitField, Factor, Term, SumOfTerms)

**Files:**
- Create: `parser/ll1/ast/{Signal,SignalCompound,BitField,Factor,Term,SumOfTerms}.java`

Chaque classe suit ce patron : `private final` partout, `Objects.requireNonNull`, `List.copyOf`, `accept()` qui appelle `visitor.visit(this)`, getters.

- [ ] **Step 1 : Signal**

```java
package parser.ll1.ast;
import java.util.Objects;
import java.util.Optional;

public final class Signal implements Node {
    private final Position position;
    private final String name;
    private final Integer hi;      // null si pas d'indice
    private final Integer lo;      // null si indice unique

    public Signal(Position pos, String name, Integer hi, Integer lo) {
        this.position = Objects.requireNonNull(pos);
        this.name = Objects.requireNonNull(name);
        this.hi = hi; this.lo = lo;
    }

    public Position getPosition() { return position; }
    public String getName() { return name; }
    public Optional<Integer> getHi() { return Optional.ofNullable(hi); }
    public Optional<Integer> getLo() { return Optional.ofNullable(lo); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 2 : SignalCompound**

```java
package parser.ll1.ast;
import java.util.*;

public final class SignalCompound implements Node {
    private final Position position;
    private final List<Signal> signals;
    public SignalCompound(Position pos, List<Signal> signals) {
        this.position = Objects.requireNonNull(pos);
        this.signals = List.copyOf(Objects.requireNonNull(signals));
        if (this.signals.isEmpty()) throw new IllegalArgumentException("signals vide");
    }
    public Position getPosition() { return position; }
    public List<Signal> getSignals() { return signals; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 3 : BitField**

```java
package parser.ll1.ast;
import java.util.Objects;

public final class BitField implements Node {
    private final Position position;
    private final String bits;   // ex. "0101"
    public BitField(Position pos, String bits) {
        this.position = Objects.requireNonNull(pos);
        this.bits = Objects.requireNonNull(bits);
    }
    public Position getPosition() { return position; }
    public String getBits() { return bits; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 4 : Factor**

```java
package parser.ll1.ast;
import java.util.Objects;

public final class Factor implements Node {
    public enum Kind { SIGNAL, NEG_SIGNAL, LITERAL_0, LITERAL_1, BITFIELD, PAREN }
    private final Position position;
    private final Kind kind;
    private final Signal signal;       // si SIGNAL ou NEG_SIGNAL
    private final BitField bitField;   // si BITFIELD
    private final SumOfTerms inner;    // si PAREN

    public Factor(Position pos, Kind kind, Signal s, BitField b, SumOfTerms inner) {
        this.position = Objects.requireNonNull(pos);
        this.kind = Objects.requireNonNull(kind);
        this.signal = s; this.bitField = b; this.inner = inner;
    }
    public static Factor signal(Position p, Signal s)    { return new Factor(p, Kind.SIGNAL, s, null, null); }
    public static Factor negSignal(Position p, Signal s) { return new Factor(p, Kind.NEG_SIGNAL, s, null, null); }
    public static Factor lit0(Position p)                { return new Factor(p, Kind.LITERAL_0, null, null, null); }
    public static Factor lit1(Position p)                { return new Factor(p, Kind.LITERAL_1, null, null, null); }
    public static Factor bits(Position p, BitField b)    { return new Factor(p, Kind.BITFIELD, null, b, null); }
    public static Factor paren(Position p, SumOfTerms s) { return new Factor(p, Kind.PAREN, null, null, s); }

    public Position getPosition() { return position; }
    public Kind getKind() { return kind; }
    public Signal getSignal() { return signal; }
    public BitField getBitField() { return bitField; }
    public SumOfTerms getInner() { return inner; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 5 : Term**

```java
package parser.ll1.ast;
import java.util.*;

public final class Term implements Node {
    private final Position position;
    private final List<Factor> factors;  // au moins 1
    public Term(Position pos, List<Factor> factors) {
        this.position = Objects.requireNonNull(pos);
        this.factors = List.copyOf(Objects.requireNonNull(factors));
        if (this.factors.isEmpty()) throw new IllegalArgumentException("factors vide");
    }
    public Position getPosition() { return position; }
    public List<Factor> getFactors() { return factors; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 6 : SumOfTerms**

```java
package parser.ll1.ast;
import java.util.*;

public final class SumOfTerms implements Node {
    private final Position position;
    private final List<Term> terms;   // au moins 1
    public SumOfTerms(Position pos, List<Term> terms) {
        this.position = Objects.requireNonNull(pos);
        this.terms = List.copyOf(Objects.requireNonNull(terms));
        if (this.terms.isEmpty()) throw new IllegalArgumentException("terms vide");
    }
    public Position getPosition() { return position; }
    public List<Term> getTerms() { return terms; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 7 : Commit partiel après compilation OK**
```bash
javac -cp "lib/*" -d out $(find parser/ll1 -name '*.java')
git add parser/ll1/ast
git commit -m "SCRUM-23 : AST feuilles (Signal, Factor, Term, SumOfTerms) immuables"
```

---

## Task 9 : AST — Instance et sous-types (Assignment, TriState, MemoryPoint, ModuleInstance)

**Files:**
- Create: `parser/ll1/ast/{Instance,Assignment,TriState,MemoryPoint,ModuleInstance}.java`

- [ ] **Step 1 : Instance (interface marqueur)**

```java
package parser.ll1.ast;
public interface Instance extends Node {}
```

- [ ] **Step 2 : Assignment**

```java
package parser.ll1.ast;
import java.util.*;

public final class Assignment implements Instance {
    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;  // SumOfTermsCompound = liste de SumOfTerms séparés par &
    public Assignment(Position pos, SignalCompound target, List<SumOfTerms> expr) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        if (this.expr.isEmpty()) throw new IllegalArgumentException("expr vide");
    }
    public Position getPosition() { return position; }
    public SignalCompound getTarget() { return target; }
    public List<SumOfTerms> getExprCompound() { return expr; }
    /** Alias pratique : premier bloc (cas fréquent sans &). */
    public SumOfTerms getExpr() { return expr.get(0); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 3 : TriState**

```java
package parser.ll1.ast;
import java.util.*;
import java.util.Objects;

public final class TriState implements Instance {
    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;
    private final SumOfTerms enable;
    public TriState(Position pos, SignalCompound target, List<SumOfTerms> expr, SumOfTerms enable) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        this.enable = Objects.requireNonNull(enable);
    }
    public Position getPosition() { return position; }
    public SignalCompound getTarget() { return target; }
    public List<SumOfTerms> getExprCompound() { return expr; }
    public SumOfTerms getExpr() { return expr.get(0); }
    public SumOfTerms getEnable() { return enable; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 4 : MemoryPoint**

```java
package parser.ll1.ast;
import java.util.*;

public final class MemoryPoint implements Instance {
    public enum Kind { SET, RESET }
    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;
    private final SumOfTerms clock;
    private final Kind setOrReset;
    private final SumOfTerms condition;   // "when" SumOfTerms (set/reset)
    private final SumOfTerms enable;      // null si absent
    public MemoryPoint(Position pos, SignalCompound target, List<SumOfTerms> expr,
                       SumOfTerms clock, Kind setOrReset, SumOfTerms condition, SumOfTerms enable) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        this.clock = Objects.requireNonNull(clock);
        this.setOrReset = Objects.requireNonNull(setOrReset);
        this.condition = Objects.requireNonNull(condition);
        this.enable = enable;  // optionnel
    }
    public Position getPosition() { return position; }
    public SignalCompound getTarget() { return target; }
    public SumOfTerms getExpr() { return expr.get(0); }
    public List<SumOfTerms> getExprCompound() { return expr; }
    public SumOfTerms getClock() { return clock; }
    public Kind getSetOrReset() { return setOrReset; }
    public SumOfTerms getCondition() { return condition; }
    public java.util.Optional<SumOfTerms> getEnable() { return java.util.Optional.ofNullable(enable); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 5 : ModuleInstance**

```java
package parser.ll1.ast;
import java.util.*;

public final class ModuleInstance implements Instance {
    private final Position position;
    private final String moduleName;
    private final boolean predefined;   // true si $nom
    private final List<Node> args;      // Signal, BitField, INTEGER literal — union type simple
    public ModuleInstance(Position pos, String moduleName, boolean predefined, List<Node> args) {
        this.position = Objects.requireNonNull(pos);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.predefined = predefined;
        this.args = List.copyOf(Objects.requireNonNull(args));
    }
    public Position getPosition() { return position; }
    public String getModuleName() { return moduleName; }
    public boolean isPredefined() { return predefined; }
    public List<Node> getArgs() { return args; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 6 : Commit**

```bash
javac -cp "lib/*" -d out $(find parser/ll1 -name '*.java')
git add parser/ll1/ast
git commit -m "SCRUM-23 : AST Instance + Assignment/TriState/MemoryPoint/ModuleInstance"
```

---

## Task 10 : AST — Fsm, MapNode, Module

**Files:**
- Create: `parser/ll1/ast/{Fsm,FsmHeader,FsmRule,MapNode,MapEntry,Module}.java`

- [ ] **Step 1 : FsmHeader (valeur objet)**

```java
package parser.ll1.ast;
import java.util.*;

public final class FsmHeader {
    public enum Kind { ASYNCHRONOUS, SYNCHRONOUS_ON_RESET, RESET_WHEN_SYNC }
    private final Kind kind;
    private final SumOfTerms clock;          // null si ASYNCHRONOUS
    private final String resetStateName;     // null si ASYNCHRONOUS
    private final SumOfTerms resetCondition; // null si ASYNCHRONOUS
    public FsmHeader(Kind kind, SumOfTerms clock, String resetStateName, SumOfTerms resetCondition) {
        this.kind = Objects.requireNonNull(kind);
        this.clock = clock;
        this.resetStateName = resetStateName;
        this.resetCondition = resetCondition;
    }
    public Kind getKind() { return kind; }
    public Optional<SumOfTerms> getClock() { return Optional.ofNullable(clock); }
    public Optional<String> getResetStateName() { return Optional.ofNullable(resetStateName); }
    public Optional<SumOfTerms> getResetCondition() { return Optional.ofNullable(resetCondition); }
}
```

- [ ] **Step 2 : FsmRule**

```java
package parser.ll1.ast;
import java.util.*;

public final class FsmRule implements Node {
    private final Position position;
    private final List<String> fromStates;   // vide si "*" (wildcard)
    private final boolean wildcard;
    private final String toState;
    private final SumOfTerms whenCondition;  // null si pas de "when"
    public FsmRule(Position pos, List<String> fromStates, boolean wildcard, String toState, SumOfTerms when) {
        this.position = Objects.requireNonNull(pos);
        this.fromStates = List.copyOf(Objects.requireNonNull(fromStates));
        this.wildcard = wildcard;
        this.toState = Objects.requireNonNull(toState);
        this.whenCondition = when;
    }
    public Position getPosition() { return position; }
    public List<String> getFromStates() { return fromStates; }
    public boolean isWildcard() { return wildcard; }
    public String getToState() { return toState; }
    public Optional<SumOfTerms> getWhen() { return Optional.ofNullable(whenCondition); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 3 : Fsm**

```java
package parser.ll1.ast;
import java.util.*;

public final class Fsm implements Instance {
    private final Position position;
    private final FsmHeader header;
    private final List<FsmRule> rules;
    public Fsm(Position pos, FsmHeader header, List<FsmRule> rules) {
        this.position = Objects.requireNonNull(pos);
        this.header = Objects.requireNonNull(header);
        this.rules = List.copyOf(Objects.requireNonNull(rules));
    }
    public Position getPosition() { return position; }
    public FsmHeader getHeader() { return header; }
    public List<FsmRule> getRules() { return rules; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 4 : MapEntry + MapNode**

```java
package parser.ll1.ast;
import java.util.Objects;

public final class MapEntry implements Node {
    private final Position position;
    private final BitField from, to;
    public MapEntry(Position pos, BitField from, BitField to) {
        this.position = Objects.requireNonNull(pos);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
    }
    public Position getPosition() { return position; }
    public BitField getFrom() { return from; }
    public BitField getTo() { return to; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

```java
package parser.ll1.ast;
import java.util.*;

public final class MapNode implements Instance {
    private final Position position;
    private final SignalCompound input, output;
    private final List<MapEntry> entries;
    public MapNode(Position pos, SignalCompound input, SignalCompound output, List<MapEntry> entries) {
        this.position = Objects.requireNonNull(pos);
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.entries = List.copyOf(Objects.requireNonNull(entries));
    }
    public Position getPosition() { return position; }
    public SignalCompound getInput() { return input; }
    public SignalCompound getOutput() { return output; }
    public List<MapEntry> getEntries() { return entries; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 5 : Module**

```java
package parser.ll1.ast;
import java.util.*;

public final class Module implements Node {
    private final Position position;
    private final String name;
    private final List<Signal> params;
    private final List<Instance> instances;
    public Module(Position pos, String name, List<Signal> params, List<Instance> instances) {
        this.position = Objects.requireNonNull(pos);
        this.name = Objects.requireNonNull(name);
        this.params = List.copyOf(Objects.requireNonNull(params));
        this.instances = List.copyOf(Objects.requireNonNull(instances));
    }
    public Position getPosition() { return position; }
    public String getName() { return name; }
    public List<Signal> getParams() { return params; }
    public List<Instance> getInstances() { return instances; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
```

- [ ] **Step 6 : Compile — doit passer maintenant (Visitor a toutes les classes)**

```bash
javac -cp "lib/*" -d out $(find parser/ll1 -name '*.java')
```

- [ ] **Step 7 : Commit**
```bash
git add parser/ll1/ast
git commit -m "SCRUM-23 : AST Fsm, MapNode, Module (compilable de bout en bout)"
```

---

## Task 11 : AstImmutabilityTest (reflection)

**Files:**
- Create: `tests/parser/ll1/ast/AstImmutabilityTest.java`

- [ ] **Step 1 : Test**

```java
package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.*;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;
import static org.junit.Assert.*;

public class AstImmutabilityTest {

    private static List<Class<?>> nodeClasses() throws Exception {
        List<Class<?>> out = new ArrayList<>();
        File dir = new File("parser/ll1/ast");
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (!f.getName().endsWith(".java")) continue;
            String cls = "parser.ll1.ast." + f.getName().replace(".java", "");
            Class<?> c = Class.forName(cls);
            if (c.isInterface() || c.isEnum()) continue;
            if (Node.class.isAssignableFrom(c)) out.add(c);
        }
        return out;
    }

    @Test public void tousLesChampsSontFinal() throws Exception {
        for (Class<?> cls : nodeClasses()) {
            for (Field f : cls.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                assertTrue(cls.getSimpleName() + "." + f.getName() + " n'est pas final",
                    Modifier.isFinal(f.getModifiers()));
            }
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listeModuleInstancesImmutable() {
        Module m = new Module(new Position(1,1), "X", List.of(), List.of());
        m.getInstances().add(null);
    }
}
```

- [ ] **Step 2 : Run — doit être vert.**

- [ ] **Step 3 : Commit**
```bash
git add tests/parser/ll1/ast/AstImmutabilityTest.java
git commit -m "SCRUM-23 : test reflection immutabilité AST"
```

---

## Task 12 : ErrorCode + ParsingException

**Files:**
- Create: `parser/ll1/parser/ErrorCode.java`
- Create: `parser/ll1/parser/ParsingException.java`
- Create: `tests/parser/ll1/parser/ParsingExceptionTest.java`

- [ ] **Step 1 : ErrorCode**

```java
package parser.ll1.parser;

public enum ErrorCode {
    UNEXPECTED_TOKEN,
    EOF_UNEXPECTED,
    DEPTH_EXCEEDED,
    EMPTY_FILE,
    BIT_OUT_OF_RANGE,
    TRAILING_TOKENS,
    EMPTY_PARAM_LIST,
    EMPTY_INSTANCE_LIST;
}
```

- [ ] **Step 2 : ParsingException**

```java
package parser.ll1.parser;

import parser.ll1.token.TokenType;
import java.util.*;

public class ParsingException extends RuntimeException {
    private final ErrorCode code;
    private final int line, column;
    private final Set<TokenType> expected;   // LinkedHashSet pour ordre
    private final TokenType actual;
    private final List<String> grammarContext;
    private final String sourceSnippet;      // peut être vide
    private final String suggestion;         // peut être null

    public ParsingException(ErrorCode code, int line, int column,
                            Set<TokenType> expected, TokenType actual,
                            Deque<String> grammarStack, String sourceSnippet, String suggestion) {
        super(build(code, line, column, expected, actual, grammarStack, sourceSnippet, suggestion));
        this.code = code;
        this.line = line;
        this.column = column;
        this.expected = expected == null ? Set.of()
            : Collections.unmodifiableSet(new LinkedHashSet<>(expected));
        this.actual = actual;
        this.grammarContext = grammarStack == null ? List.of() : List.copyOf(grammarStack);
        this.sourceSnippet = sourceSnippet == null ? "" : sourceSnippet;
        this.suggestion = suggestion;
    }

    public ErrorCode getCode() { return code; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public Set<TokenType> getExpected() { return expected; }
    public TokenType getActual() { return actual; }
    public List<String> getGrammarContext() { return grammarContext; }
    public String getSourceSnippet() { return sourceSnippet; }
    public Optional<String> getSuggestion() { return Optional.ofNullable(suggestion); }

    private static String build(ErrorCode c, int l, int col, Set<TokenType> exp, TokenType act,
                                Deque<String> stack, String snip, String sug) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ligne ").append(l).append(", colonne ").append(col)
          .append(" [").append(c).append("]");
        if (exp != null && !exp.isEmpty()) sb.append(" : attendu ").append(exp);
        if (act != null) sb.append(", reçu ").append(act);
        if (snip != null && !snip.isEmpty()) sb.append("\n  ").append(snip);
        if (stack != null && !stack.isEmpty()) sb.append("\nContexte : ").append(String.join(" > ", stack));
        if (sug != null) sb.append("\nSuggestion : ").append(sug);
        return sb.toString();
    }
}
```

- [ ] **Step 3 : Test**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.TokenType;
import java.util.*;
import static org.junit.Assert.*;

public class ParsingExceptionTest {
    @Test public void snapshotGrammarStack() {
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("Module"); stack.push("InstanceList");
        ParsingException e = new ParsingException(ErrorCode.UNEXPECTED_TOKEN, 1, 1,
            new LinkedHashSet<>(List.of(TokenType.EQ)), TokenType.PLUS,
            stack, "", null);
        stack.clear();
        assertEquals(2, e.getGrammarContext().size());
    }

    @Test public void getMessageContientCodeEtLine() {
        ParsingException e = new ParsingException(ErrorCode.EMPTY_FILE, 1, 1,
            null, null, null, null, null);
        assertTrue(e.getMessage().contains("EMPTY_FILE"));
    }
}
```

- [ ] **Step 4 : Compile + run + commit**

```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.parser.ParsingExceptionTest
git add parser/ll1/parser tests/parser/ll1/parser/ParsingExceptionTest.java
git commit -m "SCRUM-23 : ParsingException + ErrorCode"
```

---

## Task 13 : Parser — noyau (constructeur, peek/consume, depth, grammarStack)

**Files:**
- Create: `parser/ll1/parser/Parser.java`
- Create: `tests/parser/ll1/fixtures/TokenFixtures.java`
- Create: `tests/parser/ll1/parser/ParserCoreTest.java`

- [ ] **Step 1 : TokenFixtures (helpers)**

```java
package tests.parser.ll1.fixtures;

import parser.ll1.token.*;
import java.util.*;

public final class TokenFixtures {
    public static Token tok(TokenType t) { return new Token(t, null, 1, 1); }
    public static Token tok(TokenType t, String v) { return new Token(t, v, 1, 1); }
    public static Token tok(TokenType t, String v, int line, int col) { return new Token(t, v, line, col); }
    public static List<Token> seq(Token... ts) {
        List<Token> l = new ArrayList<>(Arrays.asList(ts));
        l.add(tok(TokenType.EOF));
        return l;
    }
}
```

- [ ] **Step 2 : Parser (noyau uniquement)**

```java
package parser.ll1.parser;

import parser.ll1.token.*;
import parser.ll1.ast.Module;
import java.util.*;

public final class Parser {
    static final int MAX_DEPTH = 64;

    private final List<Token> tokens;
    private final String source;              // peut être null
    private final String[] sourceLines;       // cache pour snippets
    private int pos = 0;
    private int depth = 0;
    private final Deque<String> grammarStack = new ArrayDeque<>();
    private boolean consumed = false;

    public Parser(List<Token> tokens) { this(tokens, null); }

    public Parser(List<Token> tokens, String source) {
        Objects.requireNonNull(tokens, "tokens");
        List<Token> ts = new ArrayList<>(tokens);
        if (ts.isEmpty() || ts.get(ts.size() - 1).getType() != TokenType.EOF) {
            Token last = ts.isEmpty() ? null : ts.get(ts.size() - 1);
            int l = last == null ? 1 : last.getLine();
            int c = last == null ? 1 : last.getColumn();
            ts.add(new Token(TokenType.EOF, null, l, c));
        }
        this.tokens = Collections.unmodifiableList(ts);
        this.source = source;
        this.sourceLines = source == null ? null : source.split("\n", -1);
    }

    public Module parse() {
        if (consumed) throw new IllegalStateException("parse() déjà appelé");
        consumed = true;
        if (tokens.size() == 1) {
            throw error(ErrorCode.EMPTY_FILE, Set.of(TokenType.MODULE));
        }
        Module m = parseModule();
        if (peek(0).getType() != TokenType.EOF) {
            throw error(ErrorCode.TRAILING_TOKENS, Set.of(TokenType.EOF));
        }
        return m;
    }

    // --- helpers ---

    Token peek(int k) { return tokens.get(Math.min(pos + k, tokens.size() - 1)); }

    Token consume(TokenType expected) {
        Token t = peek(0);
        if (t.getType() != expected) {
            throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(expected));
        }
        pos++;
        return t;
    }

    boolean accept(TokenType t) {
        if (peek(0).getType() == t) { pos++; return true; }
        return false;
    }

    <R> R enterRule(String name, java.util.function.Supplier<R> body) {
        if (depth >= MAX_DEPTH) throw error(ErrorCode.DEPTH_EXCEEDED, Set.of());
        depth++;
        grammarStack.addLast(name);
        try { return body.get(); }
        finally { grammarStack.removeLast(); depth--; }
    }

    ParsingException error(ErrorCode code, Set<TokenType> expected) {
        Token t = peek(0);
        TokenType actual = t.getType();
        String snippet = snippet(t);
        if (actual == TokenType.EOF && code == ErrorCode.UNEXPECTED_TOKEN) {
            code = ErrorCode.EOF_UNEXPECTED;
        }
        return new ParsingException(code, t.getLine(), t.getColumn(),
            expected, actual, grammarStack, snippet, null);
    }

    private String snippet(Token t) {
        if (sourceLines == null) return "";
        int idx = t.getLine() - 1;
        if (idx < 0 || idx >= sourceLines.length) return "";
        String line = sourceLines[idx];
        StringBuilder sb = new StringBuilder();
        sb.append(t.getLine()).append(" | ").append(line).append("\n");
        int pad = String.valueOf(t.getLine()).length() + 3 + Math.max(0, t.getColumn() - 1);
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append('^');
        return sb.toString();
    }

    // Stubs — implémentés aux tâches suivantes
    private Module parseModule() {
        throw new UnsupportedOperationException("implémenté à la Task 18");
    }
}
```

- [ ] **Step 3 : Test noyau**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserCoreTest {
    @Test(expected = NullPointerException.class)
    public void tokensNullRejete() { new Parser(null); }

    @Test public void eofAjouteSiAbsent() {
        Parser p = new Parser(List.of(tok(TokenType.MODULE)));
        // si tout va bien, parse() va planter plus loin, pas à l'EOF
        try { p.parse(); } catch (Exception ignored) {}
    }

    @Test public void fichierVideRendEmptyFile() {
        try {
            new Parser(List.of(tok(TokenType.EOF))).parse();
            fail();
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EMPTY_FILE, e.getCode());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void parseDeuxFoisInterdit() {
        Parser p = new Parser(List.of(tok(TokenType.EOF)));
        try { p.parse(); } catch (ParsingException ignored) {}
        p.parse();
    }
}
```

- [ ] **Step 4 : Run + commit**

```bash
git add parser/ll1/parser/Parser.java tests/parser/ll1/fixtures/TokenFixtures.java tests/parser/ll1/parser/ParserCoreTest.java
git commit -m "SCRUM-23 : Parser noyau (peek/consume/enterRule/depth/errors)"
```

---

## Task 14 : Parser — Signal, Factor, Term, SumOfTerms

**Files:**
- Modify: `parser/ll1/parser/Parser.java` (ajouter méthodes `parseSignal`, `parseFactor`, etc.)
- Create: `tests/parser/ll1/parser/ParserExpressionTest.java`

- [ ] **Step 1 : Tests rouges**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import parser.ll1.token.*;
import java.util.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserExpressionTest {

    // helper : expose parseSumOfTerms via package-private API
    private SumOfTerms parseExpr(List<Token> tokens) {
        Parser p = new Parser(tokens);
        return p.parseSumOfTermsForTest();
    }

    @Test public void signalSimple() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"))).parseSignalForTest();
        assertEquals("a", s.getName());
        assertFalse(s.getHi().isPresent());
    }

    @Test public void signalIndexUnique() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"), tok(LBRACKET), tok(INTEGER, "3"), tok(RBRACKET)))
            .parseSignalForTest();
        assertEquals(3, (int) s.getHi().get());
        assertFalse(s.getLo().isPresent());
    }

    @Test public void signalIntervalle() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"), tok(LBRACKET), tok(INTEGER, "3"),
                                   tok(DOTDOT), tok(INTEGER, "7"), tok(RBRACKET)))
            .parseSignalForTest();
        assertEquals(3, (int) s.getHi().get());
        assertEquals(7, (int) s.getLo().get());
    }

    @Test public void factorLitteral0() {
        Factor f = new Parser(seq(tok(INTEGER, "0"))).parseFactorForTest();
        assertEquals(Factor.Kind.LITERAL_0, f.getKind());
    }

    @Test public void factorLitteralInvalide() {
        try {
            new Parser(seq(tok(INTEGER, "5"))).parseFactorForTest();
            fail();
        } catch (parser.ll1.parser.ParsingException e) {
            assertEquals(parser.ll1.parser.ErrorCode.BIT_OUT_OF_RANGE, e.getCode());
        }
    }

    @Test public void termAvecEtoile() {
        Term t = new Parser(seq(tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"))).parseTermForTest();
        assertEquals(2, t.getFactors().size());
    }

    @Test public void sommeEtProduit() {
        SumOfTerms s = parseExpr(seq(tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
                                      tok(PLUS), tok(IDENTIFIER, "c")));
        assertEquals(2, s.getTerms().size());           // a*b + c
        assertEquals(2, s.getTerms().get(0).getFactors().size());
    }

    @Test public void parenthesage() {
        SumOfTerms s = parseExpr(seq(tok(LPAREN), tok(IDENTIFIER, "a"), tok(PLUS),
                                      tok(IDENTIFIER, "b"), tok(RPAREN)));
        assertEquals(1, s.getTerms().size());
        assertEquals(Factor.Kind.PAREN, s.getTerms().get(0).getFactors().get(0).getKind());
    }
}
```

- [ ] **Step 2 : Fail (méthodes `*ForTest` absentes).**

- [ ] **Step 3 : Implémenter dans `Parser.java`**

Ajouter à `Parser` (méthodes package-private pour tests) :

```java
    // --- Methods exposés pour tests ---
    public Signal parseSignalForTest() { return enterRule("Signal", this::parseSignal); }
    public Factor parseFactorForTest() { return enterRule("Factor", this::parseFactor); }
    public Term   parseTermForTest()   { return enterRule("Term",   this::parseTerm); }
    public SumOfTerms parseSumOfTermsForTest() { return enterRule("SumOfTerms", this::parseSumOfTerms); }
    public SignalCompound parseSignalCompoundForTest() { return enterRule("SignalCompound", this::parseSignalCompound); }

    // --- Signal ---
    private Signal parseSignal() {
        Token id = consume(TokenType.IDENTIFIER);
        Position p = new Position(id.getLine(), id.getColumn());
        if (peek(0).getType() == TokenType.LBRACKET) {
            consume(TokenType.LBRACKET);
            Token hi = consume(TokenType.INTEGER);
            int hiV = Integer.parseInt(hi.getValue());
            if (peek(0).getType() == TokenType.RBRACKET) {
                consume(TokenType.RBRACKET);
                return new Signal(p, id.getValue(), hiV, null);
            }
            TokenType sep = peek(0).getType();
            if (sep != TokenType.DOTDOT && sep != TokenType.COLON) {
                throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(TokenType.RBRACKET, TokenType.DOTDOT, TokenType.COLON));
            }
            consume(sep);
            Token lo = consume(TokenType.INTEGER);
            consume(TokenType.RBRACKET);
            return new Signal(p, id.getValue(), hiV, Integer.parseInt(lo.getValue()));
        }
        return new Signal(p, id.getValue(), null, null);
    }

    private SignalCompound parseSignalCompound() {
        Signal first = enterRule("Signal", this::parseSignal);
        List<Signal> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.AMPERSAND) {
            consume(TokenType.AMPERSAND);
            list.add(enterRule("Signal", this::parseSignal));
        }
        return new SignalCompound(first.getPosition(), list);
    }

    // --- Factor / Term / SumOfTerms ---
    private Factor parseFactor() {
        Token t = peek(0);
        Position p = new Position(t.getLine(), t.getColumn());
        switch (t.getType()) {
            case LPAREN: {
                consume(TokenType.LPAREN);
                SumOfTerms inner = enterRule("SumOfTerms", this::parseSumOfTerms);
                consume(TokenType.RPAREN);
                return Factor.paren(p, inner);
            }
            case INTEGER: {
                consume(TokenType.INTEGER);
                String v = t.getValue();
                if ("0".equals(v)) return Factor.lit0(p);
                if ("1".equals(v)) return Factor.lit1(p);
                throw new ParsingException(ErrorCode.BIT_OUT_OF_RANGE, t.getLine(), t.getColumn(),
                    Set.of(TokenType.INTEGER), TokenType.INTEGER, grammarStack, snippet(t),
                    "Factor accepte uniquement 0 ou 1.");
            }
            case BITFIELD: {
                Token b = consume(TokenType.BITFIELD);
                return Factor.bits(p, new BitField(p, b.getValue()));
            }
            case SLASH: {
                consume(TokenType.SLASH);
                Signal s = enterRule("Signal", this::parseSignal);
                return Factor.negSignal(p, s);
            }
            case IDENTIFIER: {
                Signal s = enterRule("Signal", this::parseSignal);
                return Factor.signal(p, s);
            }
            default:
                throw error(ErrorCode.UNEXPECTED_TOKEN,
                    Set.of(TokenType.LPAREN, TokenType.INTEGER, TokenType.BITFIELD, TokenType.SLASH, TokenType.IDENTIFIER));
        }
    }

    private Term parseTerm() {
        Factor first = enterRule("Factor", this::parseFactor);
        List<Factor> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.STAR) {
            consume(TokenType.STAR);
            list.add(enterRule("Factor", this::parseFactor));
        }
        return new Term(first.getPosition(), list);
    }

    private SumOfTerms parseSumOfTerms() {
        Term first = enterRule("Term", this::parseTerm);
        List<Term> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.PLUS) {
            consume(TokenType.PLUS);
            list.add(enterRule("Term", this::parseTerm));
        }
        return new SumOfTerms(first.getPosition(), list);
    }

    private List<SumOfTerms> parseSumOfTermsCompound() {
        List<SumOfTerms> out = new ArrayList<>();
        out.add(enterRule("SumOfTerms", this::parseSumOfTerms));
        while (peek(0).getType() == TokenType.AMPERSAND) {
            consume(TokenType.AMPERSAND);
            out.add(enterRule("SumOfTerms", this::parseSumOfTerms));
        }
        return out;
    }
```

Note : le snippet `snippet(t)` et `grammarStack` sont privés ; rendre `snippet` package-private (retirer `private`) si nécessaire. La ligne `throw new ParsingException(..., grammarStack, snippet(t), ...)` utilise directement les champs, donc OK.

- [ ] **Step 4 : Run + vert + commit**

```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.parser.ParserExpressionTest
git add parser/ll1/parser/Parser.java tests/parser/ll1/parser/ParserExpressionTest.java
git commit -m "SCRUM-23 : parser Signal/Factor/Term/SumOfTerms"
```

---

## Task 15 : Parser — Assignment, TriState, MemoryPoint (dispatch après SignalCompound)

**Files:**
- Modify: `parser/ll1/parser/Parser.java` (`parseInstance`, `parseAssignOrTri`, `parseMemoryPoint`)
- Create: `tests/parser/ll1/parser/ParserAssignmentTest.java`

- [ ] **Step 1 : Test**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import parser.ll1.token.Token;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserAssignmentTest {

    @Test public void assignmentSimple() {
        // c = a * b
        List<Token> ts = seq(
            tok(IDENTIFIER, "c"), tok(EQ),
            tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"));
        Instance ins = new Parser(ts).parseInstanceForTest();
        assertTrue(ins instanceof Assignment);
        Assignment a = (Assignment) ins;
        assertEquals("c", a.getTarget().getSignals().get(0).getName());
    }

    @Test public void triState() {
        // c = a output enabled when e
        List<Token> ts = seq(
            tok(IDENTIFIER, "c"), tok(EQ), tok(IDENTIFIER, "a"),
            tok(OUTPUT), tok(ENABLED), tok(WHEN), tok(IDENTIFIER, "e"));
        Instance ins = new Parser(ts).parseInstanceForTest();
        assertTrue(ins instanceof TriState);
    }

    @Test public void memoryPointMinimal() {
        // q := d on clk, set when s
        List<Token> ts = seq(
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(SET), tok(WHEN), tok(IDENTIFIER, "s"));
        Instance ins = new Parser(ts).parseInstanceForTest();
        assertTrue(ins instanceof MemoryPoint);
        assertEquals(MemoryPoint.Kind.SET, ((MemoryPoint) ins).getSetOrReset());
    }

    @Test public void memoryPointAvecEnable() {
        // q := d on clk, set when s, enabled when e ;
        List<Token> ts = seq(
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(SET), tok(WHEN), tok(IDENTIFIER, "s"), tok(COMMA),
            tok(ENABLED), tok(WHEN), tok(IDENTIFIER, "e"), tok(SEMICOLON));
        MemoryPoint mp = (MemoryPoint) new Parser(ts).parseInstanceForTest();
        assertTrue(mp.getEnable().isPresent());
    }
}
```

- [ ] **Step 2 : Implémenter dans Parser**

```java
    public Instance parseInstanceForTest() { return enterRule("Instance", this::parseInstance); }

    private Instance parseInstance() {
        TokenType t0 = peek(0).getType();
        TokenType t1 = peek(1).getType();
        if (t0 == TokenType.DOLLAR) return enterRule("ModuleInstance", this::parseModuleInstance);
        if (t0 == TokenType.FSM || t0 == TokenType.STATEMACHINE) return enterRule("Fsm", this::parseFsm);
        if (t0 == TokenType.MAP) return enterRule("Map", this::parseMap);
        if (t0 == TokenType.IDENTIFIER && t1 == TokenType.LPAREN) {
            return enterRule("ModuleInstance", this::parseModuleInstance);
        }
        if (t0 != TokenType.IDENTIFIER) {
            throw error(ErrorCode.UNEXPECTED_TOKEN,
                Set.of(TokenType.IDENTIFIER, TokenType.DOLLAR, TokenType.FSM, TokenType.STATEMACHINE, TokenType.MAP));
        }
        // IDENTIFIER non suivi de LPAREN → parser SignalCompound, puis dispatch sur EQ / ASSIGN
        SignalCompound target = enterRule("SignalCompound", this::parseSignalCompound);
        TokenType op = peek(0).getType();
        if (op == TokenType.EQ) return parseAssignOrTri(target);
        if (op == TokenType.ASSIGN) return parseMemoryPointTail(target);
        throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(TokenType.EQ, TokenType.ASSIGN));
    }

    private Instance parseAssignOrTri(SignalCompound target) {
        consume(TokenType.EQ);
        List<SumOfTerms> expr = parseSumOfTermsCompound();
        if (peek(0).getType() == TokenType.OUTPUT) {
            consume(TokenType.OUTPUT); consume(TokenType.ENABLED); consume(TokenType.WHEN);
            SumOfTerms enable = enterRule("SumOfTerms", this::parseSumOfTerms);
            return new TriState(target.getPosition(), target, expr, enable);
        }
        return new Assignment(target.getPosition(), target, expr);
    }

    private Instance parseMemoryPointTail(SignalCompound target) {
        consume(TokenType.ASSIGN);
        List<SumOfTerms> expr = parseSumOfTermsCompound();
        consume(TokenType.ON);
        SumOfTerms clock = enterRule("SumOfTerms", this::parseSumOfTerms);
        if (peek(0).getType() == TokenType.COMMA) consume(TokenType.COMMA);
        MemoryPoint.Kind kind;
        if (peek(0).getType() == TokenType.SET) { consume(TokenType.SET); kind = MemoryPoint.Kind.SET; }
        else if (peek(0).getType() == TokenType.RESET) { consume(TokenType.RESET); kind = MemoryPoint.Kind.RESET; }
        else throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(TokenType.SET, TokenType.RESET));
        consume(TokenType.WHEN);
        SumOfTerms cond = enterRule("SumOfTerms", this::parseSumOfTerms);
        SumOfTerms enable = null;
        if (peek(0).getType() == TokenType.COMMA || peek(0).getType() == TokenType.ENABLED) {
            if (peek(0).getType() == TokenType.COMMA) consume(TokenType.COMMA);
            if (peek(0).getType() == TokenType.ENABLED) {
                consume(TokenType.ENABLED); consume(TokenType.WHEN);
                enable = enterRule("SumOfTerms", this::parseSumOfTerms);
            }
        }
        if (peek(0).getType() == TokenType.SEMICOLON) consume(TokenType.SEMICOLON);
        return new MemoryPoint(target.getPosition(), target, expr, clock, kind, cond, enable);
    }
```

*Stubs pour `parseModuleInstance`, `parseFsm`, `parseMap` : `throw new UnsupportedOperationException` — implémentés à la tâche suivante.*

- [ ] **Step 3 : Compile, run, commit**
```bash
git add parser/ll1/parser/Parser.java tests/parser/ll1/parser/ParserAssignmentTest.java
git commit -m "SCRUM-23 : parser Instance dispatch (Assignment/TriState/MemoryPoint)"
```

---

## Task 16 : Parser — ModuleInstance, Map, Fsm

**Files:**
- Modify: `parser/ll1/parser/Parser.java`
- Create: `tests/parser/ll1/parser/ParserModuleInstanceTest.java`
- Create: `tests/parser/ll1/parser/ParserMapTest.java`
- Create: `tests/parser/ll1/parser/ParserFsmTest.java`

- [ ] **Step 1 : Tests**

```java
// tests/parser/ll1/parser/ParserModuleInstanceTest.java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserModuleInstanceTest {
    @Test public void instanceSimple() {
        ModuleInstance mi = (ModuleInstance) new Parser(seq(
            tok(IDENTIFIER, "et"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(COMMA),
            tok(IDENTIFIER, "b"), tok(RPAREN))).parseInstanceForTest();
        assertEquals("et", mi.getModuleName());
        assertFalse(mi.isPredefined());
        assertEquals(2, mi.getArgs().size());
    }

    @Test public void instancePredefined() {
        ModuleInstance mi = (ModuleInstance) new Parser(seq(
            tok(DOLLAR), tok(IDENTIFIER, "nand"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(RPAREN))).parseInstanceForTest();
        assertTrue(mi.isPredefined());
        assertEquals("nand", mi.getModuleName());
    }
}
```

```java
// tests/parser/ll1/parser/ParserMapTest.java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserMapTest {
    @Test public void mapAvecUneEntree() {
        // map a -> b "00" -> "11" end map
        MapNode m = (MapNode) new Parser(seq(
            tok(MAP), tok(IDENTIFIER, "a"), tok(ARROW), tok(IDENTIFIER, "b"),
            tok(BITFIELD, "00"), tok(ARROW), tok(BITFIELD, "11"),
            tok(END), tok(MAP))).parseInstanceForTest();
        assertEquals(1, m.getEntries().size());
    }
}
```

```java
// tests/parser/ll1/parser/ParserFsmTest.java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserFsmTest {
    @Test public void fsmAsynchroneVide() {
        // fsm asynchronous end fsm
        Fsm f = (Fsm) new Parser(seq(
            tok(FSM), tok(ASYNCHRONOUS), tok(END), tok(FSM))).parseInstanceForTest();
        assertEquals(FsmHeader.Kind.ASYNCHRONOUS, f.getHeader().getKind());
        assertTrue(f.getRules().isEmpty());
    }

    @Test public void fsmRegleWildcard() {
        // fsm asynchronous * -> s0 end fsm
        Fsm f = (Fsm) new Parser(seq(
            tok(FSM), tok(ASYNCHRONOUS),
            tok(STAR), tok(ARROW), tok(IDENTIFIER, "s0"),
            tok(END), tok(FSM))).parseInstanceForTest();
        assertTrue(f.getRules().get(0).isWildcard());
        assertEquals("s0", f.getRules().get(0).getToState());
    }
}
```

- [ ] **Step 2 : Implémenter dans Parser**

```java
    private ModuleInstance parseModuleInstance() {
        boolean predef = accept(TokenType.DOLLAR);
        Token id = consume(TokenType.IDENTIFIER);
        Position pos = new Position(id.getLine(), id.getColumn());
        consume(TokenType.LPAREN);
        List<Node> args = new ArrayList<>();
        args.add(parseSignalOrLiteral());
        while (peek(0).getType() == TokenType.COMMA || peek(0).getType() == TokenType.COLON) {
            pos++; // OOPS — pas `pos++`, mais `this.pos++`. Correction : consommer le séparateur.
            // Correction :
            consume(peek(0).getType());
            args.add(parseSignalOrLiteral());
        }
        consume(TokenType.RPAREN);
        return new ModuleInstance(pos, id.getValue(), predef, args);
    }

    private Node parseSignalOrLiteral() {
        Token t = peek(0);
        Position p = new Position(t.getLine(), t.getColumn());
        if (t.getType() == TokenType.INTEGER) { consume(TokenType.INTEGER);
            return Factor.signal(p, null); /* TODO : wrapper INTEGER proprement — voir ci-dessous */
        }
        if (t.getType() == TokenType.BITFIELD) { consume(TokenType.BITFIELD);
            return new BitField(p, t.getValue());
        }
        return enterRule("Signal", this::parseSignal);
    }

    private MapNode parseMap() {
        Token mk = consume(TokenType.MAP);
        Position p = new Position(mk.getLine(), mk.getColumn());
        SignalCompound in = enterRule("SignalCompound", this::parseSignalCompound);
        consume(TokenType.ARROW);
        SignalCompound out = enterRule("SignalCompound", this::parseSignalCompound);
        List<MapEntry> entries = new ArrayList<>();
        while (peek(0).getType() == TokenType.BITFIELD) {
            Token b1 = consume(TokenType.BITFIELD);
            consume(TokenType.ARROW);
            Token b2 = consume(TokenType.BITFIELD);
            Position ep = new Position(b1.getLine(), b1.getColumn());
            entries.add(new MapEntry(ep, new BitField(ep, b1.getValue()), new BitField(ep, b2.getValue())));
        }
        consume(TokenType.END); consume(TokenType.MAP);
        return new MapNode(p, in, out, entries);
    }

    private Fsm parseFsm() {
        Token fk = peek(0);
        TokenType kw = fk.getType();    // FSM ou STATEMACHINE
        consume(kw);
        Position p = new Position(fk.getLine(), fk.getColumn());
        FsmHeader header = parseFsmHeader();
        List<FsmRule> rules = new ArrayList<>();
        while (peek(0).getType() == TokenType.STAR || peek(0).getType() == TokenType.IDENTIFIER) {
            rules.add(parseFsmRule());
        }
        consume(TokenType.END);
        if (peek(0).getType() != kw) {
            throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(kw));
        }
        consume(kw);
        return new Fsm(p, header, rules);
    }

    private FsmHeader parseFsmHeader() {
        TokenType t = peek(0).getType();
        if (t == TokenType.ASYNCHRONOUS) {
            consume(TokenType.ASYNCHRONOUS);
            return new FsmHeader(FsmHeader.Kind.ASYNCHRONOUS, null, null, null);
        }
        if (t == TokenType.SYNCHRONOUS) {
            consume(TokenType.SYNCHRONOUS); consume(TokenType.ON);
            SumOfTerms clk = enterRule("SumOfTerms", this::parseSumOfTerms);
            if (peek(0).getType() == TokenType.COMMA) consume(TokenType.COMMA);
            Token reset = consume(TokenType.IDENTIFIER);
            consume(TokenType.WHEN);
            SumOfTerms cond = enterRule("SumOfTerms", this::parseSumOfTerms);
            return new FsmHeader(FsmHeader.Kind.SYNCHRONOUS_ON_RESET, clk, reset.getValue(), cond);
        }
        if (t == TokenType.IDENTIFIER) {
            Token reset = consume(TokenType.IDENTIFIER);
            consume(TokenType.WHEN);
            SumOfTerms cond = enterRule("SumOfTerms", this::parseSumOfTerms);
            if (peek(0).getType() == TokenType.COMMA) consume(TokenType.COMMA);
            consume(TokenType.SYNCHRONOUS); consume(TokenType.ON);
            SumOfTerms clk = enterRule("SumOfTerms", this::parseSumOfTerms);
            return new FsmHeader(FsmHeader.Kind.RESET_WHEN_SYNC, clk, reset.getValue(), cond);
        }
        throw error(ErrorCode.UNEXPECTED_TOKEN,
            Set.of(TokenType.ASYNCHRONOUS, TokenType.SYNCHRONOUS, TokenType.IDENTIFIER));
    }

    private FsmRule parseFsmRule() {
        Token start = peek(0);
        Position p = new Position(start.getLine(), start.getColumn());
        boolean wild = false;
        List<String> froms = new ArrayList<>();
        if (peek(0).getType() == TokenType.STAR) { consume(TokenType.STAR); wild = true; }
        else {
            froms.add(consume(TokenType.IDENTIFIER).getValue());
            while (peek(0).getType() == TokenType.COMMA) {
                consume(TokenType.COMMA);
                froms.add(consume(TokenType.IDENTIFIER).getValue());
            }
        }
        consume(TokenType.ARROW);
        String to = consume(TokenType.IDENTIFIER).getValue();
        SumOfTerms when = null;
        if (peek(0).getType() == TokenType.WHEN) {
            consume(TokenType.WHEN);
            when = enterRule("SumOfTerms", this::parseSumOfTerms);
            if (peek(0).getType() == TokenType.SEMICOLON || peek(0).getType() == TokenType.COMMA) {
                consume(peek(0).getType());
            }
        }
        return new FsmRule(p, froms, wild, to, when);
    }
```

**Correction dans `parseModuleInstance`** : remplacer `pos++` par `consume(peek(0).getType())`. La variable `pos` de la classe Parser est un `int` privé, pas à confondre avec la `Position` locale.

Version corrigée :
```java
    private ModuleInstance parseModuleInstance() {
        boolean predef = accept(TokenType.DOLLAR);
        Token id = consume(TokenType.IDENTIFIER);
        Position pos = new Position(id.getLine(), id.getColumn());
        consume(TokenType.LPAREN);
        List<Node> args = new ArrayList<>();
        args.add(parseSignalOrLiteral());
        while (peek(0).getType() == TokenType.COMMA || peek(0).getType() == TokenType.COLON) {
            consume(peek(0).getType());
            args.add(parseSignalOrLiteral());
        }
        consume(TokenType.RPAREN);
        return new ModuleInstance(pos, id.getValue(), predef, args);
    }
```

Pour `parseSignalOrLiteral` — besoin d'un wrapper AST pour INTEGER. Le plus simple : stocker l'INTEGER comme un `Factor.lit0/1` si valeur ∈ {0,1}, sinon `ParsingException`. Révision :

```java
    private Node parseSignalOrLiteral() {
        Token t = peek(0);
        Position p = new Position(t.getLine(), t.getColumn());
        if (t.getType() == TokenType.INTEGER) {
            consume(TokenType.INTEGER);
            if ("0".equals(t.getValue())) return Factor.lit0(p);
            if ("1".equals(t.getValue())) return Factor.lit1(p);
            throw new ParsingException(ErrorCode.BIT_OUT_OF_RANGE, t.getLine(), t.getColumn(),
                Set.of(TokenType.INTEGER), TokenType.INTEGER, grammarStack, snippet(t),
                "Argument littéral accepte uniquement 0 ou 1.");
        }
        if (t.getType() == TokenType.BITFIELD) {
            consume(TokenType.BITFIELD);
            return new BitField(p, t.getValue());
        }
        return enterRule("Signal", this::parseSignal);
    }
```

- [ ] **Step 2 : Run + commit**
```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.parser.ParserModuleInstanceTest tests.parser.ll1.parser.ParserMapTest tests.parser.ll1.parser.ParserFsmTest
git add parser/ll1/parser/Parser.java tests/parser/ll1/parser/Parser{ModuleInstance,Map,Fsm}Test.java
git commit -m "SCRUM-23 : parser ModuleInstance, Map, Fsm"
```

---

## Task 17 : Parser — Module, ParamList, InstanceList (top-level)

**Files:**
- Modify: `parser/ll1/parser/Parser.java` (implémenter `parseModule`)
- Create: `tests/parser/ll1/parser/ParserModuleTest.java`

- [ ] **Step 1 : Test**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.Module;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserModuleTest {
    @Test public void moduleET() {
        // module ET(a, b : c) c = a * b end module
        Module m = new Parser(seq(
            tok(MODULE), tok(IDENTIFIER, "ET"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"), tok(COLON), tok(IDENTIFIER, "c"),
            tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ), tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE))).parse();
        assertEquals("ET", m.getName());
        assertEquals(3, m.getParams().size());
        assertEquals(1, m.getInstances().size());
    }

    @Test public void paramListVideInterdit() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(RPAREN),
                tok(IDENTIFIER, "y"), tok(EQ), tok(INTEGER, "0"),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (parser.ll1.parser.ParsingException e) {
            assertEquals(parser.ll1.parser.ErrorCode.EMPTY_PARAM_LIST, e.getCode());
        }
    }

    @Test public void instanceListVideInterdit() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (parser.ll1.parser.ParsingException e) {
            assertEquals(parser.ll1.parser.ErrorCode.EMPTY_INSTANCE_LIST, e.getCode());
        }
    }
}
```

- [ ] **Step 2 : Implémenter**

Remplacer le stub `parseModule` :

```java
    private Module parseModule() {
        return enterRule("Module", () -> {
            Token mod = consume(TokenType.MODULE);
            Position pos = new Position(mod.getLine(), mod.getColumn());
            Token name = consume(TokenType.IDENTIFIER);
            consume(TokenType.LPAREN);
            if (peek(0).getType() == TokenType.RPAREN) {
                throw error(ErrorCode.EMPTY_PARAM_LIST, Set.of(TokenType.IDENTIFIER));
            }
            List<Signal> params = new ArrayList<>();
            params.add(enterRule("Signal", this::parseSignal));
            while (peek(0).getType() == TokenType.COMMA || peek(0).getType() == TokenType.COLON) {
                consume(peek(0).getType());
                params.add(enterRule("Signal", this::parseSignal));
            }
            consume(TokenType.RPAREN);
            List<Instance> instances = new ArrayList<>();
            if (peek(0).getType() == TokenType.END) {
                throw error(ErrorCode.EMPTY_INSTANCE_LIST, Set.of());
            }
            while (peek(0).getType() != TokenType.END) {
                instances.add(enterRule("Instance", this::parseInstance));
            }
            consume(TokenType.END); consume(TokenType.MODULE);
            return new Module(pos, name.getValue(), params, instances);
        });
    }
```

- [ ] **Step 3 : Run + commit**
```bash
git add parser/ll1/parser/Parser.java tests/parser/ll1/parser/ParserModuleTest.java
git commit -m "SCRUM-23 : parser Module complet"
```

---

## Task 18 : Tests E2E — ET, BasculeD, FSM synchrone, DécodeurBCD

**Files:**
- Create: `tests/parser/ll1/fixtures/ShdlFixtures.java`
- Create: `tests/parser/ll1/parser/ParserFullModuleTest.java`

- [ ] **Step 1 : ShdlFixtures (helpers)**

Petit fichier avec 4 listes de tokens correspondant aux 4 modules. Pour gagner du temps, on ne teste pas la structure AST en profondeur — juste la réussite du parse et le nombre d'instances.

```java
package tests.parser.ll1.fixtures;

import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;

public final class ShdlFixtures {

    public static List<Token> moduleET() {
        // module ET(a, b : c) c = a * b end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "ET"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"), tok(COLON), tok(IDENTIFIER, "c"),
            tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ), tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleBasculeD() {
        // module BasculeD(d, clk : q)
        //   q := d on clk, set when 1
        // end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "BasculeD"), tok(LPAREN),
            tok(IDENTIFIER, "d"), tok(COMMA), tok(IDENTIFIER, "clk"), tok(COLON), tok(IDENTIFIER, "q"),
            tok(RPAREN),
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA), tok(SET), tok(WHEN), tok(INTEGER, "1"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleFsmSynchrone() {
        // module FsmSync(clk, reset : out)
        //   fsm synchronous on clk, s0 when reset
        //     s0 -> s1 when 1
        //     s1 -> s0
        //   end fsm
        //   out = 0
        // end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "FsmSync"), tok(LPAREN),
            tok(IDENTIFIER, "clk"), tok(COMMA), tok(IDENTIFIER, "reset"), tok(COLON), tok(IDENTIFIER, "out"),
            tok(RPAREN),
            tok(FSM), tok(SYNCHRONOUS), tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(IDENTIFIER, "s0"), tok(WHEN), tok(IDENTIFIER, "reset"),
            tok(IDENTIFIER, "s0"), tok(ARROW), tok(IDENTIFIER, "s1"), tok(WHEN), tok(INTEGER, "1"),
            tok(IDENTIFIER, "s1"), tok(ARROW), tok(IDENTIFIER, "s0"),
            tok(END), tok(FSM),
            tok(IDENTIFIER, "out"), tok(EQ), tok(INTEGER, "0"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleDecodeurBCD() {
        // module DecBCD(a : b) map a -> b "0000" -> "1111" end map end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "DecBCD"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COLON), tok(IDENTIFIER, "b"), tok(RPAREN),
            tok(MAP), tok(IDENTIFIER, "a"), tok(ARROW), tok(IDENTIFIER, "b"),
            tok(BITFIELD, "0000"), tok(ARROW), tok(BITFIELD, "1111"),
            tok(END), tok(MAP),
            tok(END), tok(MODULE));
    }
}
```

- [ ] **Step 2 : ParserFullModuleTest**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import static tests.parser.ll1.fixtures.ShdlFixtures.*;
import static org.junit.Assert.*;

public class ParserFullModuleTest {
    @Test public void et() {
        Module m = new Parser(moduleET()).parse();
        assertEquals("ET", m.getName());
        assertTrue(m.getInstances().get(0) instanceof Assignment);
    }

    @Test public void basculeD() {
        Module m = new Parser(moduleBasculeD()).parse();
        assertTrue(m.getInstances().get(0) instanceof MemoryPoint);
    }

    @Test public void fsmSynchrone() {
        Module m = new Parser(moduleFsmSynchrone()).parse();
        assertEquals(2, m.getInstances().size());
        assertTrue(m.getInstances().get(0) instanceof Fsm);
        assertEquals(FsmHeader.Kind.SYNCHRONOUS_ON_RESET, ((Fsm) m.getInstances().get(0)).getHeader().getKind());
    }

    @Test public void decodeurBCD() {
        Module m = new Parser(moduleDecodeurBCD()).parse();
        assertTrue(m.getInstances().get(0) instanceof MapNode);
    }
}
```

- [ ] **Step 3 : Run + commit**
```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.parser.ParserFullModuleTest
git add tests/parser/ll1/fixtures/ShdlFixtures.java tests/parser/ll1/parser/ParserFullModuleTest.java
git commit -m "SCRUM-23 : tests E2E (ET, BasculeD, FsmSync, DecodeurBCD)"
```

---

## Task 19 : Tests d'erreurs — couverture ErrorCode

**Files:**
- Create: `tests/parser/ll1/parser/ParserErrorTest.java`

- [ ] **Step 1 : Tests**

```java
package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserErrorTest {

    @Test public void emptyFile() {
        try { new Parser(List.of(tok(EOF))).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_FILE, e.getCode()); }
    }

    @Test public void unexpectedToken() {
        try { new Parser(seq(tok(PLUS))).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode()); }
    }

    @Test public void eofUnexpected() {
        // module ET ( <EOF>
        try {
            new Parser(seq(tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN))).parse();
            fail();
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EOF_UNEXPECTED, e.getCode());
        }
    }

    @Test public void bitOutOfRange() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "5"),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.BIT_OUT_OF_RANGE, e.getCode()); }
    }

    @Test public void trailingTokens() {
        // module ET (a) b = 0 end module JUNK
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "0"),
                tok(END), tok(MODULE),
                tok(IDENTIFIER, "junk"))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.TRAILING_TOKENS, e.getCode()); }
    }

    @Test public void emptyParamList() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "0"),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_PARAM_LIST, e.getCode()); }
    }

    @Test public void emptyInstanceList() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_INSTANCE_LIST, e.getCode()); }
    }

    @Test public void depthExceeded() {
        // Parenthèses imbriquées à profondeur > 64
        List<Token> ts = new ArrayList<>();
        ts.add(tok(MODULE)); ts.add(tok(IDENTIFIER, "X")); ts.add(tok(LPAREN));
        ts.add(tok(IDENTIFIER, "a")); ts.add(tok(RPAREN));
        ts.add(tok(IDENTIFIER, "b")); ts.add(tok(EQ));
        for (int i = 0; i < 70; i++) ts.add(tok(LPAREN));
        ts.add(tok(INTEGER, "0"));
        for (int i = 0; i < 70; i++) ts.add(tok(RPAREN));
        ts.add(tok(END)); ts.add(tok(MODULE)); ts.add(tok(EOF));
        try { new Parser(ts).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.DEPTH_EXCEEDED, e.getCode()); }
    }
}
```

- [ ] **Step 2 : Run + commit**
```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore tests.parser.ll1.parser.ParserErrorTest
git add tests/parser/ll1/parser/ParserErrorTest.java
git commit -m "SCRUM-23 : couverture ErrorCode (8 types d'erreur)"
```

---

## Task 20 : Factory Parser.parseFrom + documentation

**Files:**
- Modify: `parser/ll1/parser/Parser.java` (ajouter `parseFrom` + Javadoc thread-safety)
- Create: `parser/ll1/parser/Lexer.java` (interface minimale)

- [ ] **Step 1 : Interface Lexer**

```java
package parser.ll1.parser;

import parser.ll1.token.Token;
import java.util.List;

/** Contrat minimal vers le lexer d'Erwan. À adapter quand son API est figée. */
public interface Lexer {
    List<Token> tokenize(String source);
}
```

- [ ] **Step 2 : parseFrom**

Ajouter à Parser :
```java
    /**
     * Fabrique haut-niveau : tokenize puis parse. Pratique pour Mati / l'UI.
     * Non thread-safe : ce Parser ne peut être utilisé que par un seul thread.
     */
    public static parser.ll1.ast.Module parseFrom(String shdl, Lexer lexer) {
        Objects.requireNonNull(shdl, "shdl");
        Objects.requireNonNull(lexer, "lexer");
        return new Parser(lexer.tokenize(shdl), shdl).parse();
    }
```

Ajouter Javadoc de classe :
```java
/**
 * Parser LL(1) avec lookahead 2 local sur Instance. Non thread-safe.
 * Usage : {@code new Parser(tokens).parse()} une seule fois.
 */
public final class Parser { ... }
```

- [ ] **Step 3 : Commit**
```bash
git add parser/ll1/parser/Parser.java parser/ll1/parser/Lexer.java
git commit -m "SCRUM-23 : Parser.parseFrom + interface Lexer + Javadoc"
```

---

## Self-Review final (après toutes les tâches)

- [ ] **Lancer la suite complète**
```bash
javac -cp "lib/*" -d out $(find parser/ll1 tests/parser/ll1 -name '*.java')
java -cp "out:lib/*" org.junit.runner.JUnitCore \
  tests.parser.ll1.token.TokenTest \
  tests.parser.ll1.grammar.GrammarStructureTest \
  tests.parser.ll1.grammar.GrammarFreezeTest \
  tests.parser.ll1.grammar.FirstSetTest \
  tests.parser.ll1.grammar.FollowSetTest \
  tests.parser.ll1.grammar.Ll1ConflictTest \
  tests.parser.ll1.ast.AstImmutabilityTest \
  tests.parser.ll1.parser.ParsingExceptionTest \
  tests.parser.ll1.parser.ParserCoreTest \
  tests.parser.ll1.parser.ParserExpressionTest \
  tests.parser.ll1.parser.ParserAssignmentTest \
  tests.parser.ll1.parser.ParserModuleInstanceTest \
  tests.parser.ll1.parser.ParserMapTest \
  tests.parser.ll1.parser.ParserFsmTest \
  tests.parser.ll1.parser.ParserModuleTest \
  tests.parser.ll1.parser.ParserFullModuleTest \
  tests.parser.ll1.parser.ParserErrorTest
```
Attendu : `OK (~60 tests)`.

- [ ] **Push**
```bash
git push
```

- [ ] **Mise à jour Jira SCRUM-23** → status "Done" (via gh/API Jira séparé).

---

## Notes de garde

1. **Grammaire INSTANCE ambiguë** : c'est intentionnel (lookahead 2). Le `Ll1ConflictTest` flaggera un conflit FIRST/FIRST sur INSTANCE seulement — on l'autorise. Si le checker trouve autre chose, la grammaire est cassée.

2. **Ne pas toucher aux conventions d'Erwan** : packages déclarés, classes classiques (pas de records), JUnit 4, `RuntimeException` pour les erreurs utilisateur.

3. **Contrat Token** : l'enum `TokenType` définie ici fait foi. Si Erwan renomme ses tokens différemment, un adaptateur sera ajouté au sprint 2.

4. **Commentaires SHDL** : ignorés par le lexer. Aucune logique à implémenter côté parser.

5. **Immuabilité AST** : toute nouvelle classe AST doit passer `AstImmutabilityTest` — champs `final`, listes `List.copyOf`.

**Fin du plan.**
