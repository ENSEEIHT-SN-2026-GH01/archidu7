package parser.ll1.ast;

/**
 * Visiteur par défaut qui traverse TOUS les enfants Node d'un AST.
 * Les sous-classes overrident les méthodes qui les intéressent en appelant
 * {@code super.visit(...)} pour continuer la traversée, ou renvoient une
 * valeur sans rappeler super si elles veulent stopper la descente.
 */
public abstract class DefaultVisitor<R> implements Visitor<R> {
    protected R defaultResult() {
        return null;
    }

    @Override
    public R visit(Module m) {
        for (Signal s : m.getParams())
            s.accept(this);
        for (Instance i : m.getInstances())
            i.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(Assignment a) {
        a.getTarget().accept(this);
        for (SumOfTerms e : a.getExprCompound())
            e.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(TriState t) {
        t.getTarget().accept(this);
        for (SumOfTerms e : t.getExprCompound())
            e.accept(this);
        t.getEnable().accept(this);
        return defaultResult();
    }

    @Override
    public R visit(MemoryPoint m) {
        m.getTarget().accept(this);
        for (SumOfTerms e : m.getExprCompound())
            e.accept(this);
        m.getClock().accept(this);
        m.getCondition().accept(this);
        m.getEnable().ifPresent(e -> e.accept(this));
        return defaultResult();
    }

    @Override
    public R visit(ModuleInstance mi) {
        for (Node a : mi.getArgs())
            a.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(Fsm f) {
        f.getHeader().accept(this);
        for (FsmRule r : f.getRules())
            r.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(FsmHeader h) {
        h.getClock().ifPresent(c -> c.accept(this));
        h.getResetCondition().ifPresent(c -> c.accept(this));
        return defaultResult();
    }

    @Override
    public R visit(FsmRule r) {
        r.getWhen().ifPresent(w -> w.accept(this));
        return defaultResult();
    }

    @Override
    public R visit(MapNode m) {
        m.getInput().accept(this);
        m.getOutput().accept(this);
        for (MapEntry e : m.getEntries())
            e.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(MapEntry e) {
        e.getFrom().accept(this);
        e.getTo().accept(this);
        return defaultResult();
    }

    @Override
    public R visit(SumOfTerms s) {
        for (Term t : s.getTerms())
            t.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(Term t) {
        for (Factor f : t.getFactors())
            f.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(Factor f) {
        switch (f.getKind()) {
            case SIGNAL:
            case NEG_SIGNAL:
                f.getSignal().accept(this);
                break;
            case BITFIELD:
                f.getBitField().accept(this);
                break;
            case PAREN:
                f.getInner().accept(this);
                break;
            case LITERAL_0:
            case LITERAL_1:
                break;
        }
        return defaultResult();
    }

    @Override
    public R visit(Signal s) {
        return defaultResult();
    }

    @Override
    public R visit(SignalCompound sc) {
        for (Signal s : sc.getSignals())
            s.accept(this);
        return defaultResult();
    }

    @Override
    public R visit(BitField b) {
        return defaultResult();
    }
}
