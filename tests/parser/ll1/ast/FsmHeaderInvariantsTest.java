package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.*;
import static org.junit.Assert.*;

public class FsmHeaderInvariantsTest {
    private static final Position P = new Position(1, 1);

    private static SumOfTerms sigSum(String name) {
        Signal s = new Signal(P, name, null, null);
        Term t = new Term(P, java.util.List.of(Factor.signal(P, s)));
        return new SumOfTerms(P, java.util.List.of(t));
    }

    @Test public void asyncValide() {
        FsmHeader h = new FsmHeader(P, FsmHeader.Kind.ASYNCHRONOUS, null, null, null);
        assertEquals(FsmHeader.Kind.ASYNCHRONOUS, h.getKind());
        assertFalse(h.getClock().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void asyncAvecClock() {
        new FsmHeader(P, FsmHeader.Kind.ASYNCHRONOUS, sigSum("clk"), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void asyncAvecResetName() {
        new FsmHeader(P, FsmHeader.Kind.ASYNCHRONOUS, null, "r", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void asyncAvecResetCondition() {
        new FsmHeader(P, FsmHeader.Kind.ASYNCHRONOUS, null, null, sigSum("r"));
    }

    @Test public void syncOnResetValide() {
        FsmHeader h = new FsmHeader(P, FsmHeader.Kind.SYNCHRONOUS_ON_RESET, sigSum("clk"), "r", sigSum("rst"));
        assertEquals(FsmHeader.Kind.SYNCHRONOUS_ON_RESET, h.getKind());
    }

    @Test(expected = IllegalArgumentException.class)
    public void syncOnResetSansClock() {
        new FsmHeader(P, FsmHeader.Kind.SYNCHRONOUS_ON_RESET, null, "r", sigSum("rst"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void syncOnResetSansResetName() {
        new FsmHeader(P, FsmHeader.Kind.SYNCHRONOUS_ON_RESET, sigSum("clk"), null, sigSum("rst"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void syncOnResetSansCondition() {
        new FsmHeader(P, FsmHeader.Kind.SYNCHRONOUS_ON_RESET, sigSum("clk"), "r", null);
    }

    @Test public void resetWhenSyncValide() {
        FsmHeader h = new FsmHeader(P, FsmHeader.Kind.RESET_WHEN_SYNC, sigSum("clk"), "r", sigSum("rst"));
        assertEquals(FsmHeader.Kind.RESET_WHEN_SYNC, h.getKind());
    }

    @Test(expected = IllegalArgumentException.class)
    public void resetWhenSyncSansClock() {
        new FsmHeader(P, FsmHeader.Kind.RESET_WHEN_SYNC, null, "r", sigSum("rst"));
    }
}
