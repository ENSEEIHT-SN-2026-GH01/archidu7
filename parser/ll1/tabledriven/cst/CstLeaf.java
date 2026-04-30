package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;
import parser.ll1.token.Token;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Feuille du CST : correspond à un terminal consommé par le parser.
 *
 * <p>Les offsets proviennent directement du {@link Token} sous-jacent.
 */
public record CstLeaf(Terminal t, Token token) implements CstNode {

    /** Valide que ni {@code t} ni {@code token} ne sont null. */
    public CstLeaf {
        Objects.requireNonNull(t, "t");
        Objects.requireNonNull(token, "token");
    }

    @Override public int startOffset() { return token.offset(); }
    @Override public int endOffset()   { return token.end(); }
    @Override public Symbol symbol()   { return t; }

    /** Une feuille n'a pas d'enfants : retourne toujours vide. */
    @Override public Optional<CstNode> first(Symbol s) { return Optional.empty(); }

    /** Une feuille n'a pas d'enfants : retourne toujours une liste vide. */
    @Override public List<CstNode> allOf(Symbol s)     { return List.of(); }

    /** Une feuille n'a pas d'enfants : retourne toujours {@code false}. */
    @Override public boolean has(Symbol s)             { return false; }
}
