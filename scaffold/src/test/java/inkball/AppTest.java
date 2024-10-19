package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest extends App {

    private App app;

    @BeforeEach
    public void setUp() {
        // Use app so can create real App object instance while also verifying methods called
        app = spy(new App());
        //System.setProperty("user.dir", "scaffold/src/main/resources");

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
        //assertEquals(1, App.gameLevel);
        //assertEquals(0, App.timeLimit);
//        assertEquals(0, App.lastSecond);
//        assertEquals(0.0d, App.score, 0.001);
//        //assertTrue(App.scoreIncrease.isEmpty());
//        //assertTrue(App.scoreDecrease.isEmpty());
//        assertEquals(1.0f, App.ballTimer, 0.001f);
//        assertFalse(App.ctrlPressed);
//        assertFalse(App.isDrawing);
//        assertEquals(5, App.mouseRadius);

        // Instance fields
        assertEquals(App.GameState.PLAYING, app.gameState);
        assertEquals(1.0f, app.getModScoreIncrease(), 0.001f);
        assertEquals(1.0f, app.getModScoreDecrease(), 0.001f);
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

    @Test
    public void testDraw() {
        app.isTesting = true;
        ballTimer = 1;

        // Create mock board
        app.board = new Tile[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE];
        for (int i = 0; i < app.board.length; i++) {
            for (int j = 0; j < app.board[i].length; j++) {
                app.board[i][j] = mock(Tile.class);
            }
        }

        // Create mock ball queue
        app.ballQueue = new Ball[5];
        for (int i = 0; i < app.ballQueue.length; i++) {
            app.ballQueue[i] = new Ball(5, 14, 2);
        }

        // Create dummy components
        app.balls.clear();
        app.balls.add(new Ball(100, 100, 3));
        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(new Line(new float[]{0, 0}, new float[]{100, 100}, 2, true));
        app.allLines.clear();
        app.allLines.add(new Line(new float[]{0, 0}, new float[]{100, 100}, 2, true));

        doNothing().when(app).textSize(anyFloat());
        doNothing().when(app).fill(anyInt());
        doNothing().when(app).textAlign(anyInt(), anyInt());
        doNothing().when(app).text(anyString(), anyFloat(), anyFloat());
        doNothing().when(app).strokeWeight(anyFloat());
        doNothing().when(app).rect(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        doNothing().when(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        doReturn(mock(PImage.class)).when(app).getSprite(anyString());

        app.draw();

        // TEST WIN
        app.gameState = GameState.WIN;
        // //  reset dummy components
        // Create dummy components
        app.balls.clear();
        Ball ball = mock(Ball.class);
        app.balls.add(ball);

        Line line = mock(Line.class);
        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(line);
        app.allLines.clear();
        app.allLines.add(line);

        doNothing().when(ball).draw(app);
        doNothing().when(line).draw(app);

        app.draw();

        // // WIN & SPIRAL ENDED
        app.gameState = GameState.WIN;
        App.gameLevel = 1;
        app.maxLevel = 2;

        app.firstSpiral = new int[]{app.board[0].length - 1, app.board.length - 1};
        app.secondSpiral = new int[]{0, 0};
        doNothing().when(app).restart();

        app.draw();

        app.gameState = GameState.WIN;
        App.gameLevel = 2;
        app.maxLevel = 2;
        app.draw();

        app.gameState = GameState.WIN;
        App.gameLevel = 2;
        app.maxLevel = 1;
        app.draw();

        // TEST TIMELIMIT = 0
        //Case 1: A second has passed
        app.gameState = GameState.PLAYING;
        App.timeLimit = 10;
        App.lastSecond = 2;
        app.frameCount = 350;
        app.draw();

        app.gameState = GameState.PLAYING;
        app.frameCount = 270;
        App.lastSecond = 2;
        app.ballQueue = new Ball[5];
        app.draw();

        app.gameState = GameState.PLAYING;
        app.frameCount = 270;
        App.lastSecond = 2;
        app.ballQueue[0] = new Ball(21, 14, 2);
        app.draw();

        //Case 1B: Test empty ballQueue
        app.frameCount = 350;
        App.lastSecond = 2;
        app.ballQueue = new Ball[7];
        app.draw();

        app.frameCount = 350;
        App.lastSecond = 2;
        app.ballQueue = new Ball[7];
        app.ballQueue[0] = new Ball(5, 14, 2);
        app.ballQueue[1] = new Ball(5, 14, 2);
        app.ballQueue[2] = new Ball(5, 14, 2);
        app.ballQueue[3] = new Ball(5, 14, 2);
        app.ballQueue[4] = new Ball(5, 14, 2);
        app.ballQueue[5] = new Ball(5, 14, 2);
        app.draw();

        //Case 2: Game Paused
        app.gameState = GameState.PAUSED;
        app.draw();
    }

    @Test
    public void testLineDraw() { // problem: something here makes interact, setNewDir, willCollide not work
        app.isTesting = true;

        Wall mockWall = mock(Wall.class);
        Ball mockBall = mock(Ball.class);
        //ADD mock objects for testing
        ArrayList<Line> lines = new ArrayList<>();
        Line mockLine = mock(Line.class);
        for (int i = 0; i < 10; i++) {
            app.walls.add(mockWall);
            app.getBalls().add(mockBall);
            lines.add(mockLine);
        }

        for (int i = 0; i < 10; i++) {
            // Adding line groups to app objects
            app.tempLines.add(lines);
            app.allLines.add(mockLine);
            app.drawnLines.add(lines);
        }

        app.board = new Tile[App.BOARD_HEIGHT][App.BOARD_WIDTH];
        for (int i = 0; i < app.board.length; i++) {
            for (int j = 0; j < app.board[i].length; j++) {
                app.board[i][j] = mock(Tile.class);
            }
        }

        // Create mock ball queue
        app.ballQueue = new Ball[5];

        doNothing().when(app).textSize(anyFloat());
        doNothing().when(app).fill(anyInt());
        doNothing().when(app).textAlign(anyInt(), anyInt());
        doNothing().when(app).text(anyString(), anyFloat(), anyFloat());
        doNothing().when(app).strokeWeight(anyFloat());
        doNothing().when(app).rect(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        doNothing().when(app).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        doReturn(mock(PImage.class)).when(app).getSprite(anyString());
        doNothing().when(mockLine).draw(app);
        doNothing().when(mockBall).draw(app);
        doNothing().when(mockWall).draw(app);

        app.draw();

        for (int i = 0; i < app.ballQueue.length; i++) {
            app.ballQueue[i] = new Ball(5, 14, 2);
        }

        doNothing().when(app).drawAll();

        app.draw();

        app.ballQueue[0] = null;
        app.getBalls().clear();
        app.gameState = GameState.PLAYING;

        app.draw();

    }

    @Test
    public void testAllDraw() {
        //Empty balls branch
        app.balls = new ArrayList<>();
        app.drawAll();

        //Fill balls
        Ball mockBall = mock(Ball.class);
        Wall mockWall = mock(Wall.class);
        Line mockLine = mock(Line.class);
        Hole mockHole = mock(Hole.class);
        ArrayList<Line> lines = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            app.walls.add(mockWall);
            app.balls.add(mockBall);
            app.allLines.add(mockLine);
            app.holes.add(mockHole);
            lines.add(mockLine);
        }

        for (int i = 0; i < 10; i++) {
            // Adding line groups to app objects
            app.drawnLines.add(lines);
        }

        doNothing().when(mockBall).draw(this);
        when(mockBall.willCollide(mockLine)).thenReturn(null); // branch: no collision

        when(app.getWallAssociated(mockBall, mockLine)).thenReturn(new Wall[]{mockWall, mockWall});
        doNothing().when(mockWall).damage(mockBall);
        when(mockWall.getHP()).thenReturn(0);
        doNothing().when(app).removeWall(mockWall);
        doNothing().when(mockBall).interact(mockLine);

        MockedStatic<App> mockStaticApp = mockStatic(App.class);
        when(mockBall.getBallCenter()).thenReturn(new float[] {0, 0});
        when(mockHole.getHoleCenter()).thenReturn(new float[] {50, 50});
        mockStaticApp.when(() -> getDistance(new float[] {0, 0}, new float[] {50, 50})).thenReturn(50.0d); // branch: not in hole radius
        doNothing().when(mockBall).setBallRadius(12);

        doNothing().when(mockBall).moveOne();

        doNothing().when(mockBall).interact(mockLine);
        doNothing().when(app).removeLine(mockLine);

        app.drawAll();

        when(mockBall.willCollide(mockLine)).thenReturn(new float[]{0, 0}); // Branch where collision happens

        app.drawAll();


        when(mockBall.willCollide(mockLine)).thenReturn(null); // branch: no collision

        when(app.getWallAssociated(mockBall, mockLine)).thenReturn(new Wall[]{mockWall, mockWall});
        doNothing().when(mockWall).damage(mockBall);
        when(mockWall.getHP()).thenReturn(0);
        doNothing().when(app).removeWall(mockWall);
        doNothing().when(mockBall).interact(mockLine);

        doNothing().when(mockBall).moveOne();

        doNothing().when(mockBall).interact(mockLine);
        doNothing().when(app).removeLine(mockLine);
        when(mockBall.willCollide(mockLine)).thenReturn(null); // branch: no collision

        when(mockBall.getBallCenter()).thenReturn(new float[] {0, 0});
        when(mockHole.getHoleCenter()).thenReturn(new float[] {10, 10});
        mockStaticApp.when(() -> getDistance(new float[] {0, 0}, new float[] {10, 10})).thenReturn(10.0d); // branch: in hole radius
        when(mockBall.meetHole(mockHole, app)).thenReturn(true);
        app.drawAll();

    }

    @Test
    public void testAddBorders() {
        Wall mockWall = mock(Wall.class);
        Tile mockTile = mock(Tile.class);
        app.allLines.clear();

        app.board = new Tile[32][32];

        for (int i = 0; i < app.board.length; i++) {
            for (int j = 0; j < app.board[i].length; j++) {
                if (i == 0) {
                    app.board[i][j] = mockWall;
                }
                else {

                    app.board[i][j] = mockTile;
                }
            }
        }

        doNothing().when(app).addWallLineSegment(mockWall);
        app.addBorders();

        float[] topLeftCorner = new float[] {-32, TOPBAR-32}; // H0, V0
        float[] topRightCorner = new float[] {WIDTH+32, TOPBAR-32}; // H1, V0
        float[] bottomLeftCorner = new float[] {-32, HEIGHT+32}; // H0, V1
        float[] bottomRightCorner = new float[] {WIDTH+32, HEIGHT+32}; // H1, V1
        boolean isDrawn = false;
        assertEquals(app.allLines.get(0), new Line(topLeftCorner, topRightCorner, 0, isDrawn));
        assertEquals(app.allLines.get(1), new Line(topLeftCorner, bottomLeftCorner, 0, isDrawn));
        assertEquals(app.allLines.get(2), new Line(bottomLeftCorner, bottomRightCorner, 0, isDrawn));
        assertEquals(app.allLines.get(3), new Line(bottomRightCorner, topRightCorner, 0, isDrawn));

    }


    @Test
    public void testAddWallLineSegments() {
        Wall wall = new Wall(1, 1, 2);
        app.allLines.clear();

        app.addWallLineSegment(wall);
    }

    @Test
    public void testGetWallLineSegments() {
        Wall wall = new Wall(1, 1, 2);

        app.getWallLineSegments(wall);
    }

    @Test
    public void testRemoveLine() {
        // Testing when input is a coordinate
        App app = new App();

        float[] toRemove = new float[] {2, 2};
        Line line = new Line(new float[]{0, 0}, new float[]{4,4}, 0, true);

        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(line);

        //when(app.mouseOnLine(toRemove, line.getP1(), line.getP2())).thenReturn(true);
        app.removeLine(toRemove);

        // Testing when input is coordinate, and line not found
        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(new Line(new float[]{-500, -500}, new float[]{-400, -400}, 0, true));
        app.removeLine(toRemove);

        // Testing when input is a line
        Line line2 = new Line(new float[]{0, 0}, new float[]{4, 4}, 0, true);
        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(line2);

        //when(line.equals(line2)).thenReturn(true);
        app.removeLine(line2);

        // Testing when input is line, and line not found in drawnLines
        //when(line.equals(line2)).thenReturn(false);
        app.drawnLines.clear();
        app.drawnLines.add(new ArrayList<>());
        app.drawnLines.get(0).add(new Line(new float[]{-2, -2}, new float[]{8, 3}, 0, true));
        app.removeLine(line2);
    }

    @Test
    public void testPausedMousePressed() {
        app.gameState = GameState.PAUSED;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.PRESS, Event.CTRL, 100, 100, 37, 1);
        app.mousePressed(e);

        app.gameState = GameState.WIN;
        app.mousePressed(e);

        app.gameState = GameState.OVER;
        app.mousePressed(e);
    }

    @Test
    public void testLeftMousePressed() {
        app.gameState = GameState.PLAYING;
        // Branch: CTRL pressed, expected early return
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.PRESS, Event.CTRL, 100, 100, 37, 1);
        app.mousePressed(e);

        // Branch: CTRL not pressed & not drawing
        isDrawing = false;
        e = new MouseEvent(null, 100, MouseEvent.PRESS, 0, 100, 100, 37, 1);
        app.mousePressed(e);

        // Branch: Is drawing and gap between frameCount not 1
        isDrawing = true;
        app.lastLine = 0;
        app.frameCount = 4;
        app.mousePressed(e);

        app.frameCount = 1;
        app.mousePressed(e);

        app.lastLine = 1;
        app.frameCount = 2;
        app.mousePressed(e);

        app.frameCount = 3;
        app.mousePressed(e);
    }

    @Test
    public void testRightMousePressed() {
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.PRESS, 0, 100, 100, 32, 1);
        app.mousePressed(e);

        e = new MouseEvent(null, 100, MouseEvent.PRESS, 0, 100, 100, 39, 1);
        doNothing().when(app).removeLine(any(float[].class));
        app.mousePressed(e);
    }

    @Test
    public void testPausedMouseDragged() {
        app.gameState = GameState.PAUSED;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.DRAG, 0, 100, 100, 37, 1);
        app.mouseDragged(e);

        app.gameState = GameState.WIN;
        app.mouseDragged(e);

        app.gameState = GameState.OVER;
        app.mouseDragged(e);
    }

    @Test
    public void testLeftNegativeMouseDragged() {
        // Branch: Left
        // Mouse is on start
        app.gameState = GameState.PLAYING;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.DRAG, 0, 100, 100, 37, 1); // LEFT
        app.start = new float[]{e.getX(), e.getY()};
        app.mouseDragged(e);
    }

    @Test
    public void testLeftDrawingMouseDragged() {
        app.gameState = GameState.PLAYING;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.DRAG, 0, 100, 100, 37, 1);
        // Branch: is drawing
        app.start = new float[]{0, 0}; // reset start, mouse not on start
        app.end = new float[]{0, 0};
        App.isDrawing = true;
        app.frameCount = 3;
        app.lastLine = 1;
        app.mouseDragged(e);

        app.start = new float[]{100, 0}; // reset start, mouse not on start
        app.end = new float[]{0, 0};
        app.lastLine = 0;
        app.mouseDragged(e);

        app.start = new float[]{0, 100}; // reset start, mouse not on start
        app.end = new float[]{0, 0};
        app.frameCount = 1;
        app.mouseDragged(e);

        app.start = new float[]{0, 0}; // reset start, mouse not on start
        app.end = new float[]{0, 0};
        app.lastLine = 1;
        app.frameCount = 2;
        app.mouseDragged(e);

    }

    @Test
    public void testLeftNotDrawingMouseDragged() {
        app.gameState = GameState.PLAYING;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.DRAG, 0, 100, 100, 37, 1);
        isDrawing = false;
        app.mouseDragged(e);
    }

    @Test
    public void testNotLeftMouseDragged() {
        app.gameState = GameState.PLAYING;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.DRAG, 0, 100, 100, 39, 1);
        app.mouseDragged(e);
    }

    @Test
    public void testGetWallAssociated() {
        Ball ball = new Ball(30, 34, 1);
        ball.setVector(new float[]{2, -2});
        Line line = new Line(new float[]{0, 0}, new float[]{50, 50}, 0, true);
        assertNotNull(ball.willCollide(line)); // check that ball will collide

        //Branch 1: Wall found
        Wall wall = new Wall(1, 1, 2);
        app.walls.clear();
        app.walls.add(wall);
        assertNull(app.getWallAssociated(ball, line)); // Test for branch where wall is not on board
        app.board = new Tile[32][32];
        app.board[1][1] = wall;
        assertNotNull(app.getWallAssociated(ball, line)); // Positive case

        //Branch 2: Wall not found, so distance > minDistance
        wall = new Wall(100, 100, 2);
        app.walls.clear();
        app.walls.add(wall);
        app.board = new Tile[100][100];
        app.board[99][99] = wall;
        assertNull(app.getWallAssociated(ball, line));
        app.getWallAssociated(ball, line);
    }

    @Test
    public void testRemoveWall() {
        Wall wall = new Wall(0, 0, 2);
        //Case where no wall found
        app.allLines.clear();
        app.walls.clear();
        // add dummy lines
        for (int i = 0; i < 10; i++) {
            app.allLines.add(new Line(new float[]{500, 500}, new float[]{600, 600}, 1, true));
        }

        app.removeWall(wall);

        //Case where wall is found
        app.walls.add(wall);
        app.allLines.clear();
        app.board = new Tile[32][32];
        app.board[0][0] = wall;
        Line[] toRemove = app.getWallLineSegments(wall);
        app.allLines.addAll(Arrays.asList(toRemove));

        // // add dummy lines
        app.allLines.add(new Line(new float[]{100, 100}, new float[]{100, 100}, 1, true)); // dummy lines
        app.allLines.add(new Line(new float[]{75, 50}, new float[]{75, 50}, 1, true)); // dummy lines
        app.allLines.add(new Line(new float[]{150, 60}, new float[]{150, 60}, 1, true)); // dummy lines

        app.removeWall(wall);
        assertFalse(app.walls.contains(wall));
        assertFalse(app.allLines.containsAll(Arrays.asList(toRemove)));
        assertSame(app.board[0][0].getClass(), Blank.class);
    }

    @Test
    public void testSpiral() {
        app.board = new Tile[32][32];
        int[] spiral = new int[]{1, 0};
        app.moveSpiral(spiral);

        spiral = new int[]{31, 1};
        app.moveSpiral(spiral);

        spiral = new int[]{30, 31};
        app.moveSpiral(spiral);

        spiral = new int[]{31, 31};
        app.moveSpiral(spiral);

        spiral = new int[]{0, 30};
        app.moveSpiral(spiral);

        spiral = new int[]{0, 31};
        app.moveSpiral(spiral);

        spiral = new int[]{31, 30};
        app.moveSpiral(spiral);

        spiral = new int[]{31, 32};
        app.moveSpiral(spiral);

        spiral = new int[]{0, -1};
        app.moveSpiral(spiral);
    }

    @Test
    public void testMouseReleased() {
        // Branch: not playing
        app.gameState = GameState.PAUSED;
        MouseEvent e = new MouseEvent(null, 100, MouseEvent.RELEASE, 0, 100, 100, LEFT, 1);
        app.mouseReleased(e);

        app.gameState = GameState.WIN;
        app.mouseReleased(e);

        app.gameState = GameState.OVER;
        app.mouseReleased(e);

        // Branch: playing
        app.gameState = GameState.PLAYING;
        isDrawing = false;
        app.mouseReleased(e);

        isDrawing = true;
        app.tempLines.clear();
        app.mouseReleased(e);

        isDrawing = true;
        app.tempLines.add(new ArrayList<>());
        app.tempLines.get(0).add(new Line(new float[]{0, 0}, new float[]{1,1}, 1, true));
        app.mouseReleased(e);

        e = new MouseEvent(null, 100, MouseEvent.RELEASE, 0, 100, 100, RIGHT, 1);
        app.mouseReleased(e);
    }

    @Test
    public void testSpawnBalls() {
        //Case: Empty ball queue
        app.ballQueue = new Ball[5];
        app.ballQueue[0] = new Ball(100, 100, 1);
        app.frameCount = 31;
        app.spawnInterval = 1;
        app.spawnBalls();

        //Case: Ball Queue not empty
        app.spawners.add(new Spawner(2, 2));
        app.spawners.add(new Spawner(3, 3));

        app.maxBallQueue = 5;
        app.ballQueue = new Ball[app.maxBallQueue];
        app.frameCount = 30;
        app.spawnInterval = 1;
        app.ballQueue[0] = new Ball(100, 100, 1);
        app.ballQueue[1] = new Ball(50, 40, 2);
        app.ballQueue[2] = new Ball(0, 10, 3);
        app.ballQueue[3] = new Ball(10, 70, 4);
        app.spawnBalls();


    }

    @Test
    public void testGetColourCode() {
        ArrayList<ArrayList<String>> line = new ArrayList<>();
        line.add(new ArrayList<>());
        line.get(0).add("H");
        //Catch index out of bounds
        assertEquals(-1, app.getColourCode(line, 0, 0));

        // Catch whitespace
        line.get(0).add(" ");
        assertEquals(-1, app.getColourCode(line, 0, 0));

        // Catch non-numeric input
        line.get(0).set(1, "Hello");
        assertEquals(-1, app.getColourCode(line, 0, 0));

        line.get(0).set(1, "-1");
        assertEquals(-1, app.getColourCode(line, 0, 0));

        line.get(0).set(1, "5");
        assertEquals(-1, app.getColourCode(line, 0, 0));

        line.get(0).set(1, "2");
        assertEquals(2, app.getColourCode(line, 0, 0));
    }

    @Test
    public void testSetLayout() {
        app.board = new Tile[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE];
        ArrayList<ArrayList<String>> lines = new ArrayList<>();

        // Branch: Insufficient number of lines
        when(app.getFileContents()).thenReturn(lines);
        app.setLayout();

        // Branch: Sufficient number of lines, Insufficient number of characters
        app.board = new Tile[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE]; // reset board
        for (int i = 0; i < app.board.length; i++) {
            lines.add(new ArrayList<>());
        }
        app.board[0][0] = null;
        app.setLayout();

        // Branch: Sufficient number of lines, Sufficient number of characters in lnies
        app.board = new Tile[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE]; // reset board
        app.board[0][0] = new Blank(0, 0);
        lines.get(0).add(" ");
        lines.get(0).add(" "); // Blank tile
        lines.get(0).add("X"); // Grey wall
        lines.get(0).add("1"); // Different colour walls
        lines.get(0).add("2");
        lines.get(0).add("3");
        lines.get(0).add("4");
        lines.get(0).add("H"); // Hole with colour 2
        lines.get(0).add("2");
        lines.get(3).add("H"); // Hole with invalid/unspecified colour
        lines.get(0).add(" ");
        lines.get(0).add("S"); // Add spawner
        lines.get(0).add("B"); // Ball w/ valid colour
        lines.get(0).add("1");
        lines.get(0).add("B"); // Ball w/ invalid colour
        lines.get(0).add("g");
        lines.get(0).add("$"); // Invalid input

        // Fill rest of line 0 to meet number of lines needed
        for (int i = lines.get(0).size(); i < app.board[0].length; i++) {
            lines.get(0).add(" ");
        }

        when(app.getPosition(anyInt(), anyInt())).thenReturn(Hole.GridPosition.BL);
        app.setLayout();

    }

    @Test
    public void testGetFileContents() {
        app.isTesting = false;
        app.getFileContents();

        app.isTesting = true;
        app.getFileContents();
    }

    @Test
    public void testKeyPressed() {
        KeyEvent event = new KeyEvent(null, 100, KeyEvent.PRESS, 0, 'r', 82); // Press r
        doNothing().when(app).restart();
        app.keyPressed(event);

        app.gameState = GameState.PLAYING;
        event = new KeyEvent(null, 100, KeyEvent.PRESS, 0, ' ', 32); // Press spacebar
        app.keyPressed(event);

        app.gameState = GameState.PAUSED;
        app.keyPressed(event);

        app.gameState = GameState.OVER;
        app.keyPressed(event);

        event = new KeyEvent(null, millis(), KeyEvent.PRESS, 2, 'g', 17); // Press CTRL
        app.keyPressed(event);
        assertTrue(ctrlPressed);
    }

    @Test
    public void testGetDistance() {
        // Branch: Positive inputs
        App.getDistance(new float[]{1.0f, 4.0f}, new float[]{1.0f, 4.0f});
        App.getDistance(new double[]{100.0d, 100.0d}, new float[]{200.0f, 150.0f});
    }

    @Test
    public void testColourToInt() {
        int result = app.colourToInt("grey");
        assertEquals(0, result);

        result = app.colourToInt("orange");
        assertEquals(1, result);

        result = app.colourToInt("blue");
        assertEquals(2, result);

        result = app.colourToInt("green");
        assertEquals(3, result);

        result = app.colourToInt("yellow");
        assertEquals(4, result);

        result = app.colourToInt("helloWorld");
        assertEquals(0, result);
    }

    @Test
    public void testAddDrawnLine() {
        Line line = new Line(new float[]{0, 200}, new float[]{50, 300}, 0, true);
        app.tempLines.clear();
        int initialSize = app.tempLines.size();

        app.addDrawnLine(line);
        assertEquals(initialSize + 1, app.tempLines.size());

        app.tempLines.clear();
        app.tempLines.add(new ArrayList<>());
        app.tempLines.get(0).add(line); // dummy line
        initialSize = app.tempLines.size();
        app.addDrawnLine(line);
        assertEquals(initialSize, app.tempLines.size());

        // Branch
        line = new Line(new float[]{0, 0}, new float[]{50, 300}, 0, true);
        app.addDrawnLine(line);

        line = new Line(new float[]{0, 200}, new float[]{50, 0}, 0, true);
        app.addDrawnLine(line);
    }

    @Test
    public void testGetPosition() {
        assertEquals(Hole.GridPosition.BL, app.getPosition(0, 1));
        assertEquals(Hole.GridPosition.TR, app.getPosition(1, 0));
        assertEquals(Hole.GridPosition.BR, app.getPosition(1, 1));
        assertNull(app.getPosition(2, 2));
        assertNull(app.getPosition(0, 2));
        assertNull(app.getPosition(2, 1));
        assertNull(app.getPosition(1, 2));
        assertNull(app.getPosition(2, 0));
    }
}
