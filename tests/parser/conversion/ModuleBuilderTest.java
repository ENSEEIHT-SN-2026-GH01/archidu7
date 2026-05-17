package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ModuleBuilder;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Erwan;
import erwan.Operation;
import erwan.Module;

public class ModuleBuilderTest {

    private static Module build(String src) {
        CstNode root = CstParser.parse(src);
        CstNode mod = root.first(NonTerminal.Module).orElseThrow();
        return ModuleBuilder.build(mod);
    }

    @Test
    public void singleParam_singleInstance() {
        Module m = build("module m (a) c = a end module");
        assertEquals(1, m.Plan.size());
        Erwan aff = m.Plan.get(0);
        assertEquals(Operation.AFFECTATION, aff.Op);
        assertEquals("c", aff.Nom());
        assertTrue(m.Entrees.isEmpty());
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

    @Test(expected = ConversionException.class)
    public void vectorParam_throws() {
        build("module m (a[0..3]) c = a end module");
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
}
