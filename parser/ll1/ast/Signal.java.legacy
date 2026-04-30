package parser.ll1.ast;
import java.util.Objects;
import java.util.Optional;

public final class Signal implements Node {
    private final Position position;
    private final String name;
    private final Integer hi;
    private final Integer lo;

    public Signal(Position pos, String name, Integer hi, Integer lo) {
        this.position = Objects.requireNonNull(pos);
        this.name = Objects.requireNonNull(name);
        this.hi = hi; this.lo = lo;
    }
    public Position getPosition() { return position; }
    public String getName() { return name; }
    public Optional<Integer> getHi() { return Optional.ofNullable(hi); }
    public Optional<Integer> getLo() { return Optional.ofNullable(lo); }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
