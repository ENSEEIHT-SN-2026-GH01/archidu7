package parser.ll1.token;

import java.util.Objects;

public final class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;
    private final int offset;

    public Token(TokenType type, String value, int line, int column, int offset) {
        this.type = Objects.requireNonNull(type, "type");
        this.value = value;   // peut etre null pour EOF
        this.line = line;
        this.column = column;
        this.offset = offset;
    }

    public TokenType getType()   { return type; }
    public String    getValue()  { return value; }
    public int       getLine()   { return line; }
    public int       getColumn() { return column; }
    public int       getOffset() { return offset; }

    /**Indice du dernier caractère du Token dans le texte.
     * 
     * @return
     */
    public int getFin(){
        return offset + value.length() -1;
    }

    @Override public String toString() {
        return type + "(" + value + ")@" + line + ":" + column + "#" + offset;
    }
}
