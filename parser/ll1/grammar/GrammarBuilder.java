package parser.ll1.grammar;

import parser.lexer.Token;
import java.util.*;

/** Petit DSL fluide pour decrire les productions lisiblement. */
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
        if (o instanceof Token t) return new Terminal(t);
        throw new IllegalArgumentException("Symbole invalide: " + o);
    }
}
