package parser.ll1.ast;
import java.util.*;

public final class ModuleInstance implements Instance {
    private final Position position;
    private final String moduleName;
    private final boolean predefined;
    private final List<Node> args;
    public ModuleInstance(Position pos, String moduleName, boolean predefined, List<Node> args) {
        this.position = Objects.requireNonNull(pos);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.predefined = predefined;
        this.args = List.copyOf(Objects.requireNonNull(args));
    }
    public Position getPosition() { return position; }
    public String getModuleName() { return moduleName; }
    public boolean isPredefined() { return predefined; }
    public List<Node> getArgs() { return args; }
    public <R> R accept(Visitor<R> v) { return v.visit(this); }
}
