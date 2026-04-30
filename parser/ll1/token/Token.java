package parser.ll1.token;

import java.util.Objects;

/**
 * Token lexical : type + lexème matché + offset absolu dans la source.
 *
 * <p>{@code value} contient le lexème exact tel que présent dans la source
 * (y compris pour les mots-clés et les délimiteurs). Seul l'EOF a
 * {@code value == null} car c'est une sentinelle sans lexème. {@link #end()}
 * retourne {@code offset + value.length()} (ou {@code offset} pour l'EOF) et
 * pointe sur le caractère APRÈS le dernier de ce token (intervalle
 * demi-ouvert {@code [offset, end)}).
 */
public record Token(TokenType type, String value, int offset) {
    public Token {
        Objects.requireNonNull(type, "type");
        if (offset < 0) throw new IllegalArgumentException("offset < 0");
    }

    /** Indice (exclu) de la fin du token dans la source. */
    public int end() {
        return offset + (value == null ? 0 : value.length());
    }
}
