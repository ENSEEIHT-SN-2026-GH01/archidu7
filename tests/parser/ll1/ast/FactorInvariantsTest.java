package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.*;
import static org.junit.Assert.*;

public class FactorInvariantsTest {
    private static final Position P = new Position(1, 1);

    @Test(expected = IllegalArgumentException.class)
    public void signalSansSignal() {
        new Factor(P, Factor.Kind.SIGNAL, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void signalAvecBitField() {
        new Factor(P, Factor.Kind.SIGNAL, new Signal(P, "x", null, null), new BitField(P, "0"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negSignalSansSignal() {
        new Factor(P, Factor.Kind.NEG_SIGNAL, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bitfieldSansBitfield() {
        new Factor(P, Factor.Kind.BITFIELD, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bitfieldAvecSignal() {
        new Factor(P, Factor.Kind.BITFIELD, new Signal(P, "x", null, null), new BitField(P, "0"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parenSansInner() {
        new Factor(P, Factor.Kind.PAREN, null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void literal0AvecSignal() {
        new Factor(P, Factor.Kind.LITERAL_0, new Signal(P, "x", null, null), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void literal1AvecInner() {
        Signal s = new Signal(P, "x", null, null);
        Term t = new Term(P, java.util.List.of(Factor.signal(P, s)));
        SumOfTerms sot = new SumOfTerms(P, java.util.List.of(t));
        new Factor(P, Factor.Kind.LITERAL_1, null, null, sot);
    }

    @Test public void factoriesValides() {
        Signal s = new Signal(P, "x", null, null);
        assertNotNull(Factor.signal(P, s));
        assertNotNull(Factor.negSignal(P, s));
        assertNotNull(Factor.lit0(P));
        assertNotNull(Factor.lit1(P));
        assertNotNull(Factor.bits(P, new BitField(P, "0")));
        Term t = new Term(P, java.util.List.of(Factor.lit0(P)));
        SumOfTerms sot = new SumOfTerms(P, java.util.List.of(t));
        assertNotNull(Factor.paren(P, sot));
    }
}
