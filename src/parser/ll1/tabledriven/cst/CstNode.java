package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.Symbol;
import java.util.List;
import java.util.Optional;

/**
 * Noeud d'un Concrete Syntax Tree (CST) issu du parsing LL(1).
 *
 * <p>
 * Hiérarchie sealed : seuls {@link CstLeaf} (feuille terminale) et
 * {@link CstInternal} (noeud interne non-terminal) sont autorisés.
 */
public sealed interface CstNode permits CstInternal, CstLeaf {

    /** Offset (incl.) du premier caractère couvert par ce noeud dans la source. */
    int startOffset();

    /** Offset (excl.) du dernier caractère couvert par ce noeud dans la source. */
    int endOffset();

    /**
     * Symbole associé à ce noeud : {@link parser.ll1.grammar.Terminal} pour une
     * feuille,
     * {@link parser.ll1.grammar.NonTerminal} pour un noeud interne.
     */
    Symbol symbol();

    /**
     * Retourne le premier enfant direct dont le symbole est égal à {@code s},
     * ou {@link Optional#empty()} si aucun ne correspond.
     */
    Optional<CstNode> first(Symbol s);

    /**
     * Retourne la liste (ordonnée) de tous les enfants directs dont le symbole
     * est égal à {@code s}. Retourne une liste vide si aucun ne correspond.
     */
    List<CstNode> allOf(Symbol s);

    /**
     * Retourne {@code true} si au moins un enfant direct a pour symbole {@code s}.
     */
    boolean has(Symbol s);
}
