package parser.conversion;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstNode;
import simulateur.Module;

public final class Conversion {

    private Conversion() {
    }

    public static Module convert(CstNode tree) {
        if (!(tree instanceof CstInternal root)) {
            throw new ConversionException(tree.startOffset(), String.valueOf(tree.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "CST racine doit etre un CstInternal");
        }
        if (root.nt() != NonTerminal.Start) {
            throw new ConversionException(root.startOffset(), String.valueOf(root.symbol()),
                    ConversionException.Reason.MALFORMED_CST,
                    "CST racine doit etre Start");
        }
        CstNode module = root.first(NonTerminal.Module)
                .orElseThrow(() -> new ConversionException(root.startOffset(), "Start",
                        ConversionException.Reason.MALFORMED_CST,
                        "Start sans Module enfant"));
        return ModuleBuilder.build(module);
    }
}
