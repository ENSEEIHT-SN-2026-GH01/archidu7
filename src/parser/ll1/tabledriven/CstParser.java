package parser.ll1.tabledriven;

import parser.lexer.Lexem;
import parser.lexer.Lexer;
import parser.lexer.Token;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.ll1.grammar.Symbol;
import parser.ll1.grammar.Terminal;
import parser.ll1.tabledriven.cst.CstInternal;
import parser.ll1.tabledriven.cst.CstLeaf;
import parser.ll1.tabledriven.cst.CstNode;
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
 * <p>
 * Produit un {@link CstNode} (arbre de syntaxe concrete) depuis une source
 * textuelle. Utilise directement le lexer d Erwan ({@link Lexer}).
 */
public final class CstParser {

    // Table et axiome : calcules une seule fois (la grammaire est statique)
    private static final ParsingTable TABLE = TableBuilder.build(Grammar.SHDL);
    private static final NonTerminal AXIOM = Grammar.SHDL.getAxiom();

    private CstParser() {
    }

    // -----------------------------------------------------------------------
    // API publique
    // -----------------------------------------------------------------------

    /**
     * Tokenise {@code source} via le lexer d Erwan puis parse.
     *
     * <p>
     * Les trivia (whitespace, lineTerminator, commentaires) sont filtres.
     * Les lexemes {@link Token#Error} levent une {@link ParsingException}.
     * Un lexeme EOF sentinelle est ajoute en fin de liste.
     *
     * @throws NullPointerException
     *                                  si {@code source} est null
     * @throws ParsingException
     *                                  si la source n'est pas syntaxiquement
     *                                  correcte
     */
    public static CstNode parse(String source) {
        Objects.requireNonNull(source, "source");
        List<Lexem<Token>> tokens = tokenize(source);
        return new Driver(tokens).run();
    }

    /**
     * Tokenise {@code source}, filtre les trivia, ajoute EOF et retourne la liste.
     * Les {@link Token#Error} levent une {@link ParsingException}.
     */
    static List<Lexem<Token>> tokenize(String source) {
        List<Lexem<Token>> raw = Lexer.LEXER.tokenize(source);
        List<Lexem<Token>> filtered = new ArrayList<>(raw.size() + 1);
        for (Lexem<Token> lex : raw) {
            Token t = lex.getToken();
            // Filtrage trivia : whitespace, lineTerminator et commentaires
            if (lex.isIgnored()
                    || t == Token.whiteSpace
                    || t == Token.lineTerminator
                    || t == Token.Comment) {
                continue;
            }
            // Un Token.Error indique un caractere non reconnu
            if (t == Token.Error) {
                throw new ParsingException(
                        "caractere non reconnu : \"" + lex.getText() + "\"",
                        lex.getIndexDepart(),
                        null, lex, null);
            }
            filtered.add(lex);
        }
        // Sentinelle EOF : indexDepart = indexFin = source.length()
        Lexem<Token> eofLexem = new Lexem<>(Token.EOF);
        eofLexem.storeMatched(source.length(), "");
        filtered.add(eofLexem);
        return filtered;
    }

    // -----------------------------------------------------------------------
    // Implementation interne
    // -----------------------------------------------------------------------

    /** Cadre de construction d'un CstInternal en cours. */
    static final class Frame {
        final NonTerminal nt; // null pour le rootFrame collecteur
        final Production production; // null pour le rootFrame
        final int expectedChildren;
        final List<CstNode> children;

        Frame(NonTerminal nt, Production production, int expectedChildren, List<CstNode> children) {
            this.nt = nt;
            this.production = production;
            this.expectedChildren = expectedChildren;
            this.children = children;
        }

        boolean isComplete() {
            return children.size() >= expectedChildren;
        }
    }

    /** Moteur de parsing : une instance par appel a {@link #parse}. */
    private static final class Driver {
        private final List<Lexem<Token>> tokens;
        private int cursor = 0;

        Driver(List<Lexem<Token>> tokens) {
            this.tokens = tokens;
        }

        CstNode run() {
            // Pile des symboles a traiter
            Deque<Symbol> stack = new ArrayDeque<>();

            // Pile des frames (CstInternal en construction)
            // rootFrame : nt=null, attend 1 seul enfant (le noeud Start)
            Deque<Frame> frames = new ArrayDeque<>();
            Frame rootFrame = new Frame(null, null, 1, new ArrayList<>());
            frames.push(rootFrame);

            // Push EOF puis axiome (Start) sur la pile des symboles
            stack.push(new Terminal(Token.EOF));
            stack.push(AXIOM);

            // ----------------------------------------------------------------
            // Boucle principale
            // ----------------------------------------------------------------
            while (!stack.isEmpty()) {
                Symbol top = stack.pop();
                Lexem<Token> current = tokens.get(cursor);

                if (top.isTerminal()) {
                    Terminal terminal = (Terminal) top;
                    if (terminal.getType() == current.getToken()) {
                        // Match
                        if (current.getToken() == Token.EOF) {
                            // On ne cree pas de CstLeaf pour EOF, on s'arrete
                            break;
                        }
                        CstLeaf leaf = new CstLeaf(terminal, current);
                        attach(frames, leaf);
                        cursor++;
                    } else {
                        // Mismatch
                        throw new ParsingException(
                                "token inattendu",
                                current.getIndexDepart(),
                                terminal.getType(),
                                current,
                                frameContext(frames),
                                buildContextPath(frames));
                    }
                } else {
                    // NonTerminal
                    NonTerminal nt = (NonTerminal) top;
                    Optional<Production> opt = TABLE.lookup(nt, current.getToken());
                    if (opt.isEmpty()) {
                        throw new ParsingException(
                                "aucune production applicable",
                                current.getIndexDepart(),
                                null,
                                current,
                                nt,
                                buildContextPath(frames));
                    }
                    Production prod = opt.get();
                    if (prod.isEpsilon()) {
                        // Production epsilon : construire le noeud epsilon directement
                        CstInternal epsilonNode = CstInternal.epsilon(nt, prod, current.getIndexDepart());
                        attach(frames, epsilonNode);
                    } else {
                        // Production non-epsilon : ouvrir un frame et empiler les symboles en ordre
                        // inverse
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

            // Verification finale
            if (rootFrame.children.isEmpty()) {
                throw new ParsingException(
                        "aucun arbre produit",
                        tokens.isEmpty() ? 0 : tokens.get(0).getIndexDepart(),
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

        /**
         * Retourne le NonTerminal du frame courant (innermost) pour le contexte
         * d'erreur, ou null.
         */
        private static NonTerminal frameContext(Deque<Frame> frames) {
            if (frames.isEmpty())
                return null;
            Frame top = frames.peek();
            return top.nt; // null pour rootFrame, c'est acceptable
        }

        /**
         * Construit un chemin lisible de non-terminaux depuis la racine vers le frame
         * courant.
         * Ex. "Module > Instance > Operation".
         * Retourne null si la pile ne contient que le rootFrame.
         */
        private static String buildContextPath(Deque<Frame> frames) {
            // frames est une Deque utilisee en pile : peek() == sommet == innermost
            // On veut afficher du root vers le top => inverser
            List<String> names = new ArrayList<>();
            for (Frame f : frames) {
                // iteration en ordre LIFO : f va de innermost a outermost (rootFrame en
                // dernier)
                if (f.nt != null)
                    names.add(f.nt.name());
            }
            if (names.isEmpty())
                return null;
            // inverser pour avoir root -> ... -> innermost
            StringBuilder sb = new StringBuilder();
            for (int i = names.size() - 1; i >= 0; i--) {
                if (sb.length() > 0)
                    sb.append(" > ");
                sb.append(names.get(i));
            }
            return sb.toString();
        }
    }
}
