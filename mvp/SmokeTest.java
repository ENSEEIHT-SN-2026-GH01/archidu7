package mvp;

import simulateur.Etat;

import java.util.LinkedHashMap;
import java.util.Map;

/** Verification rapide du pipeline complet sans JavaFX. */
public final class SmokeTest {
    public static void main(String[] args) throws Exception {
        Pilote p = new Pilote();
        String shdl = "module ET(a, b : c) c = a * b end module";

        verif(p, shdl, "a=UP,b=UP", "c", Etat.UP);
        verif(p, shdl, "a=DW,b=UP", "c", Etat.DW);
        verif(p, shdl, "a=UP,b=DW", "c", Etat.DW);

        String orShdl = "module OU(a, b : c) c = a + b end module";
        verif(p, orShdl, "a=DW,b=UP", "c", Etat.UP);
        verif(p, orShdl, "a=DW,b=DW", "c", Etat.DW);

        String notShdl = "module NON(a : b) b = /a end module";
        verif(p, notShdl, "a=UP", "b", Etat.DW);
        verif(p, notShdl, "a=DW", "b", Etat.UP);

        System.out.println("Smoke test OK");
    }

    private static void verif(Pilote p, String shdl, String entreesTxt, String wire, Etat attendu) throws Exception {
        Map<String, Etat> entrees = new LinkedHashMap<>();
        for (String pair : entreesTxt.split(",")) {
            String[] kv = pair.trim().split("=");
            entrees.put(kv[0], Etat.valueOf(kv[1]));
        }
        Pilote.Resultat r = p.executer(shdl, entrees);
        Etat got = r.sortieParNom.get(wire);
        if (got != attendu) {
            throw new AssertionError(shdl + " avec " + entreesTxt
                + " : attendu " + wire + "=" + attendu + ", obtenu " + got);
        }
        System.out.println("OK : " + shdl.replace('\n',' ') + " | " + entreesTxt + " => " + wire + "=" + got);
    }
}
