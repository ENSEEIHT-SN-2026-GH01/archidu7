package tests.parser.ll1.tabledriven.grammar;

import org.junit.BeforeClass;
import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Fige les ensembles FIRST caractéristiques de la grammaire SHDL LL(1) cible.
 * Un échec ici signale une évolution silencieuse de Grammar.SHDL ou des productions.
 *
 * Convention : CamelCase pour TokenType et NonTerminal.
 */
public class FirstSetShdlTest {

    private static FirstSet first;

    @BeforeClass
    public static void setUp() {
        first = new FirstSet(Grammar.SHDL);
    }

    // -----------------------------------------------------------------------
    // Test 1 : first(Module) = {ModuleKW}
    // Module ::= ModuleKW Identifiant ...
    // -----------------------------------------------------------------------
    @Test
    public void firstModule_estExactementModuleKW() {
        assertEquals(Set.of(TokenType.ModuleKW), first.of(NonTerminal.Module));
    }

    // -----------------------------------------------------------------------
    // Test 2 : first(Factor) = {LeftPar, BitField, NotOp, Identifiant}
    // Factor ::= LeftPar SumOfTerms RightPar
    //          | LiteralValue               (LiteralValue ::= BitField)
    //          | NotOp Signal
    //          | Signal                     (Signal ::= Identifiant ...)
    // -----------------------------------------------------------------------
    @Test
    public void firstFactor_contientLesQuatreAlternatives() {
        Set<TokenType> expected = Set.of(
                TokenType.LeftPar,
                TokenType.BitField,
                TokenType.NotOp,
                TokenType.Identifiant
        );
        assertEquals(expected, first.of(NonTerminal.Factor));
    }

    // -----------------------------------------------------------------------
    // Test 3 : first(Instance) = {Identifiant, Dollar}
    // Instance ::= Identifiant Operation
    //            | Dollar Identifiant ModuleCall
    // -----------------------------------------------------------------------
    @Test
    public void firstInstance_estExactementIdentifiantEtDollar() {
        assertEquals(Set.of(TokenType.Identifiant, TokenType.Dollar), first.of(NonTerminal.Instance));
    }

    // -----------------------------------------------------------------------
    // Test 4 : first(Operation) = {LeftPar, LeftSquareBrack, AssignOp, MemAssignOp}
    // Operation ::= ModuleCall              (ModuleCall ::= LeftPar ...)
    //             | Signal_Subset_Opt Assignment
    //   Signal_Subset_Opt ::= LeftSquareBrack ... | ε
    //   Assignment ::= SignalAssignment (AssignOp) | MemoryAssignment (MemAssignOp)
    // -----------------------------------------------------------------------
    @Test
    public void firstOperation_contientLesQuatreTerminaux() {
        Set<TokenType> expected = Set.of(
                TokenType.LeftPar,
                TokenType.LeftSquareBrack,
                TokenType.AssignOp,
                TokenType.MemAssignOp
        );
        assertEquals(expected, first.of(NonTerminal.Operation));
    }

    // -----------------------------------------------------------------------
    // Test 5 : first(Term) = first(Factor)
    // Term ::= Factor And_Operand_Star   → FIRST(Term) = FIRST(Factor)
    // -----------------------------------------------------------------------
    @Test
    public void firstTerm_egaleFirstFactor() {
        assertEquals(first.of(NonTerminal.Factor), first.of(NonTerminal.Term));
    }

    // -----------------------------------------------------------------------
    // Test 6 : first(SumOfTerms) = first(Factor)
    // SumOfTerms ::= Term Or_Operand_Star  → FIRST(SumOfTerms) = FIRST(Term) = FIRST(Factor)
    // -----------------------------------------------------------------------
    @Test
    public void firstSumOfTerms_egaleFirstFactor() {
        assertEquals(first.of(NonTerminal.Factor), first.of(NonTerminal.SumOfTerms));
    }

    // -----------------------------------------------------------------------
    // Test 7 : first(DotDot) = {PointPoint, Colon}
    // DotDot ::= PointPoint | Colon
    // -----------------------------------------------------------------------
    @Test
    public void firstDotDot_estExactementPointPointEtColon() {
        assertEquals(Set.of(TokenType.PointPoint, TokenType.Colon), first.of(NonTerminal.DotDot));
    }

    // -----------------------------------------------------------------------
    // Test 8 : first(Set_Or_Reset) = {ResetKW, SetKW}
    // Set_Or_Reset ::= ResetKW | SetKW
    // -----------------------------------------------------------------------
    @Test
    public void firstSetOrReset_estExactementResetKWEtSetKW() {
        assertEquals(Set.of(TokenType.ResetKW, TokenType.SetKW), first.of(NonTerminal.Set_Or_Reset));
    }

    // -----------------------------------------------------------------------
    // Test 9 : first(ModuleCall) = {LeftPar}
    // ModuleCall ::= LeftPar Arg Separ_Arg_Star RightPar
    // -----------------------------------------------------------------------
    @Test
    public void firstModuleCall_estExactementLeftPar() {
        assertEquals(Set.of(TokenType.LeftPar), first.of(NonTerminal.ModuleCall));
    }
}
