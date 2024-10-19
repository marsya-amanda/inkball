package inkball;

import java.util.Arrays;
import java.util.Random;

/**
 * Represents user-drawn lines and all other (invisible/visible) surfaces that the ball could collide against,
 * such as the surfaces of walls.
 */
public class Line {
    private float[] P1;
    private float[] P2;
    public boolean isDrawn;
    private int colourTo;

    public Line(float[] P1, float[] P2, int colourTo, boolean isDrawn) {
        this.P1 = P1;
        this.P2 = P2;
        this.colourTo = colourTo;
        this.isDrawn = isDrawn;

    }

    /**
     * Compares this Line object to another object for equality. This method overrides the existing equals() method.<br>
     *
     * @param obj The object to compare with this Line.<br>
     * @return boolean True if the objects are equal, false otherwise.<br>
     *
     * <p>Two Line objects are considered equal if:</p>
     * <ol>
     *   <li>They are the same object reference.</li>
     *   <li>The other object is also a Line instance.</li>
     *   <li>They have the same 'isDrawn' status.</li>
     *   <li>They have the same 'colourTo' value.</li>
     *   <li>Their start points (P1) are equal.</li>
     *   <li>Their end points (P2) are equal.</li>
     * </ol>

     * <p>The method uses Arrays.equals() for comparing the endpoint coordinates,<br>
     * which performs a deep equality check on the float arrays.</p>
     */
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

    /**
     * This draws the Line object if isDrawn is true.
     *
     * @param app The App object which the line object will be drawn on.
     */
    public void draw(App app) {
        if (this.isDrawn) {
            app.line(this.P1[0], this.P1[1], this.P2[0], this.P2[1]);
            app.stroke(colourTo);
            app.strokeWeight(10);
        }

    }

    public float[] getP1() {
        return this.P1;
    }

    public float[] getP2() {
        return this.P2;
    }

    /**
     * Calculates and returns the midpoint of the line segment.<br>
     *
     * @return A float array representing the coordinates of the midpoint [x, y].
     */
    public float[] getMidPoint() {
        float midPointX = (this.P2[0] + this.P1[0]) / 2;
        float midPointY = (this.P2[1] + this.P1[1]) / 2;
        return new float[] {midPointX, midPointY};
    }

    public int getColourTo() {
        return this.colourTo;
    }

    /**
     * This returns a string representation of the Line object, used for testing.
     *
     * @return String representation of the Line object, in the format:<br>
     *          "P1 is <em>[x1, y1]</em>, P2 is <em>[x2, y2]</em>, drawn is <em>true/false</em>"
     */
    public String toString() {
        return "P1 is " + Arrays.toString(this.P1) + ", P2 is " + Arrays.toString(this.P2) + ", drawn is " + this.isDrawn;
    }

}