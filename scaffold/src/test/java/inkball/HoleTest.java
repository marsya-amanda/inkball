package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import processing.core.PImage;
import static org.junit.jupiter.api.Assertions.*;

public class HoleTest {

    private Hole hole;

    @BeforeEach
    public void setUp() {
        hole = new Hole(2, 2, 4, Hole.GridPosition.BR);
    }

    @Test
    public void testGetHoleCenter() {
        float[] expected = new float[]{hole.getX()*App.CELLSIZE, hole.getY()*App.CELLSIZE+App.TOPBAR};

        assertArrayEquals(expected, hole.getHoleCenter(), 0.1f);

        hole = new Hole(1, 1, 4, Hole.GridPosition.TL);
        assertArrayEquals(expected, hole.getHoleCenter(), 0.1f);

        hole = new Hole(1, 2, 4, Hole.GridPosition.BL);
        assertArrayEquals(expected, hole.getHoleCenter(), 0.1f);

        hole = new Hole(2, 1, 4, Hole.GridPosition.TR);
        assertArrayEquals(expected, hole.getHoleCenter(), 0.1f);
    }

    @Test
    public void testColourToString() {
        hole = new Hole(2, 2, 0, null);
        assertEquals("grey", hole.colourToString());
        hole = new Hole(2, 2, 1, null);
        assertEquals("orange", hole.colourToString());
        hole = new Hole(2, 2, 2, null);
        assertEquals("blue", hole.colourToString());
        hole = new Hole(2, 2, 3, null);
        assertEquals("green", hole.colourToString());
        hole = new Hole(2, 2, 4, null);
        assertEquals("yellow", hole.colourToString());
    }

    @Test
    public void testToString() {
        assertEquals("Hole at [2.0, 2.0], colour 4", Hole.toString(hole));
    }

    @Test
    public void testDraw() {
        App app = mock(App.class);
        hole.draw(app);

        hole = new Hole(2, 2, 2, Hole.GridPosition.TL);
        doNothing().when(app).image(any(PImage.class), anyInt(), anyInt());
        when(app.getSprite("hole2")).thenReturn(mock(PImage.class));
        hole.draw(app);
    }
}