package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserMapTest {
    @Test public void mapAvecUneEntree() {
        // map a -> b "00" -> "11" end map
        MapNode m = (MapNode) new Parser(seq(
            tok(MAP), tok(IDENTIFIER, "a"), tok(ARROW), tok(IDENTIFIER, "b"),
            tok(BITFIELD, "00"), tok(ARROW), tok(BITFIELD, "11"),
            tok(END), tok(MAP))).parseInstanceForTest();
        assertEquals(1, m.getEntries().size());
    }
}
