package inkball;

import processing.core.PImage;
import java.util.*;

public class Ball {
    float x;
    float y;
    int colour;
    private float[] vector;
    private float ballRadius;
    private static final int ARTIFICIAL_RADIUS = 10;
    private static final int MAX_SPEED = 12;

    private boolean isAbsorbed;
    private int points;
    private static Random rand = new Random();

    public Ball(float x, float y, int colour) {
        this.x = x; // so it spawns in the middle of the tile/spawner
        this.y = y;
        this.colour = colour;
        if (Ball.rand.nextBoolean()) {
            this.vector = new float[] {2, -2};
        }
        else {
            this.vector = new float[] {-2, 2};
        }
        this.ballRadius = 12;
        this.isAbsorbed = false;
        this.points = 0;
    }

    public void draw(App app) {
        if (this.isAbsorbed) {
            return;
        }
        PImage ball = app.getSprite("ball"+colour);
        app.image(ball, this.x, this.y, this.ballRadius * 2, this.ballRadius * 2);
    }

    public float[] getVector() {
        return this.vector;
    }

    public float getBallRadius() {
        return this.ballRadius;
    }

    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public boolean getIsAbsorbed() {
        return this.isAbsorbed;
    }

    public void moveOne() {
        this.x += vector[0];
        this.y += vector[1];
    }

    public void interact(Line line, App app) {
        if (this.willCollide(line)) {
            //System.out.println("Collided!");
            this.setNewColour(line);
            this.setNewDirection(line);
            this.moveOne();
//            if (line.isDrawn) {
//                app.removeLine(new float[] {this.x, this.y}); // only removed by one ball
//            }
        }
    }

    public boolean willCollide(Line line) {
        float[] P1 = line.getP1();
        float[] P2 = line.getP2();
        float[] ballXY = new float[] {this.getBallCenter()[0] + this.vector[0], this.getBallCenter()[1] + this.vector[1]};

        //getDistance is good

        double distP1 = App.getDistance(ballXY, P1);
        double distP2 = App.getDistance(ballXY, P2);
        double distP1P2 = App.getDistance(P1, P2);

        return distP1 + distP2 < ARTIFICIAL_RADIUS + distP1P2;
    }

    public void setNewDirection(Line line) {
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
        double[] normalised1 = new double[] {norm1[0] / mag1, norm1[1] / mag1};
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
       // System.out.println(Arrays.toString(this.vector));
    }

    public void setNewColour(Line line) {
        if (line.getColourTo() == 0) {
            return;
        }
        if (1 <= line.getColourTo() && line.getColourTo() <= 4) {
            this.colour = line.getColourTo();
        }
    }

    public /*boolean*/ void meetHole(Hole hole) {
        if (this.isAbsorbed) {
            return;
        }

        if (hole.getHoleCenter() == null) {
            return;
        }

        float[] ballCenter = this.getBallCenter();
        float[] holeCenter = hole.getHoleCenter(); // used to use getX/Y() method, which returned inaccurate calculations
        //System.out.println(Arrays.equals(holeCenterGet, holeCenter));

        //System.out.println("Distance between ball and hole " + App.getDistance(ballCenter, holeCenter));

        float[] attractionVector = this.getAttractionVector(hole);
        this.vector[0] += attractionVector[0];
        this.vector[1] += attractionVector[1];

        float shrinkFactor = (float) (App.getDistance(holeCenter, ballCenter) / 32);
        this.ballRadius = 12 * shrinkFactor; //make ball increase/decrease proportionally to its original radius

        if (this.ballRadius < 1 || App.getDistance(ballCenter, holeCenter) < 2) {
            //System.out.println("ball suitable to absorb"); //does not pass
            this.ballRadius = 0;
            this.isAbsorbed = true;
            return/* false*/;
        }

        return/* true*/;
    }

    public float[] getBallCenter() {
        return new float[] {this.x + this.ballRadius, this.y + this.ballRadius};
    }

    public float[] getAttractionVector(Hole hole) {
        float[] ballCenter = this.getBallCenter();
        float[] holeCenter = hole.getHoleCenter();
        float[] attractionVec = new float[] {holeCenter[0] - ballCenter[0], holeCenter[1] - ballCenter[1]};

        float mag = (float) (Math.sqrt(Math.pow(attractionVec[0], 2) + Math.pow(attractionVec[1], 2)));

        if (mag == 0) { //prevent zero division error
            this.isAbsorbed = true;
            this.ballRadius = 0;
            return new float[] {0, 0};
        }

        float speed = (float) (Math.min(MAX_SPEED, (App.getDistance(ballCenter, holeCenter) * 0.005f)));
        float attractionX = attractionVec[0] / mag * speed;
        float attractionY = attractionVec[1] / mag * speed;

//        System.out.println("Normalised attraction vector is " + Arrays.toString(new float[] {attractionX, attractionY}));
//        System.out.println("Magnitude of attraction vector is " + mag);
//        System.out.println("Vector is " + Arrays.toString(this.vector));

        return new float[] {attractionX, attractionY};
    }
}