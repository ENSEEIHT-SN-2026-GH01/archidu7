package parser.ll1.ast;

import java.util.*;

public final class Module implements Node {
    private final Position position;
    private final String name;
    private final List<Signal> params;
    private final List<Instance> instances;

    public Module(Position pos, String name, List<Signal> params, List<Instance> instances) {
        this.position = Objects.requireNonNull(pos);
        this.name = Objects.requireNonNull(name);
        this.params = List.copyOf(Objects.requireNonNull(params));
        this.instances = List.copyOf(Objects.requireNonNull(instances));
    }

    public Position getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public List<Signal> getParams() {
        return params;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }
}
