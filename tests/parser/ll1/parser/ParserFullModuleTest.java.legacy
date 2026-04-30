package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.Parser;
import parser.ll1.ast.*;
import parser.ll1.ast.Module;
import static tests.parser.ll1.fixtures.ShdlFixtures.*;
import static org.junit.Assert.*;

public class ParserFullModuleTest {
    @Test public void et() {
        Module m = new Parser(moduleET()).parse();
        assertEquals("ET", m.getName());
        assertTrue(m.getInstances().get(0) instanceof Assignment);
    }

    @Test public void basculeD() {
        Module m = new Parser(moduleBasculeD()).parse();
        assertTrue(m.getInstances().get(0) instanceof MemoryPoint);
    }

    @Test public void fsmSynchrone() {
        Module m = new Parser(moduleFsmSynchrone()).parse();
        assertEquals(2, m.getInstances().size());
        assertTrue(m.getInstances().get(0) instanceof Fsm);
        assertEquals(FsmHeader.Kind.SYNCHRONOUS_ON_RESET, ((Fsm) m.getInstances().get(0)).getHeader().getKind());
    }

    @Test public void decodeurBCD() {
        Module m = new Parser(moduleDecodeurBCD()).parse();
        assertTrue(m.getInstances().get(0) instanceof MapNode);
    }
}
