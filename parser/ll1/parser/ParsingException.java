package parser.ll1.parser;

import parser.ll1.token.TokenType;
import java.util.*;

public class ParsingException extends RuntimeException {
    private final ErrorCode code;
    private final int line, column;
    private final Set<TokenType> expected;
    private final TokenType actual;
    private final List<String> grammarContext;
    private final String sourceSnippet;
    private final String suggestion;

    public ParsingException(ErrorCode code, int line, int column,
                            Set<TokenType> expected, TokenType actual,
                            Deque<String> grammarStack, String sourceSnippet, String suggestion) {
        super(build(code, line, column, expected, actual, grammarStack, sourceSnippet, suggestion));
        this.code = code;
        this.line = line;
        this.column = column;
        this.expected = expected == null ? Set.of()
            : Collections.unmodifiableSet(new LinkedHashSet<>(expected));
        this.actual = actual;
        this.grammarContext = grammarStack == null ? List.of() : List.copyOf(grammarStack);
        this.sourceSnippet = sourceSnippet == null ? "" : sourceSnippet;
        this.suggestion = suggestion;
    }

    public ErrorCode getCode() { return code; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public Set<TokenType> getExpected() { return expected; }
    public TokenType getActual() { return actual; }
    public List<String> getGrammarContext() { return grammarContext; }
    public String getSourceSnippet() { return sourceSnippet; }
    public Optional<String> getSuggestion() { return Optional.ofNullable(suggestion); }

    private static String build(ErrorCode c, int l, int col, Set<TokenType> exp, TokenType act,
                                Deque<String> stack, String snip, String sug) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ligne ").append(l).append(", colonne ").append(col)
          .append(" [").append(c).append("]");
        if (exp != null && !exp.isEmpty()) sb.append(" : attendu ").append(exp);
        if (act != null) sb.append(", reçu ").append(act);
        if (snip != null && !snip.isEmpty()) sb.append("\n  ").append(snip);
        if (stack != null && !stack.isEmpty()) sb.append("\nContexte : ").append(String.join(" > ", stack));
        if (sug != null) sb.append("\nSuggestion : ").append(sug);
        return sb.toString();
    }
}
