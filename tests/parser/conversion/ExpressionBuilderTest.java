package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

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

    @Test
    public void singleSignal_a() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a) c = a end module"));
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }

    @Test
    public void notSignal_slashA() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a) c = /a end module"));
        assertEquals(Operation.NOT, e.Op);
        assertEquals(1, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals("a", e.Entrees.get(0).Nom());
    }

    @Test
    public void andTwo_aTimesB() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = a * b end module"));
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals("b", e.Entrees.get(1).Nom());
    }

    @Test
    public void orTwo_aPlusB_opIsOR() {
        // SENTINELLE bug Erwan.OR ligne 145 (qui retournait Operation.AND)
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = a + b end module"));
        assertEquals("OR doit etre Op.OR (sentinelle bug)", Operation.OR, e.Op);
        assertEquals(2, e.Entrees.size());
    }

    @Test
    public void andThree_flat() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a * b * c end module"));
        assertEquals(Operation.AND, e.Op);
        assertEquals("AND doit etre n-aire aplati (3 operandes)", 3, e.Entrees.size());
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals("b", e.Entrees.get(1).Nom());
        assertEquals("c", e.Entrees.get(2).Nom());
    }

    @Test
    public void orThree_flat() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a + b + c end module"));
        assertEquals(Operation.OR, e.Op);
        assertEquals(3, e.Entrees.size());
    }

    @Test
    public void precedence_aPlusBTimesC() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a + b * c end module"));
        assertEquals(Operation.OR, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals("a", e.Entrees.get(0).Nom());
        assertEquals(Operation.AND, e.Entrees.get(1).Op);
        assertEquals(2, e.Entrees.get(1).Entrees.size());
    }

    @Test
    public void parens_aTimesParenBPlusC() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b, c) d = a * (b + c) end module"));
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.LITTERAL, e.Entrees.get(0).Op);
        assertEquals(Operation.OR, e.Entrees.get(1).Op);
    }

    @Test
    public void parensDegenerate_parA() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a) c = (a) end module"));
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }

    @Test
    public void notTwo_slashATimesSlashB() {
        Erwan e = ExpressionBuilder.build(rhs("module m (a, b) c = /a * /b end module"));
        assertEquals(Operation.AND, e.Op);
        assertEquals(2, e.Entrees.size());
        assertEquals(Operation.NOT, e.Entrees.get(0).Op);
        assertEquals(Operation.NOT, e.Entrees.get(1).Op);
    }

    @Test
    public void deepParens_noStackOverflow() {
        String src = "module m (a) c = ((((a)))) end module";
        Erwan e = ExpressionBuilder.build(rhs(src));
        assertEquals(Operation.LITTERAL, e.Op);
        assertEquals("a", e.Nom());
    }
}
