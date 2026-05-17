package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.lexer.Lexem;
import parser.lexer.Token;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

public class ConversionErrorsTest {

    private static ConversionException convertAndCatch(String src) {
        CstNode root = CstParser.parse(src);
        try {
            Conversion.convert(root);
            return null;
        } catch (ConversionException ex) {
            return ex;
        }
    }

    @Test
    public void concat_rejected() {
        ConversionException ex = convertAndCatch("module m (a, b) c = a & b end module");
        assertNotNull("attendu ConversionException", ex);
        assertEquals(Reason.CONCAT_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void memoryAssignment_rejected() {
        // MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms Comma_Opt Set_Or_Reset WhenKW SumOfTerms Enabled_Operand_Opt Semicolon_Opt
        ConversionException ex = convertAndCatch(
            "module m (a) c := a on a , reset when a end module");
        assertNotNull(ex);
        assertEquals(Reason.MEMORY_ASSIGNMENT_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void literalInRhs_rejected() {
        // BitField ::= \.[0-1]+  => .1 est valide
        ConversionException ex = convertAndCatch("module m (a) c = .1 end module");
        assertNotNull(ex);
        assertEquals(Reason.LITERAL_IN_RHS_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorLhs_singleIndex_accepted_task4() {
        // Task 4 : LHS index unique c[0] = a (RHS scalaire) est desormais supporte.
        ConversionException ex = convertAndCatch("module m (a) c[0] = a end module");
        assertNull("c[0] = a doit etre accepte depuis Task 4", ex);
    }

    @Test
    public void vectorRhs_accepted_task3() {
        // Task 3 : les vecteurs en RHS sont desormais supportes.
        // c = a[3] avec LHS scalaire et RHS largeur 1 doit reussir sans exception.
        ConversionException ex = convertAndCatch("module m (a) c = a[3] end module");
        assertNull("a[3] en RHS doit etre accepte depuis Task 3", ex);
    }

    @Test
    public void vectorParam_accepted() {
        // Task 5 : un parametre vecteur (a[0..3]) est desormais accepte.
        ConversionException ex = convertAndCatch("module m (a[0..3]) c[3..0] = a[3..0] end module");
        assertNull("parametre vecteur doit etre accepte depuis Task 5", ex);
    }

    @Test
    public void vectorWidthMismatch_rhsLargerThanLhsScalar() {
        // LHS scalaire c, RHS vecteur largeur 4 : VECTOR_WIDTH_MISMATCH
        ConversionException ex = convertAndCatch("module m (a) c = a[3..0] end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
    }

    @Test
    public void vectorWidthMismatch_operandsDifferentWidths() {
        // a[3..0] largeur 4, b[1..0] largeur 2 : VECTOR_WIDTH_MISMATCH dans AND
        ConversionException ex = convertAndCatch("module m (a, b) c[3..0] = a[3..0] * b[1..0] end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_WIDTH_MISMATCH, ex.reason());
    }

    // ------------------------------------------------------------------
    // I6 : malformed CST -> ConversionException(MALFORMED_CST)
    // Un CstLeaf passe directement a Conversion.convert() : avant fix,
    // on obtient ClassCastException (cast brut) ; apres fix, MALFORMED_CST.
    // ------------------------------------------------------------------

    @Test
    public void malformedCst_leafAsRoot_throwsMalformedCst() {
        Lexem<Token> lexem = new Lexem<>(Token.ModuleKW);
        lexem.storeMatched(0, "module");
        CstNode leaf = new CstLeaf(new Terminal(Token.ModuleKW), lexem);
        try {
            Conversion.convert(leaf);
            fail("Attendu ConversionException avec MALFORMED_CST");
        } catch (ConversionException ex) {
            assertEquals(Reason.MALFORMED_CST, ex.reason());
        }
    }

    @Test
    public void offsetIsCoherent_concat() {
        // 'a & b' : le '&' est apres "module m (a, b) c = a " soit offset 22
        ConversionException ex = convertAndCatch("module m (a, b) c = a & b end module");
        assertNotNull(ex);
        assertTrue("offset doit pointer vers la zone du concat",
            ex.offset() >= 20 && ex.offset() <= 26);
    }
}
