package parser.ll1.tabledriven;

import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
import parser.ll1.tabledriven.lexer.ShdlLexer;
import parser.ll1.tabledriven.table.ParsingTable;
import parser.ll1.tabledriven.table.TableBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Driver de parsing LL(1) table-driven pour SHDL.
 *
 * <p>Produit un {@link CstNode} (arbre de syntaxe concrète) depuis une source
 * textuelle ou une liste de tokens pré-calculée.
 */
public final class CstParser {

    // Table et axiome : calculés une seule fois (la grammaire est statique)
    private static final ParsingTable TABLE = TableBuilder.build(Grammar.SHDL);
    private static final NonTerminal  AXIOM = Grammar.SHDL.getAxiom();

    private CstParser() {}

    // -----------------------------------------------------------------------
    // API publique
    // -----------------------------------------------------------------------

    /**
     * Tokenise {@code source} puis parse.
     *
     * @throws NullPointerException si {@code source} est null
     * @throws ParsingException si la source n'est pas syntaxiquement correcte
     */
    public static CstNode parse(String source) {
        Objects.requireNonNull(source, "source");
        return parseTokens(ShdlLexer.tokenize(source));
    }

    /**
     * Parse une liste de tokens (doit se terminer par EOF).
     *
     * @throws NullPointerException si {@code tokens} est null
     * @throws ParsingException si les tokens ne forment pas un programme valide
     */
    public static CstNode parseTokens(List<Token> tokens) {
        Objects.requireNonNull(tokens, "tokens");
        return new Driver(tokens).run();
    }

    // -----------------------------------------------------------------------
    // Implémentation interne
    // -----------------------------------------------------------------------

    /** Cadre de construction d'un CstInternal en cours. */
    static final class Frame {
        final NonTerminal nt;           // null pour le rootFrame collecteur
        final Production  production;   // null pour le rootFrame
        final int         expectedChildren;
        final List<CstNode> children;

        Frame(NonTerminal nt, Production production, int expectedChildren, List<CstNode> children) {
            this.nt               = nt;
            this.production       = production;
            this.expectedChildren = expectedChildren;
            this.children         = children;
        }

        boolean isComplete() { return children.size() >= expectedChildren; }
    }

    /** Moteur de parsing : une instance par appel à {@link #parseTokens}. */
    private static final class Driver {
        private final List<Token> tokens;
        private int cursor = 0;

        Driver(List<Token> tokens) {
            this.tokens = tokens;
        }

        CstNode run() {
            // Pile des symboles à traiter
            Deque<Symbol> stack = new ArrayDeque<>();

            // Pile des frames (CstInternal en construction)
            // rootFrame : nt=null, attend 1 seul enfant (le noeud Start)
            Deque<Frame> frames = new ArrayDeque<>();
            Frame rootFrame = new Frame(null, null, 1, new ArrayList<>());
            frames.push(rootFrame);

            // Push EOF puis axiome (Start) sur la pile des symboles
            stack.push(new Terminal(TokenType.EOF));
            stack.push(AXIOM);

            // ----------------------------------------------------------------
            // Boucle principale
            // ----------------------------------------------------------------
            while (!stack.isEmpty()) {
                Symbol top     = stack.pop();
                Token  current = tokens.get(cursor);

                if (top.isTerminal()) {
                    Terminal terminal = (Terminal) top;
                    if (terminal.getType() == current.type()) {
                        // Match
                        if (current.type() == TokenType.EOF) {
                            // On ne crée pas de CstLeaf pour EOF, on s'arrête
                            break;
                        }
                        CstLeaf leaf = new CstLeaf(terminal, current);
                        attach(frames, leaf);
                        cursor++;
                    } else {
                        // Mismatch
                        throw new ParsingException(
                                "token inattendu",
                                current.offset(),
                                terminal.getType(),
                                current,
                                frameContext(frames));
                    }
                } else {
                    // NonTerminal
                    NonTerminal nt = (NonTerminal) top;
                    Optional<Production> opt = TABLE.lookup(nt, current.type());
                    if (opt.isEmpty()) {
                        throw new ParsingException(
                                "aucune production applicable",
                                current.offset(),
                                null,
                                current,
                                nt);
                    }
                    Production prod = opt.get();
                    if (prod.isEpsilon()) {
                        // Production ε : construire le noeud ε directement, sans frame ni push
                        CstInternal epsilonNode = CstInternal.epsilon(nt, prod, current.offset());
                        attach(frames, epsilonNode);
                    } else {
                        // Production non-ε : ouvrir un frame et empiler les symboles en ordre inverse
                        List<Symbol> body = prod.getBody();
                        frames.push(new Frame(nt, prod, body.size(), new ArrayList<>()));
                        for (int i = body.size() - 1; i >= 0; i--) {
                            stack.push(body.get(i));
                        }
                    }
                }

                // Fermer tous les frames complets (sauf le rootFrame)
                while (!frames.isEmpty() && frames.peek().isComplete()) {
                    Frame f = frames.peek();
                    if (f.nt == null) {
                        // C'est le rootFrame : ne pas fermer, sortir de la boucle de fermeture
                        break;
                    }
                    frames.pop();
                    CstInternal node = CstInternal.of(f.nt, f.production, f.children);
                    attach(frames, node);
                }
            }

            // Vérification finale
            if (rootFrame.children.isEmpty()) {
                throw new ParsingException(
                        "aucun arbre produit",
                        tokens.isEmpty() ? 0 : tokens.get(0).offset(),
                        null, null, AXIOM);
            }
            return rootFrame.children.get(0);
        }

        /** Attache {@code node} au frame au sommet de la pile. */
        private static void attach(Deque<Frame> frames, CstNode node) {
            if (frames.isEmpty()) {
                throw new IllegalStateException("Pile de frames vide lors d'attach");
            }
            frames.peek().children.add(node);
        }

        /** Retourne le NonTerminal du frame courant pour le contexte d'erreur, ou null. */
        private static NonTerminal frameContext(Deque<Frame> frames) {
            if (frames.isEmpty()) return null;
            Frame top = frames.peek();
            return top.nt; // null pour rootFrame, c'est acceptable
        }
    }
}
