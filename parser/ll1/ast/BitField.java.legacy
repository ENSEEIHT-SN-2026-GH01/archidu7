package parser.ll1.ast;
import java.util.Objects;

public final class BitField implements Node {
    private final Position position;
    private final String bits;
    public BitField(Position pos, String bits) {
        this.position = Objects.requireNonNull(pos);
        this.bits = Objects.requireNonNull(bits);
    }
    public Position getPosition() { return position; }
    public String getBits() { return bits; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
