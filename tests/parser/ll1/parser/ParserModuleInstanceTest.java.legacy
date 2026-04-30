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
