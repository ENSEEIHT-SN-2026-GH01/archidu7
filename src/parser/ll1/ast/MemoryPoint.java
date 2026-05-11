package parser.ll1.ast;

import java.util.*;

public final class MemoryPoint implements Instance {
    public enum Kind {
        SET, RESET
    }

    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;
    private final SumOfTerms clock;
    private final Kind setOrReset;
    private final SumOfTerms condition;
    private final SumOfTerms enable;

    public MemoryPoint(Position pos, SignalCompound target, List<SumOfTerms> expr,
            SumOfTerms clock, Kind setOrReset, SumOfTerms condition, SumOfTerms enable) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        this.clock = Objects.requireNonNull(clock);
        this.setOrReset = Objects.requireNonNull(setOrReset);
        this.condition = Objects.requireNonNull(condition);
        this.enable = enable;
    }

    public Position getPosition() {
        return position;
    }

    public SignalCompound getTarget() {
        return target;
    }

    public List<SumOfTerms> getExprCompound() {
        return expr;
    }

    public SumOfTerms getClock() {
        return clock;
    }

    public Kind getSetOrReset() {
        return setOrReset;
    }

    public SumOfTerms getCondition() {
        return condition;
    }

    public java.util.Optional<SumOfTerms> getEnable() {
        return java.util.Optional.ofNullable(enable);
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
