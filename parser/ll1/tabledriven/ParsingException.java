package parser.ll1.tabledriven;

import parser.ll1.grammar.NonTerminal;
import parser.lexer.Lexem;
import parser.lexer.Token;

public class ParsingException extends RuntimeException {
    private final int offset;
    private final Token expected;       // peut etre null
    private final Lexem<Token> actual;  // peut etre null
    private final NonTerminal context;  // peut etre null

    public ParsingException(String message, int offset, Token expected,
                            Lexem<Token> actual, NonTerminal context) {
        super(format(message, offset, expected, actual, context));
        this.offset = offset;
        this.expected = expected;
        this.actual = actual;
        this.context = context;
    }

    private static String format(String msg, int offset, Token expected,
                                 Lexem<Token> actual, NonTerminal ctx) {
        StringBuilder sb = new StringBuilder("Erreur syntaxique a l'offset ")
            .append(offset).append(" : ").append(msg);
        if (expected != null) sb.append(" (attendu ").append(expected).append(')');
        if (actual != null) {
            sb.append(" (trouve ").append(actual.getToken());
            if (actual.getText() != null) sb.append("(\"").append(actual.getText()).append("\")");
            sb.append(')');
        }
        if (ctx != null) sb.append(" [contexte ").append(ctx).append(']');
        return sb.toString();
    }

    public int offset() { return offset; }
    public Token expected() { return expected; }
    public Lexem<Token> actual() { return actual; }
    public NonTerminal context() { return context; }
}
