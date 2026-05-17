package tests.parser.conversion;

import org.junit.Test;
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
    public void allReasonsExposedByEnum() {
        // 7 motifs S1 + 6 motifs appels de modules (Task 1).
        // Task 5 retirera MODULE_CALL_NOT_SUPPORTED : ce compte passera a 12.
        Reason[] reasons = Reason.values();
        assertEquals(13, reasons.length);
    }
}
