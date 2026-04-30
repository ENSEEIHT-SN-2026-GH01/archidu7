package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import static org.junit.Assert.*;

public class TokenTest {
    @Test public void offset_et_end_avec_valeur() {
        var t = new Token(TokenType.Identifiant, "abc", 10);
        assertEquals(10, t.offset());
        assertEquals(13, t.end());
        assertEquals("abc", t.value());
        assertEquals(TokenType.Identifiant, t.type());
    }

    @Test public void end_avec_valeur_null() {
        var t = new Token(TokenType.EOF, null, 5);
        assertEquals(5, t.offset());
        assertEquals(5, t.end());
    }

    @Test public void egalite_records() {
        var a = new Token(TokenType.Identifiant, "x", 0);
        var b = new Token(TokenType.Identifiant, "x", 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test public void differents_offsets_pas_egaux() {
        assertNotEquals(
            new Token(TokenType.Identifiant, "x", 0),
            new Token(TokenType.Identifiant, "x", 1)
        );
    }

    @Test(expected = NullPointerException.class)
    public void type_null_rejete() {
        new Token(null, "x", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void offset_negatif_rejete() {
        new Token(TokenType.Identifiant, "x", -1);
    }
}
