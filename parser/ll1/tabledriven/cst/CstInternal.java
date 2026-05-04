package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Noeud interne du CST : correspond à un non-terminal développé par une production.
 *
 * <p>Utiliser les factory statiques {@link #of} et {@link #epsilon} plutôt que
 * le constructeur canonique du record.
 */
public record CstInternal(
        NonTerminal nt,
        Production rule,
        List<CstNode> children,
        int startOffset,
        int endOffset
) implements CstNode {

    /** Constructeur compact : valide les non-null et rend {@code children} immuable. */
    public CstInternal {
        Objects.requireNonNull(nt, "nt");
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(children, "children");
        children = List.copyOf(children);
    }

    @Override public Symbol symbol() { return nt; }

    /**
     * Retourne le premier enfant direct dont le symbole est égal à {@code s},
     * ou {@link Optional#empty()} si aucun ne correspond.
     */
    @Override
    public Optional<CstNode> first(Symbol s) {
        for (var c : children) {
            if (c.symbol().equals(s)) return Optional.of(c);
        }
        return Optional.empty();
    }

    /**
     * Retourne la liste (ordonnée) de tous les enfants directs dont le symbole
     * est égal à {@code s}.
     */
    @Override
    public List<CstNode> allOf(Symbol s) {
        var result = new ArrayList<CstNode>();
        for (var c : children) {
            if (c.symbol().equals(s)) result.add(c);
        }
        return List.copyOf(result);
    }

    /** Retourne {@code true} si au moins un enfant direct a pour symbole {@code s}. */
    @Override
    public boolean has(Symbol s) {
        for (var c : children) {
            if (c.symbol().equals(s)) return true;
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Factories statiques
    // -----------------------------------------------------------------------

    /**
     * Crée un noeud interne à partir d'une liste non-vide d'enfants.
     * Les offsets sont calculés depuis le premier et le dernier enfant.
     *
     * @throws IllegalArgumentException si {@code children} est vide
     */
    public static CstInternal of(NonTerminal nt, Production rule, List<CstNode> children) {
        Objects.requireNonNull(nt, "nt");
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(children, "children");
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Use epsilon() for empty children");
        }
        int start = children.get(0).startOffset();
        int end   = children.get(children.size() - 1).endOffset();
        return new CstInternal(nt, rule, children, start, end);
    }

    /**
     * Crée un noeud interne epsilon (production vide) avec des offsets identiques
     * au curseur courant.
     */
    public static CstInternal epsilon(NonTerminal nt, Production rule, int cursorOffset) {
        Objects.requireNonNull(nt, "nt");
        Objects.requireNonNull(rule, "rule");
        return new CstInternal(nt, rule, List.of(), cursorOffset, cursorOffset);
    }
}
