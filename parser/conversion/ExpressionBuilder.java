package parser.conversion;

import java.util.ArrayList;
import java.util.List;
import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Erwan.Erwan;

public final class ExpressionBuilder {

    private ExpressionBuilder() {}

    public static Erwan build(CstNode sumOfTermsCompound) {
        return buildSOTC(sumOfTermsCompound);
    }

    private static Erwan buildSOTC(CstNode node) {
        if (!(node instanceof CstInternal sotc) || sotc.nt() != NonTerminal.SumOfTermsCompound) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                ConversionException.Reason.MALFORMED_CST, "Attendu SumOfTermsCompound");
        }
        CstNode concat = sotc.first(NonTerminal.Concat_SumOfTerms_Star).orElseThrow();
        if (concat instanceof CstInternal cs && !cs.children().isEmpty()) {
            throw new ConversionException(concat.startOffset(), "Concat_SumOfTerms_Star",
                ConversionException.Reason.CONCAT_NOT_SUPPORTED,
                "Concatenation '&' non supportee en S1 (offset " + concat.startOffset() + ")");
        }
        CstNode sot = sotc.first(NonTerminal.SumOfTerms).orElseThrow();
        return buildSOT(sot);
    }

    private static Erwan buildSOT(CstNode node) {
        CstInternal sot = (CstInternal) node;
        Erwan first = buildTerm(sot.first(NonTerminal.Term).orElseThrow());
        List<Erwan> operands = collectOrOperands(sot.first(NonTerminal.Or_Operand_Star).orElseThrow());
        if (operands.isEmpty()) return first;
        operands.add(0, first);
        return Erwan.OR(operands);
    }

    private static List<Erwan> collectOrOperands(CstNode orStar) {
        List<Erwan> acc = new ArrayList<>();
        CstInternal node = (CstInternal) orStar;
        while (!node.children().isEmpty()) {
            // Or_Operand_Star -> OrOp Term Or_Operand_Star
            acc.add(buildTerm(node.first(NonTerminal.Term).orElseThrow()));
            node = (CstInternal) node.first(NonTerminal.Or_Operand_Star).orElseThrow();
        }
        return acc;
    }

    private static Erwan buildTerm(CstNode node) {
        CstInternal term = (CstInternal) node;
        Erwan first = buildFactor(term.first(NonTerminal.Factor).orElseThrow());
        List<Erwan> operands = collectAndOperands(term.first(NonTerminal.And_Operand_Star).orElseThrow());
        if (operands.isEmpty()) return first;
        operands.add(0, first);
        return Erwan.AND(operands);
    }

    private static List<Erwan> collectAndOperands(CstNode andStar) {
        List<Erwan> acc = new ArrayList<>();
        CstInternal node = (CstInternal) andStar;
        while (!node.children().isEmpty()) {
            // And_Operand_Star -> AndOp Factor And_Operand_Star
            acc.add(buildFactor(node.first(NonTerminal.Factor).orElseThrow()));
            node = (CstInternal) node.first(NonTerminal.And_Operand_Star).orElseThrow();
        }
        return acc;
    }

    private static Erwan buildFactor(CstNode node) {
        CstInternal factor = (CstInternal) node;
        // Factor -> '(' SOT ')' | LiteralValue | NotOp Signal | Signal
        if (factor.has(new Terminal(Token.LeftPar))) {
            return buildSOT(factor.first(NonTerminal.SumOfTerms).orElseThrow());
        }
        if (factor.has(NonTerminal.LiteralValue)) {
            throw new ConversionException(factor.startOffset(), "Factor",
                ConversionException.Reason.LITERAL_IN_RHS_NOT_SUPPORTED,
                "Constante (.0/.1) en RHS non supportee en S1 (offset " + factor.startOffset() + ")");
        }
        CstNode signal = factor.first(NonTerminal.Signal).orElseThrow();
        Erwan lit = Erwan.LITTERAL(Names.extractScalarFromSignalNT(signal));
        if (factor.has(new Terminal(Token.NotOp))) {
            return Erwan.NOT(lit);
        }
        return lit;
    }
}
