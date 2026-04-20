package tests.parser.ll1.ast;

import org.junit.Test;
import parser.ll1.ast.Position;
import static org.junit.Assert.*;

public class PositionTest {
    @Test
    public void gettersRestituentLesChamps() {
        Position p = new Position(3, 12, 42);
        assertEquals(3, p.getLine());
        assertEquals(12, p.getColumn());
        assertEquals(42, p.getOffset());
    }

    @Test
    public void equalsComparelesTroisChamps() {
        assertEquals(new Position(1, 2, 3), new Position(1, 2, 3));
        assertNotEquals(new Position(1, 2, 3), new Position(1, 2, 4));
    }

    @Test
    public void toStringContientLesTroisChamps() {
        assertEquals("1:2#3", new Position(1, 2, 3).toString());
    }
}
