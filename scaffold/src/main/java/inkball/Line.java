package inkball;

import java.util.Arrays;
import java.util.Random;

public class Line {
    private float[] P1;
    private float[] P2;
    public boolean isDrawn;
    private int colourTo;
    //private final float slope;
    //private float intercept;

    /*public Line(float[] P1, float[] P2, boolean isDrawn) {
        this.P1 = P1;
        this.P2 = P2;
        this.isDrawn = isDrawn;
    }*/

    public Line(float[] P1, float[] P2, int colourTo, boolean isDrawn) {
        this.P1 = P1;
        this.P2 = P2;
        this.colourTo = colourTo;
        this.isDrawn = isDrawn;
        /*this.slope = (P2[1] - P1[1])/(P2[0] - P1[0]);
        this.intercept = this.P1[1] - this.P1[0] * this.slope;*/
    }

//    public static String toString(Line line) {
//        return "P1 is " + Arrays.toString(line.getP1()) + ", P2 is " + Arrays.toString(line.getP2());
//    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Line line = (Line) obj;
        return this.isDrawn == line.isDrawn &&
                this.colourTo == line.colourTo &&
                Arrays.equals(this.getP1(), line.getP1()) &&
                Arrays.equals(this.getP2(), line.getP2());
    }

    public void draw(App app) {
        /*if (this.getColorTo() == 0) {
            return;
        }*/

        if (this.isDrawn) {
            app.line(this.P1[0], this.P1[1], this.P2[0], this.P2[1]);

            Random rand = new Random();
            if (rand.nextBoolean()) {
                app.stroke(colourTo);
            }
            else {
                app.stroke(colourTo);
            }
            //app.stroke(0);
            app.strokeWeight(10);
        }

    }

    public float[] getP1() {
        return this.P1;
    }

    public float[] getP2() {
        return this.P2;
    }

    public float[] getMidPoint() {
        float midPointX = (this.P2[0] + this.P1[0]) / 2;
        float midPointY = (this.P2[1] + this.P1[1]) / 2;
        return new float[] {midPointX, midPointY};
    }

    public int getColourTo() {
        return this.colourTo;
    }

    public String toString() {
        return "P1 is " + Arrays.toString(this.P1) + ", P2 is " + Arrays.toString(this.P2) + ", drawn is " + this.isDrawn;
    }

    /*public float[] getSlopeIntercept(float x) {
        return new float[] {this.slope, this.intercept};
    }*/
}