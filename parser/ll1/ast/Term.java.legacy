package parser.ll1.ast;
import java.util.*;

public final class Term implements Node {
    private final Position position;
    private final List<Factor> factors;
    public Term(Position pos, List<Factor> factors) {
        this.position = Objects.requireNonNull(pos);
        this.factors = List.copyOf(Objects.requireNonNull(factors));
        if (this.factors.isEmpty()) throw new IllegalArgumentException("factors vide");
    }
    public Position getPosition() { return position; }
    public List<Factor> getFactors() { return factors; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
