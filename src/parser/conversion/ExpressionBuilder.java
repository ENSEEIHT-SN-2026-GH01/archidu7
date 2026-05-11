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

    private ExpressionBuilder() {
    }

    public static Erwan build(CstNode sumOfTermsCompound) {
        return buildSOTC(sumOfTermsCompound);
    }

    private static Erwan buildSOTC(CstNode node) {
        if (!(node instanceof CstInternal sotc) || sotc.nt() != NonTerminal.SumOfTermsCompound) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu SumOfTermsCompound");
        }
        CstNode concat = sotc.first(NonTerminal.Concat_SumOfTerms_Star)
                .orElseThrow(() -> new ConversionException(sotc.startOffset(), "SumOfTermsCompound",
                        ConversionException.Reason.MALFORMED_CST,
                        "SumOfTermsCompound sans enfant Concat_SumOfTerms_Star"));
        if (concat instanceof CstInternal cs && !cs.children().isEmpty()) {
            throw new ConversionException(concat.startOffset(), "Concat_SumOfTerms_Star",
                    ConversionException.Reason.CONCAT_NOT_SUPPORTED,
                    "Concatenation '&' non supportee en S1 (offset " + concat.startOffset() + ")");
        }
        CstNode sot = sotc.first(NonTerminal.SumOfTerms)
                .orElseThrow(() -> new ConversionException(sotc.startOffset(), "SumOfTermsCompound",
                        ConversionException.Reason.MALFORMED_CST,
                        "SumOfTermsCompound sans enfant SumOfTerms"));
        return buildSOT(sot);
    }

    private static Erwan buildSOT(CstNode node) {
        if (!(node instanceof CstInternal sot) || sot.nt() != NonTerminal.SumOfTerms) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu SumOfTerms");
        }
        Erwan first = buildTerm(sot.first(NonTerminal.Term)
                .orElseThrow(() -> new ConversionException(sot.startOffset(), "SumOfTerms",
                        ConversionException.Reason.MALFORMED_CST,
                        "SumOfTerms sans enfant Term")));
        List<Erwan> operands = collectOrOperands(sot.first(NonTerminal.Or_Operand_Star)
                .orElseThrow(() -> new ConversionException(sot.startOffset(), "SumOfTerms",
                        ConversionException.Reason.MALFORMED_CST,
                        "SumOfTerms sans enfant Or_Operand_Star")));
        if (operands.isEmpty())
            return first;
        operands.add(0, first);
        return Erwan.OR(operands);
    }

    private static List<Erwan> collectOrOperands(CstNode orStar) {
        if (!(orStar instanceof CstInternal node) || node.nt() != NonTerminal.Or_Operand_Star) {
            throw new ConversionException(orStar.startOffset(), String.valueOf(orStar.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu Or_Operand_Star");
        }
        List<Erwan> acc = new ArrayList<>();
        while (!node.children().isEmpty()) {
            // Or_Operand_Star -> OrOp Term Or_Operand_Star
            final int currentOffset = node.startOffset();
            acc.add(buildTerm(node.first(NonTerminal.Term)
                    .orElseThrow(() -> new ConversionException(currentOffset, "Or_Operand_Star",
                            ConversionException.Reason.MALFORMED_CST,
                            "Or_Operand_Star non-epsilon sans enfant Term"))));
            CstNode nextStar = node.first(NonTerminal.Or_Operand_Star)
                    .orElseThrow(() -> new ConversionException(currentOffset, "Or_Operand_Star",
                            ConversionException.Reason.MALFORMED_CST,
                            "Or_Operand_Star non-epsilon sans enfant Or_Operand_Star recursif"));
            if (!(nextStar instanceof CstInternal nextNode) || nextNode.nt() != NonTerminal.Or_Operand_Star) {
                throw new ConversionException(nextStar.startOffset(), String.valueOf(nextStar.symbol()),
                        ConversionException.Reason.MALFORMED_CST,
                        "Enfant Or_Operand_Star recursif n'est pas CstInternal(Or_Operand_Star)");
            }
            node = nextNode;
        }
        return acc;
    }

    private static Erwan buildTerm(CstNode node) {
        if (!(node instanceof CstInternal term) || term.nt() != NonTerminal.Term) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu Term");
        }
        Erwan first = buildFactor(term.first(NonTerminal.Factor)
                .orElseThrow(() -> new ConversionException(term.startOffset(), "Term",
                        ConversionException.Reason.MALFORMED_CST,
                        "Term sans enfant Factor")));
        List<Erwan> operands = collectAndOperands(term.first(NonTerminal.And_Operand_Star)
                .orElseThrow(() -> new ConversionException(term.startOffset(), "Term",
                        ConversionException.Reason.MALFORMED_CST,
                        "Term sans enfant And_Operand_Star")));
        if (operands.isEmpty())
            return first;
        operands.add(0, first);
        return Erwan.AND(operands);
    }

    private static List<Erwan> collectAndOperands(CstNode andStar) {
        if (!(andStar instanceof CstInternal node) || node.nt() != NonTerminal.And_Operand_Star) {
            throw new ConversionException(andStar.startOffset(), String.valueOf(andStar.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu And_Operand_Star");
        }
        List<Erwan> acc = new ArrayList<>();
        while (!node.children().isEmpty()) {
            // And_Operand_Star -> AndOp Factor And_Operand_Star
            final int currentOffset = node.startOffset();
            acc.add(buildFactor(node.first(NonTerminal.Factor)
                    .orElseThrow(() -> new ConversionException(currentOffset, "And_Operand_Star",
                            ConversionException.Reason.MALFORMED_CST,
                            "And_Operand_Star non-epsilon sans enfant Factor"))));
            CstNode nextStar = node.first(NonTerminal.And_Operand_Star)
                    .orElseThrow(() -> new ConversionException(currentOffset, "And_Operand_Star",
                            ConversionException.Reason.MALFORMED_CST,
                            "And_Operand_Star non-epsilon sans enfant And_Operand_Star recursif"));
            if (!(nextStar instanceof CstInternal nextNode) || nextNode.nt() != NonTerminal.And_Operand_Star) {
                throw new ConversionException(nextStar.startOffset(), String.valueOf(nextStar.symbol()),
                        ConversionException.Reason.MALFORMED_CST,
                        "Enfant And_Operand_Star recursif n'est pas CstInternal(And_Operand_Star)");
            }
            node = nextNode;
        }
        return acc;
    }

    private static Erwan buildFactor(CstNode node) {
        if (!(node instanceof CstInternal factor) || factor.nt() != NonTerminal.Factor) {
            throw new ConversionException(node.startOffset(), String.valueOf(node.symbol()),
                    ConversionException.Reason.MALFORMED_CST, "Attendu Factor");
        }
        // Factor -> '(' SOT ')' | LiteralValue | NotOp Signal | Signal
        if (factor.has(new Terminal(Token.LeftPar))) {
            CstNode sotNode = factor.first(NonTerminal.SumOfTerms)
                    .orElseThrow(() -> new ConversionException(factor.startOffset(), "Factor",
                            ConversionException.Reason.MALFORMED_CST,
                            "Factor avec LeftPar sans enfant SumOfTerms"));
            return buildSOT(sotNode);
        }
        if (factor.has(NonTerminal.LiteralValue)) {
            throw new ConversionException(factor.startOffset(), "Factor",
                    ConversionException.Reason.LITERAL_IN_RHS_NOT_SUPPORTED,
                    "Constante (.0/.1) en RHS non supportee en S1 (offset " + factor.startOffset() + ")");
        }
        CstNode signal = factor.first(NonTerminal.Signal)
                .orElseThrow(() -> new ConversionException(factor.startOffset(), "Factor",
                        ConversionException.Reason.MALFORMED_CST,
                        "Factor sans enfant Signal (ni LeftPar ni LiteralValue)"));
        Erwan lit = Erwan.LITTERAL(Names.extractScalarFromSignalNT(signal));
        if (factor.has(new Terminal(Token.NotOp))) {
            return Erwan.NOT(lit);
        }
        return lit;
    }
}
