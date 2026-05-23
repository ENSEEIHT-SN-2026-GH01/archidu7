package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;
import static org.junit.Assert.*;

/**
 * Tests négatifs ciblant les régles FSM du parser LL(1).
 * Utilise {@code parseInstanceForTest()} pour se concentrer sur la grammaire
 * FSM sans emballer dans un module complet (ce qui diluerait le code d'erreur).
 */
public class ParserFsmErrorTest {

    /**
     * Piège documenté : dans {@code s0 -> s1 when a * -> s2}, le parser
     * consomme greedy le {@code *} comme opérateur de Term dans l'expression
     * de {@code when a}, puis voit {@code ->} où il attend un Factor.
     * Le code d'erreur résultant est UNEXPECTED_TOKEN (attendu un Factor).
     */
    @Test public void termRestGreedyPiegeDocumente() {
        try {
            new Parser(seq(
                tok(FSM), tok(ASYNCHRONOUS),
                tok(IDENTIFIER, "s0"), tok(ARROW), tok(IDENTIFIER, "s1"),
                tok(WHEN), tok(IDENTIFIER, "a"), tok(STAR),
                tok(ARROW), tok(IDENTIFIER, "s2"),
                tok(END), tok(FSM))).parseInstanceForTest();
            fail("attendu ParsingException");
        } catch (ParsingException e) {
            assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
        }
    }

    @Test public void fsmSansEnd() {
        try {
            new Parser(seq(
                tok(FSM), tok(ASYNCHRONOUS),
                tok(STAR), tok(ARROW), tok(IDENTIFIER, "s0"))).parseInstanceForTest();
            fail("attendu ParsingException");
        } catch (ParsingException e) {
            assertEquals(ErrorCode.EOF_UNEXPECTED, e.getCode());
        }
    }

    @Test public void fsmRuleSansArrow() {
        try {
            new Parser(seq(
                tok(FSM), tok(ASYNCHRONOUS),
                tok(IDENTIFIER, "s0"), tok(IDENTIFIER, "s1"),
                tok(END), tok(FSM))).parseInstanceForTest();
            fail("attendu ParsingException");
        } catch (ParsingException e) {
            assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
            assertTrue("ARROW attendu", e.getExpected().contains(ARROW));
        }
    }

    @Test public void fsmRuleArrowSansState() {
        try {
            new Parser(seq(
                tok(FSM), tok(ASYNCHRONOUS),
                tok(IDENTIFIER, "s0"), tok(ARROW),
                tok(END), tok(FSM))).parseInstanceForTest();
            fail("attendu ParsingException");
        } catch (ParsingException e) {
            assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
            assertTrue("IDENTIFIER attendu", e.getExpected().contains(IDENTIFIER));
        }
    }

    @Test public void fsmHeaderResetWhenSyncMalforme() {
        // fsm r when cond s0 -> s1 end fsm -- manque SYNCHRONOUS ON clk
        try {
            new Parser(seq(
                tok(FSM),
                tok(IDENTIFIER, "r"), tok(WHEN), tok(IDENTIFIER, "cond"),
                tok(IDENTIFIER, "s0"), tok(ARROW), tok(IDENTIFIER, "s1"),
                tok(END), tok(FSM))).parseInstanceForTest();
            fail("attendu ParsingException");
        } catch (ParsingException e) {
            assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
            assertTrue("SYNCHRONOUS attendu", e.getExpected().contains(SYNCHRONOUS));
        }
    }
}
