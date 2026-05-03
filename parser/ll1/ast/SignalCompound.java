package parser.ll1.ast;
import java.util.*;

public final class SignalCompound implements Node {
    private final Position position;
    private final List<Signal> signals;
    public SignalCompound(Position pos, List<Signal> signals) {
        this.position = Objects.requireNonNull(pos);
        this.signals = List.copyOf(Objects.requireNonNull(signals));
        if (this.signals.isEmpty()) throw new IllegalArgumentException("signals vide");
    }
    public Position getPosition() { return position; }
    public List<Signal> getSignals() { return signals; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
