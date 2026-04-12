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
