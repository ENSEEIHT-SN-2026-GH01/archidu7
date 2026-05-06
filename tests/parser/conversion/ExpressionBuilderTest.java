package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ExpressionBuilder;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;
import simulateur.Erwan.Operation;

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
}
