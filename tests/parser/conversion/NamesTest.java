package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Names;
import parser.conversion.Names.SignalRef;
import parser.conversion.Subset;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Descripteur;

public class NamesTest {

    private static CstNode firstSignal(String src) {
        CstNode root = CstParser.parse(src);
        // Start -> Module -> ... -> Param -> Signal
        return root.first(NonTerminal.Module).orElseThrow()
                   .first(NonTerminal.Param).orElseThrow()
                   .first(NonTerminal.Signal).orElseThrow();
    }

    // -----------------------------------------------------------------------
    // Tests — signalRef + subsetOf
    // -----------------------------------------------------------------------

    @Test
    public void signalRef_scalar_nomCorrect() {
        CstNode sig = firstSignal("module m (a) c = a end module");
        SignalRef ref = Names.signalRef(sig);
        assertEquals("a", ref.nom());
    }

    @Test
    public void signalRef_scalar_subsetIsScalar() {
        CstNode sig = firstSignal("module m (a) c = a end module");
        SignalRef ref = Names.signalRef(sig);
        assertFalse(ref.subset().isVector());
        assertEquals(1, ref.subset().width());
    }

    @Test
    public void signalRef_singleIndex_nomEtSubset() {
        CstNode sig = firstSignal("module m (a[3]) c = a end module");
        SignalRef ref = Names.signalRef(sig);
        assertEquals("a", ref.nom());
        assertTrue(ref.subset().isVector());
        assertEquals(3, ref.subset().hi());
        assertEquals(3, ref.subset().lo());
        assertEquals(1, ref.subset().width());
    }

    @Test
    public void signalRef_rangeDoubleDot_nomEtSubset() {
        CstNode sig = firstSignal("module m (a[3..0]) c = a end module");
        SignalRef ref = Names.signalRef(sig);
        assertEquals("a", ref.nom());
        assertTrue(ref.subset().isVector());
        assertEquals(3, ref.subset().hi());
        assertEquals(0, ref.subset().lo());
        assertEquals(4, ref.subset().width());
    }

    @Test
    public void signalRef_rangeColon_nomEtSubset() {
        CstNode sig = firstSignal("module m (a[3:0]) c = a end module");
        SignalRef ref = Names.signalRef(sig);
        assertEquals("a", ref.nom());
        assertTrue(ref.subset().isVector());
        assertEquals(3, ref.subset().hi());
        assertEquals(0, ref.subset().lo());
        assertEquals(4, ref.subset().width());
    }

    // -----------------------------------------------------------------------
    // Tests d'erreur — branches défensives signalRef / subsetOf
    // -----------------------------------------------------------------------

    /**
     * Passer un nœud NT Param (et non Signal) à signalRef doit lever
     * ConversionException(MALFORMED_CST).
     */
    @Test
    public void signalRef_noeudNonSignal_leveConversionException() {
        CstNode root = CstParser.parse("module m (a) c = a end module");
        CstNode param = root.first(NonTerminal.Module).orElseThrow()
                            .first(NonTerminal.Param).orElseThrow();
        try {
            Names.signalRef(param);
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.MALFORMED_CST, ex.reason());
        }
    }

    /**
     * Passer un nœud NT Signal (et non Signal_Subset_Opt) à subsetOf doit lever
     * ConversionException(MALFORMED_CST).
     */
    @Test
    public void subsetOf_noeudNonSignalSubsetOpt_leveConversionException() {
        CstNode root = CstParser.parse("module m (a) c = a end module");
        CstNode signal = root.first(NonTerminal.Module).orElseThrow()
                             .first(NonTerminal.Param).orElseThrow()
                             .first(NonTerminal.Signal).orElseThrow();
        try {
            Names.subsetOf(signal);
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.MALFORMED_CST, ex.reason());
        }
    }

    @Test
    public void subsetScalar_constanteEgaleA_SCALAR() {
        Subset s = Subset.SCALAR;
        assertFalse(s.isVector());
        assertEquals(0, s.hi());
        assertEquals(0, s.lo());
        assertEquals(1, s.width());
    }

    /** Subset scalaire construit avec hi/lo non nuls : le constructeur compact normalise à 0. */
    @Test
    public void subsetScalaire_hiLoNonNuls_normaliseA0() {
        Subset s = new Subset(false, 5, 9);
        assertFalse(s.isVector());
        assertEquals(0, s.hi());
        assertEquals(0, s.lo());
        assertEquals(1, s.width());
    }

    @Test
    public void subsetSingle_width1() {
        Subset s = Subset.single(7);
        assertTrue(s.isVector());
        assertEquals(7, s.hi());
        assertEquals(7, s.lo());
        assertEquals(1, s.width());
    }

    @Test
    public void subsetRange_widthCalcule() {
        Subset s = Subset.range(7, 4);
        assertTrue(s.isVector());
        assertEquals(7, s.hi());
        assertEquals(4, s.lo());
        assertEquals(4, s.width());
    }

    // -----------------------------------------------------------------------
    // Tests — descriptorOf (Task 2)
    // -----------------------------------------------------------------------

    /**
     * Signal scalaire {@code a} → Descripteur avec Nom()="a", nbSignaux()=1,
     * unique()=true. Un scalaire est marqué par le sentinelle d'indice -1
     * (cf. erwan.Descripteur).
     */
    @Test
    public void descriptorOf_scalar_nomEtUnique() {
        CstNode sig = firstSignal("module m (a) c = a end module");
        Descripteur d = Names.descriptorOf(sig);
        assertEquals("a", d.Nom());
        assertEquals(1, d.nbSignaux());
        assertEquals(-1, d.indiceDebut());
        assertEquals(-1, d.indiceFin());
        assertTrue(d.unique());
    }

    /**
     * Signal index unique {@code a[3]} → Descripteur avec Nom()="a",
     * nbSignaux()=1, indiceDebut()=3, indiceFin()=3.
     */
    @Test
    public void descriptorOf_singleIndex_indicesCorrects() {
        CstNode sig = firstSignal("module m (a[3]) c = a end module");
        Descripteur d = Names.descriptorOf(sig);
        assertEquals("a", d.Nom());
        assertEquals(1, d.nbSignaux());
        assertEquals(3, d.indiceDebut());
        assertEquals(3, d.indiceFin());
    }

    /**
     * Signal plage {@code a[3..0]} → Descripteur avec Nom()="a",
     * nbSignaux()=4, indiceDebut()=0, indiceFin()=3.
     */
    @Test
    public void descriptorOf_range_indicesEtNbSignaux() {
        CstNode sig = firstSignal("module m (a[3..0]) c = a end module");
        Descripteur d = Names.descriptorOf(sig);
        assertEquals("a", d.Nom());
        assertEquals(4, d.nbSignaux());
        assertEquals(0, d.indiceDebut());
        assertEquals(3, d.indiceFin());
    }
}
