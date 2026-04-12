package parser.ll1.grammar;

import java.util.*;

public final class Grammar {
    private final NonTerminal axiom;
    private final List<Production> productions;
    private final Map<NonTerminal, List<Production>> byHead;

    public Grammar(NonTerminal axiom, List<Production> productions) {
        this.axiom = Objects.requireNonNull(axiom);
        this.productions = List.copyOf(Objects.requireNonNull(productions));
        Map<NonTerminal, List<Production>> m = new EnumMap<>(NonTerminal.class);
        for (Production p : this.productions) {
            m.computeIfAbsent(p.getHead(), k -> new ArrayList<>()).add(p);
        }
        Map<NonTerminal, List<Production>> frozen = new EnumMap<>(NonTerminal.class);
        m.forEach((k, v) -> frozen.put(k, List.copyOf(v)));
        this.byHead = Collections.unmodifiableMap(frozen);
    }

    public NonTerminal getAxiom() { return axiom; }
    public List<Production> getProductions() { return productions; }
    public List<Production> productionsOf(NonTerminal nt) {
        return byHead.getOrDefault(nt, List.of());
    }
}
