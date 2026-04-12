package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class FirstSet {
    private final Grammar grammar;
    private final Map<NonTerminal, Set<TokenType>> first = new EnumMap<>(NonTerminal.class);
    private final Set<NonTerminal> nullable = EnumSet.noneOf(NonTerminal.class);

    public FirstSet(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
        for (NonTerminal nt : NonTerminal.values()) first.put(nt, EnumSet.noneOf(TokenType.class));
        compute();
    }

    private void compute() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                NonTerminal head = p.getHead();
                if (p.isEpsilon()) {
                    if (nullable.add(head)) changed = true;
                    continue;
                }
                boolean allNullable = true;
                for (Symbol s : p.getBody()) {
                    Set<TokenType> firstOfS = firstOfSymbol(s);
                    if (first.get(head).addAll(firstOfS)) changed = true;
                    if (!isNullable(s)) { allNullable = false; break; }
                }
                if (allNullable && nullable.add(head)) changed = true;
            }
        }
    }

    private Set<TokenType> firstOfSymbol(Symbol s) {
        if (s.isEpsilon()) return Set.of();
        if (s.isTerminal()) return Set.of(((Terminal) s).getType());
        return first.get((NonTerminal) s);
    }

    private boolean isNullable(Symbol s) {
        if (s.isEpsilon()) return true;
        if (s.isTerminal()) return false;
        return nullable.contains((NonTerminal) s);
    }

    public Set<TokenType> of(NonTerminal nt) {
        return Collections.unmodifiableSet(first.get(nt));
    }

    public boolean nullable(NonTerminal nt) { return nullable.contains(nt); }

    /** First d'une séquence (utile pour le parser et Follow). */
    public Set<TokenType> ofSequence(List<Symbol> seq) {
        Set<TokenType> out = EnumSet.noneOf(TokenType.class);
        for (Symbol s : seq) {
            out.addAll(firstOfSymbol(s));
            if (!isNullable(s)) return out;
        }
        return out;
    }

    public boolean sequenceNullable(List<Symbol> seq) {
        for (Symbol s : seq) if (!isNullable(s)) return false;
        return true;
    }
}
