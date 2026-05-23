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
 * Tests CstParser pour MemoryAssignment (Task 8.B).
 *
 * Grammaire :
 *   MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms
 *                        Comma_Opt Set_Or_Reset WhenKW SumOfTerms
 *                        Enabled_Operand_Opt Semicolon_Opt
 *
 * Separateur := est MemAssignOp (confirme par la grammaire et le lexer).
 * Note : la grammaire supporte un seul Set_Or_Reset ; la forme avec
 * "set when ... , reset when ..." (deux bascules) n'est pas dans la grammaire.
 */
public class CstParserMemoryAssignmentTest {

    /**
     * MemoryAssignment minimal : q := a on b set when c
     * (Comma_Opt epsilon, Enabled_Operand_Opt epsilon, Semicolon_Opt epsilon)
     */
    @Test
    public void memory_assignment_minimal() {
        String src = "module m (a, b, c) q := a on b set when c end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();

        assertTrue("Assignment doit contenir MemoryAssignment",
                assign.first(NonTerminal.MemoryAssignment).isPresent());

        CstInternal memA = (CstInternal) assign.first(NonTerminal.MemoryAssignment).orElseThrow();
        assertTrue("MemoryAssignment doit contenir MemAssignOp (:=)",
                memA.has(new Terminal(Token.MemAssignOp)));
        assertTrue("MemoryAssignment doit contenir OnKW",
                memA.has(new Terminal(Token.OnKW)));
        assertTrue("MemoryAssignment doit contenir WhenKW",
                memA.has(new Terminal(Token.WhenKW)));
        assertTrue("MemoryAssignment doit contenir Set_Or_Reset",
                memA.first(NonTerminal.Set_Or_Reset).isPresent());
    }

    /**
     * MemoryAssignment avec Comma avant Set_Or_Reset.
     * "q := a on b , set when c"
     */
    @Test
    public void memory_assignment_avec_comma_avant_set() {
        String src = "module m (a, b, c) q := a on b , set when c end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(src.length(), root.endOffset());

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal memA   = (CstInternal) assign.first(NonTerminal.MemoryAssignment).orElseThrow();

        CstInternal commaOpt = (CstInternal) memA.first(NonTerminal.Comma_Opt).orElseThrow();
        assertFalse("Comma_Opt doit etre non-epsilon (comma present)",
                commaOpt.children().isEmpty());
    }

    /**
     * MemoryAssignment avec SetKW.
     * Verifie que Set_Or_Reset contient SetKW.
     */
    @Test
    public void memory_assignment_set_kw() {
        String src = "module m (a, b, c) q := a on b set when c end module";
        CstNode root = CstParser.parse(src);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal memA   = (CstInternal) assign.first(NonTerminal.MemoryAssignment).orElseThrow();
        CstInternal setRst = (CstInternal) memA.first(NonTerminal.Set_Or_Reset).orElseThrow();

        assertTrue("Set_Or_Reset doit contenir SetKW",
                setRst.has(new Terminal(Token.SetKW)));
    }

    /**
     * MemoryAssignment avec ResetKW.
     * Verifie que Set_Or_Reset peut contenir ResetKW.
     */
    @Test
    public void memory_assignment_reset_kw() {
        String src = "module m (a, b, c) q := a on b reset when c end module";
        CstNode root = CstParser.parse(src);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal memA   = (CstInternal) assign.first(NonTerminal.MemoryAssignment).orElseThrow();
        CstInternal setRst = (CstInternal) memA.first(NonTerminal.Set_Or_Reset).orElseThrow();

        assertTrue("Set_Or_Reset doit contenir ResetKW",
                setRst.has(new Terminal(Token.ResetKW)));
    }
}
