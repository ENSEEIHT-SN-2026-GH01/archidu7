package parser.ll1.token;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;   // peut être null pour EOF
        this.line = line;
        this.column = column;
    }

    public TokenType getType()   { return type; }
    public String    getValue()  { return value; }
    public int       getLine()   { return line; }
    public int       getColumn() { return column; }

    @Override public String toString() {
        return type + "(" + value + ")@" + line + ":" + column;
    }
}
