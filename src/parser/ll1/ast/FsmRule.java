package parser.ll1.ast;

import java.util.*;

public final class FsmRule implements Node {
    private final Position position;
    private final List<String> fromStates;
    private final boolean wildcard;
    private final String toState;
    private final SumOfTerms whenCondition;

    public FsmRule(Position pos, List<String> fromStates, boolean wildcard, String toState, SumOfTerms when) {
        this.position = Objects.requireNonNull(pos);
        this.fromStates = List.copyOf(Objects.requireNonNull(fromStates));
        this.wildcard = wildcard;
        this.toState = Objects.requireNonNull(toState);
        this.whenCondition = when;
    }

    public Position getPosition() {
        return position;
    }

    public List<String> getFromStates() {
        return fromStates;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public String getToState() {
        return toState;
    }

    public Optional<SumOfTerms> getWhen() {
        return Optional.ofNullable(whenCondition);
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
