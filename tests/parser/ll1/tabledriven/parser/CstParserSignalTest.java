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
 * Tests CstParser pour les Signal et leurs variantes de range/index (Task 8.B).
 */
public class CstParserSignalTest {

    /**
     * Signal sans range : Signal_Subset_Opt -> epsilon.
     * "module m (a) i = .0 end module"
     */
    @Test
    public void signal_sans_range_subset_opt_epsilon() {
        String src = "module m (a) i = .0 end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        // Navigue jusqu'au Signal_Subset_Opt du premier param
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal param  = (CstInternal) module.first(NonTerminal.Param).orElseThrow();
        CstInternal signal = (CstInternal) param.first(NonTerminal.Signal).orElseThrow();
        CstInternal ssOpt  = (CstInternal) signal.first(NonTerminal.Signal_Subset_Opt).orElseThrow();

        assertTrue("Signal_Subset_Opt doit etre epsilon (children vide)", ssOpt.children().isEmpty());
        assertEquals("startOffset == endOffset pour epsilon", ssOpt.startOffset(), ssOpt.endOffset());
    }

    /**
     * Signal avec index simple : a[3]
     * "module m (a[3]) i = .0 end module"
     */
    @Test
    public void signal_avec_index_simple() {
        String src = "module m (a[3]) i = .0 end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal param  = (CstInternal) module.first(NonTerminal.Param).orElseThrow();
        CstInternal signal = (CstInternal) param.first(NonTerminal.Signal).orElseThrow();
        CstInternal ssOpt  = (CstInternal) signal.first(NonTerminal.Signal_Subset_Opt).orElseThrow();

        assertFalse("Signal_Subset_Opt doit avoir des enfants", ssOpt.children().isEmpty());
        assertTrue("Doit contenir LeftSquareBrack", ssOpt.has(new Terminal(Token.LeftSquareBrack)));
        assertTrue("Doit contenir NaturalInteger", ssOpt.has(new Terminal(Token.NaturalInteger)));
        assertTrue("Doit contenir RightSquareBrack", ssOpt.has(new Terminal(Token.RightSquareBrack)));

        // Range_Opt doit etre epsilon (pas de range)
        CstInternal rangeOpt = (CstInternal) ssOpt.first(NonTerminal.Range_Opt).orElseThrow();
        assertTrue("Range_Opt doit etre epsilon", rangeOpt.children().isEmpty());
    }

    /**
     * Signal avec range PointPoint : a[3..0]
     * "module m (a[3..0]) i = .0 end module"
     */
    @Test
    public void signal_avec_range_pointpoint() {
        String src = "module m (a[3..0]) i = .0 end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal param  = (CstInternal) module.first(NonTerminal.Param).orElseThrow();
        CstInternal signal = (CstInternal) param.first(NonTerminal.Signal).orElseThrow();
        CstInternal ssOpt  = (CstInternal) signal.first(NonTerminal.Signal_Subset_Opt).orElseThrow();
        CstInternal rangeOpt = (CstInternal) ssOpt.first(NonTerminal.Range_Opt).orElseThrow();

        assertFalse("Range_Opt doit avoir des enfants", rangeOpt.children().isEmpty());
        // DotDot -> PointPoint
        CstInternal dotdot = (CstInternal) rangeOpt.first(NonTerminal.DotDot).orElseThrow();
        assertTrue("DotDot doit contenir PointPoint", dotdot.has(new Terminal(Token.PointPoint)));
    }

    /**
     * Signal avec range Colon : a[3:0]
     * "module m (a[3:0]) i = .0 end module"
     */
    @Test
    public void signal_avec_range_colon() {
        String src = "module m (a[3:0]) i = .0 end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal param  = (CstInternal) module.first(NonTerminal.Param).orElseThrow();
        CstInternal signal = (CstInternal) param.first(NonTerminal.Signal).orElseThrow();
        CstInternal ssOpt  = (CstInternal) signal.first(NonTerminal.Signal_Subset_Opt).orElseThrow();
        CstInternal rangeOpt = (CstInternal) ssOpt.first(NonTerminal.Range_Opt).orElseThrow();

        assertFalse("Range_Opt doit avoir des enfants", rangeOpt.children().isEmpty());
        // DotDot -> Colon
        CstInternal dotdot = (CstInternal) rangeOpt.first(NonTerminal.DotDot).orElseThrow();
        assertTrue("DotDot doit contenir Colon", dotdot.has(new Terminal(Token.Colon)));
    }
}
