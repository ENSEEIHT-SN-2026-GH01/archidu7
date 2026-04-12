package parser.ll1.parser;

import parser.ll1.token.*;
import parser.ll1.ast.*;
import parser.ll1.ast.Module;
import java.util.*;
import java.util.ArrayList;

public final class Parser {
    static final int MAX_DEPTH = 64;

    private final List<Token> tokens;
    private final String source;
    private final String[] sourceLines;
    private int pos = 0;
    private int depth = 0;
    final Deque<String> grammarStack = new ArrayDeque<>();
    private boolean consumed = false;

    public Parser(List<Token> tokens) { this(tokens, null); }

    public Parser(List<Token> tokens, String source) {
        Objects.requireNonNull(tokens, "tokens");
        List<Token> ts = new ArrayList<>(tokens);
        if (ts.isEmpty() || ts.get(ts.size() - 1).getType() != TokenType.EOF) {
            Token last = ts.isEmpty() ? null : ts.get(ts.size() - 1);
            int l = last == null ? 1 : last.getLine();
            int c = last == null ? 1 : last.getColumn();
            ts.add(new Token(TokenType.EOF, null, l, c));
        }
        this.tokens = Collections.unmodifiableList(ts);
        this.source = source;
        this.sourceLines = source == null ? null : source.split("\n", -1);
    }

    public Module parse() {
        if (consumed) throw new IllegalStateException("parse() déjà appelé");
        consumed = true;
        if (tokens.size() == 1) {
            throw error(ErrorCode.EMPTY_FILE, Set.of(TokenType.MODULE));
        }
        Module m = parseModule();
        if (peek(0).getType() != TokenType.EOF) {
            throw error(ErrorCode.TRAILING_TOKENS, Set.of(TokenType.EOF));
        }
        return m;
    }

    // --- helpers (package-private pour tests éventuels) ---

    Token peek(int k) { return tokens.get(Math.min(pos + k, tokens.size() - 1)); }

    Token consume(TokenType expected) {
        Token t = peek(0);
        if (t.getType() != expected) {
            throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(expected));
        }
        pos++;
        return t;
    }

    boolean accept(TokenType t) {
        if (peek(0).getType() == t) { pos++; return true; }
        return false;
    }

    <R> R enterRule(String name, java.util.function.Supplier<R> body) {
        if (depth >= MAX_DEPTH) throw error(ErrorCode.DEPTH_EXCEEDED, Set.of());
        depth++;
        grammarStack.addLast(name);
        try { return body.get(); }
        finally { grammarStack.removeLast(); depth--; }
    }

    ParsingException error(ErrorCode code, Set<TokenType> expected) {
        Token t = peek(0);
        TokenType actual = t.getType();
        String snippet = snippet(t);
        if (actual == TokenType.EOF && code == ErrorCode.UNEXPECTED_TOKEN) {
            code = ErrorCode.EOF_UNEXPECTED;
        }
        return new ParsingException(code, t.getLine(), t.getColumn(),
            expected, actual, grammarStack, snippet, null);
    }

    String snippet(Token t) {
        if (sourceLines == null) return "";
        int idx = t.getLine() - 1;
        if (idx < 0 || idx >= sourceLines.length) return "";
        String line = sourceLines[idx];
        StringBuilder sb = new StringBuilder();
        sb.append(t.getLine()).append(" | ").append(line).append("\n");
        int pad = String.valueOf(t.getLine()).length() + 3 + Math.max(0, t.getColumn() - 1);
        for (int i = 0; i < pad; i++) sb.append(' ');
        sb.append('^');
        return sb.toString();
    }

    // --- Methods exposés pour tests (package-public via "public" ici) ---
    public Signal parseSignalForTest() { return enterRule("Signal", this::parseSignal); }
    public Factor parseFactorForTest() { return enterRule("Factor", this::parseFactor); }
    public Term   parseTermForTest()   { return enterRule("Term",   this::parseTerm); }
    public SumOfTerms parseSumOfTermsForTest() { return enterRule("SumOfTerms", this::parseSumOfTerms); }
    public SignalCompound parseSignalCompoundForTest() { return enterRule("SignalCompound", this::parseSignalCompound); }

    // --- Signal ---
    private Signal parseSignal() {
        Token id = consume(TokenType.IDENTIFIER);
        Position p = new Position(id.getLine(), id.getColumn());
        if (peek(0).getType() == TokenType.LBRACKET) {
            consume(TokenType.LBRACKET);
            Token hi = consume(TokenType.INTEGER);
            int hiV = Integer.parseInt(hi.getValue());
            if (peek(0).getType() == TokenType.RBRACKET) {
                consume(TokenType.RBRACKET);
                return new Signal(p, id.getValue(), hiV, null);
            }
            TokenType sep = peek(0).getType();
            if (sep != TokenType.DOTDOT && sep != TokenType.COLON) {
                throw error(ErrorCode.UNEXPECTED_TOKEN, Set.of(TokenType.RBRACKET, TokenType.DOTDOT, TokenType.COLON));
            }
            consume(sep);
            Token lo = consume(TokenType.INTEGER);
            consume(TokenType.RBRACKET);
            return new Signal(p, id.getValue(), hiV, Integer.parseInt(lo.getValue()));
        }
        return new Signal(p, id.getValue(), null, null);
    }

    private SignalCompound parseSignalCompound() {
        Signal first = enterRule("Signal", this::parseSignal);
        List<Signal> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.AMPERSAND) {
            consume(TokenType.AMPERSAND);
            list.add(enterRule("Signal", this::parseSignal));
        }
        return new SignalCompound(first.getPosition(), list);
    }

    // --- Factor / Term / SumOfTerms ---
    private Factor parseFactor() {
        Token t = peek(0);
        Position p = new Position(t.getLine(), t.getColumn());
        switch (t.getType()) {
            case LPAREN: {
                consume(TokenType.LPAREN);
                SumOfTerms inner = enterRule("SumOfTerms", this::parseSumOfTerms);
                consume(TokenType.RPAREN);
                return Factor.paren(p, inner);
            }
            case INTEGER: {
                consume(TokenType.INTEGER);
                String v = t.getValue();
                if ("0".equals(v)) return Factor.lit0(p);
                if ("1".equals(v)) return Factor.lit1(p);
                throw new ParsingException(ErrorCode.BIT_OUT_OF_RANGE, t.getLine(), t.getColumn(),
                    Set.of(TokenType.INTEGER), TokenType.INTEGER, grammarStack, snippet(t),
                    "Factor accepte uniquement 0 ou 1.");
            }
            case BITFIELD: {
                Token b = consume(TokenType.BITFIELD);
                return Factor.bits(p, new BitField(p, b.getValue()));
            }
            case SLASH: {
                consume(TokenType.SLASH);
                Signal s = enterRule("Signal", this::parseSignal);
                return Factor.negSignal(p, s);
            }
            case IDENTIFIER: {
                Signal s = enterRule("Signal", this::parseSignal);
                return Factor.signal(p, s);
            }
            default:
                throw error(ErrorCode.UNEXPECTED_TOKEN,
                    Set.of(TokenType.LPAREN, TokenType.INTEGER, TokenType.BITFIELD, TokenType.SLASH, TokenType.IDENTIFIER));
        }
    }

    private Term parseTerm() {
        Factor first = enterRule("Factor", this::parseFactor);
        List<Factor> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.STAR) {
            consume(TokenType.STAR);
            list.add(enterRule("Factor", this::parseFactor));
        }
        return new Term(first.getPosition(), list);
    }

    private SumOfTerms parseSumOfTerms() {
        Term first = enterRule("Term", this::parseTerm);
        List<Term> list = new ArrayList<>();
        list.add(first);
        while (peek(0).getType() == TokenType.PLUS) {
            consume(TokenType.PLUS);
            list.add(enterRule("Term", this::parseTerm));
        }
        return new SumOfTerms(first.getPosition(), list);
    }

    private List<SumOfTerms> parseSumOfTermsCompound() {
        List<SumOfTerms> out = new ArrayList<>();
        out.add(enterRule("SumOfTerms", this::parseSumOfTerms));
        while (peek(0).getType() == TokenType.AMPERSAND) {
            consume(TokenType.AMPERSAND);
            out.add(enterRule("SumOfTerms", this::parseSumOfTerms));
        }
        return out;
    }

    // Stub — implémenté aux tâches suivantes
    private Module parseModule() {
        throw new UnsupportedOperationException("parseModule implémenté à la Task 17");
    }
}
