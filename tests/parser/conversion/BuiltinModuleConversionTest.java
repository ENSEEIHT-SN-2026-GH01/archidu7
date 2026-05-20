package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Module;
import erwan.AppelModule;

/**
 * Tests unitaires côté parser/conversion pour les modules internes (préfixe '$').
 *
 * <p>On vérifie ici uniquement le pipeline SHDL source → {@code erwan.Module} :
 * que la conversion accepte {@code $BasculeD}, construit un appel module pointant
 * vers une primitive dont le nom commence par '$', et rejette les primitives
 * inconnues avec {@link Reason#BUILTIN_NOT_FOUND}. La validation comportementale
 * (échantillonnage, reset, toggle…) est couverte par les tests d'intégration
 * une fois le simulateur de Mati mergé sur cette branche.
 */
public class BuiltinModuleConversionTest {

    private static Module convert(String src) {
        CstNode cst = CstParser.parse(src);
        return Conversion.convert(cst);
    }

    /** $BasculeD : la conversion produit bien un AppelModule sur une primitive. */
    @Test
    public void basculeD_estReconnueCommePrimitive() {
        String src = "module top (en, clock, sig, rst : Q, Qbar)"
                   + " $BasculeD(en, clock, sig, rst : Q, Qbar)"
                   + " end module";
        Module top = convert(src);

        assertEquals("top a 1 appel module", 1, top.Branchements.size());
        AppelModule appel = top.Branchements.get(0);
        assertEquals("appel pointe sur $BasculeD", "$BasculeD", appel.module.Nom);
        assertTrue("nom commence par '$' (dispatch SimulateurInterne)",
            appel.module.Nom.startsWith("$"));
        assertEquals("4 entrées primitives", 4, appel.module.Entrees.size());
        assertEquals("2 sorties primitives", 2, appel.module.Sorties.size());
    }

    /** Primitive inconnue → BUILTIN_NOT_FOUND, pas MODULE_NOT_FOUND. */
    @Test
    public void primitiveInconnue_leveBuiltinNotFound() {
        String src = "module top (a, b : c)"
                   + " $Inexistant(a, b : c)"
                   + " end module";
        try {
            convert(src);
            fail("Devrait lever ConversionException pour primitive inconnue");
        } catch (ConversionException e) {
            assertEquals("raison spécifique builtin",
                Reason.BUILTIN_NOT_FOUND, e.reason());
            assertTrue("message mentionne le nom",
                e.getMessage().contains("$Inexistant"));
        }
    }

    /** Le module non préfixé '$' reste résolu par le resolver classique. */
    @Test
    public void appelNonPrefixe_passeParResolver() {
        String sub = "module sub (a : b) b = a end module";
        String top = "module top (x : y) sub(x : y) end module";
        Module mTop = Conversion.convert(
            CstParser.parse(top),
            java.util.List.of(CstParser.parse(sub)));
        assertEquals("appel vers sub résolu", "sub", mTop.Branchements.get(0).module.Nom);
        assertFalse("pas un builtin", mTop.Branchements.get(0).module.Nom.startsWith("$"));
    }

    /** Deux appels à $BasculeD ne partagent pas la même instance Module. */
    @Test
    public void deuxAppels_donnentInstancesDistinctes() {
        String src = "module top (en, clock, sig, rst : Q1, nQ1, Q2, nQ2)"
                   + " $BasculeD(en, clock, sig, rst : Q1, nQ1)"
                   + " $BasculeD(en, clock, sig, rst : Q2, nQ2)"
                   + " end module";
        Module top = convert(src);
        assertEquals(2, top.Branchements.size());
        Module m1 = top.Branchements.get(0).module;
        Module m2 = top.Branchements.get(1).module;
        assertNotSame("BuiltinModules.get retourne des instances fraîches", m1, m2);
        assertEquals("$BasculeD", m1.Nom);
        assertEquals("$BasculeD", m2.Nom);
    }
}
