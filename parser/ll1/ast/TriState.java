package parser.ll1.ast;
import java.util.*;
import java.util.Objects;

public final class TriState implements Instance {
    private final Position position;
    private final SignalCompound target;
    private final List<SumOfTerms> expr;
    private final SumOfTerms enable;
    public TriState(Position pos, SignalCompound target, List<SumOfTerms> expr, SumOfTerms enable) {
        this.position = Objects.requireNonNull(pos);
        this.target = Objects.requireNonNull(target);
        this.expr = List.copyOf(Objects.requireNonNull(expr));
        this.enable = Objects.requireNonNull(enable);
    }
    public Position getPosition() { return position; }
    public SignalCompound getTarget() { return target; }
    public List<SumOfTerms> getExprCompound() { return expr; }
    public SumOfTerms getExpr() { return expr.get(0); }
    public SumOfTerms getEnable() { return enable; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
