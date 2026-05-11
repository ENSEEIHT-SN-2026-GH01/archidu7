package parser.ll1.grammar;

public enum NonTerminal implements Symbol {
    Start, Module, Instance_Plus, Instance_Star, Separ_Param_Star, Signal, Signal_Subset_Opt, Range_Opt, SignalOrLiteralCompound, Signal_Or_Litteral_Value, Concat_Signal_Or_Litteral_Value_Star, DotDot, Arg, Param, Instance, Operation, Assignment, SumOfTermsCompound, Concat_SumOfTerms_Star, SumOfTerms, Or_Operand_Star, Term, And_Operand_Star, Factor, SignalAssignment, MemoryAssignment, Comma_Opt, Set_Or_Reset, Enabled_Operand_Opt, Semicolon_Opt, ModuleCall, Separ_Arg_Star, LiteralValue, Separ, AndOp;

    @Override
    public boolean isNonTerminal() {
        return true;
    }
}
