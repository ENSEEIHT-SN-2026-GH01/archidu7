package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.ParsingException;

import static util.test.Assert.assertThrows;

/**
 * Verifie que ParsingException.expected() == null quand l'erreur est
 * une absence d'entree dans la table (pas un mismatch terminal) (review 18d).
 */
public class CstParserNoExpectedTest {

    /**
     * "end module" commence par EndKW. Le parser cherche une production pour
     * [Start, EndKW] dans la table => aucune entree => expected == null.
     * C'est le cas "aucune production applicable".
     */
    @Test
    public void erreur_sans_terminal_attendu_expected_null() {
        // EndKW en debut => aucune production dans la table pour [Start, EndKW]
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("end module"));
        assertNull("expected() doit etre null pour absence d'entree dans la table",
                ex.expected());
        assertNotNull("actual() doit etre non null", ex.actual());
    }

    /**
     * Autre cas : RightPar en position d'expression => aucune production pour [Factor, RightPar].
     * expected() == null egalement.
     */
    @Test
    public void erreur_expression_invalide_expected_null() {
        // RightPar en position d'expression
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("module foo (a) i = ) end module"));
        assertNull("expected() doit etre null pour absence d'entree dans la table",
                ex.expected());
    }

    /**
     * Contre-exemple : mismatch terminal => expected() != null.
     * "module 42 ..." : NaturalInteger la ou Identifiant (terminal) est attendu.
     */
    @Test
    public void erreur_mismatch_terminal_expected_non_null() {
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("module 42 (a) i = .0 end module"));
        assertNotNull("expected() doit etre non null pour un mismatch terminal",
                ex.expected());
    }
}
