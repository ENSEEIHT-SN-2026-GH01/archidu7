package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static org.junit.Assert.*;
import java.util.*;

public class ParserCoreTest {
    @Test(expected = NullPointerException.class)
    public void tokensNullRejete() { new Parser(null); }

    @Test public void fichierVideRendEmptyFile() {
        try {
            new Parser(List.of(tok(TokenType.EOF))).parse();
            fail();
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EMPTY_FILE, e.getCode());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void parseDeuxFoisInterdit() {
        Parser p = new Parser(List.of(tok(TokenType.EOF)));
        try { p.parse(); } catch (ParsingException ignored) {}
        p.parse();
    }

    @Test public void eofSyntheticAjoute() {
        try {
            new Parser(List.of()).parse();
            fail();
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EMPTY_FILE, e.getCode());
        }
    }
}
