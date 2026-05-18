package tests.parser.conversion;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;
import parser.conversion.FreshNames;

public class FreshNamesTest {

    @Test
    public void prefixesSontDistincts() {
        FreshNames g = new FreshNames(Set.of());
        String a = g.fresh();
        String b = g.fresh();
        assertNotEquals(a, b);
    }

    @Test
    public void evitePrefixeEnCollision() {
        // un signal utilisateur commence par "__ff0__" -> le generateur doit
        // sauter ce prefixe.
        FreshNames g = new FreshNames(Set.of("__ff0__qm", "__ff1__nclk"));
        String p = g.fresh();
        assertFalse("aucun nom utilise ne doit commencer par le prefixe",
            "__ff0__qm".startsWith(p) || "__ff1__nclk".startsWith(p));
    }

    @Test
    public void prefixeUtilisableEnPrefixe() {
        FreshNames g = new FreshNames(Set.of());
        String p = g.fresh();
        assertTrue(p.startsWith("__ff"));
        assertTrue(p.endsWith("__"));
    }
}
