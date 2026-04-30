/**
 * Grammaire LL(1) pour SHDL.
 *
 * <p><strong>Convention de nommage des enums {@link parser.ll1.grammar.NonTerminal} et
 * {@link parser.ll1.token.TokenType} :</strong> les valeurs sont en
 * <em>CamelCase</em> et non en {@code UPPER_SNAKE_CASE} (convention JLS
 * habituelle pour les constantes). Choix délibéré : le nom de l'enum
 * coïncide mot-pour-mot avec celui du symbole dans le BNF
 * ({@code shdl_grammar_LL1.txt}), ce qui rend
 * {@link parser.ll1.grammar.Grammar#toBnf()} et le test de figeage trivialement maintenables,
 * et facilite la lecture en parallèle de la grammaire et du code par
 * des étudiants. Les underscores conservés ({@code Instance_Plus},
 * {@code Range_Opt}, {@code Comma_Opt}) traduisent les suffixes
 * {@code _Plus}/{@code _Star}/{@code _Opt} de la grammaire source.
 */
package parser.ll1.grammar;
