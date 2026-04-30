package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
import parser.ll1.tabledriven.lexer.ShdlLexer;

import java.util.List;

public class CstParserModuleTest {

    // Source minimal valide pour la grammaire SHDL :
    // module foo (a) i = .0 end module
    // Module ::= ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW
    // Param ::= Signal ; Signal ::= Identifiant Signal_Subset_Opt ; Signal_Subset_Opt ::= ε
    // Separ_Param_Star ::= ε
    // Instance_Plus ::= Instance Instance_Star ; Instance_Star ::= ε
    // Instance ::= Identifiant Operation ; Operation ::= Signal_Subset_Opt Assignment
    // Assignment ::= SignalAssignment ; SignalAssignment ::= AssignOp SumOfTermsCompound
    // SumOfTermsCompound ::= SumOfTerms Concat_SumOfTerms_Star
    // SumOfTerms ::= Term Or_Operand_Star ; Term ::= Factor And_Operand_Star ; Factor ::= LiteralValue
    // LiteralValue ::= BitField (.0)
    private static final String SRC_MINIMAL = "module foo (a) i = .0 end module";

    @Test
    public void module_minimal_passe() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        assertNotNull(root);
        assertInstanceOf(CstInternal.class, root);
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
        // "module a (x) i = .0 end module"
        String src = "module a (x) i = .0 end module";
        CstNode root = CstParser.parse(src);
        CstNode moduleNode = root.first(NonTerminal.Module).orElseThrow();
        // Le noeud Module doit avoir LeftPar, RightPar, et Param parmi ses enfants
        assertTrue("Module doit contenir LeftPar",
                moduleNode.has(new Terminal(TokenType.LeftPar)));
        assertTrue("Module doit contenir Param",
                moduleNode.has(NonTerminal.Param));
        assertTrue("Module doit contenir RightPar",
                moduleNode.has(new Terminal(TokenType.RightPar)));
    }

    @Test
    public void module_eof_correctement_consomme() {
        CstNode root = CstParser.parse(SRC_MINIMAL);
        assertFalse("EOF ne doit pas apparaître dans le CST",
                containsEof(root));
    }

    @Test
    public void parse_tokens_isole_marche() {
        List<Token> tokens = ShdlLexer.tokenize(SRC_MINIMAL);
        CstNode fromTokens = CstParser.parseTokens(tokens);
        CstNode fromString = CstParser.parse(SRC_MINIMAL);
        assertNotNull(fromTokens);
        // Même structure : même type de noeud et mêmes offsets
        assertEquals(fromString.startOffset(), fromTokens.startOffset());
        assertEquals(fromString.endOffset(), fromTokens.endOffset());
        assertEquals(fromString.symbol(), fromTokens.symbol());
    }

    @Test
    public void parse_null_jette_npe() {
        assertThrows(NullPointerException.class, () -> CstParser.parse(null));
        assertThrows(NullPointerException.class, () -> CstParser.parseTokens(null));
    }

    // -----------------------------------------------------------------------
    // Utilitaire

    private static boolean containsEof(CstNode node) {
        if (node instanceof CstLeaf leaf) {
            return leaf.t().getType() == TokenType.EOF;
        }
        if (node instanceof CstInternal internal) {
            for (CstNode child : internal.children()) {
                if (containsEof(child)) return true;
            }
        }
        return false;
    }
}
