package parser.ll1.ast;

import java.util.List;

/**
 * Imprime un arbre AST sous forme texte indentée ({@code ├─} / {@code └─}).
 * Usage : {@code AstDumper.dump(module)}.
 */
public final class AstDumper {

    private AstDumper() {}

    public static String dump(Node node) {
        StringBuilder sb = new StringBuilder();
        write(sb, node, "", true, true);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Node node, String prefix, boolean isLast, boolean isRoot) {
        sb.append(prefix);
        if (!isRoot) sb.append(isLast ? "└─ " : "├─ ");
        sb.append(label(node));
        sb.append('\n');

        String childPrefix = isRoot ? "" : prefix + (isLast ? "   " : "│  ");
        List<Child> children = children(node);
        for (int i = 0; i < children.size(); i++) {
            Child c = children.get(i);
            boolean last = (i == children.size() - 1);
            if (c.tag != null) {
                sb.append(childPrefix).append(last ? "└─ " : "├─ ").append(c.tag).append('\n');
                String grand = childPrefix + (last ? "   " : "│  ");
                for (int j = 0; j < c.nodes.size(); j++) {
                    write(sb, c.nodes.get(j), grand, j == c.nodes.size() - 1, false);
                }
            } else {
                write(sb, c.nodes.get(0), childPrefix, last, false);
            }
        }
    }

    private static String label(Node n) {
        Position p = n.getPosition();
        String pos = " [l." + p.getLine() + "]";
        if (n instanceof Module m) {
            return "Module \"" + m.getName() + "\"" + pos;
        }
        if (n instanceof Signal s) {
            String range = s.getHi().isPresent()
                ? "[" + s.getHi().get() + (s.getLo().isPresent() ? ".." + s.getLo().get() : "") + "]"
                : "";
            return "Signal \"" + s.getName() + range + "\"";
        }
        if (n instanceof SignalCompound) return "SignalCompound";
        if (n instanceof Assignment)    return "Assignment" + pos;
        if (n instanceof ModuleInstance mi) {
            return "ModuleInstance \"" + mi.getModuleName() + "\"" + (mi.isPredefined() ? " [predefined]" : "") + pos;
        }
        if (n instanceof TriState)      return "TriState" + pos;
        if (n instanceof MemoryPoint m) return "MemoryPoint [" + m.getSetOrReset() + "]" + pos;
        if (n instanceof Fsm)           return "Fsm" + pos;
        if (n instanceof FsmHeader h)   return "FsmHeader [" + h.getKind() + "]" + pos;
        if (n instanceof FsmRule r)     return "FsmRule " + r.getFromStates() + " -> \"" + r.getToState() + "\"";
        if (n instanceof MapNode)       return "MapNode" + pos;
        if (n instanceof MapEntry)      return "MapEntry" + pos;
        if (n instanceof SumOfTerms)    return "SumOfTerms";
        if (n instanceof Term)          return "Term";
        if (n instanceof Factor f) {
            String detail = switch (f.getKind()) {
                case SIGNAL     -> " \"" + f.getSignal().getName() + "\"";
                case NEG_SIGNAL -> " /\"" + f.getSignal().getName() + "\"";
                case LITERAL_0  -> " (= 0)";
                case LITERAL_1  -> " (= 1)";
                case BITFIELD, PAREN -> "";
            };
            return "Factor " + f.getKind() + detail;
        }
        if (n instanceof BitField b)    return "BitField \"" + b.getBits() + "\"";
        return n.getClass().getSimpleName();
    }

    private static List<Child> children(Node n) {
        if (n instanceof Module m) {
            return List.of(
                Child.group("Params", (List) m.getParams()),
                Child.group("Instances", (List) m.getInstances()));
        }
        if (n instanceof Assignment a) {
            return List.of(
                Child.group("Target", List.of(a.getTarget())),
                Child.group("Expr", (List) a.getExprCompound()));
        }
        if (n instanceof ModuleInstance mi) {
            return List.of(Child.group("Args", (List) mi.getArgs()));
        }
        if (n instanceof TriState t) {
            return List.of(
                Child.group("Target", List.of(t.getTarget())),
                Child.group("Expr",   (List) t.getExprCompound()),
                Child.group("Enable", List.of(t.getEnable())));
        }
        if (n instanceof MemoryPoint m) {
            List<Child> c = new java.util.ArrayList<>();
            c.add(Child.group("Target", List.of(m.getTarget())));
            c.add(Child.group("Expr",   (List) m.getExprCompound()));
            c.add(Child.group("Clock",  List.of(m.getClock())));
            c.add(Child.group("Condition", List.of(m.getCondition())));
            m.getEnable().ifPresent(e -> c.add(Child.group("Enable", List.of(e))));
            return c;
        }
        if (n instanceof Fsm f) {
            return List.of(
                Child.group("Header", List.of(f.getHeader())),
                Child.group("Rules", (List) f.getRules()));
        }
        if (n instanceof MapNode mn) {
            return List.of(
                Child.group("Input",  List.of(mn.getInput())),
                Child.group("Output", List.of(mn.getOutput())),
                Child.group("Entries", (List) mn.getEntries()));
        }
        if (n instanceof MapEntry me) {
            return List.of(
                Child.group("From", List.of(me.getFrom())),
                Child.group("To",   List.of(me.getTo())));
        }
        if (n instanceof SignalCompound sc) {
            return asChildren(sc.getSignals());
        }
        if (n instanceof SumOfTerms s) {
            return asChildren(s.getTerms());
        }
        if (n instanceof Term t) {
            return asChildren(t.getFactors());
        }
        if (n instanceof Factor f) {
            if (f.getKind() == Factor.Kind.PAREN && f.getInner() != null)
                return List.of(Child.single(f.getInner()));
            if (f.getKind() == Factor.Kind.BITFIELD && f.getBitField() != null)
                return List.of(Child.single(f.getBitField()));
            return List.of();
        }
        return List.of();
    }

    private static List<Child> asChildren(List<? extends Node> nodes) {
        List<Child> r = new java.util.ArrayList<>(nodes.size());
        for (Node n : nodes) r.add(Child.single(n));
        return r;
    }

    private static final class Child {
        final String tag;
        final List<? extends Node> nodes;
        private Child(String tag, List<? extends Node> nodes) { this.tag = tag; this.nodes = nodes; }
        static Child group(String tag, List<? extends Node> nodes) { return new Child(tag, nodes); }
        static Child single(Node n) { return new Child(null, List.of(n)); }
    }
}
