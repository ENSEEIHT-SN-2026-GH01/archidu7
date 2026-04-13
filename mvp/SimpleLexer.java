package mvp;

import parser.ll1.parser.Lexer;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lexer minimal pour la demo MVP : couvre le sous-ensemble SHDL utilise
 * dans modules/demo-et.shdl (mots-cles module/end, identifiants, parentheses,
 * virgule, deux-points, point-virgule, =, *, +, /).
 *
 * Pas le lexer "officiel" : c'est un bouchon le temps que celui d'Erwan
 * soit branche. Il n'a pas vocation a couvrir toute la grammaire SHDL.
 */
public final class SimpleLexer implements Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
        Map.entry("module",       TokenType.MODULE),
        Map.entry("end",          TokenType.END),
        Map.entry("map",          TokenType.MAP),
        Map.entry("fsm",          TokenType.FSM),
        Map.entry("statemachine", TokenType.STATEMACHINE),
        Map.entry("on",           TokenType.ON),
        Map.entry("when",         TokenType.WHEN),
        Map.entry("set",          TokenType.SET),
        Map.entry("reset",        TokenType.RESET),
        Map.entry("enabled",      TokenType.ENABLED),
        Map.entry("output",       TokenType.OUTPUT),
        Map.entry("asynchronous", TokenType.ASYNCHRONOUS),
        Map.entry("synchronous",  TokenType.SYNCHRONOUS)
    );

    @Override
    public List<Token> tokenize(String source) {
        List<Token> out = new ArrayList<>();
        int line = 1, col = 1;
        int i = 0, n = source.length();
        while (i < n) {
            char c = source.charAt(i);
            if (c == '\n') { line++; col = 1; i++; continue; }
            if (Character.isWhitespace(c)) { col++; i++; continue; }
            if (c == '-' && i + 1 < n && source.charAt(i + 1) == '-') {
                while (i < n && source.charAt(i) != '\n') { i++; col++; }
                continue;
            }
            int startCol = col;
            if (Character.isLetter(c) || c == '_') {
                int j = i;
                while (j < n && (Character.isLetterOrDigit(source.charAt(j)) || source.charAt(j) == '_')) j++;
                String word = source.substring(i, j);
                TokenType t = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
                out.add(new Token(t, word, line, startCol));
                col += (j - i); i = j; continue;
            }
            if (Character.isDigit(c)) {
                int j = i;
                while (j < n && Character.isDigit(source.charAt(j))) j++;
                out.add(new Token(TokenType.INTEGER, source.substring(i, j), line, startCol));
                col += (j - i); i = j; continue;
            }
            if (c == '"') {
                int j = i + 1;
                while (j < n && source.charAt(j) != '"') j++;
                String bits = source.substring(i + 1, Math.min(j, n));
                out.add(new Token(TokenType.BITFIELD, bits, line, startCol));
                col += (j - i + 1); i = Math.min(j + 1, n); continue;
            }
            switch (c) {
                case '=': out.add(new Token(TokenType.EQ, "=", line, startCol)); i++; col++; break;
                case '*': out.add(new Token(TokenType.STAR, "*", line, startCol)); i++; col++; break;
                case '+': out.add(new Token(TokenType.PLUS, "+", line, startCol)); i++; col++; break;
                case '/': out.add(new Token(TokenType.SLASH, "/", line, startCol)); i++; col++; break;
                case '&': out.add(new Token(TokenType.AMPERSAND, "&", line, startCol)); i++; col++; break;
                case '(': out.add(new Token(TokenType.LPAREN, "(", line, startCol)); i++; col++; break;
                case ')': out.add(new Token(TokenType.RPAREN, ")", line, startCol)); i++; col++; break;
                case '[': out.add(new Token(TokenType.LBRACKET, "[", line, startCol)); i++; col++; break;
                case ']': out.add(new Token(TokenType.RBRACKET, "]", line, startCol)); i++; col++; break;
                case ',': out.add(new Token(TokenType.COMMA, ",", line, startCol)); i++; col++; break;
                case ';': out.add(new Token(TokenType.SEMICOLON, ";", line, startCol)); i++; col++; break;
                case '$': out.add(new Token(TokenType.DOLLAR, "$", line, startCol)); i++; col++; break;
                case ':':
                    if (i + 1 < n && source.charAt(i + 1) == '=') {
                        out.add(new Token(TokenType.ASSIGN, ":=", line, startCol));
                        i += 2; col += 2;
                    } else {
                        out.add(new Token(TokenType.COLON, ":", line, startCol));
                        i++; col++;
                    }
                    break;
                case '.':
                    if (i + 1 < n && source.charAt(i + 1) == '.') {
                        out.add(new Token(TokenType.DOTDOT, "..", line, startCol));
                        i += 2; col += 2;
                    } else {
                        throw new IllegalArgumentException("Caractere imprevu '.' en " + line + ":" + col);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Caractere imprevu '" + c + "' en " + line + ":" + col);
            }
        }
        out.add(new Token(TokenType.EOF, null, line, col));
        return out;
    }
}
