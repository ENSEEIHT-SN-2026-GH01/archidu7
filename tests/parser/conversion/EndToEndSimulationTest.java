package tests.parser.conversion;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Etat;
import simulateur.FileSimulateur;
import simulateur.Module;

/**
 * Test e2e (finding I9) : pipeline complet String SHDL → FileSimulateur → table de vérité.
 *
 * Vérifie les 4 lignes de la table de vérité du XOR :
 *   (a=0, b=0) → o=0
 *   (a=0, b=1) → o=1
 *   (a=1, b=0) → o=1
 *   (a=1, b=1) → o=0
 *
 * Convention Etat : UP = 1 logique, DW = 0 logique.
 */
public class EndToEndSimulationTest {

    /** Chemin vers la fixture xor.shdl, relatif à la racine du projet. */
    private static final String XOR_FIXTURE =
            "tests/parser/ll1/fixtures/xor.shdl";

    /** Chemin vers la fixture halfAdder.shdl. */
    private static final String HALF_ADDER_FIXTURE =
            "tests/parser/ll1/fixtures/halfAdder.shdl";

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /** Charge et parse un fichier SHDL, retourne le FileSimulateur construit. */
    private static FileSimulateur buildSimulateur(String fixturePath) throws Exception {
        String src = new String(Files.readAllBytes(Paths.get(fixturePath)));
        CstNode root = CstParser.parse(src);
        Module module = Conversion.convert(root);
        return new FileSimulateur(module.Plan);
    }

    /**
     * Trouve l'index 1-based d'une entrée par son nom.
     * Lève AssertionError si introuvable.
     */
    private static int indexEntree(FileSimulateur fs, String nom) {
        for (int i = 1; i <= fs.nbEntree(); i++) {
            if (nom.equals(fs.nomEntree(i))) return i;
        }
        fail("Entrée introuvable dans le simulateur : '" + nom + "'");
        return -1; // unreachable
    }

    /**
     * Trouve l'index 1-based d'une sortie par son nom.
     * Lève AssertionError si introuvable.
     */
    private static int indexSortie(FileSimulateur fs, String nom) {
        for (int i = 1; i <= fs.nbSorties(); i++) {
            if (nom.equals(fs.nomSortie(i))) return i;
        }
        fail("Sortie introuvable dans le simulateur : '" + nom + "'");
        return -1; // unreachable
    }

    /** Convertit un int logique (0 ou 1) en Etat simulateur. */
    private static Etat toEtat(int v) {
        return (v == 1) ? Etat.UP : Etat.DW;
    }

    /** Convertit un Etat simulateur en int logique (UP→1, DW→0, ND→-1). */
    private static int fromEtat(Etat e) {
        if (e == Etat.UP) return 1;
        if (e == Etat.DW) return 0;
        return -1; // ND
    }

    // -----------------------------------------------------------------
    // Test XOR — table de vérité complète
    // -----------------------------------------------------------------

    @Test
    public void xor_tableauVerite_4lignes() throws Exception {
        FileSimulateur fs = buildSimulateur(XOR_FIXTURE);

        // Vérification structure minimale
        assertEquals("xor doit avoir 2 entrées", 2, fs.nbEntree());
        assertEquals("xor doit avoir 1 sortie",  1, fs.nbSorties());

        int idxA = indexEntree(fs, "a");
        int idxB = indexEntree(fs, "b");
        int idxO = indexSortie(fs, "o");

        // Table de vérité XOR : (a, b) → o
        int[][] cases = {
            {0, 0, 0},
            {0, 1, 1},
            {1, 0, 1},
            {1, 1, 0},
        };

        for (int[] row : cases) {
            int va = row[0], vb = row[1], expected = row[2];

            // Positionner les entrées
            fs.getEntrees(idxA, 1).set(toEtat(va));
            fs.getEntrees(idxB, 1).set(toEtat(vb));

            // Lire la sortie
            Etat sortie = fs.getSorties(idxO, 1).getValeur();
            int actual = fromEtat(sortie);

            assertEquals(
                String.format("XOR(a=%d, b=%d) attendu=%d, obtenu=%s", va, vb, expected, sortie),
                expected, actual
            );
        }
    }

    // -----------------------------------------------------------------
    // Test halfAdder — deux sorties : s (or) et c (and)
    // Note : la fixture halfAdder.shdl utilise s = a + b (OR, pas XOR)
    //        et c = a * b (AND). Ce test vérifie ce comportement réel.
    // -----------------------------------------------------------------

    @Test
    public void halfAdder_tableauVerite_4lignes() throws Exception {
        FileSimulateur fs = buildSimulateur(HALF_ADDER_FIXTURE);

        // Vérification structure minimale
        assertEquals("halfAdder doit avoir 2 entrées", 2, fs.nbEntree());
        assertEquals("halfAdder doit avoir 2 sorties", 2, fs.nbSorties());

        int idxA = indexEntree(fs, "a");
        int idxB = indexEntree(fs, "b");
        int idxS = indexSortie(fs, "s");
        int idxC = indexSortie(fs, "c");

        // Table de vérité : s = OR(a,b), c = AND(a,b)
        // (la fixture utilise s = a + b qui est OR en SHDL, pas XOR)
        int[][] cases = {
            {0, 0, 0, 0},
            {0, 1, 1, 0},
            {1, 0, 1, 0},
            {1, 1, 1, 1},
        };

        for (int[] row : cases) {
            int va = row[0], vb = row[1], expS = row[2], expC = row[3];

            fs.getEntrees(idxA, 1).set(toEtat(va));
            fs.getEntrees(idxB, 1).set(toEtat(vb));

            int actualS = fromEtat(fs.getSorties(idxS, 1).getValeur());
            int actualC = fromEtat(fs.getSorties(idxC, 1).getValeur());

            assertEquals(
                String.format("halfAdder s: a=%d b=%d attendu=%d", va, vb, expS),
                expS, actualS
            );
            assertEquals(
                String.format("halfAdder c: a=%d b=%d attendu=%d", va, vb, expC),
                expC, actualC
            );
        }
    }
}
