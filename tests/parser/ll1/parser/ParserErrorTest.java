package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserErrorTest {

    @Test public void emptyFile() {
        try { new Parser(List.of(tok(EOF))).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_FILE, e.getCode()); }
    }

    @Test public void unexpectedToken() {
        try { new Parser(seq(tok(PLUS))).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode()); }
    }

    @Test public void eofUnexpected() {
        try {
            new Parser(seq(tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN))).parse();
            fail();
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EOF_UNEXPECTED, e.getCode());
        }
    }

    @Test public void bitOutOfRange() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "5"),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.BIT_OUT_OF_RANGE, e.getCode()); }
    }

    @Test public void trailingTokens() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "0"),
                tok(END), tok(MODULE),
                tok(IDENTIFIER, "junk"))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.TRAILING_TOKENS, e.getCode()); }
    }

    @Test public void emptyParamList() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(RPAREN),
                tok(IDENTIFIER, "b"), tok(EQ), tok(INTEGER, "0"),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_PARAM_LIST, e.getCode()); }
    }

    @Test public void emptyInstanceList() {
        try {
            new Parser(seq(
                tok(MODULE), tok(IDENTIFIER, "X"), tok(LPAREN), tok(IDENTIFIER, "a"), tok(RPAREN),
                tok(END), tok(MODULE))).parse();
            fail();
        } catch (ParsingException e) { assertEquals(ErrorCode.EMPTY_INSTANCE_LIST, e.getCode()); }
    }

    @Test public void depthExceeded() {
        List<Token> ts = new ArrayList<>();
        ts.add(tok(MODULE)); ts.add(tok(IDENTIFIER, "X")); ts.add(tok(LPAREN));
        ts.add(tok(IDENTIFIER, "a")); ts.add(tok(RPAREN));
        ts.add(tok(IDENTIFIER, "b")); ts.add(tok(EQ));
        for (int i = 0; i < 70; i++) ts.add(tok(LPAREN));
        ts.add(tok(INTEGER, "0"));
        for (int i = 0; i < 70; i++) ts.add(tok(RPAREN));
        ts.add(tok(END)); ts.add(tok(MODULE)); ts.add(tok(EOF));
        try { new Parser(ts).parse(); fail(); }
        catch (ParsingException e) { assertEquals(ErrorCode.DEPTH_EXCEEDED, e.getCode()); }
    }
}
