package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.Module;
import parser.ll1.ast.Instance;
import parser.ll1.ast.Assignment;
import parser.ll1.ast.MemoryPoint;
import parser.ll1.ast.MapNode;

import static org.junit.Assert.*;

public class ExemplesAstTest {

    @Test public void etEstUnModuleAvecUnAssignment() {
        Module m = ExemplesAst.et();
        assertEquals("ET", m.getName());
        assertEquals(3, m.getParams().size());
        assertEquals(1, m.getInstances().size());
        assertTrue(m.getInstances().get(0) instanceof Assignment);
    }

    @Test public void ounAUneSommeDeDeuxTermes() {
        Module m = ExemplesAst.oun();
        Assignment a = (Assignment) m.getInstances().get(0);
        assertEquals(2, a.getExprCompound().get(0).getTerms().size());
    }

    @Test public void muxAQuatreParamsEtUnAssignment() {
        Module m = ExemplesAst.mux();
        assertEquals(4, m.getParams().size());
        assertEquals(1, m.getInstances().size());
    }

    @Test public void deuxSortiesADeuxAssignments() {
        Module m = ExemplesAst.deuxSorties();
        assertEquals(2, m.getInstances().size());
    }

    @Test public void basculeDContientMemoryPoint() {
        Module m = ExemplesAst.basculeD();
        Instance i = m.getInstances().get(0);
        assertTrue(i instanceof MemoryPoint);
    }

    @Test public void decBCDContientMapNode() {
        Module m = ExemplesAst.decBCD();
        Instance i = m.getInstances().get(0);
        assertTrue(i instanceof MapNode);
    }
}
