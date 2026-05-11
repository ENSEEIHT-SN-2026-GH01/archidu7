package parser.conversion;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;

public final class Names {

    private Names() {
    }

    public static String extractScalarFromSignalNT(CstNode signalNT) {
        if (!(signalNT instanceof CstInternal sig) || sig.nt() != NonTerminal.Signal) {
            throw new ConversionException(
                    signalNT.startOffset(), String.valueOf(signalNT.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "Attendu un noeud NT Signal");
        }
        CstNode subset = sig.first(NonTerminal.Signal_Subset_Opt).orElseThrow(() -> new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Signal_Subset_Opt enfant"));
        if (subset instanceof CstInternal sub && !sub.children().isEmpty()) {
            throw new ConversionException(
                    sig.startOffset(), "Signal",
                    ConversionException.Reason.VECTOR_SUBSET_NOT_SUPPORTED,
                    "Vecteur non supporte en S1 (offset " + sig.startOffset() + ")");
        }
        CstNode id = sig.first(new Terminal(Token.Identifiant)).orElseThrow(() -> new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Identifiant enfant"));
        if (!(id instanceof CstLeaf idLeaf)) {
            throw new ConversionException(
                    id.startOffset(), "Identifiant",
                    ConversionException.Reason.MALFORMED_CST,
                    "Enfant Identifiant de Signal n'est pas CstLeaf");
        }
        return idLeaf.lexem().getText();
    }

    public static String extractScalarFromIdAndSubset(CstNode idLeaf, CstNode subsetOpt) {
        if (subsetOpt instanceof CstInternal sub && !sub.children().isEmpty()) {
            throw new ConversionException(
                    idLeaf.startOffset(), "Identifiant",
                    ConversionException.Reason.VECTOR_SUBSET_NOT_SUPPORTED,
                    "Vecteur non supporte en S1 (offset " + idLeaf.startOffset() + ")");
        }
        if (!(idLeaf instanceof CstLeaf leaf)) {
            throw new ConversionException(
                    idLeaf.startOffset(), "Identifiant",
                    ConversionException.Reason.MALFORMED_CST,
                    "Identifiant en LHS n'est pas CstLeaf");
        }
        return leaf.lexem().getText();
    }
}
