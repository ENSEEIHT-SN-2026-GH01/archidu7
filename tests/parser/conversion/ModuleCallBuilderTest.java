package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import parser.conversion.ModuleCallBuilder;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.AppelModule;
import erwan.Descripteur;
import erwan.Module;

/**
 * Tests unitaires pour {@link ModuleCallBuilder}.
 *
 * <p>Chaque test construit un module SHDL minimaliste et descend dans
 * le CST pour extraire le nœud {@code ModuleCall}, puis invoque
 * {@link ModuleCallBuilder#build} avec un {@code erwan.Module} construit à la main.</p>
 */
public class ModuleCallBuilderTest {

    // -----------------------------------------------------------------------
    // Utilitaire : extraire le nœud ModuleCall depuis un module SHDL complet
    // Grammar: Module → ... Instance_Plus ... | Instance → Dollar Identifiant ModuleCall
    // -----------------------------------------------------------------------

    /**
     * Parse {@code src} et extrait le premier {@code ModuleCall} trouvé dans
     * la première instance du module.
     */
    private static CstNode parseModuleCall(String src) {
        CstNode root = CstParser.parse(src);
        return root
            .first(NonTerminal.Module).orElseThrow(() -> new AssertionError("Pas de Module dans le CST"))
            .first(NonTerminal.Instance_Plus).orElseThrow(() -> new AssertionError("Pas de Instance_Plus"))
            .first(NonTerminal.Instance).orElseThrow(() -> new AssertionError("Pas de Instance"))
            .first(NonTerminal.ModuleCall).orElseThrow(() -> new AssertionError("Pas de ModuleCall"));
    }

    // -----------------------------------------------------------------------
    // Modules de référence construits à la main
    // -----------------------------------------------------------------------

    /** Module fa(a, b, cin : s, cout) — 3 entrées scalaires, 2 sorties scalaires */
    private static Module faModule() {
        return new Module("fa",
            List.of(),
            List.of(new Descripteur("a"), new Descripteur("b"), new Descripteur("cin")),
            List.of(new Descripteur("s"), new Descripteur("cout")),
            List.of());
    }

    /** Module add4(a[3..0], b[3..0] : s[3..0]) — entrées vectorielles 4 bits, sortie 4 bits */
    private static Module add4Module() {
        return new Module("add4",
            List.of(),
            List.of(new Descripteur("a", 0, 3), new Descripteur("b", 0, 3)),
            List.of(new Descripteur("s", 0, 3)),
            List.of());
    }

    // -----------------------------------------------------------------------
    // Tests : découpe DE/DS correcte
    // -----------------------------------------------------------------------

    /**
     * $fa(x, y, z : s, c) → DE=[x,y,z], DS=[s,c].
     * Vérification du découpage et de l'ordre.
     */
    @Test
    public void deDs_splitOnColon_correctOrder() {
        // module top (x, y, z, s, c) $fa(x, y, z : s, c) end module
        String src = "module top (x, y, z, s, c) $fa(x, y, z : s, c) end module";
        CstNode mc = parseModuleCall(src);
        Module fa = faModule();

        AppelModule am = ModuleCallBuilder.build(mc, fa);

        assertEquals("DE doit contenir 3 entrées", 3, am.DE.size());
        assertEquals("x", am.DE.get(0).Nom());
        assertEquals("y", am.DE.get(1).Nom());
        assertEquals("z", am.DE.get(2).Nom());

        assertEquals("DS doit contenir 2 sorties", 2, am.DS.size());
        assertEquals("s", am.DS.get(0).Nom());
        assertEquals("c", am.DS.get(1).Nom());
    }

    /**
     * Le module retourné ({@code am.module}) doit être le même objet que {@code called}.
     */
    @Test
    public void build_moduleReferenceIsCallled() {
        String src = "module top (x, y, z, s, c) $fa(x, y, z : s, c) end module";
        CstNode mc = parseModuleCall(src);
        Module fa = faModule();

        AppelModule am = ModuleCallBuilder.build(mc, fa);

        assertSame("am.module doit être le même objet que called", fa, am.module);
    }

    // -----------------------------------------------------------------------
    // Tests : descripteur scalaire et vecteur
    // -----------------------------------------------------------------------

    /**
     * Descripteur scalaire : signal sans indice → Descripteur("nom"), nbSignaux()=1.
     */
    @Test
    public void scalarDescriptor_nbSignauxIsOne() {
        String src = "module top (x, y, z, s, c) $fa(x, y, z : s, c) end module";
        CstNode mc = parseModuleCall(src);
        AppelModule am = ModuleCallBuilder.build(mc, faModule());

        Descripteur d = am.DE.get(0);
        assertEquals("x", d.Nom());
        assertEquals(1, d.nbSignaux());
    }

    /**
     * Descripteur vecteur : signal avec plage a[3..0] → Descripteur("a", 0, 3), nbSignaux()=4.
     * Utilise add4Module dont les entrées/sorties sont vectorielles de 4 bits.
     */
    @Test
    public void vectorDescriptor_correctNbSignaux() {
        // module top (a[3..0], b[3..0], s[3..0]) $add4(a[3..0], b[3..0] : s[3..0]) end module
        String src = "module top (a[3..0], b[3..0], s[3..0]) $add4(a[3..0], b[3..0] : s[3..0]) end module";
        CstNode mc = parseModuleCall(src);
        AppelModule am = ModuleCallBuilder.build(mc, add4Module());

        assertEquals("DE doit contenir 2 descripteurs vectoriels", 2, am.DE.size());
        Descripteur da = am.DE.get(0);
        assertEquals("a", da.Nom());
        assertEquals(0, da.indiceDebut());
        assertEquals(3, da.indiceFin());
        assertEquals(4, da.nbSignaux());

        assertEquals("DS doit contenir 1 descripteur vectoriel", 1, am.DS.size());
        Descripteur ds = am.DS.get(0);
        assertEquals("s", ds.Nom());
        assertEquals(4, ds.nbSignaux());
    }

    // -----------------------------------------------------------------------
    // Tests : argument littéral → MODULE_CALL_INVALID_ARG
    // -----------------------------------------------------------------------

    /**
     * Argument littéral binaire (.1) → MODULE_CALL_INVALID_ARG.
     */
    @Test
    public void literalArg_throwsInvalidArg() {
        // module top (x) $fa(.1, .0, .0 : s, c) end module
        // Utiliser un module à 1 entrée et 1 sortie pour simplifier
        Module simple = new Module("simple",
            List.of(),
            List.of(new Descripteur("a")),
            List.of(new Descripteur("s")),
            List.of());
        String src = "module top (x) $simple(.1 : s) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, simple);
            fail("Attendu ConversionException MODULE_CALL_INVALID_ARG");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_CALL_INVALID_ARG, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Tests : argument concaténation → MODULE_CALL_INVALID_ARG
    // -----------------------------------------------------------------------

    /**
     * Argument avec concaténation (x & y) → MODULE_CALL_INVALID_ARG.
     */
    @Test
    public void concatArg_throwsInvalidArg() {
        // Module avec 1 entrée de 2 bits et 1 sortie scalaire
        Module simple2 = new Module("simple2",
            List.of(),
            List.of(new Descripteur("a", 0, 1)),
            List.of(new Descripteur("s")),
            List.of());
        String src = "module top (x, y, s) $simple2(x & y : s) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, simple2);
            fail("Attendu ConversionException MODULE_CALL_INVALID_ARG");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_CALL_INVALID_ARG, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Tests : zéro Colon → MODULE_BAD_SEPARATORS
    // -----------------------------------------------------------------------

    /**
     * Aucun ':' dans les arguments → MODULE_BAD_SEPARATORS.
     */
    @Test
    public void zeroColon_throwsBadSeparators() {
        // $fa(x, y, z) → pas de ':'
        String src = "module top (x, y, z) $fa(x, y, z) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, faModule());
            fail("Attendu ConversionException MODULE_BAD_SEPARATORS");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_BAD_SEPARATORS, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Tests : deux Colon → MODULE_BAD_SEPARATORS
    // -----------------------------------------------------------------------

    /**
     * Deux ':' → MODULE_BAD_SEPARATORS.
     */
    @Test
    public void twoColons_throwsBadSeparators() {
        // $fa(x : y : z) → deux ':'
        String src = "module top (x, y, z) $fa(x : y : z) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, faModule());
            fail("Attendu ConversionException MODULE_BAD_SEPARATORS");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_BAD_SEPARATORS, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Tests : arité fausse → MODULE_ARITY_MISMATCH
    // -----------------------------------------------------------------------

    /**
     * Trop peu d'entrées → MODULE_ARITY_MISMATCH.
     */
    @Test
    public void tooFewInputs_throwsArityMismatch() {
        // fa attend 3 entrées, on n'en fournit que 2
        String src = "module top (x, y, s, c) $fa(x, y : s, c) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, faModule());
            fail("Attendu ConversionException MODULE_ARITY_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_ARITY_MISMATCH, ex.reason());
        }
    }

    /**
     * Trop peu de sorties → MODULE_ARITY_MISMATCH.
     */
    @Test
    public void tooFewOutputs_throwsArityMismatch() {
        // fa attend 2 sorties, on n'en fournit qu'une
        String src = "module top (x, y, z, s) $fa(x, y, z : s) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, faModule());
            fail("Attendu ConversionException MODULE_ARITY_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_ARITY_MISMATCH, ex.reason());
        }
    }

    /**
     * Nombre d'arguments correct mais largeur de vecteur incorrecte → MODULE_ARITY_MISMATCH.
     * add4 attend des entrées de 4 bits, on fournit un signal de 2 bits.
     */
    @Test
    public void wrongVectorWidth_throwsArityMismatch() {
        // add4 attend a[3..0] (4 bits) mais on fournit a[1..0] (2 bits)
        String src = "module top (a[1..0], b[3..0], s[3..0]) $add4(a[1..0], b[3..0] : s[3..0]) end module";
        CstNode mc = parseModuleCall(src);

        try {
            ModuleCallBuilder.build(mc, add4Module());
            fail("Attendu ConversionException MODULE_ARITY_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_ARITY_MISMATCH, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Test supplémentaire : cas nominal minimal (1 entrée, 1 sortie)
    // -----------------------------------------------------------------------

    /**
     * Module trivial (1 entrée, 1 sortie) : $buf(x : y) → DE=[x], DS=[y].
     */
    @Test
    public void minimal_oneInputOneOutput() {
        Module buf = new Module("buf",
            List.of(),
            List.of(new Descripteur("a")),
            List.of(new Descripteur("b")),
            List.of());
        String src = "module top (x, y) $buf(x : y) end module";
        CstNode mc = parseModuleCall(src);
        AppelModule am = ModuleCallBuilder.build(mc, buf);

        assertEquals(1, am.DE.size());
        assertEquals("x", am.DE.get(0).Nom());
        assertEquals(1, am.DS.size());
        assertEquals("y", am.DS.get(0).Nom());
    }
}
