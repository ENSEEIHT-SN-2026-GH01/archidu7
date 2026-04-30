package parser.ll1.tabledriven;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

public class ParsingException extends RuntimeException {
    private final int offset;
    private final TokenType expected;   // peut être null
    private final Token actual;          // peut être null
    private final NonTerminal context;   // peut être null

    public ParsingException(String message, int offset, TokenType expected,
                            Token actual, NonTerminal context) {
        super(format(message, offset, expected, actual, context));
        this.offset = offset;
        this.expected = expected;
        this.actual = actual;
        this.context = context;
    }

    private static String format(String msg, int offset, TokenType expected,
                                 Token actual, NonTerminal ctx) {
        StringBuilder sb = new StringBuilder("Erreur syntaxique a l'offset ")
            .append(offset).append(" : ").append(msg);
        if (expected != null) sb.append(" (attendu ").append(expected).append(')');
        if (actual != null) {
            sb.append(" (trouve ").append(actual.type());
            if (actual.value() != null) sb.append("(\"").append(actual.value()).append("\")");
            sb.append(')');
        }
        if (ctx != null) sb.append(" [contexte ").append(ctx).append(']');
        return sb.toString();
    }

    public int offset() { return offset; }
    public TokenType expected() { return expected; }
    public Token actual() { return actual; }
    public NonTerminal context() { return context; }
}
