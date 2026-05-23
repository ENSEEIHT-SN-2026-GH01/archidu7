package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests de la recursion Instance_Star → Instance Instance_Star | ε (BLOC B).
 *
 * Grammaire concernee :
 *   Instance_Plus → Instance Instance_Star
 *   Instance_Star → Instance Instance_Star | ε
 *
 * Cas testes :
 *   1. Une seule instance  : Instance_Star immediat est epsilon
 *   2. Deux instances      : Instance_Star porte 1 Instance + 1 Instance_Star epsilon
 *   3. Trois instances     : profondeur de recursion 3
 *   4. Cinq instances      : test de montee en charge (assertion count uniquement)
 */
public class CstParserMultiInstanceTest {

    // ------------------------------------------------------------------
    // Utilitaire : collecte recursive de tous les noeuds Instance
    // ------------------------------------------------------------------

    private static List<CstNode> collectInstances(CstNode node) {
        List<CstNode> result = new ArrayList<>();
        collectInstancesRec(node, result);
        return result;
    }

    private static void collectInstancesRec(CstNode node, List<CstNode> acc) {
        if (node instanceof CstInternal internal) {
            if (internal.nt() == NonTerminal.Instance) {
                acc.add(internal);
            }
            for (CstNode child : internal.children()) {
                collectInstancesRec(child, acc);
            }
        }
    }

    /**
     * Retourne le noeud Instance_Plus depuis la racine Start.
     */
    private static CstInternal getInstancePlus(CstNode root) {
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        return (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
    }

    // ------------------------------------------------------------------
    // Cas 1 : une seule instance
    //   module m (a) i = .0 end module
    //   Instance_Plus → Instance Instance_Star(ε)
    // ------------------------------------------------------------------

    private static final String SRC_UN = "module m (a) i = .0 end module";

    @Test
    public void une_instance_parse_ok() {
        CstNode root = CstParser.parse(SRC_UN);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_UN.length(), root.endOffset());
    }

    @Test
    public void une_instance_iplus_a_un_instance() {
        CstNode root = CstParser.parse(SRC_UN);
        CstInternal iplus = getInstancePlus(root);

        // Instance_Plus doit avoir exactement 2 enfants : Instance + Instance_Star
        assertEquals("Instance_Plus doit avoir 2 enfants", 2, iplus.children().size());
        assertEquals("Premier enfant de Instance_Plus doit etre Instance",
                NonTerminal.Instance, ((CstInternal) iplus.children().get(0)).nt());
        assertEquals("Deuxieme enfant de Instance_Plus doit etre Instance_Star",
                NonTerminal.Instance_Star, ((CstInternal) iplus.children().get(1)).nt());
    }

    @Test
    public void une_instance_instance_star_est_epsilon() {
        CstNode root = CstParser.parse(SRC_UN);
        CstInternal iplus = getInstancePlus(root);

        // Le Instance_Star direct de Instance_Plus doit etre epsilon (liste vide)
        CstInternal istar = (CstInternal) iplus.children().get(1);
        assertTrue("Instance_Star doit etre epsilon pour une seule instance",
                istar.children().isEmpty());
    }

    @Test
    public void une_instance_count_total() {
        CstNode root = CstParser.parse(SRC_UN);
        List<CstNode> instances = collectInstances(root);
        assertEquals("1 instance attendue", 1, instances.size());
    }

    // ------------------------------------------------------------------
    // Cas 2 : deux instances
    //   module m (a) x = .0 y = .1 end module
    //   Instance_Plus → Instance Instance_Star
    //   Instance_Star → Instance Instance_Star(ε)
    // ------------------------------------------------------------------

    private static final String SRC_DEUX = "module m (a) x = .0 y = .1 end module";

    @Test
    public void deux_instances_parse_ok() {
        CstNode root = CstParser.parse(SRC_DEUX);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_DEUX.length(), root.endOffset());
    }

    @Test
    public void deux_instances_iplus_structure() {
        CstNode root = CstParser.parse(SRC_DEUX);
        CstInternal iplus = getInstancePlus(root);

        // Instance_Plus → Instance Instance_Star (2 enfants)
        assertEquals("Instance_Plus doit avoir 2 enfants", 2, iplus.children().size());
        CstInternal istar1 = (CstInternal) iplus.children().get(1);
        assertEquals("Le deuxieme enfant de Instance_Plus est Instance_Star",
                NonTerminal.Instance_Star, istar1.nt());

        // Instance_Star → Instance Instance_Star (non-epsilon)
        assertFalse("Instance_Star du niveau 1 doit etre non-epsilon (il reste 1 instance)",
                istar1.children().isEmpty());
        assertEquals("Instance_Star niveau 1 doit avoir 2 enfants (Instance + Instance_Star)",
                2, istar1.children().size());

        // Le Instance_Star imbrique doit etre epsilon
        CstInternal istar2 = (CstInternal) istar1.children().get(1);
        assertEquals("Le second Instance_Star doit etre Instance_Star",
                NonTerminal.Instance_Star, istar2.nt());
        assertTrue("Instance_Star de profondeur 2 doit etre epsilon",
                istar2.children().isEmpty());
    }

    @Test
    public void deux_instances_count_total() {
        CstNode root = CstParser.parse(SRC_DEUX);
        List<CstNode> instances = collectInstances(root);
        assertEquals("2 instances attendues", 2, instances.size());
    }

    @Test
    public void deux_instances_chacune_a_identifiant() {
        CstNode root = CstParser.parse(SRC_DEUX);
        List<CstNode> instances = collectInstances(root);
        for (CstNode ins : instances) {
            CstInternal insInternal = (CstInternal) ins;
            assertTrue("Chaque Instance doit avoir un Identifiant comme premier enfant",
                    insInternal.has(new Terminal(Token.Identifiant)));
        }
    }

    // ------------------------------------------------------------------
    // Cas 3 : trois instances
    //   module m (a) x = .0 y = .1 z = .0 end module
    //   Profondeur de recursion Instance_Star : 3 niveaux
    // ------------------------------------------------------------------

    private static final String SRC_TROIS = "module m (a) x = .0 y = .1 z = .0 end module";

    @Test
    public void trois_instances_parse_ok() {
        CstNode root = CstParser.parse(SRC_TROIS);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_TROIS.length(), root.endOffset());
    }

    @Test
    public void trois_instances_count_total() {
        CstNode root = CstParser.parse(SRC_TROIS);
        List<CstNode> instances = collectInstances(root);
        assertEquals("3 instances attendues", 3, instances.size());
    }

    @Test
    public void trois_instances_profondeur_recursion() {
        CstNode root = CstParser.parse(SRC_TROIS);
        CstInternal iplus = getInstancePlus(root);

        // Niveau 0 : Instance_Plus -> Instance Instance_Star(non-eps)
        CstInternal istar1 = (CstInternal) iplus.children().get(1);
        assertFalse("Instance_Star niveau 1 doit etre non-epsilon", istar1.children().isEmpty());

        // Niveau 1 : Instance_Star -> Instance Instance_Star(non-eps)
        CstInternal istar2 = (CstInternal) istar1.children().get(1);
        assertFalse("Instance_Star niveau 2 doit etre non-epsilon", istar2.children().isEmpty());

        // Niveau 2 : Instance_Star -> Instance Instance_Star(eps)
        CstInternal istar3 = (CstInternal) istar2.children().get(1);
        assertTrue("Instance_Star niveau 3 doit etre epsilon", istar3.children().isEmpty());
    }

    @Test
    public void trois_instances_chacune_a_identifiant() {
        CstNode root = CstParser.parse(SRC_TROIS);
        List<CstNode> instances = collectInstances(root);
        for (CstNode ins : instances) {
            CstInternal insInternal = (CstInternal) ins;
            assertTrue("Chaque Instance doit avoir un Identifiant",
                    insInternal.has(new Terminal(Token.Identifiant)));
        }
    }

    // ------------------------------------------------------------------
    // Cas 4 : cinq instances
    //   module m (a) v = .0 w = .1 x = .0 y = .1 z = .0 end module
    //   Test de montee en charge de la recursion
    // ------------------------------------------------------------------

    private static final String SRC_CINQ =
        "module m (a) v = .0 w = .1 x = .0 y = .1 z = .0 end module";

    @Test
    public void cinq_instances_parse_ok() {
        CstNode root = CstParser.parse(SRC_CINQ);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_CINQ.length(), root.endOffset());
    }

    @Test
    public void cinq_instances_count_total() {
        CstNode root = CstParser.parse(SRC_CINQ);
        List<CstNode> instances = collectInstances(root);
        assertEquals("5 instances attendues", 5, instances.size());
    }

    @Test
    public void cinq_instances_toutes_ont_identifiant() {
        CstNode root = CstParser.parse(SRC_CINQ);
        List<CstNode> instances = collectInstances(root);
        assertEquals("5 instances attendues", 5, instances.size());
        for (CstNode ins : instances) {
            CstInternal insInternal = (CstInternal) ins;
            assertTrue("Chaque Instance parmi 5 doit avoir un Identifiant",
                    insInternal.has(new Terminal(Token.Identifiant)));
        }
    }
}
