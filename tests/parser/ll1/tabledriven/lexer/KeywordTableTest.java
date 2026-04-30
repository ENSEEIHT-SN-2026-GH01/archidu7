package tests.parser.ll1.tabledriven.lexer;

import org.junit.Test;
import parser.ll1.tabledriven.lexer.KeywordTable;
import parser.ll1.token.TokenType;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class KeywordTableTest {
    @Test public void module_reconnu() {
        assertEquals(Optional.of(TokenType.ModuleKW), KeywordTable.lookup("module"));
    }
    @Test public void end_reconnu() {
        assertEquals(Optional.of(TokenType.EndKW), KeywordTable.lookup("end"));
    }
    @Test public void on_reconnu() {
        assertEquals(Optional.of(TokenType.OnKW), KeywordTable.lookup("on"));
    }
    @Test public void when_reconnu() {
        assertEquals(Optional.of(TokenType.WhenKW), KeywordTable.lookup("when"));
    }
    @Test public void set_reconnu() {
        assertEquals(Optional.of(TokenType.SetKW), KeywordTable.lookup("set"));
    }
    @Test public void reset_reconnu() {
        assertEquals(Optional.of(TokenType.ResetKW), KeywordTable.lookup("reset"));
    }
    @Test public void enabled_reconnu() {
        assertEquals(Optional.of(TokenType.EnabledKW), KeywordTable.lookup("enabled"));
    }

    @Test public void identifiant_quelconque_pas_reconnu() {
        assertEquals(Optional.empty(), KeywordTable.lookup("modulo"));
        assertEquals(Optional.empty(), KeywordTable.lookup("foo"));
        assertEquals(Optional.empty(), KeywordTable.lookup(""));
    }

    @Test public void casse_sensible() {
        assertEquals(Optional.empty(), KeywordTable.lookup("Module"));
        assertEquals(Optional.empty(), KeywordTable.lookup("END"));
        assertEquals(Optional.empty(), KeywordTable.lookup("ResEt"));
    }

    @Test public void couvre_les_7_keywords_du_BNF() {
        for (var kw : List.of("module", "end", "on", "when", "set", "reset", "enabled")) {
            assertTrue(kw + " manquant dans KeywordTable", KeywordTable.lookup(kw).isPresent());
        }
    }
}
