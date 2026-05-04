package tests.parser.ll1.tabledriven;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests d'invariants generiques sur le CST produit (BLOC C).
 *
 * Invariants verifies pour chaque source :
 *   1. Offsets monotones : pour tout noeud, startOffset <= endOffset
 *   2. Couverture racine : root.startOffset == 0, root.endOffset == src.length()
 *   3. EOF absent        : aucun CstLeaf n'a un Token.EOF
 *   4. Enfants non chevauchants : child[i].endOffset <= child[i+1].startOffset
 *   5. Epsilon bien type : CstInternal epsilon => children vide ET start == end
 *   6. Production coherente : CstInternal non-epsilon => children.size == body.size
 *
 * Strategie : 1 @Test par source pour avoir des messages d'echec cibles.
 */
public class CstInvariantsTest {

    // ------------------------------------------------------------------
    // Sources de test : mix court / long, simple / complexe, avec / sans epsilon
    // ------------------------------------------------------------------

    /** Minimal : 1 param, 1 instance, expression constante */
    private static final String SRC_MINIMAL = "module m (a) i = .0 end module";

    /** 1 param, expression signal simple */
    private static final String SRC_SIGNAL = "module m (a) o = a end module";

    /** 2 params, ET logique */
    private static final String SRC_ET = "module ET (a, b) c = a * b end module";

    /** 3 params, OU logique */
    private static final String SRC_OU = "module OU (a, b, c) o = a + b + c end module";

    /** 2 params, NOT (NotOp Signal) */
    private static final String SRC_NOT = "module NOT (a) o = /a end module";

    /** 2 params, XOR avec NotOp */
    private static final String SRC_XOR = "module xor (a, b) o = a * /b + /a * b end module";

    /** Multi-instance : 2 sorties */
    private static final String SRC_DEMI_ADDITIONNEUR =
        "module halfAdder (a, b) s = a + b c = a * b end module";

    /** Multi-instance : 3 sorties */
    private static final String SRC_TROIS =
        "module trois (a, b, c) x = a * b y = b * c z = a * c end module";

    /** MemoryAssignment */
    private static final String SRC_BASCULE =
        "module BasculeD (d, clk) q := d on clk set when d end module";

    /** Expression avec parentheses */
    private static final String SRC_PAREN =
        "module m (a, b) o = (a + b) * a end module";

    // ------------------------------------------------------------------
    // Helper central : assert tous les invariants sur une source
    // ------------------------------------------------------------------

    private static void assertInvariants(String source) {
        CstNode root = CstParser.parse(source);

        // Invariant 2 : couverture racine
        assertEquals("INV2 : startOffset doit etre 0 pour [" + source + "]",
                0, root.startOffset());
        assertEquals("INV2 : endOffset doit etre src.length() pour [" + source + "]",
                source.length(), root.endOffset());

        // Parcours recursif pour les autres invariants
        assertNodeInvariants(root, source);
    }

    private static void assertNodeInvariants(CstNode node, String source) {
        // Invariant 1 : offsets monotones
        assertTrue("INV1 : startOffset <= endOffset pour " + nodeDesc(node),
                node.startOffset() <= node.endOffset());

        if (node instanceof CstLeaf leaf) {
            // Invariant 3 : EOF absent
            assertNotEquals("INV3 : EOF ne doit pas apparaitre dans le CST pour " + nodeDesc(leaf),
                    Token.EOF, leaf.t().getType());

        } else if (node instanceof CstInternal internal) {
            List<CstNode> children = internal.children();

            if (internal.rule().isEpsilon()) {
                // Invariant 5 : epsilon bien type
                assertTrue("INV5 : CstInternal epsilon doit avoir children vide pour " + nodeDesc(internal),
                        children.isEmpty());
                assertEquals("INV5 : CstInternal epsilon doit avoir start == end pour " + nodeDesc(internal),
                        internal.startOffset(), internal.endOffset());
            } else {
                // Invariant 6 : production coherente
                int bodySize = internal.rule().getBody().size();
                assertEquals("INV6 : children.size() doit etre " + bodySize
                        + " (body de la production) pour " + nodeDesc(internal),
                        bodySize, children.size());

                // Invariant 4 : enfants non chevauchants
                for (int i = 0; i + 1 < children.size(); i++) {
                    CstNode ci  = children.get(i);
                    CstNode ci1 = children.get(i + 1);
                    assertTrue("INV4 : child[" + i + "].endOffset (" + ci.endOffset()
                            + ") <= child[" + (i + 1) + "].startOffset (" + ci1.startOffset()
                            + ") pour " + nodeDesc(internal),
                            ci.endOffset() <= ci1.startOffset());
                }
            }

            // Recursion sur les enfants
            for (CstNode child : children) {
                assertNodeInvariants(child, source);
            }
        }
    }

    private static String nodeDesc(CstNode node) {
        if (node instanceof CstLeaf leaf) {
            return "CstLeaf(" + leaf.t().getType() + " @" + node.startOffset() + ".." + node.endOffset() + ")";
        } else if (node instanceof CstInternal internal) {
            return "CstInternal(" + internal.nt() + " @" + node.startOffset() + ".." + node.endOffset() + ")";
        }
        return node.toString();
    }

    // ------------------------------------------------------------------
    // 1 @Test par source
    // ------------------------------------------------------------------

    @Test
    public void invariants_minimal() {
        assertInvariants(SRC_MINIMAL);
    }

    @Test
    public void invariants_signal_simple() {
        assertInvariants(SRC_SIGNAL);
    }

    @Test
    public void invariants_et_logique() {
        assertInvariants(SRC_ET);
    }

    @Test
    public void invariants_ou_logique() {
        assertInvariants(SRC_OU);
    }

    @Test
    public void invariants_not_op() {
        assertInvariants(SRC_NOT);
    }

    @Test
    public void invariants_xor() {
        assertInvariants(SRC_XOR);
    }

    @Test
    public void invariants_demi_additionneur() {
        assertInvariants(SRC_DEMI_ADDITIONNEUR);
    }

    @Test
    public void invariants_trois_portes() {
        assertInvariants(SRC_TROIS);
    }

    @Test
    public void invariants_bascule_d() {
        assertInvariants(SRC_BASCULE);
    }

    @Test
    public void invariants_expression_parentheses() {
        assertInvariants(SRC_PAREN);
    }
}
