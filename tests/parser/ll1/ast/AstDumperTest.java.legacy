package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.AstDumper;
import parser.ll1.ast.Module;
import parser.ll1.parser.Parser;
import tests.parser.ll1.fixtures.ShdlFixtures;

import static org.junit.Assert.assertTrue;

public class AstDumperTest {

    @Test public void dumpContientNomModuleEtSignaux() {
        Module m = new Parser(ShdlFixtures.moduleET()).parse();
        String out = AstDumper.dump(m);
        assertTrue(out.contains("Module \"ET\""));
        assertTrue(out.contains("Signal \"a\""));
        assertTrue(out.contains("Signal \"b\""));
        assertTrue(out.contains("Signal \"c\""));
        assertTrue(out.contains("Assignment"));
        assertTrue(out.contains("Factor SIGNAL \"a\""));
    }

    @Test public void dumpBasculeD() {
        Module m = new Parser(ShdlFixtures.moduleBasculeD()).parse();
        String out = AstDumper.dump(m);
        assertTrue(out.contains("MemoryPoint [SET]"));
        assertTrue(out.contains("Clock"));
        assertTrue(out.contains("Factor LITERAL_1"));
    }

    @Test public void dumpDecodeurBCD() {
        Module m = new Parser(ShdlFixtures.moduleDecodeurBCD()).parse();
        String out = AstDumper.dump(m);
        assertTrue(out.contains("MapNode"));
        assertTrue(out.contains("BitField \"0000\""));
        assertTrue(out.contains("BitField \"1111\""));
    }
}
