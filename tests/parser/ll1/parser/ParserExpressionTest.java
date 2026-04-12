package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import parser.ll1.token.Token;
import java.util.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

public class ParserExpressionTest {

    @Test public void signalSimple() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"))).parseSignalForTest();
        assertEquals("a", s.getName());
        assertFalse(s.getHi().isPresent());
    }

    @Test public void signalIndexUnique() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"), tok(LBRACKET), tok(INTEGER, "3"), tok(RBRACKET)))
            .parseSignalForTest();
        assertEquals(3, (int) s.getHi().get());
        assertFalse(s.getLo().isPresent());
    }

    @Test public void signalIntervalleDotDot() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"), tok(LBRACKET), tok(INTEGER, "3"),
                                   tok(DOTDOT), tok(INTEGER, "7"), tok(RBRACKET)))
            .parseSignalForTest();
        assertEquals(3, (int) s.getHi().get());
        assertEquals(7, (int) s.getLo().get());
    }

    @Test public void signalIntervalleColon() {
        Signal s = new Parser(seq(tok(IDENTIFIER, "a"), tok(LBRACKET), tok(INTEGER, "3"),
                                   tok(COLON), tok(INTEGER, "5"), tok(RBRACKET)))
            .parseSignalForTest();
        assertEquals(5, (int) s.getLo().get());
    }

    @Test public void factorLitteral0() {
        Factor f = new Parser(seq(tok(INTEGER, "0"))).parseFactorForTest();
        assertEquals(Factor.Kind.LITERAL_0, f.getKind());
    }

    @Test public void factorLitteral1() {
        Factor f = new Parser(seq(tok(INTEGER, "1"))).parseFactorForTest();
        assertEquals(Factor.Kind.LITERAL_1, f.getKind());
    }

    @Test public void factorLitteralInvalide() {
        try {
            new Parser(seq(tok(INTEGER, "5"))).parseFactorForTest();
            fail();
        } catch (parser.ll1.parser.ParsingException e) {
            assertEquals(parser.ll1.parser.ErrorCode.BIT_OUT_OF_RANGE, e.getCode());
        }
    }

    @Test public void factorNegation() {
        Factor f = new Parser(seq(tok(SLASH), tok(IDENTIFIER, "a"))).parseFactorForTest();
        assertEquals(Factor.Kind.NEG_SIGNAL, f.getKind());
    }

    @Test public void factorBitfield() {
        Factor f = new Parser(seq(tok(BITFIELD, "1010"))).parseFactorForTest();
        assertEquals(Factor.Kind.BITFIELD, f.getKind());
        assertEquals("1010", f.getBitField().getBits());
    }

    @Test public void termAvecEtoile() {
        Term t = new Parser(seq(tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"))).parseTermForTest();
        assertEquals(2, t.getFactors().size());
    }

    @Test public void sommeEtProduit() {
        SumOfTerms s = new Parser(seq(tok(IDENTIFIER, "a"), tok(STAR), tok(IDENTIFIER, "b"),
                                      tok(PLUS), tok(IDENTIFIER, "c"))).parseSumOfTermsForTest();
        assertEquals(2, s.getTerms().size());
        assertEquals(2, s.getTerms().get(0).getFactors().size());
    }

    @Test public void parenthesage() {
        SumOfTerms s = new Parser(seq(tok(LPAREN), tok(IDENTIFIER, "a"), tok(PLUS),
                                      tok(IDENTIFIER, "b"), tok(RPAREN))).parseSumOfTermsForTest();
        assertEquals(1, s.getTerms().size());
        assertEquals(Factor.Kind.PAREN, s.getTerms().get(0).getFactors().get(0).getKind());
    }

    @Test public void signalCompoundAvecAmpersand() {
        SignalCompound sc = new Parser(seq(tok(IDENTIFIER, "a"), tok(AMPERSAND), tok(IDENTIFIER, "b")))
            .parseSignalCompoundForTest();
        assertEquals(2, sc.getSignals().size());
    }
}
