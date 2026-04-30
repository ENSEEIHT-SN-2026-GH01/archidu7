package tests.parser.ll1.tabledriven.table;

import org.junit.Test;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.tabledriven.table.ParsingTable.TableKey;
import parser.ll1.token.TokenType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class ParsingTableTest {

    private static Production prodModule() {
        // Module ::= ModuleKW (production simplifiée pour le test)
        return new Production(NonTerminal.Module,
            List.of(new Terminal(TokenType.ModuleKW)));
    }

    @Test
    public void lookup_present_retourne_la_production() {
        Production p = prodModule();
        TableKey key = new TableKey(NonTerminal.Module, TokenType.ModuleKW);
        ParsingTable table = new ParsingTable(Map.of(key, p));
        assertEquals(Optional.of(p), table.lookup(NonTerminal.Module, TokenType.ModuleKW));
    }

    @Test
    public void lookup_absent_retourne_empty() {
        ParsingTable table = new ParsingTable(Map.of());
        assertEquals(Optional.empty(),
            table.lookup(NonTerminal.Module, TokenType.ModuleKW));
    }

    @Test
    public void table_immutable_apres_construction() {
        // La table doit être immutable : modifier la map d'origine ne doit pas
        // affecter la table.
        Production p = prodModule();
        TableKey key = new TableKey(NonTerminal.Module, TokenType.ModuleKW);
        java.util.HashMap<TableKey, Production> mutable = new java.util.HashMap<>();
        mutable.put(key, p);
        ParsingTable table = new ParsingTable(mutable);
        mutable.clear();  // ne doit PAS affecter la table
        assertEquals(Optional.of(p), table.lookup(NonTerminal.Module, TokenType.ModuleKW));
    }

    @Test(expected = NullPointerException.class)
    public void entries_null_rejete() {
        new ParsingTable(null);
    }

    @Test(expected = NullPointerException.class)
    public void table_key_nt_null_rejete() {
        new TableKey(null, TokenType.ModuleKW);
    }

    @Test(expected = NullPointerException.class)
    public void table_key_token_null_rejete() {
        new TableKey(NonTerminal.Module, null);
    }

    @Test
    public void table_key_egalite_records() {
        TableKey k1 = new TableKey(NonTerminal.Module, TokenType.ModuleKW);
        TableKey k2 = new TableKey(NonTerminal.Module, TokenType.ModuleKW);
        assertEquals(k1, k2);
        assertEquals(k1.hashCode(), k2.hashCode());
    }
}
