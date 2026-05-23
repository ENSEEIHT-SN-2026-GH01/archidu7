package parser.ll1.grammar;

import parser.lexer.Token;
import java.util.*;

public final class Grammar {
    public static final Grammar SHDL = buildShdl();

    private static Grammar buildShdl() {
        GrammarBuilder g = new GrammarBuilder();

        // Alias concis — NTs de la grammaire LL(1) cible
        final NonTerminal
            START   = NonTerminal.Start,
            MOD     = NonTerminal.Module,
            IPLUS   = NonTerminal.Instance_Plus,
            ISTAR   = NonTerminal.Instance_Star,
            SPM_S   = NonTerminal.Separ_Param_Star,
            SIG     = NonTerminal.Signal,
            SS_OPT  = NonTerminal.Signal_Subset_Opt,
            RNG_OPT = NonTerminal.Range_Opt,
            SOLC    = NonTerminal.SignalOrLiteralCompound,
            SOL_V   = NonTerminal.Signal_Or_Litteral_Value,
            CSOL_S  = NonTerminal.Concat_Signal_Or_Litteral_Value_Star,
            DDOT    = NonTerminal.DotDot,
            ARG     = NonTerminal.Arg,
            PARAM   = NonTerminal.Param,
            INS     = NonTerminal.Instance,
            OP      = NonTerminal.Operation,
            ASSIGN  = NonTerminal.Assignment,
            SOTC    = NonTerminal.SumOfTermsCompound,
            CSOT_S  = NonTerminal.Concat_SumOfTerms_Star,
            SOT     = NonTerminal.SumOfTerms,
            OR_S    = NonTerminal.Or_Operand_Star,
            TERM    = NonTerminal.Term,
            AND_S   = NonTerminal.And_Operand_Star,
            FACTOR  = NonTerminal.Factor,
            SIG_A   = NonTerminal.SignalAssignment,
            MEM_A   = NonTerminal.MemoryAssignment,
            COM_OPT = NonTerminal.Comma_Opt,
            SET_RST = NonTerminal.Set_Or_Reset,
            EN_OPT  = NonTerminal.Enabled_Operand_Opt,
            SEM_OPT = NonTerminal.Semicolon_Opt,
            MCALL   = NonTerminal.ModuleCall,
            SARG_S  = NonTerminal.Separ_Arg_Star,
            LITVAL  = NonTerminal.LiteralValue,
            SEPAR   = NonTerminal.Separ,
            ANDOP   = NonTerminal.AndOp;

        // Aliases terminaux (parser.lexer.Token d Erwan)
        final Token
            ModuleKW        = Token.ModuleKW,
            EndKW           = Token.EndKW,
            OnKW            = Token.OnKW,
            WhenKW          = Token.WhenKW,
            SetKW           = Token.SetKW,
            ResetKW         = Token.ResetKW,
            EnabledKW       = Token.EnabledKW,
            Identifiant     = Token.Identifiant,
            BitField        = Token.BitField,
            NaturalInteger  = Token.NaturalInteger,
            AssignOp        = Token.AssignOp,
            MemAssignOp     = Token.MemAssignOp,
            OrOp            = Token.OrOp,
            Star            = Token.Star,
            ConcatOp        = Token.ConcatOp,
            NotOp           = Token.NotOp,
            LeftPar         = Token.LeftPar,
            RightPar        = Token.RightPar,
            LeftSquareBrack = Token.LeftSquareBrack,
            RightSquareBrack= Token.RightSquareBrack,
            Comma           = Token.Comma,
            Colon           = Token.Colon,
            Semicolon       = Token.Semicolon,
            PointPoint      = Token.PointPoint,
            Dollar          = Token.Dollar;

        // Start
        g.prod(START, MOD);

        // Module ::= ModuleKW Identifiant LeftPar Param Separ_Param_Star RightPar Instance_Plus EndKW ModuleKW
        g.prod(MOD, ModuleKW, Identifiant, LeftPar, PARAM, SPM_S, RightPar, IPLUS, EndKW, ModuleKW);

        // Instance_Plus ::= Instance Instance_Star
        g.prod(IPLUS, INS, ISTAR);

        // Instance_Star ::= Instance Instance_Star | ε
        g.prod(ISTAR, INS, ISTAR);
        g.eps(ISTAR);

        // Separ_Param_Star ::= Separ Param Separ_Param_Star | ε
        g.prod(SPM_S, SEPAR, PARAM, SPM_S);
        g.eps(SPM_S);

        // Signal ::= Identifiant Signal_Subset_Opt
        g.prod(SIG, Identifiant, SS_OPT);

        // Signal_Subset_Opt ::= LeftSquareBrack NaturalInteger Range_Opt RightSquareBrack | ε
        g.prod(SS_OPT, LeftSquareBrack, NaturalInteger, RNG_OPT, RightSquareBrack);
        g.eps(SS_OPT);

        // Range_Opt ::= DotDot NaturalInteger | ε
        g.prod(RNG_OPT, DDOT, NaturalInteger);
        g.eps(RNG_OPT);

        // SignalOrLiteralCompound ::= Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star
        g.prod(SOLC, SOL_V, CSOL_S);

        // Signal_Or_Litteral_Value ::= Signal | LiteralValue
        g.prod(SOL_V, SIG);
        g.prod(SOL_V, LITVAL);

        // Concat_Signal_Or_Litteral_Value_Star ::= ConcatOp Signal_Or_Litteral_Value Concat_Signal_Or_Litteral_Value_Star | ε
        g.prod(CSOL_S, ConcatOp, SOL_V, CSOL_S);
        g.eps(CSOL_S);

        // DotDot ::= PointPoint | Colon
        g.prod(DDOT, PointPoint);
        g.prod(DDOT, Colon);

        // Arg ::= SignalOrLiteralCompound
        g.prod(ARG, SOLC);

        // Param ::= Signal
        g.prod(PARAM, SIG);

        // Instance ::= Identifiant Operation | Dollar Identifiant ModuleCall
        g.prod(INS, Identifiant, OP);
        g.prod(INS, Dollar, Identifiant, MCALL);

        // Operation ::= ModuleCall | Signal_Subset_Opt Assignment
        g.prod(OP, MCALL);
        g.prod(OP, SS_OPT, ASSIGN);

        // Assignment ::= SignalAssignment | MemoryAssignment
        g.prod(ASSIGN, SIG_A);
        g.prod(ASSIGN, MEM_A);

        // SumOfTermsCompound ::= SumOfTerms Concat_SumOfTerms_Star
        g.prod(SOTC, SOT, CSOT_S);

        // Concat_SumOfTerms_Star ::= ConcatOp SumOfTerms Concat_SumOfTerms_Star | ε
        g.prod(CSOT_S, ConcatOp, SOT, CSOT_S);
        g.eps(CSOT_S);

        // SumOfTerms ::= Term Or_Operand_Star
        g.prod(SOT, TERM, OR_S);

        // Or_Operand_Star ::= OrOp Term Or_Operand_Star | ε
        g.prod(OR_S, OrOp, TERM, OR_S);
        g.eps(OR_S);

        // Term ::= Factor And_Operand_Star
        g.prod(TERM, FACTOR, AND_S);

        // And_Operand_Star ::= AndOp Factor And_Operand_Star | ε
        g.prod(AND_S, ANDOP, FACTOR, AND_S);
        g.eps(AND_S);

        // Factor ::= LeftPar SumOfTerms RightPar | LiteralValue | NotOp Signal | Signal
        g.prod(FACTOR, LeftPar, SOT, RightPar);
        g.prod(FACTOR, LITVAL);
        g.prod(FACTOR, NotOp, SIG);
        g.prod(FACTOR, SIG);

        // SignalAssignment ::= AssignOp SumOfTermsCompound
        g.prod(SIG_A, AssignOp, SOTC);

        // MemoryAssignment ::= MemAssignOp SumOfTermsCompound OnKW SumOfTerms Comma_Opt Set_Or_Reset WhenKW SumOfTerms Enabled_Operand_Opt Semicolon_Opt
        g.prod(MEM_A, MemAssignOp, SOTC, OnKW, SOT, COM_OPT, SET_RST, WhenKW, SOT, EN_OPT, SEM_OPT);

        // Comma_Opt ::= Comma | ε
        g.prod(COM_OPT, Comma);
        g.eps(COM_OPT);

        // Set_Or_Reset ::= ResetKW | SetKW
        g.prod(SET_RST, ResetKW);
        g.prod(SET_RST, SetKW);

        // Enabled_Operand_Opt ::= Comma_Opt EnabledKW WhenKW SumOfTerms | ε
        g.prod(EN_OPT, COM_OPT, EnabledKW, WhenKW, SOT);
        g.eps(EN_OPT);

        // Semicolon_Opt ::= Semicolon | ε
        g.prod(SEM_OPT, Semicolon);
        g.eps(SEM_OPT);

        // ModuleCall ::= LeftPar Arg Separ_Arg_Star RightPar
        g.prod(MCALL, LeftPar, ARG, SARG_S, RightPar);

        // Separ_Arg_Star ::= Separ Arg Separ_Arg_Star | ε
        g.prod(SARG_S, SEPAR, ARG, SARG_S);
        g.eps(SARG_S);

        // LiteralValue ::= BitField
        g.prod(LITVAL, BitField);

        // Separ ::= Comma | Colon
        g.prod(SEPAR, Comma);
        g.prod(SEPAR, Colon);

        // AndOp ::= Star
        g.prod(ANDOP, Star);

        return g.build(START);
    }

    // -----------------------------------------------------------------------

    private final NonTerminal axiom;
    private final List<Production> productions;
    private final Map<NonTerminal, List<Production>> byHead;

    public Grammar(NonTerminal axiom, List<Production> productions) {
        this.axiom = Objects.requireNonNull(axiom);
        this.productions = List.copyOf(Objects.requireNonNull(productions));
        Map<NonTerminal, List<Production>> m = new EnumMap<>(NonTerminal.class);
        for (Production p : this.productions) {
            m.computeIfAbsent(p.getHead(), k -> new ArrayList<>()).add(p);
        }
        Map<NonTerminal, List<Production>> frozen = new EnumMap<>(NonTerminal.class);
        m.forEach((k, v) -> frozen.put(k, List.copyOf(v)));
        this.byHead = Collections.unmodifiableMap(frozen);
    }

    public NonTerminal getAxiom() { return axiom; }
    public List<Production> getProductions() { return productions; }
    public List<Production> productionsOf(NonTerminal nt) {
        return byHead.getOrDefault(nt, List.of());
    }

    /**
     * Représentation BNF textuelle de la grammaire.
     * Format : une ligne par production, "Head ::= sym1 sym2 ..." avec ε pour epsilon.
     */
    public String toBnf() {
        StringBuilder sb = new StringBuilder();
        for (Production p : productions) {
            sb.append(p.getHead().name()).append(" ::=");
            if (p.isEpsilon()) {
                sb.append(" ε");
            } else {
                for (Symbol s : p.getBody()) {
                    sb.append(' ').append(s.toString());
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
