package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.FileSimulateur;
import simulateur.Module;

public class ConversionSmokeTest {

    @Test
    public void parseConvertSimulate_etGate() {
        String src = "module et (a, b) c = a * b end module";
        CstNode root = CstParser.parse(src);
        Module module = Conversion.convert(root);

        assertEquals(1, module.Plan.size());
        assertEquals("c", module.Plan.get(0).Nom());

        // Le constructeur ne doit pas planter
        FileSimulateur fs = new FileSimulateur(module.Plan);
        assertNotNull(fs);
    }
}
