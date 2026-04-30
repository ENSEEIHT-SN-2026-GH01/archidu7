package tests.parser.ll1.tabledriven.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import parser.lexer.Token;
import parser.ll1.tabledriven.CstParser;
import parser.ll1.tabledriven.ParsingException;

public class CstParserSimpleErrorTest {

    @Test
    public void source_vide_jette_parsing_exception() {
        ParsingException e = assertThrows(ParsingException.class,
                () -> CstParser.parse(""));
        assertEquals(0, e.offset());
    }

    @Test
    public void eof_premature_jette() {
        // "module foo" : manque LeftPar, parametres, instances, end module
        ParsingException e = assertThrows(ParsingException.class,
                () -> CstParser.parse("module foo"));
        assertTrue("L'offset doit pointer apres 'module '",
                e.offset() > 0);
    }

    @Test
    public void token_inattendu_jette() {
        // "module 42 () end module" : 42 est NaturalInteger la ou un Identifiant est attendu
        ParsingException e = assertThrows(ParsingException.class,
                () -> CstParser.parse("module 42 () end module"));
        assertNotNull(e.actual());
        assertEquals(Token.NaturalInteger, e.actual().getToken());
        assertEquals(Token.Identifiant, e.expected());
    }
}
