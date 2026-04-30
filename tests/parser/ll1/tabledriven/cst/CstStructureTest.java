package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Terminal;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests de structure pour CstNode (CstLeaf et CstInternal).
 *
 * La hiérarchie CstNode est sealed : seuls CstLeaf et CstInternal sont permis.
 */
public class CstStructureTest {

    // Production réelle de la grammaire SHDL pour les tests
    private static final Production PROD = Grammar.SHDL.getProductions().get(0);

    @Test
    public void leaf_offsets_proviennent_du_token() {
        var t = new Terminal(TokenType.Identifiant);
        var token = new Token(TokenType.Identifiant, "abc", 5);
        var leaf = new CstLeaf(t, token);
        assertEquals(5, leaf.startOffset());
        assertEquals(8, leaf.endOffset()); // "abc".length() = 3, so 5+3=8
    }

    @Test
    public void leaf_value_null_endOffset_egal_offset() {
        var t = new Terminal(TokenType.LeftPar);
        var token = new Token(TokenType.LeftPar, null, 12);
        var leaf = new CstLeaf(t, token);
        assertEquals(12, leaf.startOffset());
        assertEquals(12, leaf.endOffset());
    }

    @Test
    public void internal_offsets_calcules_depuis_enfants() {
        var tid = new Terminal(TokenType.Identifiant);
        var leaf1 = new CstLeaf(tid, new Token(TokenType.Identifiant, "a", 0));
        var leaf2 = new CstLeaf(tid, new Token(TokenType.Identifiant, "bb", 4));
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(leaf1, leaf2));
        assertEquals(0, node.startOffset());
        assertEquals(6, node.endOffset()); // 4 + "bb".length() = 6
    }

    @Test
    public void internal_epsilon_offsets_egaux_au_curseur() {
        var node = CstInternal.epsilon(NonTerminal.Instance_Star, PROD, 10);
        assertEquals(10, node.startOffset());
        assertEquals(10, node.endOffset());
        assertTrue(node.children().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void internal_of_avec_children_vide_jette() {
        CstInternal.of(NonTerminal.Module, PROD, List.of());
    }

    @Test
    public void internal_children_immutable() {
        var tid = new Terminal(TokenType.Identifiant);
        var leaf = new CstLeaf(tid, new Token(TokenType.Identifiant, "x", 0));
        List<CstNode> mutableList = new ArrayList<>(List.of(leaf));
        var node = CstInternal.of(NonTerminal.Module, PROD, mutableList);
        // Modifier la liste originale ne doit pas affecter les children du noeud
        mutableList.add(leaf);
        assertEquals(1, node.children().size());
    }

    @Test
    public void null_arguments_jettent() {
        var t = new Terminal(TokenType.Identifiant);
        var token = new Token(TokenType.Identifiant, "x", 0);
        var nt = NonTerminal.Module;

        try {
            new CstLeaf(null, token);
            fail("NPE attendue pour t null");
        } catch (NullPointerException e) { /* ok */ }

        try {
            new CstLeaf(t, null);
            fail("NPE attendue pour token null");
        } catch (NullPointerException e) { /* ok */ }

        try {
            CstInternal.epsilon(null, PROD, 0);
            fail("NPE attendue pour nt null");
        } catch (NullPointerException e) { /* ok */ }

        try {
            CstInternal.epsilon(nt, null, 0);
            fail("NPE attendue pour rule null");
        } catch (NullPointerException e) { /* ok */ }
    }
}
