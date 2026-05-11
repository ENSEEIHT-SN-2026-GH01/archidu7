package parser.ll1.ast;

import java.util.*;

public final class MapNode implements Instance {
    private final Position position;
    private final SignalCompound input, output;
    private final List<MapEntry> entries;

    public MapNode(Position pos, SignalCompound input, SignalCompound output, List<MapEntry> entries) {
        this.position = Objects.requireNonNull(pos);
        this.input = Objects.requireNonNull(input);
        this.output = Objects.requireNonNull(output);
        this.entries = List.copyOf(Objects.requireNonNull(entries));
    }

    public Position getPosition() {
        return position;
    }

    public SignalCompound getInput() {
        return input;
    }

    public SignalCompound getOutput() {
        return output;
    }

    public List<MapEntry> getEntries() {
        return entries;
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
