package inkball;

import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.HashMap;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import processing.core.PImage;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BallTest {

    private App app;
    private Ball ball;
    private Line line;
    private Hole hole;
    private ArrayList<Ball> mockBallList;

    @BeforeEach
    public void setUp() {
        // Setup runs before each test
        app = mock(App.class); // Creating a mock app because only score tests are relevant
        this.appMockSetUp();

        mockBallList = mock(ArrayList.class);
        when(app.getBalls()).thenReturn(mockBallList);

        ball = new Ball(88, 88, 4);
        line = new Line(new float[]{90, 90}, new float[]{105, 105}, 1, false);
        hole = new Hole(3, 3, 0, Hole.GridPosition.BR);
    }

    public void appMockSetUp() {
        App.score = 0;
        app.modScoreIncrease = 1.1f;
        app.modScoreDecrease = 1.0f;
        App.scoreIncrease = new HashMap<>();
        App.scoreDecrease = new HashMap<>();

        App.scoreIncrease.put("grey", 70);
        App.scoreIncrease.put("orange", 50);
        App.scoreIncrease.put("blue", 30);
        App.scoreIncrease.put("green", 20);
        App.scoreIncrease.put("yellow", 100);

        App.scoreDecrease.put("grey", 0);
        App.scoreDecrease.put("orange", 25); //-25
        App.scoreDecrease.put("blue", 20);
        App.scoreDecrease.put("green", 10); //-10
        App.scoreDecrease.put("yellow", 60); //-60

        app.ballQueue = new Ball[5];
    }

    /** Testing Ball Construction **/
    @Test
    public void testNormalConstruction() {
        assertEquals(88, ball.getX()); // Test getX() method
        assertEquals(88, ball.getY()); // Test getY() method
        assertEquals(4, ball.getColour()); // Test getColour() method
        assertEquals(12, ball.getBallRadius(), 0.0001f); // Test default ball radius
        assertFalse(ball.getIsAbsorbed()); // Test default isAbsorbed state

        int MAX_TESTS = 1000;
        for (int i = 0; i < MAX_TESTS; i++) {
            Ball testBall = new Ball(20, 20, 3);
            assertTrue(Arrays.equals(new float[]{2, -2}, testBall.getVector()) || Arrays.equals(new float[]{-2, 2}, testBall.getVector())); // Test starting vector
        }
    }

    @Test
    public void testEdgeConstruction() {
        Ball edgeBall = new Ball(-100.1f, -23.55f, -1); // Edge Case: negative x,y coordinates | Negative Case: out-of-range colour code

        assertEquals(-100.1, edgeBall.getX(), 0.0001f); // Negative coordinates should be accepted
        assertEquals(-23.55, edgeBall.getY(), 0.0001f);
        assertEquals(0, edgeBall.getColour()); // Expected to default to grey

        edgeBall = new Ball(-100.1f, -23.55f, 5);
        assertEquals(0, edgeBall.getColour()); // Expected to default to grey

        edgeBall = new Ball(-100.1f, -23.55f, -1);
        assertEquals(0, edgeBall.getColour()); // Expected to default to grey
    }

    /** Testing draw() **/
    @Test
    public void testNormalDraw() {
        ball.draw(app);
        verify(app).getSprite("ball"+ball.getColour());
        verify(app).image(app.getSprite("ball"+ball.getColour()),ball.getX(), ball.getY(), ball.getBallRadius() * 2, ball.getBallRadius() * 2);
    }

    @Test
    public void testNegativeDraw() {
        ball.absorb();
        ball.draw(app);
        verify(app, never()).getSprite(anyString());
        verify(app, never()).image(any(PImage.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    /** Testing get/setVector() **/
    @Test
    public void testNormalSetVector() {
        ball.setVector(new float[] {-5.67f, 4}); // Testing setVector() with normal inputs. Any float input should be accepted

        assertArrayEquals(new float[] {-5.67f, 4}, ball.getVector());
    }

    @Test
    public void testEdgeSetVector() {
        ball.setVector(new float[] {0.0f, 0.0f}); // Edge case: setVector() with zero vector input. Should pass.

        assertArrayEquals(new float[] {0.0f, 0.0f}, ball.getVector());
    }

    /** Testing get/setBallRadius() Construction **/
    @Test
    public void testNormalSetBallRadius() {
        ball.setBallRadius(5.6f); // Normal Case: Float radius input
        assertEquals(5.6f, ball.getBallRadius(), 0.0001f);
    }

    @Test
    public void testZeroBallRadius() {
        ball.setBallRadius(0);
        assertEquals(0, ball.getBallRadius(), 0.0001f);
        assertFalse(ball.getIsAbsorbed()); // Should not return true. Only absorbed when meetHole() is called, but not setRadius()
    }

    @Test
    public void testEdgeSetBallRadius() {
        ball.setBallRadius(-5.6f);
        assertEquals(0, ball.getBallRadius()); // Negative input should default to zero
        assertFalse(ball.getIsAbsorbed()); // Should not return true. Only absorbed when meetHole() is called, but not setRadius()
    }

    /** Testing get/setIsAbsorbed() **/
    @Test
    public void testAbsorption() {
        ball.absorb();
        assertTrue(ball.getIsAbsorbed());

        ball.absorb(); // Check absorption twice
        assertTrue(ball.getIsAbsorbed());
    }

    /** Testing get/setColour() **/
    @Test
    public void testNormalSetColour() {
        ball.setNewColour(line); // Should set colour from 4 to 1
        assertEquals(line.getColourTo(), ball.getColour());
    }

    @Test
    public void testNegativeSetColour() {
        Line edgeLine = new Line(new float[]{2, 2}, new float[]{-2, -2}, 0, false);
        int initialColour = ball.getColour();
        ball.setNewColour(edgeLine);
        assertEquals(initialColour, ball.getColour()); // Ball colour should not change, as line colour is 0
    }

    @Test
    public void testEdgeSetColour() {
        Line edgeLine = new Line(new float[]{2, 2}, new float[]{-2, -2}, -2, false);
        int initialColour = ball.getColour();
        ball.setNewColour(edgeLine);
        assertEquals(initialColour, ball.getColour()); // Ball colour should not change, as line colour is 0
    }

    /** Testing getBallCenter() **/
    @Test
    public void testGetBallCenter() {
        float centerX = ball.getX() + ball.getBallRadius();
        float centerY = ball.getY() + ball.getBallRadius();
        assertEquals(centerX, ball.getBallCenter()[0], 0.0001f);
        assertEquals(centerY, ball.getBallCenter()[1], 0.0001f);
    }

    /** Testing moveOne() **/
    @Test
    public void testMoveOne() {
        // No negative/edge cases for moveOne()
        float[] initial = new float[] {ball.getX(), ball.getY()};
        ball.moveOne();
        assertEquals(initial[0]+ball.getVector()[0], ball.getX(), 0.0001f);
        assertEquals(initial[1]+ball.getVector()[1], ball.getY(), 0.0001f);
    }

    /** Testing interact() **/
    @Test
    public void testPositiveInteract() {
        ball.setVector(new float[]{-2, 2}); // Simulating a possible vector for testing

        // Calculating expected attributes after interact by calling methods called in interact()
        Ball copyBall = new Ball (ball.getX(), ball.getY(), ball.getColour());
        copyBall.setVector(new float[] {-2, 2});
        int expectedColour = line.getColourTo();
        float[] expectedVector = copyBall.setNewDirection(line);

        // Expected behaviour: pass willCollide() conditional, and calls setNewColour(), setNewDirection(), and moveOne()
        ball.interact(line);

        assertNotNull(ball.willCollide(line)); // true
        assertFalse(Arrays.equals(ball.willCollide(line), line.getP1())); //true

        assertEquals(expectedColour, ball.getColour());
        assertArrayEquals(expectedVector, ball.getVector());
        assertFalse(ball.getIsAbsorbed()); // Ensure no other attributes are modified
    }

    @Test
    public void testNegativeInteract() {
        // Expected behaviour: ball does not pass willCollide(), so no change
        ball.setVector(new float[]{-2, 2});

        // Create line that ball will not collide with
        Line negativeLine = new Line(new float[]{0, 0}, new float[]{2, 2}, 0, false);

        // Get initial attributes of ball
        int initialColour = ball.getColour();
        float[] initialVector = ball.getVector();
        float[] initialPos = new float[] {ball.getX(), ball.getY()};

        ball.interact(negativeLine);

        assertEquals(initialColour, ball.getColour());
        assertEquals(initialVector[0], ball.getVector()[0], 0.0001f);
        assertEquals(initialVector[1], ball.getVector()[1], 0.0001f);
        assertEquals(initialPos[0], ball.getX(), 0.0001f);
        assertEquals(initialPos[1], ball.getY(), 0.0001f);
        assertFalse(ball.getIsAbsorbed()); // Ensure no other attributes are modified
    }

    @Test
    public void testEdgeInteract() {
        // Edge: Initialising a "line" (a dot) at the same coordinates as the ball
        Line edgeLine = new Line(new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, 0, true);

        // Calculating expected attributes after interact by calling methods called in interact()
        Ball copyBall = ball;
        copyBall.setNewColour(edgeLine);
        float[] expectedVector = copyBall.setNewDirection(edgeLine);
        float[] expectedPos = copyBall.willCollide(edgeLine);

        // Expected behaviour: pass willCollide() conditional, and calls setNewColour(), setNewDirection(), and moveOne()
        float[] actualPos = ball.willCollide(edgeLine);
        ball.interact(edgeLine);

        assertEquals(copyBall.getColour(), ball.getColour());
        assertEquals(expectedVector[0], ball.getVector()[0], 0.0001f);
        assertEquals(expectedVector[1], ball.getVector()[1], 0.0001f);
        assertArrayEquals(expectedPos, actualPos);
        assertFalse(ball.getIsAbsorbed());
    }

    /** Testing willCollide() to return correct collision point **/
    @Test
    public void testNormalWillCollide() {
        // Fix ball vector for testing purposes
        ball.setVector(new float[] {-2, 2});

        // Expecting to collide/not return null, so calculate expected collision point
        float[] P1 = line.getP1();
        float[] P2 = line.getP2();
        float[] ballXY = new float[] {ball.getBallCenter()[0] + ball.getVector()[0], ball.getBallCenter()[1] + ball.getVector()[1]};

        double distP1 = App.getDistance(ballXY, P1);
        double distP2 = App.getDistance(ballXY, P2);
        double distP1P2 = App.getDistance(P1, P2);

        float t = (float) ((distP1 - Ball.ARTIFICIAL_RADIUS) / distP1P2);
        float collisionX = P1[0] + t * (P2[0] - P1[0]);
        float collisionY = P1[1] + t * (P2[1] - P1[1]);

        assertTrue(distP1 + distP2 < Ball.ARTIFICIAL_RADIUS + distP1P2);
        assertArrayEquals(new float[] {collisionX, collisionY}, ball.willCollide(line));
    }

    @Test
    public void testNegativeWillCollide() {
        // No collision
        Line negativeLine = new Line(new float[]{0, 0}, new float[]{2, 2}, 0, false);

        assertNull(ball.willCollide(negativeLine)); // Must return null since no collision
    }

    @Test
    public void testEdgeWillCollide() {
        // Edge Case: Line is a dot with same coordinates as ball
        Line edgeLine = new Line(new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, 0, true);

        float[] expectedCollisionP = new float[] {edgeLine.getP1()[0], edgeLine.getP1()[1]}; // Collision point must be on the dot itself

        // Expecting to collide/not return null, so calculate expected collision point
        float[] P1 = edgeLine.getP1();
        float[] P2 = edgeLine.getP2();
        float[] ballXY = new float[] {ball.getBallCenter()[0] + ball.getVector()[0], ball.getBallCenter()[1] + ball.getVector()[1]};

        double distP1 = App.getDistance(ballXY, P1);
        double distP2 = App.getDistance(ballXY, P2);
        double distP1P2 = App.getDistance(P1, P2);

        assertTrue(distP1 + distP2 < Ball.ARTIFICIAL_RADIUS + distP1P2); // passing
        assertTrue(distP1P2 < 0.001f);

        assertEquals(expectedCollisionP[0], ball.willCollide(edgeLine)[0], 0.0001f);
        assertEquals(expectedCollisionP[1], ball.willCollide(edgeLine)[1], 0.0001f);
    }

    /** Testing setNewDirection() **/
    @Test
    public void testNormalNewDirection() {
        ball.setVector(new float[]{-2, 2});
        float[] expectedNewDir = new float[]{2.0f, -2.0f};

        ball.setNewDirection(line);
        assertEquals(expectedNewDir[0], ball.getVector()[0], 0.0001f);
        assertEquals(expectedNewDir[1], ball.getVector()[1], 0.0001f);

        line = new Line(new float[]{100,96}, new float[]{104,100}, 0, false);
        expectedNewDir = new float[]{-2.0f,2.0f};
        ball.setVector(new float[]{2, -2});
        
        ball.setNewDirection(line);
        assertEquals(expectedNewDir[0], ball.getVector()[0], 0.0001f);
        assertEquals(expectedNewDir[1], ball.getVector()[1], 0.0001f);
    }

    @Test
    public void testNegativeNewDirection() {
        Line negativeLine = new Line(new float[]{0, 0}, new float[]{2, 2}, 0, false);

        assertNull(ball.willCollide(negativeLine));
        assertNull(ball.setNewDirection(negativeLine));
    }

    @Test
    public void testEdgeNewDirection() {
        Line edgeLine = new Line(new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, new float[]{ball.getBallCenter()[0], ball.getBallCenter()[1]}, 0, false);

        // Expected behaviour: bounce in the opposite direction
        float[] expectedNewDir = new float[] {-1*ball.getVector()[0], -1*ball.getVector()[1]};

        ball.setNewDirection(edgeLine);
        assertEquals(expectedNewDir[0], ball.getVector()[0], 0.0001f);
        assertEquals(expectedNewDir[1], ball.getVector()[1], 0.0001f);
    }

    /** Testing meetHole() **/
    @Test
    public void testNormalMeetHole() {
        // Positive Case 1: Ball within attraction radius, but not directly over the hole
        ball = new Ball(116, 116, 0);
        ball.setVector(new float[]{-2, -2});
        float[] ballCenter = ball.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();

        double expectedScore = App.score;
        float[] expectedVec = new float[] {ball.getVector()[0] + ball.getAttractionVector(hole)[0], ball.getVector()[1] + ball.getAttractionVector(hole)[1]};
        float expectedRad = 12 * (float) (App.getDistance(holeCenter, ballCenter) / 32); // Ensures shrink animation is applied

        assertFalse(expectedRad < 6 || App.getDistance(ballCenter, holeCenter) < 8);
        assertTrue(ball.meetHole(hole, app));

        ball.meetHole(hole, app);
        assertEquals(expectedScore, App.score);
        assertArrayEquals(expectedVec, ball.getVector(), 0.5f);
        assertEquals(expectedRad, ball.getBallRadius(), 0.5f); // Floating-point differences are unrecognisable to the eye, hence higher delta

        /** Positive Case 2: Ball over the hole
         * A) Same hole and ball colour
         * Radius < 6 **/
        Ball ball2 = new Ball(94, 157, 2); // Distance is ~12
        Hole hole2 = new Hole(3, 3, ball2.getColour(), Hole.GridPosition.BR);
        ball2.setVector(new float[]{-2, -2});
        float[] ball2Center = ball2.getBallCenter();
        float[] hole2Center = hole2.getHoleCenter();

        // Not testing for vec because doesn't matter due to absorption
        expectedRad = 12 * (float) (App.getDistance(hole2Center, ball2Center) / 32); // Ensures shrink animation is applied
        expectedScore += App.scoreIncrease.get(hole2.colourToString()) * app.modScoreIncrease; // 30 * 1.1 = 33

        assertTrue(expectedRad < 6);
        assertTrue(ball2.meetHole(hole2, app));

        ball2.meetHole(hole2, app);
        assertEquals(0, ball2.getBallRadius()); // Need to be 0
        assertEquals(ball2.getColour(), hole2.getColour());
        assertEquals(expectedScore, App.score, 0.5d);
        verify(mockBallList).remove(ball2);
        assertTrue(ball2.getIsAbsorbed());

//        /** Distance is < 8 **/
//        Ball ball2b = new Ball(90, 150, 2); // Distance is < 8
//        ball2Center = ball2b.getBallCenter();
//
//        assertTrue(App.getDistance(ball2Center, hole2Center) < 8);
//        assertTrue(ball2b.meetHole(hole2, app));
//
//        expectedRad = 12 * (float) (App.getDistance(hole2Center, ball2Center) / 32);
//        expectedScore += App.scoreIncrease.get(hole2.colourToString()) * app.modScoreIncrease; // 30 + 30 * 1.1 = 66
//
//        ball2b.meetHole(hole2, app);
//////        assertEquals(0, ball2b.getBallRadius()); // Need to be 0
////        assertEquals(ball2b.getColour(), hole2.getColour());
//////        assertEquals(expectedScore, App.score, 0.5d);
////        verify(mockBallList).remove(ball2b);
////        assertTrue(ball2b.getIsAbsorbed());

        /** B) Either colour is grey
         * Ball is grey **/
        Ball ball3 = new Ball(90, 150, 0); // hole colour is 0

        assertEquals(0,ball3.getColour());
        assertNotEquals(0, hole2.getColour());

        expectedScore += App.scoreIncrease.get("grey") * app.modScoreIncrease; // 110
        ball3.meetHole(hole2, app);
        assertEquals(expectedScore, App.score, 0.5d);
        verify(mockBallList).remove(ball3);
        assertTrue(ball3.getIsAbsorbed());

        /** Hole is grey **/
        ball3 = new Ball(ball2.getX(), ball2.getY(), ball2.getColour());
        Hole hole3 = new Hole(hole2.getX(), hole2.getY(), 0, Hole.GridPosition.BR);
        // Using ball2 to test non-grey ball

        assertNotEquals(0, ball3.getColour());
        assertEquals(0, hole3.getColour());

        expectedScore += App.scoreIncrease.get("grey") * app.modScoreIncrease; // 187
        ball3.meetHole(hole3, app);
        assertEquals(expectedScore, App.score, 0.5d);
        verify(mockBallList).remove(ball3);
        assertTrue(ball3.getIsAbsorbed());

        /** Both are grey **/
        ball3 = new Ball(ball2.getX(), ball2.getY(), 0);
        // reusing hole3

        assertEquals(0, ball3.getColour());
        assertEquals(0, hole3.getColour());

        expectedScore += App.scoreIncrease.get("grey") * app.modScoreIncrease; // 264
        ball3.meetHole(hole3, app);
        assertEquals(expectedScore, App.score, 0.5d);
        verify(mockBallList).remove(ball3);
        assertTrue(ball3.getIsAbsorbed());

        /** C) Different colours and NOT grey **/
        Random rand = new Random();
        double initialScore = App.score;
        for (int i = 0; i < 1000; i++) { // Do 1000 rounds of testing
            //System.out.println(i); //track how many tests successful
            Ball ball4 = new Ball(ball2.getX(), ball2.getY(), ball2.getColour());
            int randColour = rand.nextInt(5);
            while (randColour == 0 || randColour == ball4.getColour()) {
                randColour = rand.nextInt(5);
            }
            Hole hole4 = new Hole(hole2.getX(), hole2.getY(), randColour, Hole.GridPosition.BR);

            // Ensuring that this case follows the 'else' branch
            assertNotEquals(ball4.getColour(), hole4.getColour());
            assertFalse(ball4.getColour() == 0 || hole4.getColour() == 0);

            expectedScore = initialScore;
            App.score = initialScore;
            expectedScore -= App.scoreDecrease.get(hole4.colourToString()) * app.modScoreDecrease; //239, 254, 204
            Ball[] initialQueue = app.ballQueue.clone();

            ball4.meetHole(hole4, app);
            assertEquals(expectedScore, App.score, 0.5d);
            verify(mockBallList).remove(ball4);
            assertFalse(ball4.getIsAbsorbed()); // Ball should not be signaled as absorbed, or else it wouldn't draw

            for (int j = i % 5; j < initialQueue.length - 1; j++) {
                if (initialQueue[j] == null) {
                    if (app.ballQueue[j] == null) {
                        fail("Failed NormalMeetHole: Failed capture ball was not added to app.ballQueue");
                    }
                    else if (app.ballQueue[j].getColour() == ball4.getColour()) {
                        break;
                    }
                    else {
                        fail("Failed NormalMeetHole: Ball added but colour was wrong");
                    }
                }
            }

            if (app.ballQueue[app.ballQueue.length - 1] != null) {
                app.ballQueue = new Ball[5]; // clear array
            }
        }
    }

    @Test
    public void testNegativeMeetHole() {
        // Negative Case: Ball already absorbed
        ball.absorb();
        assertFalse(ball.meetHole(hole, app));
    }

    @Test
    public void testEdgeMeetHole() {
        // Edge Case: gridPosition of hole is null, hence center of hole unknown
        Hole edgeHole = new Hole(4, 4, 0, null);
        assertFalse(ball.meetHole(edgeHole, app));
    }

    /** Testing getAttractionVector() **/
    @Test
    public void testNormalAttractionVector() {
        // Calculate Expected Normal Vector
        Ball copyBall = new Ball(ball.getX(), ball.getY(), ball.getColour());
        copyBall.setVector(ball.getVector());
        float[] ballCenter = ball.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();
        float[] attractionVec = new float[] {holeCenter[0] - ballCenter[0], holeCenter[1] - ballCenter[1]};

        float mag = (float) (Math.sqrt(Math.pow(attractionVec[0], 2) + Math.pow(attractionVec[1], 2)));

        float speed = (float) (Math.min(Ball.MAX_SPEED, (App.getDistance(ballCenter, holeCenter) * 0.005f)));
        float attractionX = attractionVec[0] / mag * speed;
        float attractionY = attractionVec[1] / mag * speed;

        float[] expectedResult = new float[] {attractionX, attractionY};

        assertFalse(mag < 3);

        ball.getAttractionVector(hole);
        assertFalse(ball.getIsAbsorbed());
        assertNotEquals(0, ball.getBallRadius());
        assertArrayEquals(expectedResult, ball.getAttractionVector(hole), 0.1f);
    }

    @Test
    public void testNegativeAttractionVector() {
        Ball negativeBall = new Ball(84, 148, 1); // Testing when ball is very close to hole

        float[] ballCenter = negativeBall.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();
        float[] attractionVec = new float[] {holeCenter[0] - ballCenter[0], holeCenter[1] - ballCenter[1]};
        float mag = (float) (Math.sqrt(Math.pow(attractionVec[0], 2) + Math.pow(attractionVec[1], 2)));

        assertTrue(mag < 3);

        negativeBall.getAttractionVector(hole);
        assertTrue(negativeBall.getIsAbsorbed());
        assertEquals(0, negativeBall.getBallRadius());
        assertArrayEquals(new float[]{0, 0}, negativeBall.getAttractionVector(hole), 0.1f);
    }

    @Test
    public void testToString() {
        ball = new Ball(84, 148, 0);
        assertEquals("grey", ball.colourToString());

        ball = new Ball(84, 148, 1);
        assertEquals("orange", ball.colourToString());

        ball = new Ball(84, 148, 2);
        assertEquals("blue", ball.colourToString());

        ball = new Ball(84, 148, 3);
        assertEquals("green", ball.colourToString());

        ball = new Ball(84, 148, 4);
        assertEquals("yellow", ball.colourToString());
    }
}