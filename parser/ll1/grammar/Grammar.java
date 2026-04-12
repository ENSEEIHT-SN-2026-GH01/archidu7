package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class Grammar {
    public static final Grammar SHDL = buildShdl();

    private static Grammar buildShdl() {
        GrammarBuilder g = new GrammarBuilder();
        // Alias concis
        final NonTerminal
            MOD = NonTerminal.MODULE, PL = NonTerminal.PARAM_LIST, PLR = NonTerminal.PARAM_LIST_REST,
            PARAM = NonTerminal.PARAM, AL = NonTerminal.ARG_LIST, ALR = NonTerminal.ARG_LIST_REST,
            ARG = NonTerminal.ARG, SEP = NonTerminal.SEPAR,
            IL = NonTerminal.INSTANCE_LIST, ILR = NonTerminal.INSTANCE_LIST_REST, INS = NonTerminal.INSTANCE,
            AOT = NonTerminal.ASSIGN_OR_TRI, TST = NonTerminal.TRI_STATE_TAIL,
            MP = NonTerminal.MEMORY_POINT, MT = NonTerminal.MEM_TAIL, SR = NonTerminal.SET_RESET,
            OC = NonTerminal.OPT_COMMA, OS = NonTerminal.OPT_SEMI, OSC = NonTerminal.OPT_SEMI_OR_COMMA,
            MI = NonTerminal.MODULE_INSTANCE,
            MAP = NonTerminal.MAP, MVL = NonTerminal.MAP_VALUE_LIST, MV = NonTerminal.MAP_VALUE,
            FSM = NonTerminal.FSM, FK = NonTerminal.FSM_KEYWORD, FH = NonTerminal.FSM_HEADER,
            FRL = NonTerminal.FSM_RULE_LIST, FR = NonTerminal.FSM_RULE, FRR = NonTerminal.FSM_RULE_REST,
            FLL = NonTerminal.FSM_RULE_LEFT, SNL = NonTerminal.STATE_NAME_LIST, SNLR = NonTerminal.STATE_NAME_LIST_REST,
            SOTC = NonTerminal.SUM_OF_TERMS_COMPOUND, SOTCR = NonTerminal.SUM_OF_TERMS_COMPOUND_REST,
            SOT = NonTerminal.SUM_OF_TERMS, SOTR = NonTerminal.SUM_OF_TERMS_REST,
            T = NonTerminal.TERM, TR = NonTerminal.TERM_REST, F = NonTerminal.FACTOR,
            SC = NonTerminal.SIGNAL_COMPOUND, SCR = NonTerminal.SIGNAL_COMPOUND_REST,
            S = NonTerminal.SIGNAL, ST = NonTerminal.SIGNAL_TAIL, SAI = NonTerminal.SIGNAL_AFTER_INT,
            SOL = NonTerminal.SIGNAL_OR_LITERAL;

        // Module
        g.prod(MOD, TokenType.MODULE, TokenType.IDENTIFIER, TokenType.LPAREN, PL, TokenType.RPAREN,
                    IL, TokenType.END, TokenType.MODULE);
        // ParamList
        g.prod(PL, PARAM, PLR);
        g.prod(PLR, SEP, PARAM, PLR); g.eps(PLR);
        g.prod(PARAM, S);
        g.prod(SEP, TokenType.COMMA); g.prod(SEP, TokenType.COLON);
        // ArgList
        g.prod(AL, ARG, ALR);
        g.prod(ALR, SEP, ARG, ALR); g.eps(ALR);
        g.prod(ARG, SOL);
        g.prod(SOL, S);
        g.prod(SOL, TokenType.INTEGER);
        g.prod(SOL, TokenType.BITFIELD);
        // InstanceList
        g.prod(IL, INS, ILR);
        g.prod(ILR, INS, ILR); g.eps(ILR);
        // Instance (dispatch spécial lookahead-2 dans le parser, mais on enregistre les alternatives)
        g.prod(INS, MI);
        g.prod(INS, AOT);
        g.prod(INS, MP);
        g.prod(INS, FSM);
        g.prod(INS, MAP);
        // AssignOrTri
        g.prod(AOT, SC, TokenType.EQ, SOTC, TST);
        g.prod(TST, TokenType.OUTPUT, TokenType.ENABLED, TokenType.WHEN, SOT); g.eps(TST);
        // MemoryPoint
        g.prod(MP, SC, TokenType.ASSIGN, SOTC, TokenType.ON, SOT, OC, SR, TokenType.WHEN, SOT, MT, OS);
        g.prod(MT, OC, TokenType.ENABLED, TokenType.WHEN, SOT); g.eps(MT);
        g.prod(SR, TokenType.SET); g.prod(SR, TokenType.RESET);
        g.prod(OC, TokenType.COMMA); g.eps(OC);
        g.prod(OS, TokenType.SEMICOLON); g.eps(OS);
        g.prod(OSC, TokenType.SEMICOLON); g.prod(OSC, TokenType.COMMA); g.eps(OSC);
        // ModuleInstance
        g.prod(MI, TokenType.IDENTIFIER, TokenType.LPAREN, AL, TokenType.RPAREN);
        g.prod(MI, TokenType.DOLLAR, TokenType.IDENTIFIER, TokenType.LPAREN, AL, TokenType.RPAREN);
        // Map
        g.prod(MAP, TokenType.MAP, SC, TokenType.ARROW, SC, MVL, TokenType.END, TokenType.MAP);
        g.prod(MVL, MV, MVL); g.eps(MVL);
        g.prod(MV, TokenType.BITFIELD, TokenType.ARROW, TokenType.BITFIELD);
        // FSM
        g.prod(FSM, FK, FH, FRL, TokenType.END, FK);
        g.prod(FK, TokenType.FSM); g.prod(FK, TokenType.STATEMACHINE);
        g.prod(FH, TokenType.ASYNCHRONOUS);
        g.prod(FH, TokenType.SYNCHRONOUS, TokenType.ON, SOT, OC, TokenType.IDENTIFIER, TokenType.WHEN, SOT);
        g.prod(FH, TokenType.IDENTIFIER, TokenType.WHEN, SOT, OC, TokenType.SYNCHRONOUS, TokenType.ON, SOT);
        g.prod(FRL, FR, FRL); g.eps(FRL);
        g.prod(FR, FLL, TokenType.ARROW, TokenType.IDENTIFIER, FRR);
        g.prod(FRR, TokenType.WHEN, SOT, OSC); g.eps(FRR);
        g.prod(FLL, TokenType.STAR); g.prod(FLL, SNL);
        g.prod(SNL, TokenType.IDENTIFIER, SNLR);
        g.prod(SNLR, TokenType.COMMA, TokenType.IDENTIFIER, SNLR); g.eps(SNLR);
        // Expressions
        g.prod(SOTC, SOT, SOTCR);
        g.prod(SOTCR, TokenType.AMPERSAND, SOT, SOTCR); g.eps(SOTCR);
        g.prod(SOT, T, SOTR);
        g.prod(SOTR, TokenType.PLUS, T, SOTR); g.eps(SOTR);
        g.prod(T, F, TR);
        g.prod(TR, TokenType.STAR, F, TR); g.eps(TR);
        g.prod(F, TokenType.LPAREN, SOT, TokenType.RPAREN);
        g.prod(F, TokenType.INTEGER);
        g.prod(F, TokenType.BITFIELD);
        g.prod(F, TokenType.SLASH, S);
        g.prod(F, S);
        // Signal
        g.prod(SC, S, SCR);
        g.prod(SCR, TokenType.AMPERSAND, S, SCR); g.eps(SCR);
        g.prod(S, TokenType.IDENTIFIER, ST);
        g.prod(ST, TokenType.LBRACKET, TokenType.INTEGER, SAI); g.eps(ST);
        g.prod(SAI, TokenType.RBRACKET);
        g.prod(SAI, TokenType.DOTDOT, TokenType.INTEGER, TokenType.RBRACKET);
        g.prod(SAI, TokenType.COLON, TokenType.INTEGER, TokenType.RBRACKET);

        return g.build(MOD);
    }

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
}
