package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Conversion;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.tabledriven.CstParser;
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
    public void moduleCall_rejected() {
        ConversionException ex = convertAndCatch("module m (a, b, c) $add(a, b, c) end module");
        assertNotNull(ex);
        assertEquals(Reason.MODULE_CALL_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void literalInRhs_rejected() {
        // BitField ::= \.[0-1]+  => .1 est valide
        ConversionException ex = convertAndCatch("module m (a) c = .1 end module");
        assertNotNull(ex);
        assertEquals(Reason.LITERAL_IN_RHS_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorLhs_rejected() {
        // Signal_Subset_Opt ::= LeftSquareBrack NaturalInteger Range_Opt RightSquareBrack
        ConversionException ex = convertAndCatch("module m (a) c[0] = a end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorRhs_rejected() {
        ConversionException ex = convertAndCatch("module m (a) c = a[3] end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
    }

    @Test
    public void vectorParam_rejected() {
        // Signal_Subset_Opt dans la liste de parametres
        ConversionException ex = convertAndCatch("module m (a[0..3]) c = a end module");
        assertNotNull(ex);
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
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
