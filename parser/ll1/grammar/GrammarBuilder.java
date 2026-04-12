package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

/** Petit DSL fluide pour décrire les productions lisiblement. */
public final class GrammarBuilder {
    private final List<Production> productions = new ArrayList<>();

    public GrammarBuilder prod(NonTerminal head, Object... body) {
        List<Symbol> b = new ArrayList<>(body.length);
        for (Object o : body) b.add(toSymbol(o));
        productions.add(new Production(head, b));
        return this;
    }

    public GrammarBuilder eps(NonTerminal head) {
        productions.add(new Production(head, List.of(Terminal.EPSILON)));
        return this;
    }

    public Grammar build(NonTerminal axiom) { return new Grammar(axiom, productions); }

    private static Symbol toSymbol(Object o) {
        if (o instanceof Symbol) return (Symbol) o;
        if (o instanceof TokenType) return new Terminal((TokenType) o);
        throw new IllegalArgumentException("Symbole invalide: " + o);
    }
}
