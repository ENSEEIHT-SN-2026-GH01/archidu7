package tests.parser.ll1.ast;

import parser.ll1.ast.Module;
import parser.ll1.parser.Parser;
import parser.ll1.token.Token;
import tests.parser.ll1.fixtures.ShdlFixtures;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;

import java.util.List;

/**
 * Modules SHDL prêts à l'emploi pour tester un interprète.
 * Chaque méthode renvoie un {@link Module} déjà parsé — pas besoin
 * d'importer ni d'invoquer le parser ou les fixtures de tokens.
 *
 * <pre>{@code
 *     Module m = ExemplesAst.et();
 *     monInterprete.interpreter(m);
 * }</pre>
 */
public final class ExemplesAst {

    private ExemplesAst() {}

    /** module ET(a, b : c) c = a * b end module */
    public static Module et() {
        return parse(ShdlFixtures.moduleET());
    }

    /** module OUN(a, b : c) c = /a + b end module */
    public static Module oun() {
        return parse(seq(
            tok(MODULE), tok(IDENTIFIER, "OUN"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
            tok(COLON), tok(IDENTIFIER, "c"), tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ),
            tok(SLASH), tok(IDENTIFIER, "a"), tok(PLUS), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE)));
    }

    /** module MUX(a, b, s : c) c = s*a + /s*b end module */
    public static Module mux() {
        return parse(seq(
            tok(MODULE), tok(IDENTIFIER, "MUX"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
            tok(COMMA), tok(IDENTIFIER, "s"),
            tok(COLON), tok(IDENTIFIER, "c"), tok(RPAREN),
            tok(IDENTIFIER, "c"), tok(EQ),
            tok(IDENTIFIER, "s"), tok(STAR), tok(IDENTIFIER, "a"),
            tok(PLUS),
            tok(SLASH), tok(IDENTIFIER, "s"), tok(STAR), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE)));
    }

    /** module DEUX(a, b : x, y) x = a  y = b end module */
    public static Module deuxSorties() {
        return parse(seq(
            tok(MODULE), tok(IDENTIFIER, "DEUX"), tok(LPAREN),
            tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
            tok(COLON), tok(IDENTIFIER, "x"), tok(COMMA), tok(IDENTIFIER, "y"),
            tok(RPAREN),
            tok(IDENTIFIER, "x"), tok(EQ), tok(IDENTIFIER, "a"),
            tok(IDENTIFIER, "y"), tok(EQ), tok(IDENTIFIER, "b"),
            tok(END), tok(MODULE)));
    }

    /**
     * module BasculeD(d, clk : q) q := d on clk, set when 1 end module
     * Contient un MemoryPoint — hors scope MVP, utile pour tester le rejet propre.
     */
    public static Module basculeD() {
        return parse(ShdlFixtures.moduleBasculeD());
    }

    /**
     * module DecBCD(a : b) map a -> b "0000" -> "1111" end map end module
     * Contient un MapNode — hors scope MVP, utile pour tester le rejet propre.
     */
    public static Module decBCD() {
        return parse(ShdlFixtures.moduleDecodeurBCD());
    }

    private static Module parse(List<Token> tokens) {
        return new Parser(tokens).parse();
    }
}
