package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.AppelModule;
import erwan.Descripteur;
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

    // ------------------------------------------------------------------
    // Test Task 6 : appel de sous-module multi-fichiers
    //   fa  : module fa  (a, b : s)    s = a + b end module
    //   top : module top (a, b : s)    $fa(a, b : s) end module
    //
    // Verifie :
    //   - top.Nom = "top" ; top.Plan vide ; top.Branchements.size() = 1
    //   - top.Entrees = [a, b] ; top.Sorties = [s]
    //   - am.module.Nom = "fa" ; am.DE.size() = 2 ; am.DS.size() = 1
    //   - sous-module : am.module.Entrees = [a, b] ; am.module.Sorties = [s]
    //   - sous-module : am.module.Plan non vide (corps de fa)
    // ------------------------------------------------------------------

    @Test
    public void subModuleCall_multiFile_wiresAppelModule() {
        String srcFa  = "module fa (a, b : s) s = a + b end module";
        String srcTop = "module top (a, b : s) $fa(a, b : s) end module";

        CstNode faCst  = CstParser.parse(srcFa);
        CstNode topCst = CstParser.parse(srcTop);

        erwan.Module m = Conversion.convert(topCst, List.of(faCst));

        // --- module top ---
        assertEquals("top: Nom attendu 'top'", "top", m.Nom);
        assertEquals("top: Plan doit etre vide (seul contenu = appel de sous-module)",
                     0, m.Plan.size());
        assertEquals("top: Branchements doit contenir exactement 1 AppelModule",
                     1, m.Branchements.size());

        // Signature de top
        assertEquals("top: Entrees doit avoir 2 descripteurs (a, b)",
                     2, m.Entrees.size());
        assertEquals("top: Entrees[0].Nom() attendu 'a'",
                     "a", m.Entrees.get(0).Nom());
        assertEquals("top: Entrees[1].Nom() attendu 'b'",
                     "b", m.Entrees.get(1).Nom());
        assertEquals("top: Sorties doit avoir 1 descripteur (s)",
                     1, m.Sorties.size());
        assertEquals("top: Sorties[0].Nom() attendu 's'",
                     "s", m.Sorties.get(0).Nom());

        // --- AppelModule ---
        AppelModule am = m.Branchements.get(0);
        assertEquals("am.module.Nom attendu 'fa'", "fa", am.module.Nom);
        assertEquals("am.DE doit contenir 2 descripteurs (signaux d'entree fournis : a, b)",
                     2, am.DE.size());
        assertEquals("am.DS doit contenir 1 descripteur (signal de sortie fourni : s)",
                     1, am.DS.size());

        // --- Interface propre du sous-module fa ---
        assertEquals("fa.Entrees doit avoir 2 descripteurs",
                     2, am.module.Entrees.size());
        assertEquals("fa.Entrees[0].Nom() attendu 'a'",
                     "a", am.module.Entrees.get(0).Nom());
        assertEquals("fa.Entrees[1].Nom() attendu 'b'",
                     "b", am.module.Entrees.get(1).Nom());
        assertEquals("fa.Sorties doit avoir 1 descripteur",
                     1, am.module.Sorties.size());
        assertEquals("fa.Sorties[0].Nom() attendu 's'",
                     "s", am.module.Sorties.get(0).Nom());

        // Corps de fa non vide (contient l'affectation s = a + b)
        assertFalse("fa.Plan ne doit pas etre vide (corps de fa)", am.module.Plan.isEmpty());
    }

    /**
     * Limitation assumee du design (cf. spec, "Limitations assumees") : l'IR
     * (LITTERANGE/ARANGE) n'indexe qu'en ordre croissant, donc une plage
     * descendante [3..0] et une plage ascendante [0..3] produisent le MEME
     * Module. Ce test fige ce contrat : si un coequipier rend LITTERANGE
     * sensible a l'ordre, ce test signalera la rupture.
     */
    @Test
    public void invertedRange_descendingAndAscending_produceIdenticalModule() {
        String desc = "module m (a[3..0], b[3..0]) s[3..0] = a[3..0] + b[3..0] end module";
        String asc  = "module m (a[0..3], b[0..3]) s[0..3] = a[0..3] + b[0..3] end module";
        Module mDesc = Conversion.convert(CstParser.parse(desc));
        Module mAsc  = Conversion.convert(CstParser.parse(asc));
        assertEquals("plans de meme taille", mDesc.Plan.size(), mAsc.Plan.size());
        for (int i = 0; i < mDesc.Plan.size(); i++) {
            Erwan affDesc = mDesc.Plan.get(i);
            Erwan affAsc  = mAsc.Plan.get(i);
            assertEquals("bit " + i + " : meme Nom", affDesc.Nom, affAsc.Nom);
            assertEquals("bit " + i + " : meme Numero", affDesc.Numero, affAsc.Numero);
            assertEquals("bit " + i + " : meme Op du calcul",
                affDesc.Entrees.get(0).Op, affAsc.Entrees.get(0).Op);
        }
    }
}
