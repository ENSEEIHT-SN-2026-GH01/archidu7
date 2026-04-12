package parser.ll1.grammar;

import parser.ll1.token.TokenType;
import java.util.*;

public final class Ll1Conflict {
    public enum Type { FIRST_FIRST, FIRST_FOLLOW, LEFT_RECURSION }

    private final Type type;
    private final NonTerminal nt;
    private final Set<TokenType> conflictingTokens;
    private final String detail;

    public Ll1Conflict(Type type, NonTerminal nt, Set<TokenType> conflictingTokens, String detail) {
        this.type = type;
        this.nt = nt;
        this.conflictingTokens = Set.copyOf(conflictingTokens);
        this.detail = detail;
    }

    public Type getType() { return type; }
    public NonTerminal getNonTerminal() { return nt; }
    public Set<TokenType> getConflictingTokens() { return conflictingTokens; }
    public String getDetail() { return detail; }

    @Override public String toString() { return type + " sur " + nt + " : " + detail; }
}
