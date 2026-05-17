package tests.simulateur.Erwan;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import simulateur.Erwan.Erwan;
import simulateur.Erwan.Operation;

/**
 * Sentinelle anti-regression : verifie que Erwan.OR() retourne bien Op.OR.
 *
 * Bug corrige : OR() retournait Operation.AND (ligne 152 de Erwan.java).
 * Ce test empeche toute regression future sur ce point.
 *
 * Note : ce fichier est place dans tests/simulateur/Erwan/ (repertoire d'Erwan)
 * avec accord explicite d'Alexis pour ce cas d'anti-regression.
 */
public class ErwanOrSentinelTest {

    @Test
    public void or_returnsOpOR_notAND() {
        List<Erwan> entrees = Arrays.asList(Erwan.LITTERAL("a"), Erwan.LITTERAL("b"));
        Erwan e = Erwan.OR(entrees);
        assertEquals("Erwan.OR doit retourner Operation.OR (regression du bug ligne 152)",
                     Operation.OR, e.Op);
    }

    @Test
    public void and_returnsOpAND() {
        List<Erwan> entrees = Arrays.asList(Erwan.LITTERAL("a"), Erwan.LITTERAL("b"));
        Erwan e = Erwan.AND(entrees);
        assertEquals(Operation.AND, e.Op);
    }
}
