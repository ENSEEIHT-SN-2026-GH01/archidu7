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
