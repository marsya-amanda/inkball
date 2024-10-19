package inkball;

import processing.core.PImage;

public class Hole extends Tile {
    private int colour;
    /**
     * Defines enum called GridPosition for the position of the tile on a 2x2 space
     * TL=TopLeft, BL=BottomLeft, TR=TopRight, BR=BottomRight
     */
    public enum GridPosition {
        TL, BL, TR, BR
    }
    private GridPosition gridPosition;

    public Hole(int x, int y, int colour, GridPosition gridPosition) {
        super(x, y);
        this.colour = colour;
        this.gridPosition = gridPosition;
    }

    @Override
    public void draw(App app) {
        PImage tile;
        if (this.gridPosition == GridPosition.TL) {
            tile = app.getSprite("hole"+colour);
        }
        else {
            return;
        }
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR, App.CELLSIZE * 2, App.CELLSIZE * 2);
    }

    public int getColour() {
        return this.colour;
    }

    /**
     * Converts colour code of Hole into a string representation.
     * @return String, either 1="orange", 2="blue", 3="green", 4="yellow", 0="grey"
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

    /**
     * Gets the coordinates of the center of the hole.
     * @return float[] or null if invalid GridPosition
     */
    public float[] getHoleCenter() {
        if (this.gridPosition == GridPosition.TL) {
            return new float[] {(this.getX()+1)*App.CELLSIZE, (this.getY()+1)*App.CELLSIZE+App.TOPBAR};
        }
        else if (this.gridPosition == GridPosition.BL) {
            return new float[] {(this.getX()+1)*App.CELLSIZE, this.getY()*App.CELLSIZE+App.TOPBAR};
        }
        else if (this.gridPosition == GridPosition.TR) {
            return new float[] {this.getX()*App.CELLSIZE, (this.getY()+1)*App.CELLSIZE+App.TOPBAR};
        }
        else if (this.gridPosition == GridPosition.BR) { //BR
            return new float[] {this.getX()*App.CELLSIZE, this.getY()*App.CELLSIZE+App.TOPBAR};
        }
        return null;
    }
}