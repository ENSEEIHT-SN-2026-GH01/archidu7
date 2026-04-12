package tests.parser.ll1.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import parser.ll1.token.TokenType;
import java.util.Set;
import static org.junit.Assert.*;
import static parser.ll1.token.TokenType.*;

public class FollowSetTest {
    private final FirstSet first = new FirstSet(Grammar.SHDL);
    private final FollowSet follow = new FollowSet(Grammar.SHDL, first);

    @Test public void followModuleEstEof() {
        assertEquals(Set.of(EOF), follow.of(NonTerminal.MODULE));
    }

    @Test public void followInstanceListContientEnd() {
        assertTrue(follow.of(NonTerminal.INSTANCE_LIST).contains(END));
    }

    @Test public void followInstanceContientPremierInstanceEtEnd() {
        Set<TokenType> f = follow.of(NonTerminal.INSTANCE);
        assertTrue(f.contains(IDENTIFIER));
        assertTrue(f.contains(DOLLAR));
        assertTrue(f.contains(END));
    }

    @Test public void followFactorContientStarPlus() {
        Set<TokenType> f = follow.of(NonTerminal.FACTOR);
        assertTrue(f.contains(STAR));
        assertTrue(f.contains(PLUS));
    }
}
