package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class Ll1ConflictTest {

    @Test public void checkerDetecteConflitFirstFirst() {
        // A → EQ MODULE | EQ END  (FIRST/FIRST évident sur EQ)
        NonTerminal A = NonTerminal.MODULE;
        Grammar bad = new GrammarBuilder()
            .prod(A, TokenType.EQ, TokenType.MODULE)
            .prod(A, TokenType.EQ, TokenType.END)
            .build(A);
        List<Ll1Conflict> c = new Ll1ConflictChecker(bad).findAllConflicts();
        assertFalse(c.isEmpty());
        assertTrue(c.stream().anyMatch(x -> x.getType() == Ll1Conflict.Type.FIRST_FIRST));
    }

    @Test public void checkerDetecteRecursionGauche() {
        // A → A PLUS | INTEGER
        NonTerminal A = NonTerminal.MODULE;
        Grammar bad = new GrammarBuilder()
            .prod(A, A, TokenType.PLUS)
            .prod(A, TokenType.INTEGER)
            .build(A);
        List<Ll1Conflict> c = new Ll1ConflictChecker(bad).findAllConflicts();
        assertTrue(c.stream().anyMatch(x -> x.getType() == Ll1Conflict.Type.LEFT_RECURSION));
    }

    @Test public void checkerDetecteFirstFollow() {
        // A → B EQ | EQ  ; B → PLUS | ε
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

    @Test public void grammaireShdlAConflitsAttendusConnus() {
        // Conflits INTENTIONNELS, résolus dans le code du parser :
        //   - INSTANCE (FIRST/FIRST sur IDENTIFIER) : lookahead 2 pour distinguer
        //     ModuleInstance (IDENTIFIER LPAREN) vs Assignment/MemoryPoint (IDENTIFIER non-LPAREN)
        //   - TERM_REST (FIRST/FOLLOW sur STAR) : dans une règle FSM wildcard,
        //     `when x * y` vs `* -> s2` sont ambigus. Résolution greedy dans parseTerm :
        //     STAR continue toujours l'expression. Si l'utilisateur veut un wildcard
        //     après un when, il doit terminer la règle par `;` ou `,`.
        java.util.Set<NonTerminal> attendus = java.util.Set.of(
            NonTerminal.INSTANCE,
            NonTerminal.TERM_REST);
        List<Ll1Conflict> all = new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts();
        for (Ll1Conflict c : all) {
            assertTrue("conflit inattendu sur " + c.getNonTerminal() + " : " + c,
                attendus.contains(c.getNonTerminal()));
        }
    }
}
