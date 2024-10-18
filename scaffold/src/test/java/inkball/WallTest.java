package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import processing.core.PImage;
import static org.junit.jupiter.api.Assertions.*;

public class WallTest {
    private Wall wall;

    @BeforeEach
    public void setUp() {
        wall = new Wall(1, 1, 1);
    }

    @Test
    public void testGetHP() {
        assertEquals(3, wall.getHP());
    }

    @Test
    public void testDamage() {
        Ball ball  = new Ball(1, 1, 1);
        wall.damage(ball);
        assertEquals(2, wall.getHP());

        Wall wall = new Wall(1, 1, 0);
        wall.damage(ball);
        assertEquals(2, wall.getHP());

        wall = new Wall(1, 1, 3);
        wall.damage(ball);
        assertEquals(3, wall.getHP());
    }

    @Test
    public void testColourToString() {
        wall = new Wall(2, 2, 0);
        assertEquals("grey", wall.colourToString());
        wall = new Wall(2, 2, 1);
        assertEquals("orange", wall.colourToString());
        wall = new Wall(2, 2, 2);
        assertEquals("blue", wall.colourToString());
        wall = new Wall(2, 2, 3);
        assertEquals("green", wall.colourToString());
        wall = new Wall(2, 2, 4);
        assertEquals("yellow", wall.colourToString());
        wall = new Wall(2, 2, 5);
        assertEquals("grey", wall.colourToString());
    }

    @Test
    public void testDraw() {
        Ball ball  = new Ball(1, 1, 1);
        wall.damage(ball);
        wall.damage(ball);
        wall.damage(ball);

        App app = mock(App.class);
        when(app.getSprite("wall1-damaged")).thenReturn(mock(PImage.class));
        doNothing().when(app).image(any(PImage.class), anyInt(), anyInt(), anyInt(), anyInt());
        wall.draw(app);
    }
}