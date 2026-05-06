package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;

public class ConversionExceptionTest {

    @Test
    public void exposeOffsetNodeKindAndReason() {
        ConversionException ex = new ConversionException(
            42, "Signal", Reason.VECTOR_SUBSET_NOT_SUPPORTED,
            "Vecteur non supporte en S1 (offset 42)"
        );
        assertEquals(42, ex.offset());
        assertEquals("Signal", ex.nodeKind());
        assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
        assertEquals("Vecteur non supporte en S1 (offset 42)", ex.getMessage());
    }

    @Test
    public void isRuntimeException() {
        ConversionException ex = new ConversionException(0, "X", Reason.MALFORMED_CST, "x");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    public void allReasonsExposedByEnum() {
        Reason[] reasons = Reason.values();
        assertEquals(7, reasons.length);
    }
}
