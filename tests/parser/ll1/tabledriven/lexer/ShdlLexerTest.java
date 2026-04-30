package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests batch 5.A : tokenisation isolée par type de token.
 * Les batches 5.B (whitespace/commentaires) et 5.C (edge cases) sont dans
 * d'autres fichiers.
 */
public class ShdlLexerTest {

    @Test public void source_vide_donne_juste_EOF() {
        var tokens = ShdlLexer.tokenize("");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
        assertNull(tokens.get(0).value());
        assertEquals(0, tokens.get(0).offset());
    }

    @Test public void identifiant_simple() {
        var tokens = ShdlLexer.tokenize("foo");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(0, tokens.get(0).offset());
        assertEquals(TokenType.EOF, tokens.get(1).type());
        assertEquals(3, tokens.get(1).offset());
    }

    @Test public void keywords_reclassifies() {
        record Cas(String src, TokenType expected) {}
        for (var c : List.of(
            new Cas("module",  TokenType.ModuleKW),
            new Cas("end",     TokenType.EndKW),
            new Cas("on",      TokenType.OnKW),
            new Cas("when",    TokenType.WhenKW),
            new Cas("set",     TokenType.SetKW),
            new Cas("reset",   TokenType.ResetKW),
            new Cas("enabled", TokenType.EnabledKW)
        )) {
            var tokens = ShdlLexer.tokenize(c.src);
            assertEquals(c.src, c.expected, tokens.get(0).type());
            assertNull("value doit etre null pour un keyword (" + c.src + ")", tokens.get(0).value());
        }
    }

    @Test public void bitfield() {
        var tokens = ShdlLexer.tokenize(".0101");
        assertEquals(TokenType.BitField, tokens.get(0).type());
        assertEquals(".0101", tokens.get(0).value());
    }

    @Test public void natural_integer() {
        var tokens = ShdlLexer.tokenize("42");
        assertEquals(TokenType.NaturalInteger, tokens.get(0).type());
        assertEquals("42", tokens.get(0).value());
    }

    @Test public void delimiteurs() {
        record Cas(String src, TokenType expected) {}
        for (var c : List.of(
            new Cas("(", TokenType.LeftPar),
            new Cas(")", TokenType.RightPar),
            new Cas("[", TokenType.LeftSquareBrack),
            new Cas("]", TokenType.RightSquareBrack),
            new Cas(",", TokenType.Comma),
            new Cas(":", TokenType.Colon),
            new Cas(";", TokenType.Semicolon),
            new Cas("$", TokenType.Dollar)
        )) {
            var tokens = ShdlLexer.tokenize(c.src);
            assertEquals(c.src, c.expected, tokens.get(0).type());
        }
    }

    @Test public void operateurs() {
        record Cas(String src, TokenType expected) {}
        for (var c : List.of(
            new Cas("=",   TokenType.AssignOp),
            new Cas("::=", TokenType.MemAssignOp),
            new Cas("+",   TokenType.OrOp),
            new Cas("*",   TokenType.Star),
            new Cas("&",   TokenType.ConcatOp),
            new Cas("/",   TokenType.NotOp),
            new Cas("..",  TokenType.PointPoint)
        )) {
            var tokens = ShdlLexer.tokenize(c.src);
            assertEquals(c.src, c.expected, tokens.get(0).type());
        }
    }

    @Test public void value_lexeme_pour_3_types_valeur_seulement() {
        // Identifiant, BitField, NaturalInteger -> value = lexeme
        // Tout le reste -> value = null
        assertEquals("abc", ShdlLexer.tokenize("abc").get(0).value());
        assertEquals(".10", ShdlLexer.tokenize(".10").get(0).value());
        assertEquals("7",   ShdlLexer.tokenize("7").get(0).value());
        assertNull(ShdlLexer.tokenize("module").get(0).value());
        assertNull(ShdlLexer.tokenize("(").get(0).value());
        assertNull(ShdlLexer.tokenize("=").get(0).value());
        assertNull(ShdlLexer.tokenize("::=").get(0).value());
    }

    @Test public void sequence_minimale_module() {
        // "module a(b)end module" → 7 tokens + EOF
        // ModuleKW Identifiant LeftPar Identifiant RightPar EndKW ModuleKW EOF
        var tokens = ShdlLexer.tokenize("module a(b)end module");
        assertEquals(8, tokens.size());
        assertEquals(TokenType.ModuleKW,    tokens.get(0).type());
        assertEquals(TokenType.Identifiant, tokens.get(1).type());
        assertEquals("a", tokens.get(1).value());
        assertEquals(TokenType.LeftPar,     tokens.get(2).type());
        assertEquals(TokenType.Identifiant, tokens.get(3).type());
        assertEquals("b", tokens.get(3).value());
        assertEquals(TokenType.RightPar,    tokens.get(4).type());
        assertEquals(TokenType.EndKW,       tokens.get(5).type());
        assertEquals(TokenType.ModuleKW,    tokens.get(6).type());
        assertEquals(TokenType.EOF,         tokens.get(7).type());
    }

    @Test public void identifiant_avec_underscore_et_chiffres() {
        var tokens = ShdlLexer.tokenize("_foo123_bar");
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("_foo123_bar", tokens.get(0).value());
    }
}
