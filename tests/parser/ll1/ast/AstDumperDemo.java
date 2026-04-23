package tests.parser.ll1.ast;

import parser.ll1.ast.AstDumper;
import parser.ll1.ast.Module;

/**
 * Harnais qui imprime le dump AST pour les exemples canoniques.
 * Lancement : {@code java ... tests.parser.ll1.ast.AstDumperDemo}
 */
public final class AstDumperDemo {
    public static void main(String[] args) {
        run("1. ET simple : c = a * b",                ExemplesAst.et());
        run("2. OU avec NON : c = /a + b",             ExemplesAst.oun());
        run("3. Multiplexeur : c = s*a + /s*b",        ExemplesAst.mux());
        run("4. Multi-sorties : x = a ; y = b",        ExemplesAst.deuxSorties());
        run("5. Bascule D (hors scope) : q := d on clk, set when 1", ExemplesAst.basculeD());
        run("6. Table de verite (hors scope) : map a -> b",          ExemplesAst.decBCD());
    }

    private static void run(String title, Module m) {
        System.out.println("==== " + title + " ====");
        System.out.println(AstDumper.dump(m));
    }
}
