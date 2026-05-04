package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Lexer;
import parser.ll1.parser.Parser;
import parser.ll1.ast.Module;
import parser.ll1.token.Token;
import static tests.parser.ll1.fixtures.ShdlFixtures.*;
import static org.junit.Assert.*;

import java.util.List;

public class ParserFactoryTest {
    @Test public void parseFromUtiliseLexerFourni() {
        List<Token> tokens = moduleET();
        Lexer bidon = new Lexer() {
            @Override public List<Token> tokenize(String source) {
                assertEquals("source-fictive", source);
                return tokens;
            }
        };
        Module m = Parser.parseFrom("source-fictive", bidon);
        assertNotNull(m);
        assertEquals("ET", m.getName());
    }

    @Test(expected = NullPointerException.class)
    public void parseFromRejetteSourceNull() {
        Parser.parseFrom(null, s -> List.of());
    }

    @Test(expected = NullPointerException.class)
    public void parseFromRejetteLexerNull() {
        Parser.parseFrom("x", null);
    }
}
