package tests.parser.ll1.tabledriven.grammar;

import org.junit.BeforeClass;
import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;

import static org.junit.Assert.*;

/**
 * Fige les ensembles FOLLOW caractéristiques de la grammaire SHDL LL(1) cible.
 * Un échec ici signale une évolution silencieuse de Grammar.SHDL ou des productions.
 *
 * Convention : CamelCase pour TokenType et NonTerminal.
 */
public class FollowSetShdlTest {

    private static FollowSet follow;

    @BeforeClass
    public static void setUp() {
        FirstSet first = new FirstSet(Grammar.SHDL);
        follow = new FollowSet(Grammar.SHDL, first);
    }

    // -----------------------------------------------------------------------
    // Test 1 : EOF ∈ follow(Module)
    // Start ::= Module  → EOF mis dans follow(axiome) par convention
    // -----------------------------------------------------------------------
    @Test
    public void followModule_contientEOF() {
        assertTrue(follow.of(NonTerminal.Module).contains(TokenType.EOF));
    }

    // -----------------------------------------------------------------------
    // Test 2 : follow(Instance) ⊇ {EndKW, Identifiant, Dollar}
    // Instance_Plus ::= Instance Instance_Star
    // Instance_Star ::= Instance Instance_Star | ε
    //   → FIRST(Instance) = {Identifiant, Dollar} ∈ follow(Instance)
    //   → Instance_Star nullable + Instance_Plus dans Module avant EndKW
    //     → EndKW ∈ follow(Instance)
    // -----------------------------------------------------------------------
    @Test
    public void followInstance_contientEndKW() {
        assertTrue(follow.of(NonTerminal.Instance).contains(TokenType.EndKW));
    }

    @Test
    public void followInstance_contientIdentifiantEtDollar() {
        assertTrue(follow.of(NonTerminal.Instance).contains(TokenType.Identifiant));
        assertTrue(follow.of(NonTerminal.Instance).contains(TokenType.Dollar));
    }

    // -----------------------------------------------------------------------
    // Test 3 : follow(Param) ⊇ {Comma, Colon, RightPar}
    // Module ::= ... Param Separ_Param_Star RightPar ...
    //   FIRST(Separ_Param_Star) ∖ {ε} = FIRST(Separ) = {Comma, Colon}
    //   Separ_Param_Star nullable → RightPar ∈ follow(Param)
    // Separ_Param_Star ::= Separ Param Separ_Param_Star
    //   → follow(Param) ⊇ FIRST(Separ_Param_Star) ∖ {ε} ∪ follow(Separ_Param_Star)
    //     ⊇ {Comma, Colon, RightPar}
    // -----------------------------------------------------------------------
    @Test
    public void followParam_contientCommaColonRightPar() {
        assertTrue(follow.of(NonTerminal.Param).contains(TokenType.Comma));
        assertTrue(follow.of(NonTerminal.Param).contains(TokenType.Colon));
        assertTrue(follow.of(NonTerminal.Param).contains(TokenType.RightPar));
    }

    // -----------------------------------------------------------------------
    // Test 4 : Star ∈ follow(Factor)
    // Term ::= Factor And_Operand_Star
    //   FIRST(And_Operand_Star) = FIRST(AndOp) = {Star}
    //   → Star ∈ follow(Factor)
    // And_Operand_Star ::= AndOp Factor And_Operand_Star
    //   → Star ∈ follow(Factor) via la récurrence aussi
    // -----------------------------------------------------------------------
    @Test
    public void followFactor_contientStar() {
        assertTrue(follow.of(NonTerminal.Factor).contains(TokenType.Star));
    }

    // -----------------------------------------------------------------------
    // Test 5 : OrOp ∈ follow(Term)
    // SumOfTerms ::= Term Or_Operand_Star
    //   FIRST(Or_Operand_Star) = FIRST(OrOp) = {OrOp}
    //   → OrOp ∈ follow(Term)
    // -----------------------------------------------------------------------
    @Test
    public void followTerm_contientOrOp() {
        assertTrue(follow.of(NonTerminal.Term).contains(TokenType.OrOp));
    }

    // -----------------------------------------------------------------------
    // Test 6 : follow(SumOfTerms) ⊇ {Comma, ResetKW, SetKW}
    // MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms Comma_Opt Set_Or_Reset ...
    //   Après le 1er SumOfTerms : FIRST(Comma_Opt) = {Comma}, Set_Or_Reset non nullable
    //   → Comma ∈ follow(SumOfTerms)
    //   Comma_Opt nullable → FIRST(Set_Or_Reset) = {ResetKW, SetKW} ∈ follow(SumOfTerms)
    // -----------------------------------------------------------------------
    @Test
    public void followSumOfTerms_contientCommaResetKWSetKW() {
        assertTrue(follow.of(NonTerminal.SumOfTerms).contains(TokenType.Comma));
        assertTrue(follow.of(NonTerminal.SumOfTerms).contains(TokenType.ResetKW));
        assertTrue(follow.of(NonTerminal.SumOfTerms).contains(TokenType.SetKW));
    }

    // -----------------------------------------------------------------------
    // Test 7 : RightPar ∈ follow(SumOfTerms)
    // Factor ::= LeftPar SumOfTerms RightPar → RightPar ∈ follow(SumOfTerms)
    // -----------------------------------------------------------------------
    @Test
    public void followSumOfTerms_contientRightPar() {
        assertTrue(follow.of(NonTerminal.SumOfTerms).contains(TokenType.RightPar));
    }
}
