package parser.ll1.tabledriven.lexer;

import parser.ll1.token.TokenType;

import java.util.Map;
import java.util.Optional;

/**
 * Table des 7 mots-clés du langage SHDL. Utilisée par {@link ShdlLexer} pour
 * reclassifier post-lex un Identifiant lexé qui matche un mot-clé.
 *
 * <p>Justification : l'automate déterministe {@code parser.automate.AutomateDeterministe}
 * du sprint 1 ne supporte pas les priorités entre patterns ; tous les mots-clés
 * sont d'abord reconnus comme {@link TokenType#Identifiant}, puis cette table
 * réécrit le {@code TokenType} si le lexème matche.
 */
public final class KeywordTable {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
        "module",  TokenType.ModuleKW,
        "end",     TokenType.EndKW,
        "on",      TokenType.OnKW,
        "when",    TokenType.WhenKW,
        "set",     TokenType.SetKW,
        "reset",   TokenType.ResetKW,
        "enabled", TokenType.EnabledKW
    );

    private KeywordTable() {}

    /** Retourne le {@link TokenType} mot-clé pour ce lexème, ou empty si l'identifiant n'est pas un mot-clé. */
    public static Optional<TokenType> lookup(String lexeme) {
        return Optional.ofNullable(KEYWORDS.get(lexeme));
    }
}
