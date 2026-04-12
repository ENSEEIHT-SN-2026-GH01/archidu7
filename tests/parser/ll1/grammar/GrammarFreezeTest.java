package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import static org.junit.Assert.*;

public class GrammarFreezeTest {
    @Test public void axiomeEstModule() {
        assertEquals(NonTerminal.MODULE, Grammar.SHDL.getAxiom());
    }

    @Test public void grammaireContientToutesLesProductionsPrincipales() {
        for (NonTerminal nt : NonTerminal.values()) {
            assertFalse("aucune production pour " + nt,
                Grammar.SHDL.productionsOf(nt).isEmpty());
        }
    }

    @Test public void hashStableCommeGarantieAntiRegression() {
        int h = 0;
        for (Production p : Grammar.SHDL.getProductions()) h = h * 31 + p.toString().hashCode();
        System.out.println("Grammar.SHDL hash = " + h);
        assertEquals(75804105, h);
    }
}
