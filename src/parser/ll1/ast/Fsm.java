package parser.ll1.ast;

import java.util.*;

public final class Fsm implements Instance {
    private final Position position;
    private final FsmHeader header;
    private final List<FsmRule> rules;

    public Fsm(Position pos, FsmHeader header, List<FsmRule> rules) {
        this.position = Objects.requireNonNull(pos);
        this.header = Objects.requireNonNull(header);
        this.rules = List.copyOf(Objects.requireNonNull(rules));
    }

    public Position getPosition() {
        return position;
    }

    public FsmHeader getHeader() {
        return header;
    }

    public List<FsmRule> getRules() {
        return rules;
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
