package tests.parser.ll1.tabledriven.cst;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Lexem;
import parser.lexer.Token;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;

import java.util.List;

/**
 * Tests de navigation (first / allOf / has) sur CstNode.
 *
 * La hierarchie CstNode est sealed : seuls CstLeaf et CstInternal sont permis.
 */
public class CstNavigationTest {

    private static final Production PROD = Grammar.SHDL.getProductions().get(0);

    /** Helper : cree un CstLeaf avec le bon Lexem<Token>. */
    private CstLeaf leaf(Token token, String text, int offset) {
        Lexem<Token> lex = new Lexem<>(token);
        lex.storeMatched(offset, text);
        return new CstLeaf(new Terminal(token), lex);
    }

    @Test
    public void first_terminal_present_retourne_match() {
        var l1 = leaf(Token.Identifiant, "a", 0);
        var l2 = leaf(Token.LeftPar, "(", 2);
        var l3 = leaf(Token.Identifiant, "b", 4);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1, l2, l3));
        var result = node.first(new Terminal(Token.LeftPar));
        assertTrue(result.isPresent());
        assertSame(l2, result.get());
    }

    @Test
    public void first_absent_retourne_empty() {
        var l1 = leaf(Token.Identifiant, "a", 0);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1));
        var result = node.first(new Terminal(Token.RightPar));
        assertFalse(result.isPresent());
    }

    @Test
    public void first_retourne_le_premier_si_plusieurs_matches() {
        var l1 = leaf(Token.Identifiant, "x", 0);
        var l2 = leaf(Token.Identifiant, "y", 5);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1, l2));
        var result = node.first(new Terminal(Token.Identifiant));
        assertTrue(result.isPresent());
        assertEquals(0, result.get().startOffset());
    }

    @Test
    public void allOf_retourne_tous_dans_lordre() {
        var l1 = leaf(Token.Identifiant, "a", 0);
        var l2 = leaf(Token.LeftPar, "(", 2);
        var l3 = leaf(Token.Identifiant, "b", 5);
        var l4 = leaf(Token.Identifiant, "c", 10);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1, l2, l3, l4));
        var result = node.allOf(new Terminal(Token.Identifiant));
        assertEquals(3, result.size());
        assertEquals(0,  result.get(0).startOffset());
        assertEquals(5,  result.get(1).startOffset());
        assertEquals(10, result.get(2).startOffset());
    }

    @Test
    public void allOf_aucun_match_renvoie_liste_vide() {
        var l1 = leaf(Token.Identifiant, "a", 0);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1));
        var result = node.allOf(new Terminal(Token.RightPar));
        assertTrue(result.isEmpty());
    }

    @Test
    public void has_terminal_oui_non() {
        var l1 = leaf(Token.Identifiant, "a", 0);
        var l2 = leaf(Token.LeftPar, "(", 2);
        var node = CstInternal.of(NonTerminal.Module, PROD, List.of(l1, l2));
        assertTrue(node.has(new Terminal(Token.Identifiant)));
        assertFalse(node.has(new Terminal(Token.RightPar)));
    }

    @Test
    public void first_avec_NonTerminal() {
        var leafNode = leaf(Token.Identifiant, "x", 0);
        var inner = CstInternal.of(NonTerminal.Module, PROD, List.of(leafNode));
        var leaf2 = leaf(Token.Identifiant, "y", 3);
        var outer = CstInternal.of(NonTerminal.Start, PROD, List.of(inner, leaf2));
        var result = outer.first(NonTerminal.Module);
        assertTrue(result.isPresent());
        assertSame(inner, result.get());
    }

    @Test
    public void leaf_first_toujours_empty_independamment_du_symbol() {
        var l = leaf(Token.Identifiant, "x", 0);
        assertFalse(l.first(new Terminal(Token.Identifiant)).isPresent());
        assertFalse(l.first(NonTerminal.Module).isPresent());
        assertTrue(l.allOf(new Terminal(Token.Identifiant)).isEmpty());
        assertFalse(l.has(new Terminal(Token.Identifiant)));
        assertFalse(l.has(NonTerminal.Module));
    }
}
