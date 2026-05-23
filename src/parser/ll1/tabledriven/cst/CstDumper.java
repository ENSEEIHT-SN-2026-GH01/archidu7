package parser.ll1.tabledriven.cst;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;

import java.util.List;

/**
 * Produit une representation textuelle indentee (box-drawing) d'un {@link CstNode}.
 *
 * <p>Format pour un {@link CstInternal} :
 * <pre>
 *   NT @start..end [NT -> sym1 sym2 ...]
 * </pre>
 * Si la production est epsilon ou children est vide, ajoute deux espaces puis {@code (vide)}.
 *
 * <p>Format pour un {@link CstLeaf} avec texte non-vide :
 * <pre>
 *   TokenType("texte") @start..end
 * </pre>
 * Si le texte est vide :
 * <pre>
 *   TokenType @start..end
 * </pre>
 *
 * <p>Prefixes box-drawing :
 * <ul>
 *   <li>{@code ├── } pour un enfant non-dernier</li>
 *   <li>{@code └── } pour le dernier enfant</li>
 *   <li>Indentation cumulee par {@code │   } (sous ├──) ou {@code     } (sous └──)</li>
 * </ul>
 */
public final class CstDumper {

    private CstDumper() {}

    /**
     * Retourne la representation textuelle indentee du CST roote en {@code root}.
     *
     * @param root le noeud racine, non null
     * @return une chaine multi-lignes, terminee par '\n'
     */
    public static String dump(CstNode root) {
        StringBuilder sb = new StringBuilder();
        dumpNode(root, sb, "", "");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Implementation recursive
    // -----------------------------------------------------------------------

    private static void dumpNode(CstNode node, StringBuilder sb,
                                  String prefix, String childPrefix) {
        sb.append(prefix);
        appendNodeLabel(node, sb);
        sb.append('\n');

        if (node instanceof CstInternal internal) {
            List<CstNode> children = internal.children();
            for (int i = 0; i < children.size(); i++) {
                boolean last = (i == children.size() - 1);
                String connector  = last ? "└── " : "├── ";
                String continuation = last ? "    " : "│   ";
                dumpNode(children.get(i), sb,
                        childPrefix + connector,
                        childPrefix + continuation);
            }
        }
        // CstLeaf : pas d'enfants
    }

    private static void appendNodeLabel(CstNode node, StringBuilder sb) {
        if (node instanceof CstLeaf leaf) {
            // Format : TokenType("text") @start..end  ou  TokenType @start..end
            String tokenName = leaf.t().getType().name();
            String text = leaf.lexem().getText();
            sb.append(tokenName);
            if (text != null && !text.isEmpty()) {
                sb.append("(\"").append(text).append("\")");
            }
            sb.append(" @").append(leaf.startOffset()).append("..").append(leaf.endOffset());
        } else {
            CstInternal internal = (CstInternal) node;
            NonTerminal nt = internal.nt();
            sb.append(nt.name())
              .append(" @").append(internal.startOffset()).append("..").append(internal.endOffset())
              .append(" [").append(nt.name()).append(" → ").append(productionBody(internal.rule())).append(']');
            if (internal.children().isEmpty()) {
                sb.append("  (vide)");
            }
        }
    }

    /** Retourne la representation des symboles du corps de la production. */
    private static String productionBody(Production prod) {
        if (prod.isEpsilon()) {
            return "ε"; // ε
        }
        StringBuilder sb = new StringBuilder();
        for (Symbol s : prod.getBody()) {
            if (sb.length() > 0) sb.append(' ');
            if (s instanceof NonTerminal nt) {
                sb.append(nt.name());
            } else if (s instanceof Terminal t) {
                sb.append(t.getType().name());
            }
        }
        return sb.toString();
    }
}
