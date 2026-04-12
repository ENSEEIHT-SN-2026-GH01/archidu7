package parser.ll1.ast;
import java.util.*;

public final class Assignment implements Instance {
    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;
    public Assignment(Position pos, SignalCompound target, List<SumOfTerms> expr) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        if (this.expr.isEmpty()) throw new IllegalArgumentException("expr vide");
    }
    public Position getPosition() { return position; }
    public SignalCompound getTarget() { return target; }
    public List<SumOfTerms> getExprCompound() { return expr; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
