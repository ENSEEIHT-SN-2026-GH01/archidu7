package parser.ll1.ast;
import java.util.*;

public final class SumOfTerms implements Node {
    private final Position position;
    private final List<Term> terms;
    public SumOfTerms(Position pos, List<Term> terms) {
        this.position = Objects.requireNonNull(pos);
        this.terms = List.copyOf(Objects.requireNonNull(terms));
        if (this.terms.isEmpty()) throw new IllegalArgumentException("terms vide");
    }
    public Position getPosition() { return position; }
    public List<Term> getTerms() { return terms; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
