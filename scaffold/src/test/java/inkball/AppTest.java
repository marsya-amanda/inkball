package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.io.File;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest extends App {

//    private final String configPath = "config.json";
//    private final JSONObject json = loadJSONObject(configPath);
//    private JSONArray mockLevels = json.getJSONArray("levels");
//    private JSONObject mockLevel;
//    private File mockFile;
//    private Scanner mockScanner;

    private App app;

    @BeforeEach
    public void setUp() {
        // Use app so can create real App object instance while also verifying methods called
        app = spy(new App());
        System.setProperty("user.dir", "scaffold/src/main/resources");

//        app.json = 'config.json';
//        app.board = new Tile[App.BOARD_HEIGHT][App.BOARD_WIDTH];

        // Mock JSON structure
//        when(mockJson.getJSONArray("levels")).thenReturn(mockLevels);
//        when(mockLevels.getJSONObject(anyInt())).thenReturn(mockLevel);
//        when(mockLevel.getString("layout")).thenReturn("mockLayout.txt");

        // Mock File and Scanner
    }

    @Test
    public void testAppConstructor() {
        app.configPath = "config.json";

        // Previously checked static fields
        assertEquals(32, App.CELLSIZE);
        assertEquals(32, App.CELLHEIGHT);
        assertEquals(32, App.CELLAVG);
        assertEquals(64, App.TOPBAR);
        assertEquals(576, App.WIDTH);
        assertEquals(640, App.HEIGHT);
        assertEquals(18, App.BOARD_WIDTH);
        assertEquals(20, App.BOARD_HEIGHT);
        assertEquals(1, App.INITIAL_PARACHUTES);
        assertEquals(30, app.FPS);

        // Static fields
        assertEquals(1, App.gameLevel);
        assertEquals(0, App.timeLimit);
        assertEquals(0, App.lastSecond);
        assertEquals(0.0d, App.score, 0.001);
        //assertTrue(App.scoreIncrease.isEmpty());
        //assertTrue(App.scoreDecrease.isEmpty());
        assertEquals(1.0f, App.ballTimer, 0.001f);
        assertFalse(App.ctrlPressed);
        assertFalse(App.isDrawing);
        assertEquals(5, App.mouseRadius);

        // Instance fields
        assertEquals(App.GameState.PLAYING, app.gameState);
        assertEquals(1.0f, app.modScoreIncrease, 0.001f);
        assertEquals(1.0f, app.modScoreDecrease, 0.001f);
        assertEquals(0, app.spawnInterval);
        assertEquals(0, app.lastLine);

        assertArrayEquals(new float[2], app.start);
        assertArrayEquals(new float[2], app.end);

        assertTrue(app.getBalls().isEmpty());
        assertTrue(app.walls.isEmpty());
        assertTrue(app.allLines.isEmpty());
        assertTrue(app.drawnLines.isEmpty());
        assertTrue(app.tempLines.isEmpty());
        assertTrue(app.spawners.isEmpty());
        assertTrue(app.holes.isEmpty());
        assertTrue(app.sprites.isEmpty());

        assertArrayEquals(new int[]{0, 0}, app.firstSpiral);
        assertArrayEquals(new int[]{App.WIDTH/App.CELLSIZE - 1, (App.HEIGHT-App.TOPBAR)/App.CELLSIZE - 1}, app.secondSpiral);
    }

    @Test
    public void testGetBoard() {
        Tile[][] board = new Tile[App.BOARD_HEIGHT][App.BOARD_WIDTH];
        app.board = new Tile[App.BOARD_HEIGHT][App.BOARD_WIDTH];
        assertArrayEquals(board, app.getBoard());
    }

    @Test
    public void testGetLineSegments() {
        ArrayList<Line> lines = new ArrayList<>();
        assertEquals(lines, app.getLineSegments());
    }

    @Test
    public void testGetBalls() {
        ArrayList<Ball> balls = new ArrayList<>();
        assertEquals(balls, app.getBalls());
    }

    @Test
    public void testSettings() {
        // Mock the size method
        doNothing().when(app).size(anyInt(), anyInt());

        app.settings();

        // Verify that size was called with correct parameters
        verify(app).size(App.WIDTH, App.HEIGHT);
    }

    @Test
    public void testRestart() {
        // Mock JSON data
        JSONObject mockJson = mock(JSONObject.class);
        JSONArray mockLevels = mock(JSONArray.class);
        when(mockJson.getJSONArray("levels")).thenReturn(mockLevels);
        when(mockLevels.size()).thenReturn(3);
        app.json = mockJson;

        // Set initial state
        App.gameLevel = 2;
        app.frameCount = 100;
        App.ballTimer = 0.5f;
        App.isDrawing = true;
        app.lastLine = 10;
        app.ballQueue = new Ball[5];

        // Prevent nullPointer for FPS due to spy objects being unreliable with static fields
        doNothing().when(app).setup();
        // Call restart method
        app.restart();

        // Verify reset values
        assertEquals(0, app.frameCount);
        assertEquals(1.0f, App.ballTimer, 0.001f);
        assertFalse(App.isDrawing);
        assertEquals(0, app.lastLine);
        assertEquals(0, app.ballQueue.length);

        // Verify new ArrayLists are created
        assertTrue(app.getBalls().isEmpty());
        assertTrue(app.getLineSegments().isEmpty());
        assertTrue(app.allLines.isEmpty());
        assertTrue(app.drawnLines.isEmpty());
        assertTrue(app.tempLines.isEmpty());
        assertTrue(app.spawners.isEmpty());
        assertTrue(app.holes.isEmpty());

        // Verify gameLevel
        assertEquals(2, App.gameLevel);

        // Verify setup() is called
        verify(app).setup();
    }

    @Test
    public void testRestartWithGameLevelExceedingJsonLevels() {
        // Mock JSON data
        JSONObject mockJson = mock(JSONObject.class);
        JSONArray mockLevels = mock(JSONArray.class);
        when(mockJson.getJSONArray("levels")).thenReturn(mockLevels);
        when(mockLevels.size()).thenReturn(3);
        app.json = mockJson;

        // Set gameLevel higher than available levels
        App.gameLevel = 5;

        // Prevent nullPointer for FPS due to spy objects being unreliable with static fields
        doNothing().when(app).setup();
        // Call restart method
        app.restart();

        // Verify gameLevel is reset to 1
        assertEquals(1, App.gameLevel);

        // Verify setup() is called
        verify(app).setup();
    }

    /** Setup **/
    @Test
    public void testSetup() {
        //App testApp = spy(new App());
        //testApp.gameState = App.GameState.PLAYING;
        //testApp.json = mock(JSONObject.class);
        //testApp.configPath = "config.json";
        App testApp = spy(new App());
        testApp.configPath = "scaffold/config.json";
        testApp.isTesting = true;
        when(testApp.sketchPath(anyString())).thenReturn("scaffold/src/main/resources");
        doNothing().when(testApp).setLayout();
        testApp.setup();
    }

    /** Spawn Balls **/
    @Test
    public void testNormalSpawnBalls() {
        app.ballQueue = new Ball[5];
        app.ballQueue[0] = new Ball(21, 14, 2);
        app.spawners.add(new Spawner(0, 0));
        app.spawnInterval = 1; //frameCount always 0 in testing

        app.spawnBalls();

        app.ballQueue = new Ball[1];
        app.ballQueue[0] = new Ball(21, 14, 2);
        app.spawnBalls();
    }

    @Test
    public void testNegativeSpawnBalls() {
        app.ballQueue = new Ball[0];
        app.spawnBalls();

        app.ballQueue = new Ball[5];
        app.spawnBalls();

        app.spawnInterval = 2;
        app.spawnBalls(); //doesnt pass 3rd conditional
    }
}
