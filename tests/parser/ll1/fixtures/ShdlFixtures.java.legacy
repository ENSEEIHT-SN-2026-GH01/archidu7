package tests.parser.ll1.fixtures;

import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;

public final class ShdlFixtures {

    public static List<Token> moduleET() {
        // module ET(a, b : c) c = a * b end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "ET"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"), tok(COLON), tok(IDENTIFIER, "c"),
            tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ), tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleBasculeD() {
        // module BasculeD(d, clk : q)
        //   q := d on clk, set when 1
        // end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "BasculeD"), tok(LPAREN),
            tok(IDENTIFIER, "d"), tok(COMMA), tok(IDENTIFIER, "clk"), tok(COLON), tok(IDENTIFIER, "q"),
            tok(RPAREN),
            tok(IDENTIFIER, "q"), tok(ASSIGN), tok(IDENTIFIER, "d"),
            tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA), tok(SET), tok(WHEN), tok(INTEGER, "1"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleFsmSynchrone() {
        // module FsmSync(clk, reset : out)
        //   fsm synchronous on clk, s0 when reset
        //     s0 -> s1 when 1
        //     s1 -> s0
        //   end fsm
        //   out = 0
        // end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "FsmSync"), tok(LPAREN),
            tok(IDENTIFIER, "clk"), tok(COMMA), tok(IDENTIFIER, "reset"), tok(COLON), tok(IDENTIFIER, "out"),
            tok(RPAREN),
            tok(FSM), tok(SYNCHRONOUS), tok(ON), tok(IDENTIFIER, "clk"), tok(COMMA),
            tok(IDENTIFIER, "s0"), tok(WHEN), tok(IDENTIFIER, "reset"),
            tok(IDENTIFIER, "s0"), tok(ARROW), tok(IDENTIFIER, "s1"), tok(WHEN), tok(INTEGER, "1"),
            tok(IDENTIFIER, "s1"), tok(ARROW), tok(IDENTIFIER, "s0"),
            tok(END), tok(FSM),
            tok(IDENTIFIER, "out"), tok(EQ), tok(INTEGER, "0"),
            tok(END), tok(MODULE));
    }

    public static List<Token> moduleDecodeurBCD() {
        // module DecBCD(a : b) map a -> b "0000" -> "1111" end map end module
        return seq(
            tok(MODULE), tok(IDENTIFIER, "DecBCD"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COLON), tok(IDENTIFIER, "b"), tok(RPAREN),
            tok(MAP), tok(IDENTIFIER, "a"), tok(ARROW), tok(IDENTIFIER, "b"),
            tok(BITFIELD, "0000"), tok(ARROW), tok(BITFIELD, "1111"),
            tok(END), tok(MAP),
            tok(END), tok(MODULE));
    }
}
