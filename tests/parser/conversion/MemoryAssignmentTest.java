package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Etat;
import simulateur.FileSimulateur;
import erwan.Module;

/**
 * Tests e2e de l'affectation memoire := : pipeline String SHDL ->
 * FileSimulateur. Voir docs/specs/2026-05-18-conversion-sequentiel-design.md.
 */
public class MemoryAssignmentTest {

    // --- Harnais : resolution des E/S par nom ---

    private static FileSimulateur build(String src) throws Exception {
        CstNode cst = CstParser.parse(src);
        Module m = Conversion.convert(cst);
        // FileSimulateur(Module) expose correctement les E/S declares dans la
        // signature SHDL, y compris les sorties qui sont aussi lues en RHS
        // (ex: toggle Q := /Q). FileSimulateur(m.Plan) ne peut pas distinguer
        // les sorties des signaux internes dans ce cas.
        return new FileSimulateur(m);
    }

    private static int idxE(FileSimulateur fs, String n) {
        for (int i = 1; i <= fs.nbEntree(); i++) {
            if (n.equals(fs.nomEntree(i))) return i;
        }
        fail("entree introuvable : " + n);
        return -1;
    }

    private static int idxS(FileSimulateur fs, String n) {
        for (int i = 1; i <= fs.nbSorties(); i++) {
            if (n.equals(fs.nomSortie(i))) return i;
        }
        fail("sortie introuvable : " + n);
        return -1;
    }

    private static void set(FileSimulateur fs, int idx, int v) {
        fs.getEntrees(idx, 1).set(v == 1 ? Etat.UP : Etat.DW);
    }

    private static int read(FileSimulateur fs, int idx) {
        Etat e = fs.getSorties(idx, 1).getValeur();
        return e == Etat.UP ? 1 : e == Etat.DW ? 0 : -1; // -1 = ND
    }

    /** Bascule D nue avec reset asynchrone. */
    private static final String DFF =
        "module dff (D, clk, rst : Q) "
        + "Q := D on clk, reset when rst "
        + "end module";

    /** Bascule D avec enable. */
    private static final String DFFE =
        "module dffe (D, clk, rst, en : Q) "
        + "Q := D on clk, reset when rst, enabled when en "
        + "end module";

    // --- Tests ---

    @Test
    public void captureSurFrontMontant() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, D, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("capture de 1 sur front montant", 1, read(fs, Q));
        set(fs, D, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("capture de 0 sur front montant", 0, read(fs, Q));
    }

    @Test
    public void maintienPendantClkHaut() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1);                       // front montant, capture 1
        assertEquals(1, read(fs, Q));
        set(fs, D, 0);                         // D change pendant clk haut
        assertEquals("Q doit rester 1 (pas transparent)", 1, read(fs, Q));
    }

    @Test
    public void resetAsynchroneForceZero() throws Exception {
        FileSimulateur fs = build(DFF);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 1
        assertEquals(1, read(fs, Q));
        set(fs, rst, 1);                       // reset asynchrone, clk bas
        assertEquals("reset force Q a 0 sans front", 0, read(fs, Q));
    }

    @Test
    public void setAsynchroneForceUn() throws Exception {
        FileSimulateur fs = build(
            "module dffs (D, clk, s : Q) "
            + "Q := D on clk, set when s "
            + "end module");
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), s = idxE(fs, "s");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, s, 1); set(fs, s, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 0
        assertEquals(0, read(fs, Q));
        set(fs, s, 1);                         // set asynchrone
        assertEquals("set force Q a 1 sans front", 1, read(fs, Q));
    }

    @Test
    public void enableCaptureEtMaintien() throws Exception {
        FileSimulateur fs = build(DFFE);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst"), en = idxE(fs, "en");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 0); set(fs, en, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, D, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=1 : capture", 1, read(fs, Q));
        set(fs, en, 0); set(fs, D, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=0 : maintien", 1, read(fs, Q));
        set(fs, en, 1); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("en=1 : capture de 0", 0, read(fs, Q));
    }

    @Test
    public void resetDomineEnable() throws Exception {
        FileSimulateur fs = build(DFFE);
        int D = idxE(fs, "D"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst"), en = idxE(fs, "en");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, D, 1); set(fs, en, 1); set(fs, rst, 1); set(fs, rst, 0);
        set(fs, clk, 1); set(fs, clk, 0);      // Q = 1
        assertEquals(1, read(fs, Q));
        set(fs, en, 0);                        // enable desactive
        set(fs, rst, 1);                       // reset doit dominer malgre en=0
        assertEquals("reset async domine l'enable", 0, read(fs, Q));
    }

    @Test
    public void toggleCompteur() throws Exception {
        // Q := /Q : diviseur de frequence, rebouclage Q->D.
        FileSimulateur fs = build(
            "module toggle (clk, rst : Q) "
            + "Q := /Q on clk, reset when rst "
            + "end module");
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");
        set(fs, clk, 0); set(fs, rst, 1); set(fs, rst, 0);
        assertEquals(0, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 1", 1, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 2", 0, read(fs, Q));
        set(fs, clk, 1); set(fs, clk, 0);
        assertEquals("cycle 3", 1, read(fs, Q));
    }

    @Test
    public void registreVectoriel() throws Exception {
        // q[1..0] := d[1..0] : registre 2 bits.
        // Ajustement fixture : FileSimulateur(Module) expose les vecteurs par
        // nom de base ('d', 'q') avec acces par slot (slot 1 = bit bas = [0]).
        // idxE("d[0]") et idxS("q[0]") seraient introuvables ; on utilise
        // l'indice de l'entree/sortie vectorielle + le slot correspondant.
        FileSimulateur fs = build(
            "module reg (d[1..0], clk, rst : q[1..0]) "
            + "q[1..0] := d[1..0] on clk, reset when rst "
            + "end module");
        int idxD = idxE(fs, "d");    // vecteur d : slot 1 = d[0], slot 2 = d[1]
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int idxQ = idxS(fs, "q");    // vecteur q : slot 1 = q[0], slot 2 = q[1]

        fs.getEntrees(clk, 1).set(Etat.DW);
        fs.getEntrees(idxD, 1).set(Etat.DW); fs.getEntrees(idxD, 2).set(Etat.DW);
        fs.getEntrees(rst, 1).set(Etat.UP); fs.getEntrees(rst, 1).set(Etat.DW);

        // Capture d[0]=1, d[1]=0
        fs.getEntrees(idxD, 1).set(Etat.UP); fs.getEntrees(idxD, 2).set(Etat.DW);
        fs.getEntrees(clk, 1).set(Etat.UP); fs.getEntrees(clk, 1).set(Etat.DW);
        Etat q0a = fs.getSorties(idxQ, 1).getValeur();
        Etat q1a = fs.getSorties(idxQ, 2).getValeur();
        assertEquals("bit 0 capture 1", Etat.UP, q0a);
        assertEquals("bit 1 capture 0", Etat.DW, q1a);

        // Capture d[0]=0, d[1]=1
        fs.getEntrees(idxD, 1).set(Etat.DW); fs.getEntrees(idxD, 2).set(Etat.UP);
        fs.getEntrees(clk, 1).set(Etat.UP); fs.getEntrees(clk, 1).set(Etat.DW);
        Etat q0b = fs.getSorties(idxQ, 1).getValeur();
        Etat q1b = fs.getSorties(idxQ, 2).getValeur();
        assertEquals("bit 0 capture 0", Etat.DW, q0b);
        assertEquals("bit 1 capture 1", Etat.UP, q1b);
    }

    @Test
    public void deuxBasculesIndependantes() throws Exception {
        FileSimulateur fs = build(
            "module deux (a, b, clk, rst : X, Y) "
            + "X := a on clk, reset when rst "
            + "Y := b on clk, reset when rst "
            + "end module");
        int a = idxE(fs, "a"), b = idxE(fs, "b");
        int clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int X = idxS(fs, "X"), Y = idxS(fs, "Y");
        set(fs, clk, 0); set(fs, a, 0); set(fs, b, 0);
        set(fs, rst, 1); set(fs, rst, 0);
        set(fs, a, 1); set(fs, b, 0); set(fs, clk, 1); set(fs, clk, 0);
        assertEquals(1, read(fs, X));
        assertEquals(0, read(fs, Y));
    }

    @Test
    public void horlogeNonResetee_resteND() throws Exception {
        // Sans front de reset, l'etat de la bascule est ND (non defini).
        FileSimulateur fs = build(DFF);
        int Q = idxS(fs, "Q");
        assertEquals("bascule jamais resetee : etat ND", -1, read(fs, Q));
    }

    @Test
    public void rejetHorlogeVectorielle() throws Exception {
        try {
            build("module bad (D, c[1..0], rst : Q) "
                + "Q := D on c[1..0], reset when rst "
                + "end module");
            fail("une horloge vectorielle doit etre rejetee");
        } catch (ConversionException e) {
            assertEquals(ConversionException.Reason.VECTOR_WIDTH_MISMATCH, e.reason());
        }
    }

    @Test
    public void rejetDataMauvaiseLargeur() throws Exception {
        try {
            build("module bad (d[1..0], clk, rst : Q) "
                + "Q := d[1..0] on clk, reset when rst "
                + "end module");
            fail("data de largeur 2 sur LHS scalaire doit etre rejete");
        } catch (ConversionException e) {
            assertEquals(ConversionException.Reason.VECTOR_WIDTH_MISMATCH, e.reason());
        }
    }
}
