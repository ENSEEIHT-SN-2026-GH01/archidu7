package tests.parser.ll1.tabledriven.grammar;

import org.junit.Test;
import parser.ll1.grammar.*;
import java.util.List;
import static org.junit.Assert.*;

/**
 * TDD — RED phase.
 * Vérifie que la grammaire LL(1) SHDL cible est sans conflit.
 */
public class Ll1ConflictTest {

    @Test
    public void grammaireShdlSansConflitLl1() {
        List<Ll1Conflict> conflicts = new Ll1ConflictChecker(Grammar.SHDL).findAllConflicts();
        assertTrue(
            "Conflits LL(1) inattendus : " + conflicts,
            conflicts.isEmpty()
        );
    }
}
