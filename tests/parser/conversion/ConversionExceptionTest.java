package tests.parser.conversion;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;

public class ConversionExceptionTest {

    @Test
    public void exposeOffsetNodeKindAndReason() {
        ConversionException ex = new ConversionException(
            42, "Signal", Reason.MALFORMED_CST,
            "Structure CST invalide (offset 42)"
        );
        assertEquals(42, ex.offset());
        assertEquals("Signal", ex.nodeKind());
        assertEquals(Reason.MALFORMED_CST, ex.reason());
        assertEquals("Structure CST invalide (offset 42)", ex.getMessage());
    }

    @Test
    public void isRuntimeException() {
        ConversionException ex = new ConversionException(0, "X", Reason.MALFORMED_CST, "x");
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    @Ignore("diverge de main (lignee apparence) - cf branche test/bascule-d-integration")
    public void allReasonsExposedByEnum() {
        // 6 motifs S1 + 6 motifs appels de modules = 12.
        Reason[] reasons = Reason.values();
        assertEquals(12, reasons.length);
    }
}
