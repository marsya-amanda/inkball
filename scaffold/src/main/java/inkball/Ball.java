package inkball;

import processing.core.PImage;
import java.util.*;

/**
 *  Represents the balls seen in the game.
 *  This class is responsible for individual ball movement logic,
 *  individual ball-with-hole/line/wall interaction logic,
 *  and the mathematical calculations needed for interaction animation.
 */
public class Ball {
    private float x;
    private float y;
    int colour;
    /**
     * Represents the direction vector of the Ball object.
     */
    private float[] vector;
    private float ballRadius = 12;
    /**
     * Added to provide more accurate collision animations
     * and prevent premature collision.
     */
    public static final int ARTIFICIAL_RADIUS = 10;

    /**
     * Limits speed to ensure that game is playable and prevent major bugs.
     */
    public static final int MAX_SPEED = 12;

    /**
     * True when the Ball object has been absorbed by a Hole.
     */
    private boolean isAbsorbed;

    /**
     * Constructor for a Ball object.
     *
     * @param x The x-coordinate of the Ball, in 1px units
     *          and relative to the app window.
     * @param y The y-coordinate of the Ball, in 1px units
     *          and relative to the app window
     * @param colour The colour integer code of the Ball, where
     *               0 = grey, 1 = orange, 2 = blue, 3 = green, 4 = yellow.
     *               Defaults to grey if the argument is given outside of this range.<br>
     *
     * Note: The starting Ball vector attribute is randomised to either be [-2, 2] or [2, -2].
     * Absorbed attribute starts as false.
     */
    public Ball(float x, float y, int colour) {
        this.x = x; // so it spawns in the middle of the tile/spawner
        this.y = y;
        if (colour < 0 || colour > 4) {
            this.colour = 0;
        }
        else {
            this.colour = colour;
        }
        Random rand = new Random();
        if (rand.nextBoolean()) {
            this.vector = new float[] {2, -2};
        }
        else {
            this.vector = new float[] {-2, 2};
        }
        this.isAbsorbed = false;
    }

    public void draw(App app) {
        if (this.isAbsorbed) {
            return;
        }
        PImage ball = app.getSprite("ball"+colour);
        app.image(ball, this.x, this.y, this.ballRadius * 2, this.ballRadius * 2);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getColour() {
        return colour;
    }

    public float[] getVector() {
        return this.vector;
    }

    public float getBallRadius() {
        return this.ballRadius;
    }

    public void setBallRadius(float ballRadius) {
        if (ballRadius < 0) {
            ballRadius = 0;
        }
        this.ballRadius = ballRadius;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public boolean getIsAbsorbed() {
        return this.isAbsorbed;
    }

    public void absorb() {
        this.isAbsorbed = true;
    }

    public void moveOne() {
        this.x += vector[0];
        this.y += vector[1];
    }

    /**
     * Handles the interaction between the ball and a given line.
     *
     * @param line The Line object that the ball potentially interacts with.<br>
     *
     * Note: This method assumes the existence of several other methods:<br>
     *           - willCollide(Line): Checks if the ball will collide with the given line. <br>
     *           - setNewColour(Line): Updates the ball's color based on the collision. <br>
     *           - setNewDirection(Line): Calculates and sets the new direction of the ball after collision.<br>
     *           - moveOne(): Moves the ball one step in its current direction.<br>
     *<p>
     * The method performs the following actions if a collision is detected:<br>
     * 1. Changes the ball's color (if applicable).<br>
     * 2. Calculates and sets the new direction of the ball.<br>
     * 3. Moves the ball one step in the new direction.<br>
     *</p>
     * If no collision is detected (willCollide returns null), no action is taken.
     *
     */
    public void interact(Line line) {
        if (this.willCollide(line) != null) {
            //System.out.println("Collided!");
            this.setNewColour(line);
            this.setNewDirection(line);
            this.moveOne();
        }
    }

    /**
     * Determines if the ball will collide with a given line and calculates the collision point if so.
     *
     * @param line The Line object representing the line segment to check for collision.
     * @return A float array representing the collision point [x, y] if a collision will occur,
     *         or null otherwise.<br>
     *
     * Note: The method performs the following steps:<br>
     * 1. Calculates the ball's next position based on its current position and movement vector.<br>
     * 2. Computes distances between the ball's next position and the line's endpoints.<br>
     * 3. Checks if these distances indicate a collision using the artificial radius.<br>
     * 4. If a collision is detected, calculates the exact collision point on the line.<br>
     * Special case: If the line segment is extremely short,
     * it returns the first endpoint as the collision point. This is done to prevent ArithmeticExceptions due to
     * zero-division errors.
     */
    public float[] willCollide(Line line) {
        float[] P1 = line.getP1();
        float[] P2 = line.getP2();
        float[] ballXY = new float[] {this.getBallCenter()[0] + this.vector[0], this.getBallCenter()[1] + this.vector[1]};

        double distP1 = App.getDistance(ballXY, P1);
        double distP2 = App.getDistance(ballXY, P2);
        double distP1P2 = App.getDistance(P1, P2);

        if (distP1 + distP2 < ARTIFICIAL_RADIUS + distP1P2) {
            if (distP1P2 < 0.001f) {
                return P1;
            }
            // Calculate the collision point
            float t = (float) ((distP1 - ARTIFICIAL_RADIUS) / distP1P2);
            float collisionX = P1[0] + t * (P2[0] - P1[0]);
            float collisionY = P1[1] + t * (P2[1] - P1[1]);
            return new float[] {collisionX, collisionY};
        }
        return null;

    }


    /**
     * Calculates and sets the new direction of the ball after collision with a line.<br>
     *
     * @param line The Line object that the ball is colliding with.<br>
     * @return A float array representing the new direction vector [x, y], or null if no collision occurs.<br>
     *
     * Note: The method performs the following steps:<br>
     *
     * <p>1. Collision Check:</p>
     *    - Returns null if no collision is detected.<br>
     *    - Handles special case when the ball collides exactly at a dot to prevent zero division errors.<br>
     *
     * <p>2. Normal Vector Calculation.</p>

     * <p>3. Selects Closest  Normal Vector.</p>
     *    - Determines which normal vector is closer to the ball's current position.<br>
     *
     * <p>4. New Direction Calculation:</p>
     *    - Uses the selected normal to calculate the new direction vector.<br>
     *    - Applies the reflection formula: v' = v - 2(v · n)n<br>
     *      where v is the incoming vector, n is the normal, and v' is the reflected vector.<br>
     *
     * <p>5. Vector Update:</p>
     *    - Sets the ball's vector to the newly calculated direction.<br>
     *    - Returns the new direction vector.<br>
     */
    public float[] setNewDirection(Line line) {
        if (this.willCollide(line) == null) {
            return null;
        }

        // Handle zeroDivisionError and when ball is directly on a dot
        if (Arrays.equals(this.willCollide(line), line.getP1())) {
            this.vector = new float[] {-1*this.getVector()[0], -1*this.getVector()[1]};
            return new float[] {-1*this.getVector()[0], -1*this.getVector()[1]};
        }

        float[] P1 = line.getP1();
        float[] P2 = line.getP2();
        float dy = P2[1] - P1[1];
        float dx = P2[0] - P1[0];

        //CALCULATE NORMALS OF LINE
        float[] norm1 = new float[] {-1*dy, dx};
        float[] norm2 = new float[] {dy, -1*dx};
        ////normalise
        double mag1 = Math.sqrt(Math.pow(norm1[0], 2) + Math.pow(norm1[1], 2));
        double mag2 = Math.sqrt(Math.pow(norm2[0], 2) + Math.pow(norm2[1], 2));
        double[] normalised1 = new double[] {norm1[0] / mag1, norm1[1] / mag1}; // zeroDivisionError not handled
        double[] normalised2 = new double[] {norm2[0] / mag2, norm2[1] / mag2};

        //GET CLOSEST NORMAL
        double[] normUsed;
        float[] ballXY = new float[] {this.getBallCenter()[0], this.getBallCenter()[1]};
        float[] midpoint = line.getMidPoint();
        double[] n1Line = new double[] {midpoint[0] + normalised1[0], midpoint[1] + normalised1[1]};
        double[] n2Line = new double[] {midpoint[0] + normalised2[0], midpoint[1] + normalised2[1]};

        if (App.getDistance(n1Line, ballXY) < App.getDistance(n2Line, ballXY)) {
            normUsed = normalised1;
        }
        else {
            normUsed = normalised2;
        }

        //CALCULATE NEW DIRECTION VECTOR
        double vDotn = this.vector[0] * normUsed[0] + this.vector[1] * normUsed[1];
        double newDirectionX = this.vector[0] - 2 * vDotn * normUsed[0];
        double newDirectionY = this.vector[1] - 2 * vDotn * normUsed[1];

        this.vector = new float[] {(float)newDirectionX, (float)newDirectionY};
        return new float[] {(float)newDirectionX, (float)newDirectionY};
    }

    /**
     * Updates the ball's color based on the color of the line it collides with.<br>
     *
     * @param line The Line object that the ball is interacting with.<br>
     *
     * <p>This method changes the ball's color under the following conditions:</p>
     * - If the line's color (getColourTo) is 0, no change occurs.<br>
     * - If the line's color is between 1 and 4 (inclusive), the ball's color is set to match the line's color.<br>
     *
     * Note: This method assumes that the colour given follows the colour dictionary of Inkball.
     *           (0 = grey, 1 = orange, 2 = blue, 3 = green, 4 = yellow).
     */
    public void setNewColour(Line line) {
        if (line.getColourTo() == 0) {
            return;
        }
        if (1 <= line.getColourTo() && line.getColourTo() <= 4) {
            this.colour = line.getColourTo();
        }
    }

    /**
     * Handles the interaction between a ball and a hole in the game.<br>
     *
     * @param hole The Hole object that the ball is potentially interacting with.<br>
     * @param app The main App object, used for accessing game state and methods.<br>
     * @return boolean True if the ball interacts with the hole, false otherwise.<br>
     *
     * <p>This method performs the following actions:</p>
     * <ol>
     *   <li>Checks if the ball is already absorbed or if the hole is invalid.</li>
     *   <li>Calculates and applies an attraction vector from the hole to the ball.</li>
     *   <li>Shrinks the ball as it approaches the hole.</li>
     *   <li>When the ball is sufficiently close to the hole center:
     *     <ul>
     *       <li>If colors match: Increases score and removes the ball.</li>
     *       <li>If either color is grey (0): Increases score (less than color match) and removes the ball.</li>
     *       <li>If colors mismatch: Decreases score, removes the ball, and adds it back to the queue.</li>
     *     </ul>
     *    </li>
     * </ol>
     */
    public boolean meetHole(Hole hole, App app) {
        if (this.getIsAbsorbed()) {
            return false;
        }

        if (hole.getHoleCenter() == null) {
            return false;
        }

        float[] ballCenter = this.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();

        float[] attractionVector = this.getAttractionVector(hole);
        this.vector[0] += attractionVector[0];
        this.vector[1] += attractionVector[1];

        float shrinkFactor = (float) (App.getDistance(holeCenter, ballCenter) / 32);
        this.ballRadius = 12 * shrinkFactor; //make ball increase/decrease proportionally to its original radius

        if (this.ballRadius < 6) {
            this.ballRadius = 0;
            //this.isAbsorbed = true;

            if (this.getColour() == hole.getColour()) {
                App.scoreTemp += app.getScoreIncrease().get(hole.colourToString()) * app.modScoreIncrease;
                app.getBalls().remove(this);
                this.absorb();
            }

            else if (this.colour == 0 || hole.getColour() == 0) {
                App.scoreTemp += app.getScoreIncrease().get("grey") * app.getModScoreIncrease();
                app.getBalls().remove(this);
                this.absorb();
            }

            else {
                App.scoreTemp -= app.getScoreDecrease().get(hole.colourToString()) * app.getModScoreDecrease();
                app.getBalls().remove(this); // avoid concurrent modification
                for (int i = 0; i < app.ballQueue.length; i++) {
                    if (app.ballQueue[i] == null) {
                        int c = this.getColour();
                        app.ballQueue[i] = new Ball(19 + 28 * i, 21, c); // add back to queue
                        break;
                    }
                }
            }

        }
        return true;
    }

    /**
     * @return The centre coordinates of the Ball object.
     */
    public float[] getBallCenter() {
        return new float[] {this.x + this.ballRadius, this.y + this.ballRadius};
    }

    /**
     * Calculates the attraction vector between the ball and a given hole.<br>
     *
     * @param hole The Hole object that is attracting the ball.<br>
     * @return A float array representing the attraction vector [x, y].<br>
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Calculates the vector from the ball's center to the hole's center.</li>
     *   <li>Computes the magnitude of this vector.</li>
     *   <li>Handles the case when the ball is very close to the hole center:
     *     <ul>
     *       <li>If magnitude less than 3, sets the ball as absorbed and returns [0, 0].</li>
     *     </ul>
     *   </li>
     *   <li>Calculates the attraction speed based on the distance to the hole.</li>
     *   <li>Normalizes the attraction vector and scales it by the calculated speed.</li>
     * </ol>
     *
     * Note:
     * <p>Special considerations:</p>
     * <ul>
     *   <li>Prevents division by zero when the ball is very close to the hole center.</li>
     *   <li>Caps the attraction speed to MAX_SPEED to prevent excessive acceleration.</li>
     *   <li>Sets the ball as absorbed if it's extremely close to the hole center.</li>
     * </ul>
     *
     */
    public float[] getAttractionVector(Hole hole) {
        float[] ballCenter = this.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();
        float[] attractionVec = new float[] {holeCenter[0] - ballCenter[0], holeCenter[1] - ballCenter[1]};

        float mag = (float) (Math.sqrt(Math.pow(attractionVec[0], 2) + Math.pow(attractionVec[1], 2)));

        if (mag < 3) { //prevent zero division error
            this.isAbsorbed = true;
            this.ballRadius = 0;
            return new float[] {0, 0};
        }

        float speed = (float) (Math.min(MAX_SPEED, (App.getDistance(ballCenter, holeCenter) * 0.005f)));
        float attractionX = attractionVec[0] / mag * speed;
        float attractionY = attractionVec[1] / mag * speed;

        return new float[] {attractionX, attractionY};
    }

    /**
     * This returns a string representation for the colour of the Wall object.
     *
     * @return String representing the colour of the Wall object, which is represented by an integer:<br>
     *              0 = grey, 1 = orange, 2 = blue, 3 = green, 4 = yellow.<br>
     *
     * Note: If the Ball object has a colour attribute that is
     * not within the range, this method will return "grey" by default.
     */
    public String colourToString() {
        if (this.colour == 1) {
            return "orange";
        }
        if (this.colour == 2) {
            return "blue";
        }
        if (this.colour == 3) {
            return "green";
        }
        if (this.colour == 4) {
            return "yellow";
        }
        return "grey";
    }

}