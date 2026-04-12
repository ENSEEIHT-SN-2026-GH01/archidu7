package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class FollowSet {
    private final Grammar grammar;
    private final FirstSet first;
    private final Map<NonTerminal, Set<TokenType>> follow = new EnumMap<>(NonTerminal.class);

    public FollowSet(Grammar grammar, FirstSet first) {
        this.grammar = Objects.requireNonNull(grammar);
        this.first = Objects.requireNonNull(first);
        for (NonTerminal nt : NonTerminal.values()) follow.put(nt, EnumSet.noneOf(TokenType.class));
        follow.get(grammar.getAxiom()).add(TokenType.EOF);
        compute();
    }

    private void compute() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.getProductions()) {
                List<Symbol> body = p.getBody();
                for (int i = 0; i < body.size(); i++) {
                    Symbol s = body.get(i);
                    if (!s.isNonTerminal()) continue;
                    NonTerminal X = (NonTerminal) s;
                    List<Symbol> beta = body.subList(i + 1, body.size());
                    Set<TokenType> firstBeta = first.ofSequence(beta);
                    if (follow.get(X).addAll(firstBeta)) changed = true;
                    if (first.sequenceNullable(beta)) {
                        if (follow.get(X).addAll(follow.get(p.getHead()))) changed = true;
                    }
                }
            }
        }
    }

    public Set<TokenType> of(NonTerminal nt) {
        return Collections.unmodifiableSet(follow.get(nt));
    }
}
