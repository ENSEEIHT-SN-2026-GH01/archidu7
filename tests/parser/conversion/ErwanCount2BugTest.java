package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Module;
import simulateur.Etat;
import simulateur.FileSimulateur;

/**
 * Reproducteur pour le stack overflow signalé par Erwan le 2026-05-20 :
 * cliquer un bouton sur un circuit qui combine sous-module {@code not} +
 * affectation memoire {@code :=} fait exploser la pile.
 */
public class ErwanCount2BugTest {

    @Test(timeout = 5000)
    public void count2_clickRst_noStackOverflow() {
        String srcNot = "module not (a : b)"
                      + " b = /a"
                      + " end module";
        String srcCount2 = "module count2 (rst, clk : D[1..0], c[1..0])"
                         + " not(c[0] : D[0])"
                         + " D[1] = (c[0] * /c[1] + c[1] * /c[0])"
                         + " c[1..0] := D[1..0] on clk reset when rst"
                         + " end module";

        CstNode cstNot = CstParser.parse(srcNot);
        CstNode cstTop = CstParser.parse(srcCount2);
        Module top = Conversion.convert(cstTop, java.util.List.of(cstNot));
        FileSimulateur fs = new FileSimulateur(top);

        int rst = -1, clk = -1;
        for (int i = 1; i <= fs.nbEntree(); i++) {
            if ("rst".equals(fs.nomEntree(i))) rst = i;
            if ("clk".equals(fs.nomEntree(i))) clk = i;
        }
        assertTrue("rst trouvé", rst > 0);
        assertTrue("clk trouvé", clk > 0);

        // Simule le clic du bouton 'rst' : passage à UP.
        fs.getEntrees(rst, 1).set(Etat.UP);
    }
}
