package tests.parser.ll1.token;

import org.junit.Test;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class TokenTest {
    @Test
    public void gettersRestituentLesChamps() {
        Token t = new Token(TokenType.IDENTIFIER, "abc", 3, 12, 42);
        assertEquals(TokenType.IDENTIFIER, t.getType());
        assertEquals("abc", t.getValue());
        assertEquals(3, t.getLine());
        assertEquals(12, t.getColumn());
        assertEquals(42, t.getOffset());
    }

    @Test(expected = NullPointerException.class)
    public void typeNullInterdit() { new Token(null, "x", 1, 1, 0); }

    @Test
    public void toStringContientType() {
        assertTrue(new Token(TokenType.MODULE, "module", 1, 1, 0).toString().contains("MODULE"));
    }
}
