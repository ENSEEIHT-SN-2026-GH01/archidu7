package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.conversion.Names;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

public class NamesTest {

    private static CstNode firstSignal(String src) {
        CstNode root = CstParser.parse(src);
        // Start -> Module -> ... -> Param -> Signal
        return root.first(NonTerminal.Module).orElseThrow()
                   .first(NonTerminal.Param).orElseThrow()
                   .first(NonTerminal.Signal).orElseThrow();
    }

    @Test
    public void extractScalarFromSignalNT_scalarSignal_returnsName() {
        CstNode sig = firstSignal("module m (alpha) c = alpha end module");
        assertEquals("alpha", Names.extractScalarFromSignalNT(sig));
    }

    @Test(expected = ConversionException.class)
    public void extractScalarFromSignalNT_vectorSignal_throws() {
        CstNode sig = firstSignal("module m (alpha[0..3]) c = alpha end module");
        Names.extractScalarFromSignalNT(sig);
    }

    @Test
    public void extractScalarFromSignalNT_vectorSignal_reasonIsVector() {
        CstNode sig = firstSignal("module m (alpha[0..3]) c = alpha end module");
        try {
            Names.extractScalarFromSignalNT(sig);
            fail("expected ConversionException");
        } catch (ConversionException ex) {
            assertEquals(Reason.VECTOR_SUBSET_NOT_SUPPORTED, ex.reason());
        }
    }
}
