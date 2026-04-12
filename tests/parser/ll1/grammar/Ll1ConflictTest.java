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

    @Test public void grammaireShdlAUnConflitAttenduSurInstanceSeulement() {
        // INSTANCE a un conflit FIRST/FIRST intentionnel sur IDENTIFIER (résolu par lookahead 2 dans le parser).
        // Tous les autres non-terminaux doivent être propres.
        List<Ll1Conflict> all = new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts();
        for (Ll1Conflict c : all) {
            assertEquals("conflit inattendu sur " + c.getNonTerminal() + " : " + c,
                NonTerminal.INSTANCE, c.getNonTerminal());
        }
    }
}
