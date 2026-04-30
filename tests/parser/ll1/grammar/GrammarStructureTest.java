package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.List;
import static org.junit.Assert.*;

public class GrammarStructureTest {
    @Test public void terminalEqualsParTokenType() {
        assertEquals(new Terminal(TokenType.AssignOp), new Terminal(TokenType.AssignOp));
        assertNotEquals(new Terminal(TokenType.AssignOp), new Terminal(TokenType.OrOp));
    }

    @Test public void productionCopieBody() {
        java.util.ArrayList<Symbol> body = new java.util.ArrayList<>();
        body.add(new Terminal(TokenType.AssignOp));
        Production p = new Production(NonTerminal.Signal, body);
        body.clear();
        assertEquals(1, p.getBody().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void bodyImmutable() {
        new Production(NonTerminal.Signal, List.of(new Terminal(TokenType.AssignOp)))
            .getBody().add(new Terminal(TokenType.OrOp));
    }

    @Test public void epsilonEstDistinctDesAutresSymboles() {
        assertTrue(Terminal.EPSILON.isEpsilon());
        assertFalse(new Terminal(TokenType.AssignOp).isEpsilon());
    }
}
