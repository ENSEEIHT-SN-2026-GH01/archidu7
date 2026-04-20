package tests.parser.ll1.fixtures;

import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import java.util.Arrays;
import java.util.List;

public final class TokenFixtures {
    public static Token tok(TokenType t) { return new Token(t, null, 1, 1, 0); }
    public static Token tok(TokenType t, String v) { return new Token(t, v, 1, 1, 0); }
    public static Token tok(TokenType t, String v, int line, int col) { return new Token(t, v, line, col, 0); }
    public static Token tok(TokenType t, String v, int line, int col, int offset) { return new Token(t, v, line, col, offset); }

    public static List<Token> seq(Token... tokens) { return Arrays.asList(tokens); }
}
