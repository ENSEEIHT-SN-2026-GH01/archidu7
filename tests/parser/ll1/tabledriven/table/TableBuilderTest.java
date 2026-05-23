package tests.parser.ll1.tabledriven.table;

import org.junit.Test;
import parser.lexer.Token;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.tabledriven.table.TableBuilder;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class TableBuilderTest {

    @Test
    public void shdl_construit_sans_conflit() {
        ParsingTable t = TableBuilder.build(Grammar.SHDL);
        assertNotNull(t);
    }

    @Test
    public void shdl_module_sur_moduleKW_present() {
        ParsingTable t = TableBuilder.build(Grammar.SHDL);
        assertTrue(t.lookup(NonTerminal.Module, Token.ModuleKW).isPresent());
    }

    @Test
    public void shdl_factor_sur_4_terminaux() {
        // Factor a 4 alternatives : LeftPar, BitField (via LiteralValue), NotOp, Identifiant (via Signal)
        ParsingTable t = TableBuilder.build(Grammar.SHDL);
        assertTrue(t.lookup(NonTerminal.Factor, Token.LeftPar).isPresent());
        assertTrue(t.lookup(NonTerminal.Factor, Token.BitField).isPresent());
        assertTrue(t.lookup(NonTerminal.Factor, Token.NotOp).isPresent());
        assertTrue(t.lookup(NonTerminal.Factor, Token.Identifiant).isPresent());
    }

    @Test
    public void shdl_instance_star_sur_endKW_via_follow() {
        // Instance_Star → ε ; FOLLOW(Instance_Star) doit contenir EndKW
        ParsingTable t = TableBuilder.build(Grammar.SHDL);
        Optional<Production> p = t.lookup(NonTerminal.Instance_Star, Token.EndKW);
        assertTrue("Instance_Star sur EndKW (via eps FOLLOW) doit etre present", p.isPresent());
        assertTrue("La production recuperee doit etre l'eps", p.get().isEpsilon());
    }

    @Test
    public void grammaire_non_LL1_simple_S_a_a_leve() {
        // S → a | a : conflit FIRST/FIRST
        Production p1b = new Production(NonTerminal.Start, List.of(new Terminal(Token.Identifiant)));
        Production p2b = new Production(NonTerminal.Start, List.of(new Terminal(Token.Identifiant), new Terminal(Token.LeftPar)));
        Grammar g = new Grammar(NonTerminal.Start, List.of(p1b, p2b));
        // FIRST(p1b) = {Identifiant}, FIRST(p2b) = {Identifiant} → conflit
        try {
            TableBuilder.build(g);
            fail("IllegalStateException attendue pour conflit FIRST/FIRST");
        } catch (IllegalStateException e) {
            // OK
        }
    }

    @Test
    public void mini_grammaire_S_a_b() {
        // S → a | b : table = {(S, a) → P1, (S, b) → P2}
        Production pa = new Production(NonTerminal.Start, List.of(new Terminal(Token.Identifiant)));
        Production pb = new Production(NonTerminal.Start, List.of(new Terminal(Token.NaturalInteger)));
        Grammar g = new Grammar(NonTerminal.Start, List.of(pa, pb));
        ParsingTable t = TableBuilder.build(g);
        assertEquals(Optional.of(pa), t.lookup(NonTerminal.Start, Token.Identifiant));
        assertEquals(Optional.of(pb), t.lookup(NonTerminal.Start, Token.NaturalInteger));
        assertEquals(Optional.empty(), t.lookup(NonTerminal.Start, Token.LeftPar));
    }
}
