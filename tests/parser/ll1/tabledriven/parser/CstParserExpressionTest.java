package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;

/**
 * Tests CstParser pour les expressions (Task 8.B).
 *
 * Grammaire des expressions :
 *   SumOfTermsCompound = SumOfTerms Concat_SumOfTerms_Star
 *   SumOfTerms = Term Or_Operand_Star
 *   Term = Factor And_Operand_Star
 *   Factor = LeftPar SumOfTerms RightPar | LiteralValue | NotOp Signal | Signal
 *   AndOp = Star
 *   OrOp = OrOp (token)
 *   ConcatOp = ConcatOp (token)
 */
public class CstParserExpressionTest {

    /** Helper : retourne le noeud SumOfTermsCompound d'une instance simple. */
    private CstInternal getSumOfTermsCompound(String src) {
        CstNode root = CstParser.parse(src);
        CstInternal module = (CstInternal) root.first(NonTerminal.Module).orElseThrow();
        CstInternal iplus  = (CstInternal) module.first(NonTerminal.Instance_Plus).orElseThrow();
        CstInternal ins    = (CstInternal) iplus.first(NonTerminal.Instance).orElseThrow();
        CstInternal op     = (CstInternal) ins.first(NonTerminal.Operation).orElseThrow();
        CstInternal assign = (CstInternal) op.first(NonTerminal.Assignment).orElseThrow();
        CstInternal sigA   = (CstInternal) assign.first(NonTerminal.SignalAssignment).orElseThrow();
        return (CstInternal) sigA.first(NonTerminal.SumOfTermsCompound).orElseThrow();
    }

    /**
     * Expression avec + (OrOp) et * (Star/AndOp) : a + b * c
     * "module m (a, b, c) o = a + b * c end module"
     * Priorite : * > + => Term = b * c, SumOfTerms = a + (b*c)
     */
    @Test
    public void expression_or_et_and() {
        String src = "module m (a, b, c) o = a + b * c end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc = getSumOfTermsCompound(src);
        assertNotNull(sotc);
        // SumOfTerms doit contenir Or_Operand_Star non-epsilon
        CstInternal sot = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal orStar = (CstInternal) sot.first(NonTerminal.Or_Operand_Star).orElseThrow();
        assertFalse("Or_Operand_Star doit etre non-epsilon (il y a un +)", orStar.children().isEmpty());
    }

    /**
     * NotOp : /a => Factor = NotOp Signal
     * "module m (a) o = /a end module"
     */
    @Test
    public void expression_not_op() {
        String src = "module m (a) o = /a end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc  = getSumOfTermsCompound(src);
        CstInternal sot   = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal term  = (CstInternal) sot.first(NonTerminal.Term).orElseThrow();
        CstInternal factor = (CstInternal) term.first(NonTerminal.Factor).orElseThrow();
        // Factor contient Signal_Subset_Opt via Signal => verifie qu'il a Signal
        assertTrue("Factor doit contenir Signal (via NotOp Signal)",
                factor.first(NonTerminal.Signal).isPresent());
    }

    /**
     * Parentheses : (a + b) * a
     * "module m (a, b) o = (a + b) * a end module"
     */
    @Test
    public void expression_parentheses() {
        String src = "module m (a, b) o = (a + b) * a end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc  = getSumOfTermsCompound(src);
        CstInternal sot   = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal term  = (CstInternal) sot.first(NonTerminal.Term).orElseThrow();
        CstInternal factor = (CstInternal) term.first(NonTerminal.Factor).orElseThrow();
        // Factor contient un SumOfTerms (production LeftPar SumOfTerms RightPar)
        assertTrue("Factor doit contenir SumOfTerms pour la parenthese",
                factor.first(NonTerminal.SumOfTerms).isPresent());
    }

    /**
     * Concat (&) : a & b & c
     * "module m (a, b, c) o = a & b & c end module"
     * ConcatOp est au niveau SumOfTermsCompound.
     */
    @Test
    public void expression_concat() {
        String src = "module m (a, b, c) o = a & b & c end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc = getSumOfTermsCompound(src);
        CstInternal csotS = (CstInternal) sotc.first(NonTerminal.Concat_SumOfTerms_Star).orElseThrow();
        assertFalse("Concat_SumOfTerms_Star doit etre non-epsilon (il y a des &)",
                csotS.children().isEmpty());
    }

    /**
     * BitField simple : .0
     * "module m (a) o = .0 end module"
     * Factor = LiteralValue = BitField
     */
    @Test
    public void expression_bitfield() {
        String src = "module m (a) o = .0 end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc  = getSumOfTermsCompound(src);
        CstInternal sot   = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal term  = (CstInternal) sot.first(NonTerminal.Term).orElseThrow();
        CstInternal factor = (CstInternal) term.first(NonTerminal.Factor).orElseThrow();
        assertTrue("Factor doit contenir LiteralValue pour BitField",
                factor.first(NonTerminal.LiteralValue).isPresent());
    }

    /**
     * Signal simple : a
     * "module m (a) o = a end module"
     * Factor = Signal
     */
    @Test
    public void expression_signal_simple() {
        String src = "module m (a) o = a end module";
        CstNode root = CstParser.parse(src);
        assertNotNull(root);
        CstInternal sotc  = getSumOfTermsCompound(src);
        CstInternal sot   = (CstInternal) sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        CstInternal term  = (CstInternal) sot.first(NonTerminal.Term).orElseThrow();
        CstInternal factor = (CstInternal) term.first(NonTerminal.Factor).orElseThrow();
        assertTrue("Factor doit contenir Signal",
                factor.first(NonTerminal.Signal).isPresent());
    }
}
