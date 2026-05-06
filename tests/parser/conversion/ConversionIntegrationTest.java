package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Module;

/**
 * Tests d'integration : verifie que chaque fixture de EndToEndExtendedTest
 * se convertit correctement via Conversion.convert().
 *
 * Toutes les fixtures sont S1-valides (pas de vecteur, pas de concat,
 * pas de memoire, pas d'appel de module, pas de litteraux binaires en RHS).
 *
 * Sources copiees exactement depuis EndToEndExtendedTest.java.
 */
public class ConversionIntegrationTest {

    private static Module convert(String src) {
        CstNode root = CstParser.parse(src);
        return Conversion.convert(root);
    }

    // ------------------------------------------------------------------
    // Fixture 1 : MUX 2-vers-1  — S1-valide
    //   module mux (a, b, sel) o = a * /sel + b * sel end module
    //   1 sortie scalaire : o
    // ------------------------------------------------------------------

    @Test
    public void mux2to1_isS1Valid() {
        Module m = convert("module mux (a, b, sel) o = a * /sel + b * sel end module");
        assertEquals(1, m.Plan.size());
        assertEquals("o", m.Plan.get(0).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 2 : DEMUX simple  — S1-valide
    //   module demux (a, sel) o0 = a * /sel o1 = a * sel end module
    //   2 sorties scalaires : o0, o1
    // ------------------------------------------------------------------

    @Test
    public void demux_isS1Valid() {
        Module m = convert("module demux (a, sel) o0 = a * /sel o1 = a * sel end module");
        assertEquals(2, m.Plan.size());
    }

    // ------------------------------------------------------------------
    // Fixture 3 : HalfAdder  — S1-valide
    //   module halfAdder (a, b) s = a + b c = a * b end module
    //   2 sorties scalaires : s, c
    // ------------------------------------------------------------------

    @Test
    public void halfAdder_isS1Valid() {
        Module m = convert("module halfAdder (a, b) s = a + b c = a * b end module");
        assertEquals(2, m.Plan.size());
    }

    // ------------------------------------------------------------------
    // Fixture 4 : XOR  — S1-valide
    //   module xor (a, b) o = a * /b + /a * b end module
    //   1 sortie scalaire : o
    // ------------------------------------------------------------------

    @Test
    public void xor_isS1Valid() {
        Module m = convert("module xor (a, b) o = a * /b + /a * b end module");
        assertEquals(1, m.Plan.size());
        assertEquals("o", m.Plan.get(0).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 5 : Trois-portes  — S1-valide
    //   module trois (a, b, c) x = a * b y = b * c z = a * c end module
    //   3 sorties scalaires : x, y, z
    // ------------------------------------------------------------------

    @Test
    public void troisPortes_isS1Valid() {
        Module m = convert("module trois (a, b, c) x = a * b y = b * c z = a * c end module");
        assertEquals(3, m.Plan.size());
    }
}
