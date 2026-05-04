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
 * Tests d'integration etendus (BLOC A) :
 * modules realistes multi-instances exercant des expressions complexes.
 *
 * Pas de golden CstDumper : on privilegia des assertions structurelles ciblees.
 *
 * Fixtures :
 *   1. MUX 2-vers-1     : mux(a, b, sel)   o = a * /sel + b * sel
 *   2. DEMUX simple     : demux(a, sel)     o0 = a * /sel   o1 = a * sel
 *   3. HalfAdder        : halfAdder(a, b)   s = a + b       c = a * b
 *   4. OuExclusif (XOR) : xor(a, b)         o = a * /b + /a * b
 *   5. Trois-portes     : trois(a, b, c)    x = a * b       y = b * c   z = a * c
 */
public class EndToEndExtendedTest {

    // ------------------------------------------------------------------
    // Utilitaire : parcours recursif pour collecter les noeuds Instance
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

    /** Verifie que le parse reussit et couvre toute la source. */
    private static CstNode assertParseOk(String src) {
        CstNode root = CstParser.parse(src);
        assertNotNull("parse() ne doit pas retourner null", root);
        assertEquals("startOffset doit etre 0", 0, root.startOffset());
        assertEquals("endOffset doit etre src.length()", src.length(), root.endOffset());
        return root;
    }

    // ------------------------------------------------------------------
    // Fixture 1 : MUX 2-vers-1
    //   module mux (a, b, sel) o = a * /sel + b * sel end module
    // Note: /sel est Factor -> NotOp Signal (supporte par la grammaire)
    //       a * /sel + b * sel utilise * (AndOp) et + (OrOp)
    // ------------------------------------------------------------------

    private static final String SRC_MUX =
        "module mux (a, b, sel) o = a * /sel + b * sel end module";

    @Test
    public void fixture_mux_parse_sans_exception() {
        assertParseOk(SRC_MUX);
    }

    @Test
    public void fixture_mux_offsets_coherents() {
        CstNode root = CstParser.parse(SRC_MUX);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_MUX.length(), root.endOffset());
    }

    @Test
    public void fixture_mux_contient_not_op() {
        CstNode root = CstParser.parse(SRC_MUX);
        // /sel est un NotOp : Factor -> NotOp Signal
        // On verifie que le CST contient au moins un Factor avec NotOp
        assertTrue("Le CST doit contenir un NotOp pour /sel",
                containsToken(root, Token.NotOp));
    }

    // ------------------------------------------------------------------
    // Fixture 2 : DEMUX simple (multi-instance : o0 et o1)
    //   module demux (a, sel) o0 = a * /sel  o1 = a * sel end module
    // ------------------------------------------------------------------

    private static final String SRC_DEMUX =
        "module demux (a, sel) o0 = a * /sel o1 = a * sel end module";

    @Test
    public void fixture_demux_parse_sans_exception() {
        assertParseOk(SRC_DEMUX);
    }

    @Test
    public void fixture_demux_deux_instances() {
        CstNode root = CstParser.parse(SRC_DEMUX);
        List<CstNode> instances = collectInstances(root);
        assertEquals("demux doit avoir 2 instances (o0 et o1)", 2, instances.size());
    }

    @Test
    public void fixture_demux_instances_commencent_par_identifiant() {
        CstNode root = CstParser.parse(SRC_DEMUX);
        List<CstNode> instances = collectInstances(root);
        for (CstNode ins : instances) {
            CstInternal insInternal = (CstInternal) ins;
            assertTrue("Chaque Instance doit avoir un Identifiant comme premier enfant",
                    insInternal.has(new Terminal(Token.Identifiant)));
        }
    }

    // ------------------------------------------------------------------
    // Fixture 3 : HalfAdder (multi-instance : s et c)
    //   module halfAdder (a, b) s = a + b  c = a * b end module
    // ------------------------------------------------------------------

    private static final String SRC_HALF_ADDER =
        "module halfAdder (a, b) s = a + b c = a * b end module";

    @Test
    public void fixture_half_adder_parse_sans_exception() {
        assertParseOk(SRC_HALF_ADDER);
    }

    @Test
    public void fixture_half_adder_deux_instances() {
        CstNode root = CstParser.parse(SRC_HALF_ADDER);
        List<CstNode> instances = collectInstances(root);
        assertEquals("halfAdder doit avoir 2 instances (s et c)", 2, instances.size());
    }

    @Test
    public void fixture_half_adder_instance_s_contient_or_op() {
        CstNode root = CstParser.parse(SRC_HALF_ADDER);
        // s = a + b : SumOfTerms contient Or_Operand_Star non-epsilon
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        // La premiere Instance est 's'
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal sigA   = (CstInternal) assign.first(NonTerminal.SignalAssignment).orElseThrow();
        CstInternal sotc   = (CstInternal) sigA.first(NonTerminal.SumOfTermsCompound).orElseThrow();
        CstInternal sot    = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal orStar = (CstInternal) sot.first(NonTerminal.Or_Operand_Star).orElseThrow();
        assertFalse("Or_Operand_Star de s = a + b doit etre non-epsilon",
                orStar.children().isEmpty());
    }

    // ------------------------------------------------------------------
    // Fixture 4 : OuExclusif / XOR
    //   module xor (a, b) o = a * /b + /a * b end module
    // Note: /b et /a sont des NotOp Signal, supportes par Factor -> NotOp Signal
    // ------------------------------------------------------------------

    private static final String SRC_XOR =
        "module xor (a, b) o = a * /b + /a * b end module";

    @Test
    public void fixture_xor_parse_sans_exception() {
        assertParseOk(SRC_XOR);
    }

    @Test
    public void fixture_xor_offsets_coherents() {
        CstNode root = CstParser.parse(SRC_XOR);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_XOR.length(), root.endOffset());
    }

    @Test
    public void fixture_xor_contient_not_op() {
        CstNode root = CstParser.parse(SRC_XOR);
        // /b et /a sont des NotOp : le CST doit en contenir au moins un
        assertTrue("Le CST du XOR doit contenir un NotOp", containsToken(root, Token.NotOp));
    }

    // ------------------------------------------------------------------
    // Fixture 5 : Trois-portes (3 instances : x, y, z)
    //   module trois (a, b, c) x = a * b  y = b * c  z = a * c end module
    // ------------------------------------------------------------------

    private static final String SRC_TROIS =
        "module trois (a, b, c) x = a * b y = b * c z = a * c end module";

    @Test
    public void fixture_trois_parse_sans_exception() {
        assertParseOk(SRC_TROIS);
    }

    @Test
    public void fixture_trois_trois_instances() {
        CstNode root = CstParser.parse(SRC_TROIS);
        List<CstNode> instances = collectInstances(root);
        assertEquals("trois doit avoir 3 instances (x, y, z)", 3, instances.size());
    }

    @Test
    public void fixture_trois_toutes_instances_ont_identifiant() {
        CstNode root = CstParser.parse(SRC_TROIS);
        List<CstNode> instances = collectInstances(root);
        for (CstNode ins : instances) {
            CstInternal insInternal = (CstInternal) ins;
            assertTrue("Chaque Instance doit avoir un Identifiant",
                    insInternal.has(new Terminal(Token.Identifiant)));
        }
    }

    // ------------------------------------------------------------------
    // Utilitaire : parcours recursif pour tester la presence d'un token
    // ------------------------------------------------------------------

    private static boolean containsToken(CstNode node, Token expected) {
        if (node instanceof CstLeaf leaf) {
            return leaf.t().getType() == expected;
        }
        if (node instanceof CstInternal internal) {
            for (CstNode child : internal.children()) {
                if (containsToken(child, expected)) return true;
            }
        }
        return false;
    }
}
