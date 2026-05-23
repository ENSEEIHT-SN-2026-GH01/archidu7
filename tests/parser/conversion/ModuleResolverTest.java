package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import parser.conversion.ModuleResolver;
import parser.conversion.ConversionException;
import parser.conversion.ConversionException.Reason;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
import parser.lexer.Lexem;
import parser.lexer.Token;
import parser.ll1.grammar.Terminal;
import erwan.Module;

public class ModuleResolverTest {

    private static CstNode parse(String src) {
        return CstParser.parse(src);
    }

    // ------------------------------------------------------------------
    // Indexation + resolve d'un module simple
    // ------------------------------------------------------------------

    @Test
    public void simpleModule_indexAndResolve() {
        CstNode root = parse("module fa (a, b : s) s = a + b end module");
        ModuleResolver resolver = new ModuleResolver(List.of(root));

        assertEquals("fa", resolver.mainName());

        Module m = resolver.resolve("fa");
        assertNotNull(m);
        assertEquals("fa", m.Nom);
    }

    // ------------------------------------------------------------------
    // Deux fichiers de même nom → DUPLICATE_MODULE_DEFINITION
    // ------------------------------------------------------------------

    @Test
    public void duplicateName_throwsDuplicateModuleDefinition() {
        CstNode root1 = parse("module fa (a : s) s = a end module");
        CstNode root2 = parse("module fa (x : y) y = x end module");

        try {
            new ModuleResolver(List.of(root1, root2));
            fail("Attendu ConversionException DUPLICATE_MODULE_DEFINITION");
        } catch (ConversionException ex) {
            assertEquals(Reason.DUPLICATE_MODULE_DEFINITION, ex.reason());
        }
    }

    // ------------------------------------------------------------------
    // Nom absent → MODULE_NOT_FOUND
    // ------------------------------------------------------------------

    @Test
    public void absentName_throwsModuleNotFound() {
        CstNode root = parse("module fa (a : s) s = a end module");
        ModuleResolver resolver = new ModuleResolver(List.of(root));

        try {
            resolver.resolve("inexistant");
            fail("Attendu ConversionException MODULE_NOT_FOUND");
        } catch (ConversionException ex) {
            assertEquals(Reason.MODULE_NOT_FOUND, ex.reason());
        }
    }

    // ------------------------------------------------------------------
    // Mémoïsation : deux resolve du même nom → même instance
    // ------------------------------------------------------------------

    @Test
    public void memoization_sameInstanceReturned() {
        CstNode root = parse("module fa (a : s) s = a end module");
        ModuleResolver resolver = new ModuleResolver(List.of(root));

        Module m1 = resolver.resolve("fa");
        Module m2 = resolver.resolve("fa");

        assertSame("deux resolve du meme module doivent retourner la meme instance", m1, m2);
    }

    // ------------------------------------------------------------------
    // mainName() retourne le nom du PREMIER fichier, pas des suivants
    // ------------------------------------------------------------------

    @Test
    public void mainName_firstFileWins() {
        CstNode root1 = parse("module fa (a : s) s = a end module");
        CstNode root2 = parse("module or2 (a, b : s) s = a + b end module");
        CstNode root3 = parse("module and2 (a, b : s) s = a * b end module");

        ModuleResolver resolver = new ModuleResolver(List.of(root1, root2, root3));

        assertEquals("mainName() doit retourner le nom du premier fichier",
            "fa", resolver.mainName());
    }

    // ------------------------------------------------------------------
    // MALFORMED_CST : feuille passée comme racine
    // ------------------------------------------------------------------

    @Test
    public void malformedCst_leafAsRoot_throwsMalformedCst() {
        Lexem<Token> lexem = new Lexem<>(Token.ModuleKW);
        lexem.storeMatched(0, "module");
        CstNode leaf = new CstLeaf(new Terminal(Token.ModuleKW), lexem);

        try {
            new ModuleResolver(List.of(leaf));
            fail("Attendu ConversionException MALFORMED_CST");
        } catch (ConversionException ex) {
            assertEquals(Reason.MALFORMED_CST, ex.reason());
        }
    }
}
