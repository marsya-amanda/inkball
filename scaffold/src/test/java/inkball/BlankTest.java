package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import processing.core.PImage;
import static org.junit.jupiter.api.Assertions.*;

public class BlankTest {
    Blank blank;
    App app;

    @BeforeEach
    public void setUp() {
        blank = new Blank(0, 0);
        app = mock(App.class);
    }

    @Test
    public void testDraw() {
        when(app.getSprite("tile")).thenReturn(mock(PImage.class));
        doNothing().when(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        blank.draw(app);
        verify(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }
}