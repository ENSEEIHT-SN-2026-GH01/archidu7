package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.Set;
import static org.junit.Assert.*;
import static parser.ll1.token.TokenType.*;

public class FirstSetTest {
    private final FirstSet first = new FirstSet(Grammar.SHDL);

    @Test public void firstModule() {
        assertEquals(Set.of(MODULE), first.of(NonTerminal.MODULE));
    }

    @Test public void firstSignal() {
        assertEquals(Set.of(IDENTIFIER), first.of(NonTerminal.SIGNAL));
    }

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

    @Test public void optCommaEstNullable() {
        assertTrue(first.nullable(NonTerminal.OPT_COMMA));
    }

    @Test public void termRestEstNullable() {
        assertTrue(first.nullable(NonTerminal.TERM_REST));
    }

    @Test public void moduleNonNullable() {
        assertFalse(first.nullable(NonTerminal.MODULE));
    }
}
