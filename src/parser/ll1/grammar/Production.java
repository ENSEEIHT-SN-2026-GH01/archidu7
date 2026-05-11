package parser.ll1.grammar;

import java.util.List;
import java.util.Objects;

public final class Production {
    private final NonTerminal head;
    private final List<Symbol> body; // immutable via List.copyOf

    public Production(NonTerminal head, List<Symbol> body) {
        this.head = Objects.requireNonNull(head, "head");
        this.body = List.copyOf(Objects.requireNonNull(body, "body"));
    }

    public NonTerminal getHead() {
        return head;
    }

    public List<Symbol> getBody() {
        return body;
    }

    public boolean isEpsilon() {
        return body.size() == 1 && body.get(0).isEpsilon();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(head.name()).append(" →");
        for (Symbol s : body)
            sb.append(' ').append(s);
        return sb.toString();
    }
}
