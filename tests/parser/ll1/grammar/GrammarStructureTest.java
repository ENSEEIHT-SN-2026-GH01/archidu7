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
