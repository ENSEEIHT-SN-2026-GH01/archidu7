package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import parser.conversion.Conversion;
import parser.conversion.ModuleBuilder;
import parser.conversion.ModuleResolver;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.AppelModule;
import erwan.Erwan;
import erwan.Operation;
import erwan.Module;

public class ModuleBuilderTest {

    private static Module build(String src) {
        CstNode root = CstParser.parse(src);
        ModuleResolver resolver = new ModuleResolver(List.of(root));
        CstNode mod = root.first(NonTerminal.Module).orElseThrow();
        return ModuleBuilder.build(mod, resolver);
    }

    @Test
    public void singleParam_singleInstance() {
        Module m = build("module m (a) c = a end module");
        assertEquals(1, m.Plan.size());
        Erwan aff = m.Plan.get(0);
        assertEquals(Operation.AFFECTATION, aff.Op);
        assertEquals("c", aff.Nom());
        // Aucun ':' → tout va dans Entrees ; Sorties vide
        assertEquals(1, m.Entrees.size());
        assertEquals("a", m.Entrees.get(0).Nom());
        assertTrue(m.Sorties.isEmpty());
        assertTrue(m.Branchements.isEmpty());
    }

    @Test
    public void twoInstances_orderPreserved() {
        Module m = build("module m (a, b) c = a * b d = a + b end module");
        assertEquals(2, m.Plan.size());
        assertEquals("c", m.Plan.get(0).Nom());
        assertEquals("d", m.Plan.get(1).Nom());
    }

    @Test(expected = ConversionException.class)
    public void duplicateLhs_throws() {
        build("module m (a, b) c = a c = b end module");
    }

    @Test
    public void duplicateLhs_reasonIsDuplicate() {
        try {
            build("module m (a, b) c = a c = b end module");
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    @Test
    public void vectorParam_accepted() {
        // Task 5 : les parametres vecteurs sont desormais acceptes.
        Module m = build("module m (a[3..0]) c[3..0] = a[3..0] end module");
        assertEquals("vectorParam_accepted: plan doit contenir 4 affectations (un par bit)", 4, m.Plan.size());
    }

    /**
     * LHS index unique s[0] = a (RHS scalaire) : doit produire une AFFECTATION avec Numero=0.
     */
    @Test
    public void singleIndexLhs_scalarRhs_ok() {
        Module m = build("module m (a) c[0] = a end module");
        assertEquals(1, m.Plan.size());
        Erwan aff = m.Plan.get(0);
        assertEquals(Operation.AFFECTATION, aff.Op);
        assertEquals("c", aff.Nom);
        assertEquals(Integer.valueOf(0), aff.Numero);
    }

    /**
     * LHS plage s[3..0] = a[3..0] + b[3..0] : plan de 4 Erwan (indices 0 à 3).
     */
    @Test
    public void rangeLhs_vectorRhs_producesFourErwan() {
        Module m = build("module m (a, b) s[3..0] = a[3..0] + b[3..0] end module");
        assertEquals(4, m.Plan.size());
        for (int i = 0; i < 4; i++) {
            Erwan e = m.Plan.get(i);
            assertEquals(Operation.AFFECTATION, e.Op);
            assertEquals("s", e.Nom);
            assertEquals(Integer.valueOf(i), e.Numero);
        }
    }

    /**
     * LHS plage s[3..0] (largeur 4) mais RHS de largeur 2 : VECTOR_WIDTH_MISMATCH.
     */
    @Test
    public void rangeLhs_rhsWidthMismatch_throws() {
        try {
            build("module m (a, b) s[3..0] = a[1..0] + b[1..0] end module");
            fail("Attendu ConversionException VECTOR_WIDTH_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
        }
    }

    /**
     * LHS scalaire mais RHS vectoriel (a[3..0] produit un Bus de largeur 4) :
     * buildInstance doit rejeter avec VECTOR_WIDTH_MISMATCH.
     */
    @Test
    public void scalarLhs_vectorRhs_throwsWidthMismatch() {
        try {
            build("module m (a) s = a[3..0] end module");
            fail("Attendu ConversionException VECTOR_WIDTH_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
        }
    }

    /**
     * Déduplication vecteur : même signal indexé deux fois s[0]=a ; s[0]=b → DUPLICATE_LHS.
     */
    @Test
    public void duplicateVectorIndex_sameIndex_throws() {
        try {
            build("module m (a, b) s[0] = a s[0] = b end module");
            fail("Attendu ConversionException DUPLICATE_LHS");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    /**
     * Déduplication vecteur : deux index distincts s[0]=a ; s[1]=b → plan de 2 Erwan.
     */
    @Test
    public void distinctVectorIndices_twoDifferentIndexes_ok() {
        Module m = build("module m (a, b) s[0] = a s[1] = b end module");
        assertEquals(2, m.Plan.size());
        Erwan e0 = null, e1 = null;
        for (Erwan e : m.Plan) {
            assertEquals(Operation.AFFECTATION, e.Op);
            assertEquals("s", e.Nom);
            if (Integer.valueOf(0).equals(e.Numero)) e0 = e;
            else if (Integer.valueOf(1).equals(e.Numero)) e1 = e;
        }
        assertNotNull("s[0] absent du plan", e0);
        assertNotNull("s[1] absent du plan", e1);
    }

    /**
     * Déduplication au niveau du bit : un index inclus dans une plage déjà
     * assignée (s[3] puis s[3..0]) doit lever DUPLICATE_LHS — la clé textuelle
     * "s[3]" ≠ "s[0..3]" ratait ce recouvrement.
     */
    @Test
    public void indexInsideRange_overlap_throws() {
        try {
            build("module m (a[3..0], b) s[3] = b s[3..0] = a[3..0] end module");
            fail("Attendu ConversionException DUPLICATE_LHS");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    /**
     * Déduplication au niveau du bit : deux plages chevauchantes (s[2..0] puis
     * s[3..1], bits 1 et 2 communs) doivent lever DUPLICATE_LHS.
     */
    @Test
    public void overlappingRanges_throws() {
        try {
            build("module m (a[3..0]) s[2..0] = a[2..0] s[3..1] = a[3..1] end module");
            fail("Attendu ConversionException DUPLICATE_LHS");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    /**
     * Non-régression : deux plages disjointes du même signal (s[1..0] puis
     * s[3..2]) restent acceptées — plan de 4 affectations.
     */
    @Test
    public void disjointRanges_sameSignal_ok() {
        Module m = build("module m (a[3..0]) s[1..0] = a[1..0] s[3..2] = a[3..2] end module");
        assertEquals(4, m.Plan.size());
    }

    // -----------------------------------------------------------------------
    // Tests — signature Entrees/Sorties (Task 2)
    // -----------------------------------------------------------------------

    /**
     * fa(a,b,cin : s,cout) → Entrees=[a,b,cin], Sorties=[s,cout] (ordre et noms).
     */
    @Test
    public void signature_entreesSorties_colonSepare() {
        Module m = build("module fa (a, b, cin : s, cout) s = a + b cout = a * b end module");
        assertEquals(3, m.Entrees.size());
        assertEquals("a",   m.Entrees.get(0).Nom());
        assertEquals("b",   m.Entrees.get(1).Nom());
        assertEquals("cin", m.Entrees.get(2).Nom());
        assertEquals(2, m.Sorties.size());
        assertEquals("s",    m.Sorties.get(0).Nom());
        assertEquals("cout", m.Sorties.get(1).Nom());
    }

    /**
     * mux(a,b,sel) sans ':' → Sorties vide, tout dans Entrees.
     */
    @Test
    public void signature_sansColon_sortiesVide() {
        Module m = build("module mux (a, b, sel) c = a * b end module");
        assertEquals(3, m.Entrees.size());
        assertEquals("a",   m.Entrees.get(0).Nom());
        assertEquals("b",   m.Entrees.get(1).Nom());
        assertEquals("sel", m.Entrees.get(2).Nom());
        assertTrue(m.Sorties.isEmpty());
    }

    /**
     * Signature avec deux ':' → MODULE_BAD_SEPARATORS.
     */
    @Test
    public void signature_deuxColons_throwsBadSeparators() {
        try {
            build("module bad (a : b : c) x = a end module");
            fail("Attendu ConversionException MODULE_BAD_SEPARATORS");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_BAD_SEPARATORS, ex.reason());
        }
    }

    /**
     * Vérifie que le mapping bit→indice est correct pour une affectation vecteur→vecteur.
     * Source : s[1..0] = a[1..0]
     * Le plan doit contenir 2 AFFECTATION : s[0] câblé sur a[0], s[1] câblé sur a[1].
     * Ce test verrouille l'ordre : s[i] reçoit a[i], pas a[1-i].
     */
    @Test
    public void rangeLhs_vectorRhs_bitMappingIsIdentity() {
        Module m = build("module m (a) s[1..0] = a[1..0] end module");
        assertEquals(2, m.Plan.size());

        // Retrouver l'Erwan d'affectation par son Numero
        Erwan s0 = null, s1 = null;
        for (Erwan e : m.Plan) {
            assertEquals(Operation.AFFECTATION, e.Op);
            assertEquals("s", e.Nom);
            if (Integer.valueOf(0).equals(e.Numero)) s0 = e;
            else if (Integer.valueOf(1).equals(e.Numero)) s1 = e;
        }
        assertNotNull("s[0] absent du plan", s0);
        assertNotNull("s[1] absent du plan", s1);

        // L'entrée directe de chaque affectation est le LITTERAL a[i]
        Erwan src0 = s0.Entrees.get(0);
        Erwan src1 = s1.Entrees.get(0);

        assertEquals("s[0] doit lire a", "a", src0.Nom);
        assertEquals("s[0] doit lire a[0]", Integer.valueOf(0), src0.Numero);

        assertEquals("s[1] doit lire a", "a", src1.Nom);
        assertEquals("s[1] doit lire a[1]", Integer.valueOf(1), src1.Numero);
    }

    /**
     * Composition signature + vecteurs : un paramètre vecteur en entrée et un
     * en sortie doivent traverser buildSignature avec les bons indices.
     * module m (a[3..0] : s[1..0]) → Entrees=[a 0..3], Sorties=[s 0..1].
     */
    @Test
    public void signature_vectorParams_indicesCorrects() {
        Module m = build("module m (a[3..0] : s[1..0]) s[1..0] = a[1..0] end module");
        assertEquals("Entrees doit contenir 1 descripteur", 1, m.Entrees.size());
        assertEquals("Sorties doit contenir 1 descripteur", 1, m.Sorties.size());
        erwan.Descripteur e = m.Entrees.get(0);
        assertEquals("a", e.Nom());
        assertEquals(0, e.indiceDebut());
        assertEquals(3, e.indiceFin());
        assertEquals(4, e.nbSignaux());
        erwan.Descripteur s = m.Sorties.get(0);
        assertEquals("s", s.Nom());
        assertEquals(0, s.indiceDebut());
        assertEquals(1, s.indiceFin());
        assertEquals(2, s.nbSignaux());
    }

    // -----------------------------------------------------------------------
    // Tests Task 5 : appels de modules (ModuleCall → AppelModule)
    // -----------------------------------------------------------------------

    /**
     * Happy path (forme $) : top appelle fa.
     * module fa (a, b : s) s = a + b end module
     * module top (a, b : s) $fa(a, b : s) end module
     * → Branchements de taille 1 ; AppelModule.module.Nom = "fa" ;
     *   DE=2, DS=1 ; top.Plan vide.
     */
    @Test
    public void moduleCall_dollar_happyPath() {
        CstNode cstFa = CstParser.parse("module fa (a, b : s) s = a + b end module");
        CstNode cstTop = CstParser.parse("module top (a, b : s) $fa(a, b : s) end module");
        Module top = Conversion.convert(cstTop, java.util.List.of(cstFa));

        assertEquals("top.Plan doit etre vide (seul contenu = appel)", 0, top.Plan.size());
        assertEquals("top.Branchements doit contenir 1 AppelModule", 1, top.Branchements.size());
        AppelModule am = top.Branchements.get(0);
        assertEquals("module appele doit etre fa", "fa", am.module.Nom);
        assertEquals("DE doit contenir 2 descripteurs (a, b)", 2, am.DE.size());
        assertEquals("DS doit contenir 1 descripteur (s)", 1, am.DS.size());
        assertEquals("top.Entrees", 2, top.Entrees.size());
        assertEquals("top.Sorties", 1, top.Sorties.size());
    }

    /**
     * Happy path (forme sans $) : top appelle fa sans préfixe dollar.
     * module fa (a, b : s) s = a + b end module
     * module top (a, b : s) fa(a, b : s) end module
     * → Branchements de taille 1 ; AppelModule.module.Nom = "fa" ;
     *   DE=2, DS=1 ; top.Plan vide.
     */
    @Test
    public void moduleCall_noDollar_happyPath() {
        CstNode cstFa = CstParser.parse("module fa (a, b : s) s = a + b end module");
        CstNode cstTop = CstParser.parse("module top (a, b : s) fa(a, b : s) end module");
        Module top = Conversion.convert(cstTop, java.util.List.of(cstFa));

        assertEquals("top.Plan doit etre vide (seul contenu = appel)", 0, top.Plan.size());
        assertEquals("top.Branchements doit contenir 1 AppelModule", 1, top.Branchements.size());
        AppelModule am = top.Branchements.get(0);
        assertEquals("module appele doit etre fa", "fa", am.module.Nom);
        assertEquals("DE doit contenir 2 descripteurs (a, b)", 2, am.DE.size());
        assertEquals("DS doit contenir 1 descripteur (s)", 1, am.DS.size());
        assertEquals("top.Entrees", 2, top.Entrees.size());
        assertEquals("top.Sorties", 1, top.Sorties.size());
    }

    /**
     * Appel d'un module inconnu : MODULE_NOT_FOUND.
     */
    @Test
    public void moduleCall_unknownModule_throwsNotFound() {
        CstNode cstTop = CstParser.parse("module top (a, b : s) $missing(a, b : s) end module");
        try {
            Conversion.convert(cstTop, java.util.List.of());
            fail("Attendu ConversionException MODULE_NOT_FOUND");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_NOT_FOUND, ex.reason());
        }
    }

    /**
     * Cycle A appelle B, B appelle A : MODULE_CALL_CYCLE.
     */
    @Test
    public void moduleCall_cycle_throwsCycle() {
        CstNode cstA = CstParser.parse("module a (x : y) $b(x : y) end module");
        CstNode cstB = CstParser.parse("module b (x : y) $a(x : y) end module");
        try {
            Conversion.convert(cstA, java.util.List.of(cstB));
            fail("Attendu ConversionException MODULE_CALL_CYCLE");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_CALL_CYCLE, ex.reason());
        }
    }

    /**
     * Cycle de longueur 1 : un module qui s'appelle lui-même → MODULE_CALL_CYCLE.
     */
    @Test
    public void moduleCall_selfReference_throwsCycle() {
        CstNode cstA = CstParser.parse("module a (x : y) $a(x : y) end module");
        try {
            Conversion.convert(cstA, java.util.List.of());
            fail("Attendu ConversionException MODULE_CALL_CYCLE");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_CALL_CYCLE, ex.reason());
        }
    }

    // -----------------------------------------------------------------------
    // Tests — corrections post-audit adversarial (I1, I2, C3)
    // -----------------------------------------------------------------------

    /**
     * I1 — appel d'un module sans ':' dans sa signature (donc sans sorties
     * déclarées) : MODULE_ARITY_MISMATCH, avec un message explicite sur
     * l'absence de sortie plutôt qu'un décompte d'arité trompeur.
     */
    @Test
    public void moduleCall_calledHasNoColon_throwsClearArityError() {
        CstNode cstFa = CstParser.parse("module fa (a, b, s) s = a + b end module");
        CstNode cstTop = CstParser.parse("module top (a, b : s) $fa(a, b : s) end module");
        try {
            Conversion.convert(cstTop, java.util.List.of(cstFa));
            fail("Attendu ConversionException MODULE_ARITY_MISMATCH");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_ARITY_MISMATCH, ex.reason());
            assertTrue("le message doit signaler l'absence de sortie : " + ex.getMessage(),
                ex.getMessage().contains("aucune sortie"));
        }
    }

    /**
     * I2 — un signal piloté par les sorties de deux appels distincts doit
     * lever DUPLICATE_LHS (double pilotage silencieux auparavant).
     */
    @Test
    public void twoCalls_sameOutputSignal_throwsDuplicateLhs() {
        CstNode cstFa = CstParser.parse("module fa (a, b : s) s = a + b end module");
        CstNode cstTop = CstParser.parse(
            "module top (x, y : s) $fa(x, y : s) $fa(y, x : s) end module");
        try {
            Conversion.convert(cstTop, java.util.List.of(cstFa));
            fail("Attendu ConversionException DUPLICATE_LHS");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    /**
     * I2 — un signal piloté à la fois par la sortie d'un appel et par une
     * affectation doit lever DUPLICATE_LHS.
     */
    @Test
    public void callOutputAndAssignment_sameSignal_throwsDuplicateLhs() {
        CstNode cstFa = CstParser.parse("module fa (a, b : s) s = a + b end module");
        CstNode cstTop = CstParser.parse(
            "module top (x, y : s) $fa(x, y : s) s = x end module");
        try {
            Conversion.convert(cstTop, java.util.List.of(cstFa));
            fail("Attendu ConversionException DUPLICATE_LHS");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_LHS, ex.reason());
        }
    }

    /**
     * C3 — MODULE_NOT_FOUND porte l'offset du site d'appel, pas 0.
     */
    @Test
    public void moduleCall_unknownModule_reportsCallSiteOffset() {
        CstNode cstTop = CstParser.parse(
            "module top (a, b : s) $missing(a, b : s) end module");
        try {
            Conversion.convert(cstTop, java.util.List.of());
            fail("Attendu ConversionException MODULE_NOT_FOUND");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_NOT_FOUND, ex.reason());
            assertTrue("l'offset doit pointer le site d'appel, pas 0 (C3)",
                ex.offset() > 0);
        }
    }
}
