package parser.ll1.parser;

import parser.ll1.token.*;
import parser.ll1.ast.Module;
import java.util.*;

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

    // Stub — implémenté aux tâches suivantes
    private Module parseModule() {
        throw new UnsupportedOperationException("parseModule implémenté à la Task 17");
    }
}
