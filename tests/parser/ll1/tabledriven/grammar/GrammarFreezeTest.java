package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import static org.junit.Assert.*;

/**
 * TDD — RED phase.
 * Ce test fige la grammaire LL(1) SHDL cible via toBnf().
 * Il DOIT échouer avant la mise à jour de Grammar.SHDL / NonTerminal.
 */
public class GrammarFreezeTest {

    private static final String EXPECTED_BNF =
        "Start ::= Module\n" +
        "Module ::= ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW\n" +
        "Instance_Plus ::= Instance Instance_Star\n" +
        "Instance_Star ::= Instance Instance_Star\n" +
        "Instance_Star ::= ε\n" +
        "Separ_Param_Star ::= Separ Param Separ_Param_Star\n" +
        "Separ_Param_Star ::= ε\n" +
        "Signal ::= Identifiant Signal_Subset_Opt\n" +
        "Signal_Subset_Opt ::= LeftSquareBrack NaturalInteger Range_Opt RightSquareBrack\n" +
        "Signal_Subset_Opt ::= ε\n" +
        "Range_Opt ::= DotDot NaturalInteger\n" +
        "Range_Opt ::= ε\n" +
        "SignalOrLiteralCompound ::= Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star\n" +
        "Signal_Or_Litteral_Value ::= Signal\n" +
        "Signal_Or_Litteral_Value ::= LiteralValue\n" +
        "Concat_Signal_Or_Litteral_Value_Star ::= ConcatOp Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star\n" +
        "Concat_Signal_Or_Litteral_Value_Star ::= ε\n" +
        "DotDot ::= PointPoint\n" +
        "DotDot ::= Colon\n" +
        "Arg ::= SignalOrLiteralCompound\n" +
        "Param ::= Signal\n" +
        "Instance ::= Identifiant Operation\n" +
        "Instance ::= Dollar Identifiant ModuleCall\n" +
        "Operation ::= ModuleCall\n" +
        "Operation ::= Signal_Subset_Opt Assignment\n" +
        "Assignment ::= SignalAssignment\n" +
        "Assignment ::= MemoryAssignment\n" +
        "SumOfTermsCompound ::= SumOfTerms Concat_SumOfTerms_Star\n" +
        "Concat_SumOfTerms_Star ::= ConcatOp SumOfTerms Concat_SumOfTerms_Star\n" +
        "Concat_SumOfTerms_Star ::= ε\n" +
        "SumOfTerms ::= Term Or_Operand_Star\n" +
        "Or_Operand_Star ::= OrOp Term Or_Operand_Star\n" +
        "Or_Operand_Star ::= ε\n" +
        "Term ::= Factor And_Operand_Star\n" +
        "And_Operand_Star ::= AndOp Factor And_Operand_Star\n" +
        "And_Operand_Star ::= ε\n" +
        "Factor ::= LeftPar SumOfTerms RightPar\n" +
        "Factor ::= LiteralValue\n" +
        "Factor ::= NotOp Signal\n" +
        "Factor ::= Signal\n" +
        "SignalAssignment ::= AssignOp SumOfTermsCompound\n" +
        "MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms Comma_Opt Set_Or_Reset WhenKW SumOfTerms Enabled_Operand_Opt Semicolon_Opt\n" +
        "Comma_Opt ::= Comma\n" +
        "Comma_Opt ::= ε\n" +
        "Set_Or_Reset ::= ResetKW\n" +
        "Set_Or_Reset ::= SetKW\n" +
        "Enabled_Operand_Opt ::= Comma_Opt EnabledKW WhenKW SumOfTerms\n" +
        "Enabled_Operand_Opt ::= ε\n" +
        "Semicolon_Opt ::= Semicolon\n" +
        "Semicolon_Opt ::= ε\n" +
        "ModuleCall ::= LeftPar Arg Separ_Arg_Star RightPar\n" +
        "Separ_Arg_Star ::= Separ Arg Separ_Arg_Star\n" +
        "Separ_Arg_Star ::= ε\n" +
        "LiteralValue ::= BitField\n" +
        "Separ ::= Comma\n" +
        "Separ ::= Colon\n" +
        "AndOp ::= Star\n";

    @Test
    public void grammaireShdlEstFigeeParBnf() {
        String actual = Grammar.SHDL.toBnf();
        assertEquals(EXPECTED_BNF, actual);
    }
}
