package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Erwan;
import erwan.Module;
import erwan.Operation;

/**
 * Tests d'integration : lit chaque fixture .shdl depuis le disque
 * (tests/parser/ll1/fixtures/) et verifie la conversion via Conversion.convert().
 *
 * Toutes les fixtures sont S1-valides (pas de vecteur, pas de concat,
 * pas de memoire, pas d'appel de module, pas de litteraux binaires en RHS).
 *
 * Respect spec §7.5 et plan task 10 : lecture depuis le vrai fichier sur disque.
 */
public class ConversionIntegrationTest {

    private static final String FIXTURES_DIR = "tests/parser/ll1/fixtures";

    /**
     * Lit une fixture .shdl depuis le disque et la convertit.
     * Echoue avec un message clair si le fichier est absent.
     */
    private static Module convertFixture(String fixtureName) {
        Path path = Path.of(FIXTURES_DIR, fixtureName + ".shdl");
        String src;
        try {
            src = Files.readString(path);
        } catch (IOException e) {
            fail("Fixture introuvable sur disque : " + path.toAbsolutePath()
                 + " — " + e.getMessage());
            throw new AssertionError("unreachable");
        }
        CstNode root = CstParser.parse(src.strip());
        return Conversion.convert(root);
    }

    // ------------------------------------------------------------------
    // Fixture 1 : MUX 2-vers-1  — S1-valide
    //   module mux (a, b, sel) o = a * /sel + b * sel end module
    //   1 sortie scalaire : o
    // ------------------------------------------------------------------

    @Test
    public void mux2to1_isS1Valid() {
        Module m = convertFixture("mux");
        assertEquals("mux: attendu 1 affectation", 1, m.Plan.size());
        assertEquals("mux: signal de sortie attendu 'o'", "o", m.Plan.get(0).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 2 : DEMUX simple  — S1-valide
    //   module demux (a, sel) o0 = a * /sel o1 = a * sel end module
    //   2 sorties scalaires : o0, o1
    // ------------------------------------------------------------------

    @Test
    public void demux_isS1Valid() {
        Module m = convertFixture("demux");
        assertEquals("demux: attendu 2 affectations", 2, m.Plan.size());
        assertEquals("demux: 1er signal attendu 'o0'", "o0", m.Plan.get(0).Nom());
        assertEquals("demux: 2e signal attendu 'o1'", "o1", m.Plan.get(1).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 3 : HalfAdder  — S1-valide
    //   module halfAdder (a, b) s = a + b c = a * b end module
    //   2 sorties scalaires : s, c
    // ------------------------------------------------------------------

    @Test
    public void halfAdder_isS1Valid() {
        Module m = convertFixture("halfAdder");
        assertEquals("halfAdder: attendu 2 affectations", 2, m.Plan.size());
        assertEquals("halfAdder: 1er signal attendu 's'", "s", m.Plan.get(0).Nom());
        assertEquals("halfAdder: 2e signal attendu 'c'", "c", m.Plan.get(1).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 4 : XOR  — S1-valide
    //   module xor (a, b) o = a * /b + /a * b end module
    //   1 sortie scalaire : o
    // ------------------------------------------------------------------

    @Test
    public void xor_isS1Valid() {
        Module m = convertFixture("xor");
        assertEquals("xor: attendu 1 affectation", 1, m.Plan.size());
        assertEquals("xor: signal de sortie attendu 'o'", "o", m.Plan.get(0).Nom());
    }

    // ------------------------------------------------------------------
    // Fixture 5 : Trois-portes  — S1-valide
    //   module trois (a, b, c) x = a * b y = b * c z = a * c end module
    //   3 sorties scalaires : x, y, z
    // ------------------------------------------------------------------

    @Test
    public void troisPortes_isS1Valid() {
        Module m = convertFixture("trois");
        assertEquals("trois: attendu 3 affectations", 3, m.Plan.size());
        assertEquals("trois: 1er signal attendu 'x'", "x", m.Plan.get(0).Nom());
        assertEquals("trois: 2e signal attendu 'y'", "y", m.Plan.get(1).Nom());
        assertEquals("trois: 3e signal attendu 'z'", "z", m.Plan.get(2).Nom());
    }

    // ------------------------------------------------------------------
    // Circuit 4 bits — Task 5 : parametres vecteurs + signaux vecteurs
    //   module andBus4 (a[3..0], b[3..0]) s[3..0] = a[3..0] * b[3..0] end module
    //   ET bit a bit sur bus 4 bits : produit 4 Erwan AFFECTATION pour s[0..3].
    // ------------------------------------------------------------------

    @Test
    public void andBus4_planSize4AndNomS() {
        String src = "module andBus4 (a[3..0], b[3..0]) s[3..0] = a[3..0] * b[3..0] end module";
        CstNode root = CstParser.parse(src);
        Module m = Conversion.convert(root);
        assertEquals("andBus4: plan doit contenir 4 affectations (un par bit)", 4, m.Plan.size());
        for (int i = 0; i < 4; i++) {
            Erwan aff = m.Plan.get(i);
            assertEquals("andBus4: bit " + i + " doit s'appeler 's'", "s", aff.Nom);
            assertEquals("andBus4: bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), aff.Numero);
            // Verifier l'operateur : l'entree de l'affectation doit etre un AND (et non un OR)
            Erwan calcul = aff.Entrees.get(0);
            assertEquals("andBus4: bit " + i + " doit utiliser Op=AND", Operation.AND, calcul.Op);
            // Verifier le cablage : les deux entrees du AND sont a[i] et b[i]
            assertEquals("andBus4: AND bit " + i + " doit avoir 2 entrees", 2, calcul.Entrees.size());
            Erwan litA = calcul.Entrees.get(0);
            Erwan litB = calcul.Entrees.get(1);
            assertEquals("andBus4: 1ere entree AND bit " + i + " doit lire 'a'", "a", litA.Nom);
            assertEquals("andBus4: 1ere entree AND bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), litA.Numero);
            assertEquals("andBus4: 2e entree AND bit " + i + " doit lire 'b'", "b", litB.Nom);
            assertEquals("andBus4: 2e entree AND bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), litB.Numero);
        }
    }

    @Test
    public void orBus4_planSize4AndNomO() {
        // OU bit a bit sur bus 4 bits avec parametre vecteur
        String src = "module orBus4 (a[3..0], b[3..0]) o[3..0] = a[3..0] + b[3..0] end module";
        CstNode root = CstParser.parse(src);
        Module m = Conversion.convert(root);
        assertEquals("orBus4: plan doit contenir 4 affectations", 4, m.Plan.size());
        for (int i = 0; i < 4; i++) {
            Erwan aff = m.Plan.get(i);
            assertEquals("orBus4: bit " + i + " doit s'appeler 'o'", "o", aff.Nom);
            assertEquals("orBus4: bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), aff.Numero);
            // Verifier l'operateur : l'entree de l'affectation doit etre un OR (et non un AND)
            Erwan calcul = aff.Entrees.get(0);
            assertEquals("orBus4: bit " + i + " doit utiliser Op=OR", Operation.OR, calcul.Op);
            // Verifier le cablage : les deux entrees du OR sont a[i] et b[i]
            assertEquals("orBus4: OR bit " + i + " doit avoir 2 entrees", 2, calcul.Entrees.size());
            Erwan litA = calcul.Entrees.get(0);
            Erwan litB = calcul.Entrees.get(1);
            assertEquals("orBus4: 1ere entree OR bit " + i + " doit lire 'a'", "a", litA.Nom);
            assertEquals("orBus4: 1ere entree OR bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), litA.Numero);
            assertEquals("orBus4: 2e entree OR bit " + i + " doit lire 'b'", "b", litB.Nom);
            assertEquals("orBus4: 2e entree OR bit " + i + " doit avoir Numero=" + i,
                Integer.valueOf(i), litB.Numero);
        }
    }
}
