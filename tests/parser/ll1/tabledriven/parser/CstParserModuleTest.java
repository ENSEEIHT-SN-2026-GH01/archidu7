package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

public class CstParserModuleTest {

    // Source minimal valide pour la grammaire SHDL :
    // module foo (a) i = .0 end module
    private static final String SRC_MINIMAL = "module foo (a) i = .0 end module";

    @Test
    public void module_minimal_passe() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        assertNotNull(root);
        assertTrue("root doit etre un CstInternal", root instanceof CstInternal);
        CstInternal internal = (CstInternal) root;
        assertEquals(NonTerminal.Start, internal.nt());
        assertTrue("Start doit contenir un enfant Module",
                root.first(NonTerminal.Module).isPresent());
    }

    @Test
    public void module_offsets_couvrent_la_source() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        assertEquals(0, root.startOffset());
        assertEquals(SRC_MINIMAL.length(), root.endOffset());
    }

    @Test
    public void module_avec_param_unique() {
        String src = "module a (x) i = .0 end module";
        CstNode root = CstParser.parse(src);
        CstNode moduleNode = root.first(NonTerminal.Module).orElseThrow();
        assertTrue("Module doit contenir LeftPar",
                moduleNode.has(new Terminal(Token.LeftPar)));
        assertTrue("Module doit contenir Param",
                moduleNode.has(NonTerminal.Param));
        assertTrue("Module doit contenir RightPar",
                moduleNode.has(new Terminal(Token.RightPar)));
    }

    @Test
    public void module_eof_correctement_consomme() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        assertFalse("EOF ne doit pas apparaitre dans le CST",
                containsEof(root));
    }

    @Test
    public void parse_null_jette_npe() {
        assertThrows(NullPointerException.class, () -> CstParser.parse(null));
    }

    // -----------------------------------------------------------------------
    // Utilitaire

    private static boolean containsEof(CstNode node) {
        if (node instanceof CstLeaf leaf) {
            return leaf.t().getType() == Token.EOF;
        }
        if (node instanceof CstInternal internal) {
            for (CstNode child : internal.children()) {
                if (containsEof(child)) return true;
            }
        }
        return false;
    }
}
