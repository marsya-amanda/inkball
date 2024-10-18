package inkball;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PImage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class SpawnerTest {
    Spawner spawner;
    App app;

    @BeforeEach
    public void setUp() {
        spawner = new Spawner(0, 0);
        app = mock(App.class);
    }

    @Test
    public void testDraw() {
        when(app.getSprite("entrypoint")).thenReturn(mock(PImage.class));
        doNothing().when(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        spawner.draw(app);
        verify(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }
}