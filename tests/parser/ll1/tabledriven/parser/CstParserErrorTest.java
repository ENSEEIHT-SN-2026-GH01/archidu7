package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.ParsingException;

/**
 * Tests CstParser pour les cas d'erreur avec verification des offsets (Task 8.B).
 */
public class CstParserErrorTest {

    /**
     * Token inattendu en debut : "end module" au lieu de "module ...".
     * Le parser cherche [Start, EndKW] => aucune production => offset 0.
     */
    @Test
    public void token_inattendu_debut_offset_zero() {
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("end module"));
        assertEquals("L'offset doit etre 0 (debut de la source)", 0, ex.offset());
        assertNull("expected() doit etre null (absence d'entree table)", ex.expected());
    }

    /**
     * RightPar manquant : "module foo (a end module"
     * Apres 'a' le parser attend RightPar (ou une virgule pour d'autres params).
     * 'a' est a l'offset 12, 'end' commence a l'offset 14.
     * => ParsingException.offset() == 14 (position de EndKW).
     */
    @Test
    public void rightpar_manquant_offset_pointe_vers_end() {
        String src = "module foo (a end module";
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse(src));
        // 'end' commence apres "module foo (a " => offset 14
        assertEquals("L'offset doit pointer sur 'end'", 14, ex.offset());
        assertNotNull("actual() doit etre non null", ex.actual());
        assertEquals("Le token actuel doit etre EndKW", Token.EndKW, ex.actual().getToken());
    }

    /**
     * EndKW manquant : "module foo (a) i = .0" (EOF premature).
     * Le source a longueur 21 ; le parser trouve EOF la ou il attendait la suite.
     * => ParsingException.offset() == 21 (longueur de la source).
     */
    @Test
    public void endkw_manquant_eof_premature_offset_fin_source() {
        String src = "module foo (a) i = .0";
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse(src));
        assertEquals("L'offset doit etre a la fin de la source (EOF)", src.length(), ex.offset());
    }

    /**
     * Token inattendu au milieu d'expression : "module foo (a) i = + end module"
     * '+' (OrOp) en position de debut d'expression => aucune production pour [Factor, OrOp].
     * '+' est a l'offset 19.
     */
    @Test
    public void token_inattendu_expression_offset_correct() {
        String src = "module foo (a) i = + end module";
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse(src));
        // '+' est le 20eme caractere (offset 19)
        assertEquals("L'offset doit pointer sur '+'", 19, ex.offset());
        assertNull("expected() doit etre null (aucune production pour [Factor, OrOp])",
                ex.expected());
        assertNotNull("actual() doit etre non null", ex.actual());
        assertEquals("Le token actuel doit etre OrOp", Token.OrOp, ex.actual().getToken());
    }
}
