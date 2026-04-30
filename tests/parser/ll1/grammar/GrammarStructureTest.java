package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.lexer.Token;
import java.util.List;
import static org.junit.Assert.*;

public class GrammarStructureTest {
    @Test public void terminalEqualsParToken() {
        assertEquals(new Terminal(Token.AssignOp), new Terminal(Token.AssignOp));
        assertNotEquals(new Terminal(Token.AssignOp), new Terminal(Token.OrOp));
    }

    @Test public void productionCopieBody() {
        java.util.ArrayList<Symbol> body = new java.util.ArrayList<>();
        body.add(new Terminal(Token.AssignOp));
        Production p = new Production(NonTerminal.Signal, body);
        body.clear();
        assertEquals(1, p.getBody().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void bodyImmutable() {
        new Production(NonTerminal.Signal, List.of(new Terminal(Token.AssignOp)))
            .getBody().add(new Terminal(Token.OrOp));
    }

    @Test public void epsilonEstDistinctDesAutresSymboles() {
        assertTrue(Terminal.EPSILON.isEpsilon());
        assertFalse(new Terminal(Token.AssignOp).isEpsilon());
    }
}
