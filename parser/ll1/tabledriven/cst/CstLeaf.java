package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;
import parser.lexer.Lexem;
import parser.lexer.Token;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Feuille du CST : correspond a un terminal consomme par le parser.
 *
 * <p>Les offsets proviennent directement du {@link Lexem} sous-jacent.
 * Pour le token EOF sentinelle, indexDepart == indexFin == source.length().
 */
public record CstLeaf(Terminal t, Lexem<Token> lexem) implements CstNode {

    /** Valide que ni {@code t} ni {@code lexem} ne sont null. */
    public CstLeaf {
        Objects.requireNonNull(t, "t");
        Objects.requireNonNull(lexem, "lexem");
    }

    @Override public int startOffset() { return lexem.getIndexDepart(); }
    @Override public int endOffset()   { return lexem.getIndexFin(); }
    @Override public Symbol symbol()   { return t; }

    /** Une feuille n'a pas d'enfants : retourne toujours vide. */
    @Override public Optional<CstNode> first(Symbol s) { return Optional.empty(); }

    /** Une feuille n'a pas d'enfants : retourne toujours une liste vide. */
    @Override public List<CstNode> allOf(Symbol s)     { return List.of(); }

    /** Une feuille n'a pas d'enfants : retourne toujours {@code false}. */
    @Override public boolean has(Symbol s)             { return false; }
}
