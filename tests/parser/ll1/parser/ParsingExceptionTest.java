package tests.parser.ll1.parser;

import org.junit.Test;
import parser.ll1.parser.*;
import parser.ll1.token.TokenType;
import java.util.*;
import static org.junit.Assert.*;

public class ParsingExceptionTest {
    @Test public void snapshotGrammarStack() {
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("Module"); stack.push("InstanceList");
        ParsingException e = new ParsingException(ErrorCode.UNEXPECTED_TOKEN, 1, 1, 0,
            new LinkedHashSet<>(List.of(TokenType.EQ)), TokenType.PLUS,
            stack, "", null);
        stack.clear();
        assertEquals(2, e.getGrammarContext().size());
    }

    @Test public void getMessageContientCodeEtLine() {
        ParsingException e = new ParsingException(ErrorCode.EMPTY_FILE, 1, 1, 0,
            null, null, null, null, null);
        assertTrue(e.getMessage().contains("EMPTY_FILE"));
    }

    @Test public void gettersRetournentValeursInjectees() {
        ParsingException e = new ParsingException(ErrorCode.UNEXPECTED_TOKEN, 3, 12, 42,
            java.util.Set.of(TokenType.SEMICOLON), TokenType.IDENTIFIER,
            new java.util.ArrayDeque<>(), "", null);
        assertEquals(ErrorCode.UNEXPECTED_TOKEN, e.getCode());
        assertEquals(3, e.getLine());
        assertEquals(12, e.getColumn());
        assertEquals(42, e.getOffset());
        assertEquals(TokenType.IDENTIFIER, e.getActual());
    }

    @Test public void gettersRetournentValeursInjecteesBitOutOfRange() {
        ParsingException e = new ParsingException(ErrorCode.BIT_OUT_OF_RANGE, 3, 12, 0,
            new LinkedHashSet<>(List.of(TokenType.INTEGER)), TokenType.INTEGER,
            null, "context", "try 0 or 1");
        assertEquals(ErrorCode.BIT_OUT_OF_RANGE, e.getCode());
        assertEquals(3, e.getLine());
        assertEquals(12, e.getColumn());
        assertEquals(0, e.getOffset());
        assertEquals(TokenType.INTEGER, e.getActual());
        assertEquals("context", e.getSourceSnippet());
        assertEquals(Optional.of("try 0 or 1"), e.getSuggestion());
    }
}
