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

    @Test public void memoryPointResetSansVirgule() {
        // q := d on clk reset when s
        List<Token> ts = seq(
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"),
            tok(RESET), tok(WHEN), tok(IDENTIFIER, "s"));
        MemoryPoint mp = (MemoryPoint) new Parser(ts).parseInstanceForTest();
        assertEquals(MemoryPoint.Kind.RESET, mp.getSetOrReset());
    }
}
