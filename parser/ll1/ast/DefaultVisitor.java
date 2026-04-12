package parser.ll1.ast;

public abstract class DefaultVisitor<R> implements Visitor<R> {
    protected R defaultResult() { return null; }

    @Override public R visit(Module m)           { m.getInstances().forEach(i -> i.accept(this)); return defaultResult(); }
    @Override public R visit(Assignment a)       { a.getTarget().accept(this); a.getExpr().accept(this); return defaultResult(); }
    @Override public R visit(TriState t)         { t.getTarget().accept(this); t.getExpr().accept(this); t.getEnable().accept(this); return defaultResult(); }
    @Override public R visit(MemoryPoint m)      { m.getTarget().accept(this); m.getExpr().accept(this); m.getClock().accept(this); m.getCondition().accept(this); return defaultResult(); }
    @Override public R visit(ModuleInstance mi)  { mi.getArgs().forEach(a -> a.accept(this)); return defaultResult(); }
    @Override public R visit(Fsm f)              { f.getRules().forEach(r -> r.accept(this)); return defaultResult(); }
    @Override public R visit(FsmRule r)          { return defaultResult(); }
    @Override public R visit(MapNode m)          { m.getInput().accept(this); m.getOutput().accept(this); m.getEntries().forEach(e -> e.accept(this)); return defaultResult(); }
    @Override public R visit(MapEntry e)         { return defaultResult(); }
    @Override public R visit(SumOfTerms s)       { s.getTerms().forEach(t -> t.accept(this)); return defaultResult(); }
    @Override public R visit(Term t)             { t.getFactors().forEach(f -> f.accept(this)); return defaultResult(); }
    @Override public R visit(Factor f)           { return defaultResult(); }
    @Override public R visit(Signal s)           { return defaultResult(); }
    @Override public R visit(SignalCompound sc)  { sc.getSignals().forEach(s -> s.accept(this)); return defaultResult(); }
    @Override public R visit(BitField b)         { return defaultResult(); }
}
