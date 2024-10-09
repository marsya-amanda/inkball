package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BallTest {

    App app;
    private Ball ball;
    private Line line;
    private Hole hole;

    @BeforeEach
    public void setUp() {
        // Setup runs before each test
        app = new App();
        ball = new Ball(88, 88, 4);
        line = new Line(new float[]{90, 90}, new float[]{105, 105}, 1, false);
        hole = new Hole(3, 3, 0, Hole.GridPosition.BR);
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

        ball.setNewDirection(line); // also not passing correctly
        assertEquals(expectedNewDir[0], ball.getVector()[0], 0.0001f);
        assertEquals(expectedNewDir[1], ball.getVector()[1], 0.0001f);
    }

    @Test
    public void testNegativeNewDirection() {
        Line negativeLine = new Line(new float[]{0, 0}, new float[]{2, 2}, 0, false);

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

    }

    @Test
    public void testEdgeMeetHole() {
        Ball copyBall = ball;
        copyBall.absorb();
        assertFalse(copyBall.meetHole(hole, app));

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
    public void testEdgeAttractionVector() {

    }
}