package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

/**
 * Verifie qu'une production epsilon produit bien un CstInternal vide
 * avec startOffset == endOffset (review 18a).
 */
public class CstParserEpsilonTest {

    /**
     * Dans "module m (a) i = .0 end module", le signal 'a' n'a pas d'index.
     * Donc Signal_Subset_Opt → epsilon.
     * On navigue : Start > Module > Param > Signal > Signal_Subset_Opt
     * et on verifie que le noeud est vide et que ses offsets sont egaux.
     */
    @Test
    public void signal_subset_opt_epsilon_enfants_vides_offsets_egaux() {
        String src = "module m (a) i = .0 end module";
        CstNode root = CstParser.parse(src);

        // Start > Module
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        // Module > Param
        CstInternal param = (CstInternal) module.first(NonTerminal.Param).orElseThrow();
        // Param > Signal
        CstInternal signal = (CstInternal) param.first(NonTerminal.Signal).orElseThrow();
        // Signal > Signal_Subset_Opt
        CstInternal subsetOpt = (CstInternal) signal.first(NonTerminal.Signal_Subset_Opt)
                .orElseThrow(() -> new AssertionError("Signal_Subset_Opt manquant"));

        assertTrue("Signal_Subset_Opt epsilon doit avoir children vide",
                subsetOpt.children().isEmpty());
        assertEquals("startOffset doit egal endOffset pour noeud epsilon",
                subsetOpt.startOffset(), subsetOpt.endOffset());
    }

    /**
     * Instance_Star → epsilon pour un module avec une seule instance.
     * Le module "module m (a) i = .0 end module" a exactement une instance.
     * Instance_Plus → Instance Instance_Star => Instance_Star doit etre epsilon.
     */
    @Test
    public void instance_star_epsilon_enfants_vides_offsets_egaux() {
        String src = "module m (a) i = .0 end module";
        CstNode root = CstParser.parse(src);

        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal istar = (CstInternal) iplus.first(NonTerminal.Instance_Star).orElseThrow();

        assertTrue("Instance_Star epsilon doit avoir children vide",
                istar.children().isEmpty());
        assertEquals("startOffset doit egal endOffset pour noeud epsilon",
                istar.startOffset(), istar.endOffset());
    }
}
