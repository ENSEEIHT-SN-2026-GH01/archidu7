package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

/**
 * Tests CstParser pour SignalAssignment (Task 8.B).
 *
 * SignalAssignment ::= AssignOp SumOfTermsCompound
 */
public class CstParserAssignmentTest {

    /**
     * SignalAssignment simple : c = a + b
     * "module m (a, b) c = a + b end module"
     */
    @Test
    public void signal_assignment_simple() {
        String src = "module m (a, b) c = a + b end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        assertTrue("Assignment doit contenir SignalAssignment",
                assign.first(NonTerminal.SignalAssignment).isPresent());

        CstInternal sigA = (CstInternal) assign.first(NonTerminal.SignalAssignment).orElseThrow();
        assertTrue("SignalAssignment doit contenir AssignOp",
                sigA.has(new Terminal(Token.AssignOp)));
        assertTrue("SignalAssignment doit contenir SumOfTermsCompound",
                sigA.first(NonTerminal.SumOfTermsCompound).isPresent());
    }

    /**
     * Verifie les offsets de la racine couvrent toute la source.
     */
    @Test
    public void signal_assignment_offsets() {
        String src = "module m (a, b) c = a + b end module";
        CstNode root = CstParser.parse(src);
        assertEquals(0, root.startOffset());
        assertEquals(src.length(), root.endOffset());
    }

    /**
     * SignalAssignment avec expression plus complexe (parens).
     * "module m (a, b) c = (a + b) end module"
     */
    @Test
    public void signal_assignment_expression_avec_parens() {
        String src = "module m (a, b) c = (a + b) end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(src.length(), root.endOffset());
    }
}
