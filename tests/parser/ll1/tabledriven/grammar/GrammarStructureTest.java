package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import static org.junit.Assert.*;

/**
 * TDD — RED phase.
 * Vérifie la structure statique de Grammar.SHDL :
 *   - axiome == NonTerminal.Start
 *   - exactement 57 productions
 *   - chaque NonTerminal de l'enum a au moins une production
 */
public class GrammarStructureTest {

    @Test
    public void axiomeEstStart() {
        assertEquals(NonTerminal.Start, Grammar.SHDL.getAxiom());
    }

    @Test
    public void nombreExactDeProductions() {
        assertEquals(57, Grammar.SHDL.getProductions().size());
    }

    @Test
    public void chaqueNonTerminalAUneProduction() {
        for (NonTerminal nt : NonTerminal.values()) {
            assertFalse(
                "aucune production pour " + nt,
                Grammar.SHDL.productionsOf(nt).isEmpty()
            );
        }
    }
}
