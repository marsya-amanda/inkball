package inkball;

import processing.core.PImage;
import java.util.*;

public class Ball {
    private float x;
    private float y;
    int colour;
    private float[] vector;
    private float ballRadius = 12;
    public static final int ARTIFICIAL_RADIUS = 10;
    public static final int MAX_SPEED = 12;

    private boolean isAbsorbed;
    private static Random rand = new Random();

    public Ball(float x, float y, int colour) {
        this.x = x; // so it spawns in the middle of the tile/spawner
        this.y = y;
        if (colour < 0 || colour > 4) {
            this.colour = 0;
        }
        else {
            this.colour = colour;
        }
        if (Ball.rand.nextBoolean()) {
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

    public void interact(Line line) {
        if (this.willCollide(line) != null) {
            //System.out.println("Collided!");
            this.setNewColour(line);
            this.setNewDirection(line); // removed app arg
            this.moveOne();
        }
    }

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

    public void setNewColour(Line line) {
        if (line.getColourTo() == 0) {
            return;
        }
        if (1 <= line.getColourTo() && line.getColourTo() <= 4) {
            this.colour = line.getColourTo();
        }
    }

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
                App.score += App.scoreIncrease.get(hole.colourToString()) * app.modScoreIncrease;
                app.getBalls().remove(this);
                this.absorb();
            }

            else if (this.colour == 0 || hole.getColour() == 0) {
                App.score += App.scoreIncrease.get("grey") * app.modScoreIncrease;
                app.getBalls().remove(this);
                this.absorb();
            }

            else {
                App.score -= App.scoreDecrease.get(hole.colourToString()) * app.modScoreDecrease;
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

    public float[] getBallCenter() {
        return new float[] {this.x + this.ballRadius, this.y + this.ballRadius};
    }

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