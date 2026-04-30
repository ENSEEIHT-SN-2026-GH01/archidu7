package parser.ll1.tabledriven.lexer;

import parser.automate.AutomateDeterministe;
import parser.automate.LexingException;
import parser.regex.Builder;
import parser.regex.Regex;
import parser.ll1.token.Token;
import parser.ll1.token.TokenType;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lexer SHDL : transforme une source en {@link List}<{@link Token}>,
 * en s'appuyant sur le moteur d'automate déterministe d'Erwan.
 *
 * <p>Pipeline :
 * <ol>
 *   <li>Skip whitespace + commentaires ({@code // ...}, {@code # ...}) en
 *       code Java pur (l'automate d'Erwan ne supporte pas {@code [^...]}
 *       qui serait nécessaire pour matcher "tout sauf newline").</li>
 *   <li>Longest match manuel : on essaie chaque automate individuel sur la
 *       sous-chaîne courante et on garde le match le plus long.</li>
 *   <li>Reclassif post-lex : un {@link TokenType#Identifiant} dont le
 *       lexème est dans {@link KeywordTable} devient le mot-clé correspondant.</li>
 * </ol>
 *
 * <p>Note d'implémentation : {@code AutomateDeterministe.fromList} présente un
 * bug quand la liste dépasse une règle — les états finaux se réécrivent
 * mutuellement lors de la fusion des NFA. Contournement légal : on construit
 * un automate par règle et on orchestre soi-même le longest match en Java pur.
 * Le moteur d'Erwan ({@link AutomateDeterministe}, {@link Builder}) est toujours
 * utilisé pour la reconnaissance de chaque pattern individuel.
 *
 * <p>Le résultat se termine toujours par un token {@link TokenType#EOF}
 * d'offset = {@code source.length()} et de value {@code null}.
 */
public final class ShdlLexer {

    /**
     * Paire (automate, type) pour une règle lexicale.
     * L'automate ne connaît qu'une seule règle ; le longest match entre
     * toutes les règles est calculé manuellement dans {@link #tokenize}.
     */
    private record Rule(AutomateDeterministe<TokenType> aut, TokenType type) {}

    private static final List<Rule> RULES = buildRules();

    private static List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();
        // Identifiants — les mots-clés sont reclassifiés post-lex via KeywordTable
        rules.add(rule("[a-zA-Z_][a-zA-Z_0-9]*", TokenType.Identifiant));
        // Valeurs littérales
        rules.add(rule("\\.[0-1]+",               TokenType.BitField));
        rules.add(rule("[0-9]+",                  TokenType.NaturalInteger));
        // Opérateurs (le longest match entre ":" et "::=" est géré par comparaison de longueur)
        rules.add(rule("::=",                     TokenType.MemAssignOp));
        rules.add(rule("=",                       TokenType.AssignOp));
        rules.add(rule("\\+",                     TokenType.OrOp));
        rules.add(rule("\\*",                     TokenType.Star));
        rules.add(rule("&",                       TokenType.ConcatOp));
        rules.add(rule("/",                       TokenType.NotOp));
        rules.add(rule("\\.\\.",                  TokenType.PointPoint));
        // Délimiteurs
        rules.add(rule(":",                       TokenType.Colon));
        rules.add(rule(",",                       TokenType.Comma));
        rules.add(rule(";",                       TokenType.Semicolon));
        rules.add(rule("\\$",                     TokenType.Dollar));
        rules.add(rule("\\(",                     TokenType.LeftPar));
        rules.add(rule("\\)",                     TokenType.RightPar));
        rules.add(rule("\\[",                     TokenType.LeftSquareBrack));
        rules.add(rule("\\]",                     TokenType.RightSquareBrack));
        return List.copyOf(rules);
    }

    private static Rule rule(String regex, TokenType t) {
        Regex r = Builder.parseRegex(regex);
        List<Pair<Regex, TokenType>> singleRule = List.of(Pair.pair(r, t));
        try {
            return new Rule(AutomateDeterministe.fromList(singleRule), t);
        } catch (LexingException e) {
            throw new IllegalStateException(
                "Construction du DFA pour la règle " + t + " (" + regex + ") a échoué", e);
        }
    }

    private ShdlLexer() {}

    /**
     * Tokenise {@code source} et retourne la liste de tokens, terminée par EOF.
     *
     * <p>Le longest match est calculé en lançant chaque automate sur la
     * sous-chaîne courante et en conservant le résultat de longueur maximale.
     * En cas d'égalité de longueur, la première règle dans {@link #RULES} gagne
     * (ordre de déclaration = priorité).
     *
     * @throws LexerException si un caractère ne correspond à aucune règle
     * @throws NullPointerException si {@code source} est null
     */
    public static List<Token> tokenize(String source) {
        Objects.requireNonNull(source, "source");
        List<Token> tokens = new ArrayList<>();
        int offset = 0;

        while (offset < source.length()) {
            int trivia = skipTrivia(source, offset);
            if (trivia > 0) {
                offset += trivia;
                continue;
            }

            String reste = source.substring(offset);

            // Longest match : essaye chaque automate, garde le match le plus long
            TokenType bestType = null;
            int bestLen = -1;
            for (Rule rule : RULES) {
                try {
                    Pair<TokenType, Integer> p = rule.aut().exec1(reste);
                    int len = p.snd();
                    if (len > bestLen) {
                        bestLen = len;
                        bestType = p.fst();
                    }
                } catch (LexingException e) {
                    // pas de match pour cette règle, on continue
                }
            }

            if (bestType == null) {
                throw new LexerException(
                    "lexeme non reconnu : " + previewChar(reste), offset);
            }

            String lexeme = source.substring(offset, offset + bestLen);

            if (bestType == TokenType.Identifiant) {
                var kw = KeywordTable.lookup(lexeme);
                if (kw.isPresent()) bestType = kw.get();
            }

            tokens.add(new Token(bestType, lexeme, offset));
            offset += bestLen;
        }
        tokens.add(new Token(TokenType.EOF, null, source.length()));
        return tokens;
    }

    private static String previewChar(String s) {
        return s.isEmpty() ? "<vide>" : "'" + s.charAt(0) + "'";
    }

    /**
     * Avance sur whitespace + commentaires consécutifs ({@code // ...} et
     * {@code # ...}). Retourne le nombre de caractères sautés.
     *
     * <p>Implémenté en code Java pur car le moteur de regex d'Erwan ne
     * supporte pas la négation {@code [^\n]} qui serait nécessaire.
     */
    private static int skipTrivia(String source, int from) {
        int i = from;
        boolean progressed = true;
        while (progressed && i < source.length()) {
            progressed = false;
            // Whitespace
            while (i < source.length() && isWs(source.charAt(i))) {
                i++;
                progressed = true;
            }
            // Commentaire //
            if (i + 1 < source.length()
                    && source.charAt(i) == '/'
                    && source.charAt(i + 1) == '/') {
                while (i < source.length()
                        && source.charAt(i) != '\n'
                        && source.charAt(i) != '\r') {
                    i++;
                }
                progressed = true;
            // Commentaire #
            } else if (i < source.length() && source.charAt(i) == '#') {
                while (i < source.length()
                        && source.charAt(i) != '\n'
                        && source.charAt(i) != '\r') {
                    i++;
                }
                progressed = true;
            }
        }
        return i - from;
    }

    private static boolean isWs(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
}
