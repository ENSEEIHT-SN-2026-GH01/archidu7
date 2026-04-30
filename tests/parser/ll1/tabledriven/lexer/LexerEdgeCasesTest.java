package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.LexerException;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.token.TokenType;

import static org.junit.Assert.*;

/**
 * Tests batch 5.C : edge cases du ShdlLexer.
 * - lexème invalide (caractère non reconnu)
 * - séquence de tokens et offsets
 * - longest match (mots-clés vs identifiants)
 * - position de l'EOF
 * - opérateurs multi-caractères pas décomposés
 */
public class LexerEdgeCasesTest {

    @Test
    public void lexeme_invalide_leve_LexerException_avec_offset() {
        try {
            ShdlLexer.tokenize("@");
            fail("LexerException attendue pour caractere invalide '@'");
        } catch (LexerException e) {
            assertEquals(0, e.offset());
        }
    }

    @Test
    public void lexeme_invalide_apres_token_valide() {
        // 'foo @' : le '@' est à l'offset 4
        try {
            ShdlLexer.tokenize("foo @");
            fail("LexerException attendue");
        } catch (LexerException e) {
            assertEquals(4, e.offset());
        }
    }

    @Test
    public void offset_correct_apres_plusieurs_tokens() {
        var tokens = ShdlLexer.tokenize("a b c");
        assertEquals(0, tokens.get(0).offset());  // a
        assertEquals(2, tokens.get(1).offset());  // b
        assertEquals(4, tokens.get(2).offset());  // c
        assertEquals(5, tokens.get(3).offset());  // EOF
    }

    @Test
    public void identifiant_qui_commence_par_un_keyword_reste_identifiant() {
        // "modulo" doit rester un Identifiant entier (longest match), pas
        // se faire tronquer en "module" + "o"
        var tokens = ShdlLexer.tokenize("modulo");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("modulo", tokens.get(0).value());
    }

    @Test
    public void identifiant_qui_contient_un_keyword_partiel() {
        // "endless" : 'end' est préfixe mais 'endless' est un seul identifiant
        var tokens = ShdlLexer.tokenize("endless");
        assertEquals(TokenType.Identifiant, tokens.get(0).type());
        assertEquals("endless", tokens.get(0).value());
    }

    @Test
    public void token_eof_offset_egal_taille_source() {
        var tokens = ShdlLexer.tokenize("foo");
        var eof = tokens.get(tokens.size() - 1);
        assertEquals(TokenType.EOF, eof.type());
        assertEquals(3, eof.offset());
        assertNull(eof.value());
    }

    @Test
    public void mem_assign_op_pas_decompose_en_colon_colon_egal() {
        // "::=" doit être un seul token MemAssignOp, pas Colon + Colon + AssignOp
        var tokens = ShdlLexer.tokenize("::=");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.MemAssignOp, tokens.get(0).type());
        assertEquals(TokenType.EOF, tokens.get(1).type());
    }

    @Test
    public void point_point_pas_decompose_en_deux_points() {
        var tokens = ShdlLexer.tokenize("..");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.PointPoint, tokens.get(0).type());
    }

    @Test
    public void colon_seul_reste_colon() {
        // S'assurer que ":" seul ne se transforme pas en MemAssignOp
        var tokens = ShdlLexer.tokenize(":");
        assertEquals(2, tokens.size());
        assertEquals(TokenType.Colon, tokens.get(0).type());
    }

    @Test
    public void point_seul_pas_de_match_leve_exception() {
        // "." seul n'est pas un token valide (BitField nécessite ≥ 1 chiffre 0/1)
        // PointPoint nécessite 2 points. Un point seul → erreur.
        try {
            ShdlLexer.tokenize(".");
            fail("LexerException attendue pour '.' seul");
        } catch (LexerException e) {
            assertEquals(0, e.offset());
        }
    }

    @Test
    public void natural_integer_avec_zero_initial() {
        var tokens = ShdlLexer.tokenize("007");
        assertEquals(TokenType.NaturalInteger, tokens.get(0).type());
        assertEquals("007", tokens.get(0).value());
    }

    @Test
    public void bitfield_avec_un_seul_bit() {
        var tokens = ShdlLexer.tokenize(".0");
        assertEquals(TokenType.BitField, tokens.get(0).type());
        assertEquals(".0", tokens.get(0).value());
    }
}
