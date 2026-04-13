package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class Ll1ConflictChecker {
    private final Grammar grammar;
    private final FirstSet first;
    private final FollowSet follow;

    public Ll1ConflictChecker(Grammar grammar) {
        this.grammar = grammar;
        this.first = new FirstSet(grammar);
        this.follow = new FollowSet(grammar, first);
    }

    public List<Ll1Conflict> findAllConflicts() {
        List<Ll1Conflict> result = new ArrayList<>();
        // Récursion gauche directe
        for (Production p : grammar.getProductions()) {
            List<Symbol> body = p.getBody();
            if (!body.isEmpty() && body.get(0) == p.getHead()) {
                result.add(new Ll1Conflict(Ll1Conflict.Type.LEFT_RECURSION, p.getHead(),
                    Set.of(), "production " + p));
            }
        }
        // FIRST/FIRST et FIRST/FOLLOW par non-terminal
        for (NonTerminal nt : NonTerminal.values()) {
            List<Production> prods = grammar.productionsOf(nt);
            if (prods.size() < 2) continue;
            List<Set<TokenType>> firsts = new ArrayList<>();
            int nullableIdx = -1;
            for (int i = 0; i < prods.size(); i++) {
                Production p = prods.get(i);
                Set<TokenType> f = p.isEpsilon() ? Set.of() : first.ofSequence(p.getBody());
                firsts.add(f);
                if (p.isEpsilon() || first.sequenceNullable(p.getBody())) nullableIdx = i;
            }
            // FIRST/FIRST
            for (int i = 0; i < firsts.size(); i++) {
                for (int j = i + 1; j < firsts.size(); j++) {
                    Set<TokenType> a = firsts.get(i);
                    Set<TokenType> b = firsts.get(j);
                    if (a.isEmpty() || b.isEmpty()) continue;
                    Set<TokenType> inter = EnumSet.copyOf(a);
                    inter.retainAll(b);
                    if (!inter.isEmpty()) {
                        result.add(new Ll1Conflict(Ll1Conflict.Type.FIRST_FIRST, nt,
                            inter, "productions " + i + " et " + j));
                    }
                }
            }
            // FIRST/FOLLOW
            if (nullableIdx >= 0) {
                Set<TokenType> followNt = follow.of(nt);
                for (int i = 0; i < firsts.size(); i++) {
                    if (i == nullableIdx) continue;
                    Set<TokenType> fi = firsts.get(i);
                    if (fi.isEmpty()) continue;
                    Set<TokenType> inter = EnumSet.copyOf(fi);
                    inter.retainAll(followNt);
                    if (!inter.isEmpty()) {
                        result.add(new Ll1Conflict(Ll1Conflict.Type.FIRST_FOLLOW, nt,
                            inter, "ε production en conflit avec production " + i));
                    }
                }
            }
        }
        return result;
    }
}
