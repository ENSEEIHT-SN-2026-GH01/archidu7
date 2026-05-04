package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.ParsingException;

/**
 * Tests pour le champ contextPath de ParsingException (review M3).
 *
 * Verifie que les erreurs dans des sous-arbres profonds mentionnent
 * plusieurs non-terminaux dans le message et dans contextPath().
 */
public class CstParserContextPathTest {

    /**
     * Erreur dans un sous-arbre profond : l'expression apres '=' est invalide.
     * Module > Instance > Operation > Assignment > SignalAssignment > SumOfTermsCompound > ...
     * Le token '+' en debut d'expression force une erreur dans Factor (ou SumOfTerms).
     * Le contextPath doit mentionner au moins deux NT.
     */
    @Test
    public void erreur_profonde_contextPath_multiple_nt() {
        // '+' en position d'expression valide => pas de production pour Factor avec OrOp
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("module foo (a) i = + end module"));
        String path = ex.contextPath();
        assertNotNull("contextPath ne doit pas etre null pour une erreur profonde", path);
        // Le chemin doit contenir au moins un " > " (plusieurs NT)
        assertTrue("contextPath doit contenir plusieurs NT : got [" + path + "]",
                path.contains(" > "));
        // Le message de l'exception doit contenir le chemin
        String msg = ex.getMessage();
        assertTrue("Le message doit inclure le contextPath : got [" + msg + "]",
                msg.contains(path));
    }

    /**
     * Erreur de premier niveau (token inattendu juste apres 'module') :
     * le contextPath peut etre un seul NT ou null (pas de sous-arbre profond).
     */
    @Test
    public void erreur_premier_niveau_contextPath_simple() {
        // 'module 42 ...' : NaturalInteger la ou Identifiant est attendu
        ParsingException ex = assertThrows(ParsingException.class,
                () -> CstParser.parse("module 42 (a) i = .0 end module"));
        // contextPath peut etre null ou simple (un seul NT)
        String path = ex.contextPath();
        if (path != null) {
            // S'il y a un chemin, il doit au moins contenir "Module"
            assertTrue("contextPath doit commencer par Module : got [" + path + "]",
                    path.contains("Module"));
        }
    }
}
