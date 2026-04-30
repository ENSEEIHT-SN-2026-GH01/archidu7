package parser.ll1.tabledriven.table;

import parser.ll1.grammar.FirstSet;
import parser.ll1.grammar.FollowSet;
import parser.ll1.grammar.Grammar;
import parser.ll1.grammar.Production;
import parser.ll1.token.TokenType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Construit la {@link ParsingTable} à partir d'une grammaire LL(1).
 *
 * <p>Algorithme standard (cf. Aho/Sethi/Ullman, dragon book §4.4) :
 * <pre>
 * pour chaque production p : A → α
 *     pour chaque a ∈ FIRST(α) :
 *         M[A, a] := p
 *     si α →* ε :
 *         pour chaque b ∈ FOLLOW(A) :
 *             M[A, b] := p
 * </pre>
 *
 * <p>Si une cellule M[A, a] est assignée deux fois à des productions
 * <em>distinctes</em>, la grammaire n'est pas LL(1) : on lève
 * {@link IllegalStateException} avec le couple en conflit.
 */
public final class TableBuilder {

    private TableBuilder() {}

    public static ParsingTable build(Grammar g) {
        FirstSet first = new FirstSet(g);
        FollowSet follow = new FollowSet(g, first);
        Map<ParsingTable.TableKey, Production> table = new HashMap<>();

        for (Production p : g.getProductions()) {
            Set<TokenType> firstAlpha = first.ofSequence(p.getBody());
            for (TokenType a : firstAlpha) {
                put(table, new ParsingTable.TableKey(p.getHead(), a), p);
            }
            if (first.sequenceNullable(p.getBody())) {
                for (TokenType b : follow.of(p.getHead())) {
                    put(table, new ParsingTable.TableKey(p.getHead(), b), p);
                }
            }
        }

        return new ParsingTable(table);
    }

    private static void put(Map<ParsingTable.TableKey, Production> table,
                             ParsingTable.TableKey k, Production p) {
        Production existing = table.put(k, p);
        if (existing != null && !existing.equals(p)) {
            throw new IllegalStateException(
                "Conflit LL(1) en " + k + " : " + existing + " vs " + p);
        }
    }
}
