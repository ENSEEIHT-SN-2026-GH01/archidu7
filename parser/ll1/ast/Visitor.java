package parser.ll1.ast;

public interface Visitor<R> {
    R visit(Module m);
    R visit(Assignment a);
    R visit(TriState t);
    R visit(MemoryPoint m);
    R visit(ModuleInstance mi);
    R visit(Fsm f);
    R visit(FsmRule r);
    R visit(MapNode m);
    R visit(MapEntry e);
    R visit(SumOfTerms s);
    R visit(Term t);
    R visit(Factor f);
    R visit(Signal s);
    R visit(SignalCompound sc);
    R visit(BitField b);
}
