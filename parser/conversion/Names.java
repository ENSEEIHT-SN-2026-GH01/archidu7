package parser.conversion;

import parser.lexer.Token;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
import erwan.Descripteur;

public final class Names {

    private Names() {}

    /**
     * Référence complète à un signal : nom + sous-ensemble optionnel.
     */
    public record SignalRef(String nom, Subset subset) {}

    /**
     * Extrait le {@link Subset} d'un nœud NT {@code Signal_Subset_Opt}.
     * <ul>
     *   <li>ε (children vide) → {@link Subset#SCALAR}</li>
     *   <li>{@code [i]}       → {@link Subset#single(int)}</li>
     *   <li>{@code [hi..lo]}  → {@link Subset#range(int, int)}</li>
     * </ul>
     */
    public static Subset subsetOf(CstNode signalSubsetOpt) {
        if (!(signalSubsetOpt instanceof CstInternal sub)
                || sub.nt() != NonTerminal.Signal_Subset_Opt) {
            throw new ConversionException(
                signalSubsetOpt.startOffset(),
                String.valueOf(signalSubsetOpt.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Attendu un noeud NT Signal_Subset_Opt");
        }
        if (sub.children().isEmpty()) {
            return Subset.SCALAR;
        }
        // Structure : '[' NaturalInteger Range_Opt ']'
        CstNode hiNode = sub.first(new Terminal(Token.NaturalInteger)).orElseThrow(() ->
            new ConversionException(
                sub.startOffset(), "Signal_Subset_Opt",
                ConversionException.Reason.MALFORMED_CST,
                "Signal_Subset_Opt non-ε sans NaturalInteger enfant"));
        if (!(hiNode instanceof CstLeaf hiLeaf)) {
            throw new ConversionException(
                hiNode.startOffset(), "NaturalInteger",
                ConversionException.Reason.MALFORMED_CST,
                "NaturalInteger hi de Signal_Subset_Opt n'est pas CstLeaf");
        }
        int hi;
        try {
            hi = Integer.parseInt(hiLeaf.lexem().getText());
        } catch (NumberFormatException e) {
            throw new ConversionException(
                hiNode.startOffset(), "NaturalInteger",
                ConversionException.Reason.MALFORMED_CST,
                "Indice de vecteur invalide : " + hiLeaf.lexem().getText());
        }

        CstNode rangeOpt = sub.first(NonTerminal.Range_Opt).orElseThrow(() ->
            new ConversionException(
                sub.startOffset(), "Signal_Subset_Opt",
                ConversionException.Reason.MALFORMED_CST,
                "Signal_Subset_Opt sans Range_Opt enfant"));
        if (!(rangeOpt instanceof CstInternal rng)) {
            throw new ConversionException(
                rangeOpt.startOffset(), "Range_Opt",
                ConversionException.Reason.MALFORMED_CST,
                "Range_Opt n'est pas CstInternal");
        }
        if (rng.children().isEmpty()) {
            return Subset.single(hi);
        }
        // Structure Range_Opt non-ε : DotDot NaturalInteger
        CstNode loNode = rng.first(new Terminal(Token.NaturalInteger)).orElseThrow(() ->
            new ConversionException(
                rng.startOffset(), "Range_Opt",
                ConversionException.Reason.MALFORMED_CST,
                "Range_Opt non-ε sans NaturalInteger enfant"));
        if (!(loNode instanceof CstLeaf loLeaf)) {
            throw new ConversionException(
                loNode.startOffset(), "NaturalInteger",
                ConversionException.Reason.MALFORMED_CST,
                "NaturalInteger lo de Range_Opt n'est pas CstLeaf");
        }
        int lo;
        try {
            lo = Integer.parseInt(loLeaf.lexem().getText());
        } catch (NumberFormatException e) {
            throw new ConversionException(
                loNode.startOffset(), "NaturalInteger",
                ConversionException.Reason.MALFORMED_CST,
                "Indice de vecteur invalide : " + loLeaf.lexem().getText());
        }
        return Subset.range(hi, lo);
    }

    /**
     * Construit un {@link Descripteur} depuis un nœud NT {@code Signal}.
     * <ul>
     *   <li>Signal scalaire → {@code new Descripteur(nom)}</li>
     *   <li>Signal vecteur {@code a[3]} ou {@code a[3..0]} →
     *       {@code new Descripteur(nom, minIndex, maxIndex)}</li>
     * </ul>
     */
    public static Descripteur descriptorOf(CstNode signalNode) {
        SignalRef ref = signalRef(signalNode);
        if (!ref.subset().isVector()) {
            return new Descripteur(ref.nom());
        } else {
            return new Descripteur(ref.nom(), ref.subset().minIndex(), ref.subset().maxIndex());
        }
    }

    /**
     * Extrait le {@link SignalRef} (nom + sous-ensemble) d'un nœud NT {@code Signal}.
     */
    public static SignalRef signalRef(CstNode signalNT) {
        if (!(signalNT instanceof CstInternal sig) || sig.nt() != NonTerminal.Signal) {
            throw new ConversionException(
                signalNT.startOffset(), String.valueOf(signalNT.symbol()),
                ConversionException.Reason.MALFORMED_CST,
                "Attendu un noeud NT Signal");
        }
        CstNode id = sig.first(new Terminal(Token.Identifiant)).orElseThrow(() ->
            new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Identifiant enfant"));
        if (!(id instanceof CstLeaf idLeaf)) {
            throw new ConversionException(
                id.startOffset(), "Identifiant",
                ConversionException.Reason.MALFORMED_CST,
                "Enfant Identifiant de Signal n'est pas CstLeaf");
        }
        String nom = idLeaf.lexem().getText();
        CstNode subsetOpt = sig.first(NonTerminal.Signal_Subset_Opt).orElseThrow(() ->
            new ConversionException(
                sig.startOffset(), "Signal",
                ConversionException.Reason.MALFORMED_CST,
                "Signal sans Signal_Subset_Opt enfant"));
        return new SignalRef(nom, subsetOf(subsetOpt));
    }

}
