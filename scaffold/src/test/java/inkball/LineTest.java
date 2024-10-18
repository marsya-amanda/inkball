package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import processing.core.PImage;
import static org.junit.jupiter.api.Assertions.*;

public class LineTest {
    private Line line;

    @BeforeEach
    public void setUp() {
        line = new Line(new float[]{0, 0}, new float[]{200, 200}, 0, true);
    }

    @Test
    public void testMidPoint() {
        assertArrayEquals(new float[]{100, 100}, line.getMidPoint(), 0.5f);
    }

    @Test
    public void testToString() {
        assertEquals("P1 is [0.0, 0.0], P2 is [200.0, 200.0], drawn is true", line.toString());
    }

    @Test
    public void testDraw() {
        line = new Line(new float[]{0, 0}, new float[]{200, 200}, 0, true);
        App app = mock(App.class);
        doNothing().when(app).line(line.getP1()[0], line.getP1()[1], line.getP2()[0], line.getP2()[1]);
        doNothing().when(app).stroke(line.getColourTo());
        doNothing().when(app).strokeWeight(10);
        line.draw(app);

        line = new Line(new float[]{0, 0}, new float[]{200, 200}, 0, false);
        line.draw(app);
    }

    @Test
    public void testEquals() {
        assertFalse(line.equals(null));
        assertFalse(line.equals(new Spawner(0, 0)));

        line = new Line(new float[]{0, 0}, new float[]{200, 200}, 1, true);
        Line line2 = new Line(new float[]{0, 0}, new float[]{200, 200}, 1, true);
        assertTrue(line.equals(line2));

        line2 = new Line(new float[]{0, 0}, new float[]{200, 200}, 1, false);
        assertFalse(line.equals(line2));

        line2 = new Line(new float[]{0, 0}, new float[]{0, 200}, 1, false);
        assertFalse(line.equals(line2));

        line2 = new Line(new float[]{0, 0}, new float[]{200, 200}, 3, true);
        assertFalse(line.equals(line2));
    }
}