package parser.ll1;

import parser.lexer.Lexem;

/**
 * Helpers sur {@link Lexem} qui ne peuvent pas etre ajoutes au code amont.
 *
 * <p>L'API amont expose {@link Lexem#getIndexDepart()} (inclus) et
 * {@link Lexem#getIndexFin()} (exclus). Cette classe ajoute les conventions
 * complementaires utiles pour la coloration syntaxique (index inclusif du
 * dernier caractere).
 */
public final class Lexems {
    private Lexems() {}

    /**
     * Index inclusif du dernier caractere du lexeme dans la source.
     *
     * <p>Equivalent a {@code lex.getIndexFin() - 1}. A utiliser pour les outils
     * d'edition qui raisonnent sur des bornes inclusives (ex. surlignage
     * caractere par caractere via {@code TextArea.selectRange(start, end+1)}).
     *
     * @param lex le lexeme a inspecter
     * @return l'index inclusif du dernier caractere
     */
    public static int getFin(Lexem<?> lex) {
        return lex.getIndexFin() - 1;
    }
}
