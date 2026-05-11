package parser.ll1.ast;

import java.util.Objects;

public final class MapEntry implements Node {
    private final Position position;
    private final BitField from, to;

    public MapEntry(Position pos, BitField from, BitField to) {
        this.position = Objects.requireNonNull(pos);
        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
    }

    public Position getPosition() {
        return position;
    }

    public BitField getFrom() {
        return from;
    }

    public BitField getTo() {
        return to;
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
