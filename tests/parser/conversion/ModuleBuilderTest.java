package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ModuleBuilder;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Erwan;
import erwan.Operation;
import erwan.Module;

public class ModuleBuilderTest {

    private static Module build(String src) {
        CstNode root = CstParser.parse(src);
        CstNode mod = root.first(NonTerminal.Module).orElseThrow();
        return ModuleBuilder.build(mod);
    }

    @Test
    public void singleParam_singleInstance() {
        Module m = build("module m (a) c = a end module");
        assertEquals(1, m.Plan.size());
        Erwan aff = m.Plan.get(0);
        assertEquals(Operation.AFFECTATION, aff.Op);
        assertEquals("c", aff.Nom());
        assertTrue(m.Entrees.isEmpty());
        assertTrue(m.Sorties.isEmpty());
        assertTrue(m.Branchements.isEmpty());
    }

    @Test
    public void twoInstances_orderPreserved() {
        Module m = build("module m (a, b) c = a * b d = a + b end module");
        assertEquals(2, m.Plan.size());
        assertEquals("c", m.Plan.get(0).Nom());
        assertEquals("d", m.Plan.get(1).Nom());
    }

    @Test(expected = ConversionException.class)
    public void duplicateLhs_throws() {
        build("module m (a, b) c = a c = b end module");
    }

    @Test
    public void duplicateLhs_reasonIsDuplicate() {
        try {
            build("module m (a, b) c = a c = b end module");
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    @Test(expected = ConversionException.class)
    public void vectorParam_throws() {
        build("module m (a[0..3]) c = a end module");
    }

    @Test(expected = ConversionException.class)
    public void vectorLhs_throws() {
        build("module m (a) c[0] = a end module");
    }

    /**
     * LHS scalaire mais RHS vectoriel (a[3..0] produit un Bus de largeur 4) :
     * buildInstance doit rejeter avec VECTOR_WIDTH_MISMATCH.
     */
    @Test
    public void scalarLhs_vectorRhs_throwsWidthMismatch() {
        try {
            build("module m (a) s = a[3..0] end module");
            fail("Attendu ConversionException VECTOR_WIDTH_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
        }
    }
}
