package tests.parser.ll1.ast;

import parser.ll1.ast.AstDumper;
import parser.ll1.ast.Module;
import parser.ll1.parser.Parser;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import tests.parser.ll1.fixtures.ShdlFixtures;
import static tests.parser.ll1.fixtures.TokenFixtures.*;
import static parser.ll1.token.TokenType.*;

import java.util.List;

/**
 * Harnais qui imprime le dump AST pour quelques exemples canoniques.
 * Lancement : {@code java ... tests.parser.ll1.ast.AstDumperDemo}
 * Sert à capturer les sorties pour la doc destinée à Mati.
 */
public final class AstDumperDemo {
    public static void main(String[] args) {
        run("1. ET simple : module ET(a, b : c) c = a * b end module",
            ShdlFixtures.moduleET());
        run("2. OU avec NON : module OUN(a, b : c) c = /a + b end module",
            seq(tok(MODULE), tok(IDENTIFIER, "OUN"), tok(LPAREN),
                tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
                tok(COLON), tok(IDENTIFIER, "c"), tok(RPAREN),
                tok(IDENTIFIER, "c"), tok(EQ),
                tok(SLASH), tok(IDENTIFIER, "a"), tok(PLUS), tok(IDENTIFIER, "b"),
                tok(END), tok(MODULE)));
        run("3. Expression parenthésée : module MUX(a, b, s : c) c = s*a + /s*b end module",
            seq(tok(MODULE), tok(IDENTIFIER, "MUX"), tok(LPAREN),
                tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
                tok(COMMA), tok(IDENTIFIER, "s"),
                tok(COLON), tok(IDENTIFIER, "c"), tok(RPAREN),
                tok(IDENTIFIER, "c"), tok(EQ),
                tok(IDENTIFIER, "s"), tok(STAR), tok(IDENTIFIER, "a"),
                tok(PLUS),
                tok(SLASH), tok(IDENTIFIER, "s"), tok(STAR), tok(IDENTIFIER, "b"),
                tok(END), tok(MODULE)));
        run("4. Multi-sorties : module DEUX(a, b : x, y) x = a  y = b end module",
            seq(tok(MODULE), tok(IDENTIFIER, "DEUX"), tok(LPAREN),
                tok(IDENTIFIER, "a"), tok(COMMA), tok(IDENTIFIER, "b"),
                tok(COLON), tok(IDENTIFIER, "x"), tok(COMMA), tok(IDENTIFIER, "y"),
                tok(RPAREN),
                tok(IDENTIFIER, "x"), tok(EQ), tok(IDENTIFIER, "a"),
                tok(IDENTIFIER, "y"), tok(EQ), tok(IDENTIFIER, "b"),
                tok(END), tok(MODULE)));
        run("5. Bascule D : module BasculeD(d, clk : q) q := d on clk, set when 1 end module",
            ShdlFixtures.moduleBasculeD());
    }

    private static void run(String title, List<Token> tokens) {
        System.out.println("==== " + title + " ====");
        Module m = new Parser(tokens).parse();
        System.out.println(AstDumper.dump(m));
    }
}
