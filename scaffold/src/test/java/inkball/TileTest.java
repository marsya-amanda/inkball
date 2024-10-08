package inkball;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TileTest {

    ConcreteTile tilePositive = new ConcreteTile(2, 5);
    ConcreteTile tileNegativeInt = new ConcreteTile(-1, -5);
    ConcreteTile tileZero = new ConcreteTile(0, 0);

    @Test
    public void testGetX() {
        assertEquals(2, tilePositive.getX());
        assertEquals(-1, tileNegativeInt.getX());
        assertEquals(0, tileZero.getX());
    }

    @Test
    public void testGetY() {
        assertEquals(5, tilePositive.getY());
        assertEquals(-5, tileNegativeInt.getY());
        assertEquals(0, tileZero.getY());
    }
}