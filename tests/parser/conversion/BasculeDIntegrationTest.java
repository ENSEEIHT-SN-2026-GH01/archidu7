package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Etat;
import simulateur.FileSimulateur;
import erwan.Module;

/**
 * Tests d'intégration du module interne {@code $BasculeD} : pipeline complet
 * parser → conversion → simulation, manipulation des entrées et lecture des
 * sorties Q et Qbar.
 *
 * <p>Convention temporaire pendant cette phase exploratoire : un coup d'horloge
 * complet = {@code set(DW)} suivi de {@code set(UP)} sur l'entrée {@code clock}.
 */
public class BasculeDIntegrationTest {

    private static FileSimulateur build(String src) {
        CstNode cst = CstParser.parse(src);
        Module top = Conversion.convert(cst);
        return new FileSimulateur(top);
    }

    private static int idxE(FileSimulateur fs, String nom) {
        for (int i = 1; i <= fs.nbEntree(); i++) if (nom.equals(fs.nomEntree(i))) return i;
        fail("Entrée introuvable : " + nom); return -1;
    }
    private static int idxS(FileSimulateur fs, String nom) {
        for (int i = 1; i <= fs.nbSorties(); i++) if (nom.equals(fs.nomSortie(i))) return i;
        fail("Sortie introuvable : " + nom); return -1;
    }
    private static Etat e(int v) { return v == 1 ? Etat.UP : Etat.DW; }
    private static int v(Etat s) { return s == Etat.UP ? 1 : s == Etat.DW ? 0 : -1; }

    /** Tick complet : retombée puis montée d'horloge. */
    private static void tick(FileSimulateur fs, int clk) {
        fs.getEntrees(clk, 1).set(Etat.DW);
        fs.getEntrees(clk, 1).set(Etat.UP);
    }

    // ----------------------------------------------------------------- T1
    /**
     * Bascule D nue : on expose ses 4 entrées et 2 sorties via les params du
     * module wrapper. Vérifie reset asynchrone et échantillonnage sur tick.
     */
    @Test
    public void t1_basculeD_resetEtEchantillonnage() {
        // Contournement bug FileSimulateur:150 : sortie de sous-module non
        // consommée → on insère une affectation triviale Q = Qx, Qbar = Qbarx
        // pour forcer un nœud combi qui place un EntreeModule sur le Lien.
        String src = "module top (en, clock, sig, rst : Q, Qbar)"
                   + " $BasculeD(en, clock, sig, rst : Qx, Qbarx)"
                   + " Q = Qx Qbar = Qbarx"
                   + " end module";
        FileSimulateur fs = build(src);

        int en = idxE(fs, "en"), clk = idxE(fs, "clock"),
            sig = idxE(fs, "sig"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q"), Qb = idxS(fs, "Qbar");

        // Reset → Q=0, Qbar=1
        fs.getEntrees(rst, 1).set(Etat.UP);
        assertEquals("reset → Q=0", 0, v(fs.getSorties(Q, 1).getValeur()));
        assertEquals("reset → Qbar=1", 1, v(fs.getSorties(Qb, 1).getValeur()));

        // Lever reset, en=1, sig=1, tick → Q=1
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);
        fs.getEntrees(sig, 1).set(Etat.UP);
        tick(fs, clk);
        assertEquals("après tick signal=1 → Q=1", 1, v(fs.getSorties(Q, 1).getValeur()));
        assertEquals("après tick signal=1 → Qbar=0", 0, v(fs.getSorties(Qb, 1).getValeur()));

        // signal=0, tick → Q=0
        fs.getEntrees(sig, 1).set(Etat.DW);
        tick(fs, clk);
        assertEquals("après tick signal=0 → Q=0", 0, v(fs.getSorties(Q, 1).getValeur()));
        assertEquals("après tick signal=0 → Qbar=1", 1, v(fs.getSorties(Qb, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T2
    /** Sans tick d'horloge, Q ne doit pas changer même si signal change. */
    @Test
    public void t2_basculeD_pasDeTickPasDeChangement() {
        String src = "module top (en, clock, sig, rst : Q, Qbar)"
                   + " $BasculeD(en, clock, sig, rst : Q, Qbar)"
                   + " end module";
        FileSimulateur fs = build(src);
        int en = idxE(fs, "en"), clk = idxE(fs, "clock"),
            sig = idxE(fs, "sig"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");

        // Reset puis lever reset, en=1
        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);
        assertEquals("avant tick : Q reste à 0 (reset)", 0, v(fs.getSorties(Q, 1).getValeur()));

        // Changer signal sans tick
        fs.getEntrees(sig, 1).set(Etat.UP);
        assertEquals("signal=1 sans tick → Q reste 0", 0, v(fs.getSorties(Q, 1).getValeur()));

        // Tick → Q=1
        tick(fs, clk);
        assertEquals("après tick → Q=1", 1, v(fs.getSorties(Q, 1).getValeur()));

        // Re-changer signal à 0 sans tick → Q reste 1
        fs.getEntrees(sig, 1).set(Etat.DW);
        assertEquals("signal=0 sans tick → Q reste 1", 1, v(fs.getSorties(Q, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T3
    /**
     * Compteur 1 bit (toggle) : Q alterne 0/1/0/1 à chaque coup d'horloge.
     * On réinjecte Qbar dans signal — c'est le cas classique de boucle
     * combi+bascule. Si l'événementiel de Mati a un défaut de front, c'est
     * ici qu'on l'attrape (double-toggle, oscillation, etc.).
     */
    @Test
    public void t3_compteur1bit_toggleParTick() {
        // On nomme la sortie de Qbar "nQ" puis on la passe en signal.
        // Comme Arg = Signal nu (pas d'expression), on passe par un signal interne.
        String src = "module top (en, clock, rst : Q)"
                   + " $BasculeD(en, clock, nQ, rst : Q, nQ)"
                   + " end module";
        FileSimulateur fs = build(src);
        int en = idxE(fs, "en"), clk = idxE(fs, "clock"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");

        // Init : reset
        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);
        assertEquals("Q initial après reset = 0", 0, v(fs.getSorties(Q, 1).getValeur()));

        // 6 ticks : 1,0,1,0,1,0
        int[] expected = {1, 0, 1, 0, 1, 0};
        for (int i = 0; i < expected.length; i++) {
            tick(fs, clk);
            assertEquals("tick #" + (i + 1) + " → Q",
                expected[i], v(fs.getSorties(Q, 1).getValeur()));
        }
    }

    // ----------------------------------------------------------------- T4
    /**
     * en=0 doit geler la bascule : aucun tick ne doit faire évoluer Q.
     */
    @Test
    public void t4_basculeD_enableBas_figeQ() {
        String src = "module top (en, clock, sig, rst : Q, Qbar)"
                   + " $BasculeD(en, clock, sig, rst : Q, Qbar)"
                   + " end module";
        FileSimulateur fs = build(src);
        int en = idxE(fs, "en"), clk = idxE(fs, "clock"),
            sig = idxE(fs, "sig"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");

        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.DW); // enable bas
        fs.getEntrees(sig, 1).set(Etat.UP);

        for (int i = 0; i < 5; i++) tick(fs, clk);
        assertEquals("en=0 figé : Q doit rester 0", 0, v(fs.getSorties(Q, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T5
    /**
     * Test du compteur 1 bit emboîté : on définit un module compteur1
     * (qui contient $BasculeD), et on l'appelle depuis un module top
     * qui propage clock/en/reset. C'est ici qu'on stresse la propagation
     * de l'horloge à travers un module SHDL composé — coeur du bug P1.
     */
    @Test
    public void t5_compteur1bitEmboite_propagationHorloge() {
        String compteur1 = "module compteur1 (en, clock, rst : Q)"
                         + " $BasculeD(en, clock, nQ, rst : Q, nQ)"
                         + " end module";
        String top = "module top (en, clock, rst : Q)"
                   + " $compteur1(en, clock, rst : Q)"
                   + " end module";

        CstNode cstSub = CstParser.parse(compteur1);
        CstNode cstTop = CstParser.parse(top);
        // NB : $compteur1 commence par '$', donc actuellement il sera cherché
        // dans BuiltinModules — ce qui échouera. Cas d'exemption : il faudrait
        // soit retirer le préfixe (utiliser compteur1 sans $), soit étendre
        // la conversion pour fall-back sur le resolver quand le nom n'est pas
        // builtin. Pour l'instant, on bascule en appel sans $.
        cstTop = CstParser.parse(
            "module top (en, clock, rst : Q)"
          + " compteur1(en, clock, rst : Q)"
          + " end module");

        Module mTop = Conversion.convert(cstTop, java.util.List.of(cstSub));
        FileSimulateur fs = new FileSimulateur(mTop);

        int en = idxE(fs, "en"), clk = idxE(fs, "clock"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "Q");

        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);

        int[] expected = {1, 0, 1, 0};
        for (int i = 0; i < expected.length; i++) {
            tick(fs, clk);
            assertEquals("tick emboîté #" + (i + 1) + " → Q",
                expected[i], v(fs.getSorties(Q, 1).getValeur()));
        }
    }

    // ----------------------------------------------------------------- T6
    /**
     * Registre 2 bits parallèle : un sous-module contient deux $BasculeD
     * partageant la même horloge et le même enable. C'est le cas typique
     * d'un registre N bits — on stresse l'indépendance des deux bascules
     * et la mutualisation de clk.
     */
    @Test
    public void t6_registre2bits_parallele() {
        // Les Qbar (nq0, nq1) doivent être exposés en sorties du wrapper :
        // sinon, leur Lien n'a pas de Composant aval et FileSimulateur NPE.
        String reg2 = "module reg2 (en, clk, rst, d0, d1 : q0, q1, nq0, nq1)"
                    + " $BasculeD(en, clk, d0, rst : q0, nq0)"
                    + " $BasculeD(en, clk, d1, rst : q1, nq1)"
                    + " end module";
        String top = "module top (en, clk, rst, d0, d1 : q0, q1, nq0, nq1)"
                   + " reg2(en, clk, rst, d0, d1 : q0, q1, nq0, nq1)"
                   + " end module";

        Module mTop = Conversion.convert(
            CstParser.parse(top), java.util.List.of(CstParser.parse(reg2)));
        FileSimulateur fs = new FileSimulateur(mTop);

        int en = idxE(fs, "en"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int d0 = idxE(fs, "d0"), d1 = idxE(fs, "d1");
        int Q0 = idxS(fs, "q0"), Q1 = idxS(fs, "q1");

        // Reset puis enable
        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);

        // Charge "10" : d0=1, d1=0, tick → q0=1, q1=0
        fs.getEntrees(d0, 1).set(Etat.UP);
        fs.getEntrees(d1, 1).set(Etat.DW);
        tick(fs, clk);
        assertEquals("registre charge q0=1", 1, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("registre charge q1=0", 0, v(fs.getSorties(Q1, 1).getValeur()));

        // Charge "01" : d0=0, d1=1, tick → q0=0, q1=1
        fs.getEntrees(d0, 1).set(Etat.DW);
        fs.getEntrees(d1, 1).set(Etat.UP);
        tick(fs, clk);
        assertEquals("registre charge q0=0", 0, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("registre charge q1=1", 1, v(fs.getSorties(Q1, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T7
    /**
     * Deux niveaux d'emboîtement : top → mid → inner → $BasculeD. On vérifie
     * que la propagation de l'horloge traverse correctement 2 frontières de
     * sous-module au lieu d'une seule (T5).
     */
    @Test
    public void t7_deuxNiveauxEmboitement() {
        String inner = "module inner (en, clk, sig, rst : q, nq)"
                     + " $BasculeD(en, clk, sig, rst : q, nq)"
                     + " end module";
        String mid = "module mid (en, clk, sig, rst : q, nq)"
                   + " inner(en, clk, sig, rst : q, nq)"
                   + " end module";
        String top = "module top (en, clk, sig, rst : q, nq)"
                   + " mid(en, clk, sig, rst : q, nq)"
                   + " end module";

        Module mTop = Conversion.convert(
            CstParser.parse(top),
            java.util.List.of(CstParser.parse(inner), CstParser.parse(mid)));
        FileSimulateur fs = new FileSimulateur(mTop);

        int en = idxE(fs, "en"), clk = idxE(fs, "clk"),
            sig = idxE(fs, "sig"), rst = idxE(fs, "rst");
        int Q = idxS(fs, "q");

        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);
        fs.getEntrees(sig, 1).set(Etat.UP);
        tick(fs, clk);
        assertEquals("après tick à travers 2 modules → q=1",
            1, v(fs.getSorties(Q, 1).getValeur()));

        fs.getEntrees(sig, 1).set(Etat.DW);
        tick(fs, clk);
        assertEquals("après tick à travers 2 modules → q=0",
            0, v(fs.getSorties(Q, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T8
    /**
     * Deux instances du même sous-module séquentiel partageant la même
     * horloge depuis le top. Cas classique : deux bits indépendants d'un
     * registre construits par deux appels du même module unitaire.
     */
    @Test
    public void t8_deuxSousModulesMemeHorloge() {
        String bit1 = "module bit1 (en, clk, sig, rst : q, nq)"
                    + " $BasculeD(en, clk, sig, rst : q, nq)"
                    + " end module";
        String top = "module top (en, clk, rst, s0, s1 : q0, q1, nq0, nq1)"
                   + " bit1(en, clk, s0, rst : q0, nq0)"
                   + " bit1(en, clk, s1, rst : q1, nq1)"
                   + " end module";

        Module mTop = Conversion.convert(
            CstParser.parse(top), java.util.List.of(CstParser.parse(bit1)));
        FileSimulateur fs = new FileSimulateur(mTop);

        int en = idxE(fs, "en"), clk = idxE(fs, "clk"), rst = idxE(fs, "rst");
        int s0 = idxE(fs, "s0"), s1 = idxE(fs, "s1");
        int Q0 = idxS(fs, "q0"), Q1 = idxS(fs, "q1");

        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);

        fs.getEntrees(s0, 1).set(Etat.UP);
        fs.getEntrees(s1, 1).set(Etat.DW);
        tick(fs, clk);
        assertEquals("horloge partagée : q0=1", 1, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("horloge partagée : q1=0", 0, v(fs.getSorties(Q1, 1).getValeur()));

        // Indépendance : changer s0 sans tick ne doit pas affecter q1
        fs.getEntrees(s0, 1).set(Etat.DW);
        fs.getEntrees(s1, 1).set(Etat.UP);
        assertEquals("avant tick : q0 inchangé", 1, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("avant tick : q1 inchangé", 0, v(fs.getSorties(Q1, 1).getValeur()));
        tick(fs, clk);
        assertEquals("après 2e tick : q0=0", 0, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("après 2e tick : q1=1", 1, v(fs.getSorties(Q1, 1).getValeur()));
    }

    // ----------------------------------------------------------------- T9
    /**
     * Shift register 2 étages : la sortie de la première $BasculeD alimente
     * directement le data de la seconde, dans le même module. C'est le cas
     * test du vrai séquentiel synchrone — sans isolation maître-esclave
     * correcte au niveau de la simulation, q1 risque de suivre q0
     * <em>instantanément</em> au lieu d'avoir un tick de retard.
     *
     * <p>On documente le comportement observé : si le test échoue avec
     * q1 = q0 = sig au lieu de q1 = ancienne valeur de q0, c'est que le
     * simulateur n'isole pas correctement les fronts d'horloge entre
     * bascules cascadées.
     */
    @Test
    public void t9_shiftRegister_2etages() {
        String top = "module top (en, clk, sig, rst : q0, q1, nq0, nq1)"
                   + " $BasculeD(en, clk, sig, rst : q0, nq0)"
                   + " $BasculeD(en, clk, q0, rst : q1, nq1)"
                   + " end module";

        Module mTop = Conversion.convert(CstParser.parse(top));
        FileSimulateur fs = new FileSimulateur(mTop);

        int en = idxE(fs, "en"), clk = idxE(fs, "clk"),
            sig = idxE(fs, "sig"), rst = idxE(fs, "rst");
        int Q0 = idxS(fs, "q0"), Q1 = idxS(fs, "q1");

        fs.getEntrees(rst, 1).set(Etat.UP);
        fs.getEntrees(rst, 1).set(Etat.DW);
        fs.getEntrees(en, 1).set(Etat.UP);

        // sig=1, tick 1 → comportement vrai shift register : q0=1, q1=0
        fs.getEntrees(sig, 1).set(Etat.UP);
        tick(fs, clk);
        assertEquals("shift tick 1 : q0=1", 1, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("shift tick 1 : q1=0 (un tick de retard)",
            0, v(fs.getSorties(Q1, 1).getValeur()));

        // sig=0, tick 2 → q0=0, q1=1 (q1 voit l'ancien q0)
        fs.getEntrees(sig, 1).set(Etat.DW);
        tick(fs, clk);
        assertEquals("shift tick 2 : q0=0", 0, v(fs.getSorties(Q0, 1).getValeur()));
        assertEquals("shift tick 2 : q1=1", 1, v(fs.getSorties(Q1, 1).getValeur()));
    }
}
