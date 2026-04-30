package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.TokenType;

import static org.junit.Assert.*;

/**
 * Tests batch 5.B : skip whitespace et commentaires entre tokens.
 * L'impl de skipTrivia dans {@link ShdlLexer} doit déjà gérer ces cas.
 */
public class WhitespaceCommentSkipTest {

    @Test public void espaces_entre_tokens() {
        var tokens = ShdlLexer.tokenize("foo  bar");
        // foo (3 chars) + 2 espaces + bar (3 chars) + EOF
        assertEquals(3, tokens.size());
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(0, tokens.get(0).offset());
        assertEquals(TokenType.Identifiant, tokens.get(1).type());
        assertEquals("bar", tokens.get(1).value());
        assertEquals(5, tokens.get(1).offset());  // foo (3) + "  " (2) = offset 5
        assertEquals(TokenType.EOF, tokens.get(2).type());
        assertEquals(8, tokens.get(2).offset());
    }

    @Test public void retours_ligne_et_tabs() {
        var tokens = ShdlLexer.tokenize("foo\n\tbar");
        assertEquals(3, tokens.size());
        assertEquals("foo", tokens.get(0).value());
        assertEquals("bar", tokens.get(1).value());
        assertEquals(5, tokens.get(1).offset());  // foo (3) + \n + \t = offset 5
    }

    @Test public void commentaire_double_slash_jusqu_au_newline() {
        var tokens = ShdlLexer.tokenize("foo // commentaire\nbar");
        assertEquals(3, tokens.size());
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(TokenType.Identifiant, tokens.get(1).type());
        assertEquals("bar", tokens.get(1).value());
        assertEquals(TokenType.EOF, tokens.get(2).type());
    }

    @Test public void commentaire_diese_jusqu_au_newline() {
        var tokens = ShdlLexer.tokenize("foo # commentaire\nbar");
        assertEquals(3, tokens.size());
        assertEquals("foo", tokens.get(0).value());
        assertEquals("bar", tokens.get(1).value());
    }

    @Test public void commentaire_jusqu_a_eof_sans_newline() {
        var tokens = ShdlLexer.tokenize("foo // jusqu'a la fin");
        assertEquals(2, tokens.size());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(TokenType.EOF, tokens.get(1).type());
        assertEquals("foo // jusqu'a la fin".length(), tokens.get(1).offset());
    }

    @Test public void source_uniquement_whitespace() {
        var tokens = ShdlLexer.tokenize("   \n\t  ");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
        assertEquals("   \n\t  ".length(), tokens.get(0).offset());
    }

    @Test public void source_uniquement_commentaire_slash() {
        var tokens = ShdlLexer.tokenize("// rien d'autre");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }

    @Test public void source_uniquement_commentaire_diese() {
        var tokens = ShdlLexer.tokenize("# rien d'autre");
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }

    @Test public void plusieurs_commentaires_consecutifs() {
        var tokens = ShdlLexer.tokenize("foo\n// c1\n// c2\nbar");
        assertEquals(3, tokens.size());
        assertEquals("foo", tokens.get(0).value());
        assertEquals("bar", tokens.get(1).value());
    }

    @Test public void whitespace_au_debut() {
        var tokens = ShdlLexer.tokenize("   foo");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("foo", tokens.get(0).value());
        assertEquals(3, tokens.get(0).offset());  // skipped 3 espaces
    }
}
