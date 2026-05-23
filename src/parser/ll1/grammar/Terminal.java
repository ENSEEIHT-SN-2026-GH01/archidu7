package parser.ll1.grammar;

import parser.lexer.Token;
import java.util.Objects;

public final class Terminal implements Symbol {
    public static final Terminal EPSILON = new Terminal(null, true);

    private final Token type;
    private final boolean epsilon;

    public Terminal(Token type) { this(Objects.requireNonNull(type), false); }
    private Terminal(Token type, boolean epsilon) { this.type = type; this.epsilon = epsilon; }

    public Token getType() { return type; }
    @Override public boolean isTerminal() { return !epsilon; }
    @Override public boolean isEpsilon()  { return epsilon; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terminal)) return false;
        Terminal t = (Terminal) o;
        return epsilon == t.epsilon && type == t.type;
    }
    @Override public int hashCode() { return Objects.hash(type, epsilon); }
    @Override public String toString() { return epsilon ? "ε" : type.name(); }
}
