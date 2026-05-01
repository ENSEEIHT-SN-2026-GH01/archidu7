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
 * Tests CstParser pour ModuleCall (Task 8.B).
 *
 * Grammaire :
 *   Instance ::= Identifiant Operation | Dollar Identifiant ModuleCall
 *   Operation ::= ModuleCall | Signal_Subset_Opt Assignment
 *   ModuleCall ::= LeftPar Arg Separ_Arg_Star RightPar
 *
 * Un appel non-memoire : "fullAdder(a, b)" => Instance = Identifiant Operation = Identifiant ModuleCall
 * Un appel memoire    : "$fullAdder(a, b)" => Instance = Dollar Identifiant ModuleCall
 */
public class CstParserModuleCallTest {

    /**
     * Appel non-memoire : fullAdder(a, b)
     * "module m (a, b) fullAdder(a, b) end module"
     */
    @Test
    public void module_call_non_memoire() {
        String src = "module m (a, b) fullAdder(a, b) end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(src.length(), root.endOffset());

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();

        // Instance = Identifiant Operation
        assertTrue("Instance doit contenir Identifiant",
                ins.has(new Terminal(Token.Identifiant)));
        assertTrue("Instance doit contenir Operation",
                ins.first(NonTerminal.Operation).isPresent());

        // Operation = ModuleCall
        CstInternal op = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        assertTrue("Operation doit contenir ModuleCall",
                op.first(NonTerminal.ModuleCall).isPresent());

        // ModuleCall = LeftPar Arg Separ_Arg_Star RightPar
        CstInternal call = (CstInternal) op.first(NonTerminal.ModuleCall).orElseThrow();
        assertTrue("ModuleCall doit contenir LeftPar", call.has(new Terminal(Token.LeftPar)));
        assertTrue("ModuleCall doit contenir RightPar", call.has(new Terminal(Token.RightPar)));
        assertTrue("ModuleCall doit contenir Arg", call.first(NonTerminal.Arg).isPresent());
    }

    /**
     * Appel memoire : $fullAdder(a, b)
     * "module m (a, b) $fullAdder(a, b) end module"
     */
    @Test
    public void module_call_memoire_dollar() {
        String src = "module m (a, b) $fullAdder(a, b) end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        assertEquals(0, root.startOffset());
        assertEquals(src.length(), root.endOffset());

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();

        // Instance = Dollar Identifiant ModuleCall
        assertTrue("Instance doit contenir Dollar", ins.has(new Terminal(Token.Dollar)));
        assertTrue("Instance doit contenir ModuleCall",
                ins.first(NonTerminal.ModuleCall).isPresent());

        CstInternal call = (CstInternal) ins.first(NonTerminal.ModuleCall).orElseThrow();
        assertTrue("ModuleCall doit contenir LeftPar", call.has(new Terminal(Token.LeftPar)));
        assertTrue("ModuleCall doit contenir RightPar", call.has(new Terminal(Token.RightPar)));
    }

    /**
     * Separ_Arg_Star avec plusieurs args separes par virgule.
     * "module m (a, b) fullAdder(a, b) end module"
     * => Separ_Arg_Star non-epsilon.
     */
    @Test
    public void module_call_separ_arg_star_non_epsilon() {
        String src = "module m (a, b) fullAdder(a, b) end module";
        CstNode root = CstParser.parse(src);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal call   = (CstInternal) op.first(NonTerminal.ModuleCall).orElseThrow();
        CstInternal sarStar = (CstInternal) call.first(NonTerminal.Separ_Arg_Star).orElseThrow();
        assertFalse("Separ_Arg_Star doit etre non-epsilon (plusieurs args)",
                sarStar.children().isEmpty());
    }
}
