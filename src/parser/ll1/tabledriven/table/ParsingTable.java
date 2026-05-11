package parser.ll1.tabledriven.table;

import parser.ll1.grammar.NonTerminal;
import parser.ll1.grammar.Production;
import parser.lexer.Token;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Table d'analyse LL(1) M[NT, T] → Production.
 *
 * <p>
 * Construite via {@link TableBuilder#build(parser.ll1.grammar.Grammar)} a
 * partir d'une grammaire LL(1). Chaque cellule contient au plus une production
 * (la grammaire est LL(1) ; sinon {@link TableBuilder} leve
 * {@link IllegalStateException}).
 *
 * <p>
 * Immutable : le constructeur copie la map d'entrees.
 */
public record ParsingTable(Map<TableKey, Production> entries) {

    public ParsingTable {
        Objects.requireNonNull(entries, "entries");
        entries = Map.copyOf(entries);
    }

    /**
     * Cherche la production a appliquer pour le couple (non-terminal courant, token
     * de lookahead).
     */
    public Optional<Production> lookup(NonTerminal nt, Token t) {
        return Optional.ofNullable(entries.get(new TableKey(nt, t)));
    }

    /**
     * Cle de la table : couple (non-terminal en sommet de pile, token de
     * lookahead).
     */
    public record TableKey(NonTerminal nt, Token t) {
        public TableKey {
            Objects.requireNonNull(nt, "nt");
            Objects.requireNonNull(t, "t");
        }
    }
}
