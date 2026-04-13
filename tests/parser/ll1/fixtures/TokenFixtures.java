package tests.parser.ll1.fixtures;

import parser.ll1.token.*;
import java.util.*;

public final class TokenFixtures {
    public static Token tok(TokenType t) { return new Token(t, null, 1, 1); }
    public static Token tok(TokenType t, String v) { return new Token(t, v, 1, 1); }
    public static Token tok(TokenType t, String v, int line, int col) { return new Token(t, v, line, col); }
    public static List<Token> seq(Token... ts) {
        List<Token> l = new ArrayList<>(Arrays.asList(ts));
        l.add(tok(TokenType.EOF));
        return l;
    }
}
