package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import parser.conversion.Bus;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.conversion.ExpressionBuilder;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Erwan;
import erwan.Operation;

public class ExpressionBuilderTest {

    /** Extrait le SumOfTermsCompound RHS de la 1re Instance d'un module. */
    private static CstNode rhs(String moduleSrc) {
        CstNode root = CstParser.parse(moduleSrc);
        CstNode module = root.first(NonTerminal.Module).orElseThrow();
        CstNode instancePlus = module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstNode instance = instancePlus.first(NonTerminal.Instance).orElseThrow();
        CstNode op = instance.first(NonTerminal.Operation).orElseThrow();
        CstNode assignment = op.first(NonTerminal.Assignment).orElseThrow();
        CstNode sigA = assignment.first(NonTerminal.SignalAssignment).orElseThrow();
        return sigA.first(NonTerminal.SumOfTermsCompound).orElseThrow();
    }

    /** Raccourci pour les cas scalaires : extrait le seul bit du Bus. */
    private static Erwan scalar(String moduleSrc) {
        Bus bus = ExpressionBuilder.build(rhs(moduleSrc));
        assertEquals("Attendu un Bus scalaire (width==1)", 1, bus.width());
        return bus.bits().get(0);
    }

    // ------------------------------------------------------------------
    // Cas scalaires existants (largeur 1)
    // ------------------------------------------------------------------

    @Test
    public void singleSignal_a() {
        Erwan e = scalar("module m (a) c = a end module");
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }

    @Test
    public void notSignal_slashA() {
        Erwan e = scalar("module m (a) c = /a end module");
        assertEquals(Operation.NOT, e.Op);
        assertEquals(1, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals("a", e.Entrees.get(0).Nom());
    }

    @Test
    public void andTwo_aTimesB() {
        Erwan e = scalar("module m (a, b) c = a * b end module");
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals("b", e.Entrees.get(1).Nom());
    }

    @Test
    public void orTwo_aPlusB_opIsOR() {
        // SENTINELLE bug Erwan.OR ligne 145 (qui retournait Operation.AND)
        Erwan e = scalar("module m (a, b) c = a + b end module");
        assertEquals("OR doit etre Op.OR (sentinelle bug)", Operation.OR, e.Op);
        assertEquals(2, e.Entrees.size());
    }

    @Test
    public void andThree_flat() {
        Erwan e = scalar("module m (a, b, c) d = a * b * c end module");
        assertEquals(Operation.AND, e.Op);
        assertEquals("AND doit etre n-aire aplati (3 operandes)", 3, e.Entrees.size());
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals("b", e.Entrees.get(1).Nom());
        assertEquals("c", e.Entrees.get(2).Nom());
    }

    @Test
    public void orThree_flat() {
        Erwan e = scalar("module m (a, b, c) d = a + b + c end module");
        assertEquals(Operation.OR, e.Op);
        assertEquals(3, e.Entrees.size());
    }

    @Test
    public void precedence_aPlusBTimesC() {
        Erwan e = scalar("module m (a, b, c) d = a + b * c end module");
        assertEquals(Operation.OR, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals(Operation.AND, e.Entrees.get(1).Op);
        assertEquals(2, e.Entrees.get(1).Entrees.size());
    }

    @Test
    public void parens_aTimesParenBPlusC() {
        Erwan e = scalar("module m (a, b, c) d = a * (b + c) end module");
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals(Operation.OR, e.Entrees.get(1).Op);
    }

    @Test
    public void parensDegenerate_parA() {
        Erwan e = scalar("module m (a) c = (a) end module");
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }

    @Test
    public void notTwo_slashATimesSlashB() {
        Erwan e = scalar("module m (a, b) c = /a * /b end module");
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.NOT, e.Entrees.get(0).Op);
        assertEquals(Operation.NOT, e.Entrees.get(1).Op);
    }

    @Test
    public void deepParens_noStackOverflow() {
        String src = "module m (a) c = ((((a)))) end module";
        Erwan e = scalar(src);
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }

    // ------------------------------------------------------------------
    // Cas vectoriels nouveaux
    // ------------------------------------------------------------------

    @Test
    public void vectorRange_a3to0_width4() {
        Bus bus = ExpressionBuilder.build(rhs("module m (a[3..0]) c[3..0] = a[3..0] end module"));
        assertEquals("a[3..0] doit produire un Bus de largeur 4", 4, bus.width());
        // Chaque bit doit etre LITTERAL avec le bon numero
        // LITTERANGE boucle for i=debut; i<=fin, donc apres normalisation (lo=0, hi=3)
        // bus.bits().get(0).Numero == 0, get(1).Numero == 1, etc.
        for (int i = 0; i < 4; i++) {
            Erwan bit = bus.bits().get(i);
            assertEquals(Operation.LITTERAL, bit.Op);
            assertEquals("Numero du bit " + i, Integer.valueOf(i), bit.Numero);
        }
    }

    @Test
    public void notVectorRange_slashA3to0_width4() {
        Bus bus = ExpressionBuilder.build(rhs("module m (a[3..0]) c[3..0] = /a[3..0] end module"));
        assertEquals("NOTR de a[3..0] doit produire un Bus de largeur 4", 4, bus.width());
        for (Erwan bit : bus.bits()) {
            assertEquals("Chaque bit doit etre NOT", Operation.NOT, bit.Op);
        }
    }

    @Test
    public void andVectorRange_a3to0_times_b3to0() {
        Bus bus = ExpressionBuilder.build(rhs("module m (a[3..0], b[3..0]) c[3..0] = a[3..0] * b[3..0] end module"));
        assertEquals("ANDR de a[3..0]*b[3..0] doit produire un Bus de largeur 4", 4, bus.width());
        for (Erwan bit : bus.bits()) {
            assertEquals("Chaque bit doit etre AND", Operation.AND, bit.Op);
        }
    }

    @Test
    public void orVectorRange_a3to0_plus_b3to0() {
        Bus bus = ExpressionBuilder.build(rhs("module m (a[3..0], b[3..0]) c[3..0] = a[3..0] + b[3..0] end module"));
        assertEquals("ORR de a[3..0]+b[3..0] doit produire un Bus de largeur 4", 4, bus.width());
        for (Erwan bit : bus.bits()) {
            assertEquals("Chaque bit doit etre OR", Operation.OR, bit.Op);
        }
    }

    @Test
    public void widthMismatch_a3to0_plus_b1to0_throws() {
        try {
            ExpressionBuilder.build(rhs("module m (a[3..0], b[1..0]) c[3..0] = a[3..0] + b[1..0] end module"));
            fail("Attendu ConversionException VECTOR_WIDTH_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
        }
    }
}
