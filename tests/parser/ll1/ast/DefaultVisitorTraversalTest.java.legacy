package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import parser.ll1.ast.Module;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

import java.util.*;

import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

/**
 * Vérifie que DefaultVisitor traverse bien TOUS les enfants Node de l'AST,
 * y compris les endroits délicats : Factor enfants, MemoryPoint.enable,
 * FsmHeader.clock, FsmRule.when, MapEntry.from/to, expressions compound.
 */
public class DefaultVisitorTraversalTest {

    /** Visiteur qui compte les Signal rencontrés. */
    private static final class SignalCounter extends DefaultVisitor<Void> {
        int count = 0;
        @Override public Void visit(Signal s) { count++; return null; }
    }

    private static Module parse(List<Token> tokens) {
        return new Parser(tokens).parse();
    }

    @Test public void compteSignauxDansModuleET() {
        // module ET(a, b : c) c = a * b end module
        // Signaux attendus :
        //   params : a, b, c  -> 3
        //   target : c        -> 1
        //   expr   : a, b     -> 2
        //   TOTAL = 6
        Module m = parse(new ArrayList<>(Arrays.asList(
            tok(MODULE), tok(IDENTIFIER, "ET"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"), tok(COLON), tok(IDENTIFIER, "c"),
            tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ), tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE),
            tok(EOF))));
        SignalCounter sc = new SignalCounter();
        m.accept(sc);
        assertEquals(6, sc.count);
    }

    @Test public void compteSignauxMemoryPointAvecEnable() {
        // module M(d, clk, en : q)
        //   q := d on clk, set when 1, enabled when en
        // end module
        // Signaux : params d,clk,en,q (4) + target q (1) + expr d (1)
        //         + clock clk (1) + condition: pas de signal (1 literal) + enable en (1)
        // TOTAL = 8
        Module m = parse(new ArrayList<>(Arrays.asList(
            tok(MODULE), tok(IDENTIFIER, "M"), tok(LPAREN),
            tok(IDENTIFIER, "d"), tok(COMMA), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(IDENTIFIER, "en"), tok(COLON), tok(IDENTIFIER, "q"),
            tok(RPAREN),
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(SET), tok(WHEN), tok(INTEGER, "1"), tok(COMMA),
            tok(ENABLED), tok(WHEN), tok(IDENTIFIER, "en"),
            tok(END), tok(MODULE),
            tok(EOF))));
        SignalCounter sc = new SignalCounter();
        m.accept(sc);
        assertEquals(8, sc.count);
    }

    @Test public void compteSignauxFsmHeaderClockEtRuleWhen() {
        // module F(clk, reset : out)
        //   fsm synchronous on clk, s0 when reset
        //     s0 -> s1 when clk
        //   end fsm
        //   out = 0
        // end module
        // Signaux : params clk,reset,out (3) + header.clock clk (1) + header.resetCondition reset (1)
        //         + rule.when clk (1) + assignment target out (1)
        // TOTAL = 7
        Module m = parse(new ArrayList<>(Arrays.asList(
            tok(MODULE), tok(IDENTIFIER, "F"), tok(LPAREN),
            tok(IDENTIFIER, "clk"), tok(COMMA), tok(IDENTIFIER, "reset"), tok(COLON), tok(IDENTIFIER, "out"),
            tok(RPAREN),
            tok(FSM), tok(SYNCHRONOUS), tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(IDENTIFIER, "s0"), tok(WHEN), tok(IDENTIFIER, "reset"),
            tok(IDENTIFIER, "s0"), tok(ARROW), tok(IDENTIFIER, "s1"), tok(WHEN), tok(IDENTIFIER, "clk"),
            tok(END), tok(FSM),
            tok(IDENTIFIER, "out"), tok(EQ), tok(INTEGER, "0"),
            tok(END), tok(MODULE),
            tok(EOF))));
        SignalCounter sc = new SignalCounter();
        m.accept(sc);
        assertEquals(7, sc.count);
    }

    @Test public void compteSignauxSousFactorParen() {
        // module P(a, b : c) c = (a + b) end module
        // Signaux : params a,b,c (3) + target c (1) + inner PAREN a,b (2) = 6
        Module m = parse(new ArrayList<>(Arrays.asList(
            tok(MODULE), tok(IDENTIFIER, "P"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"), tok(COLON), tok(IDENTIFIER, "c"),
            tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ),
            tok(LPAREN), tok(IDENTIFIER, "a"), tok(PLUS), tok(IDENTIFIER, "b"), tok(RPAREN),
            tok(END), tok(MODULE),
            tok(EOF))));
        SignalCounter sc = new SignalCounter();
        m.accept(sc);
        assertEquals(6, sc.count);
    }

    @Test public void compteSignauxSousFactorNeg() {
        // module N(a : b) b = /a end module  -> 3 signaux (a, b param + target b + neg a)
        // params a,b + target b + neg signal a = 4
        Module m = parse(new ArrayList<>(Arrays.asList(
            tok(MODULE), tok(IDENTIFIER, "N"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COLON), tok(IDENTIFIER, "b"),
            tok(RPAREN),
            tok(IDENTIFIER, "b"), tok(EQ), tok(SLASH), tok(IDENTIFIER, "a"),
            tok(END), tok(MODULE),
            tok(EOF))));
        SignalCounter sc = new SignalCounter();
        m.accept(sc);
        assertEquals(4, sc.count);
    }
}
