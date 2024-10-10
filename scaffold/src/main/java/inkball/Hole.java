package inkball;

import processing.core.PImage;
import java.util.Arrays;

public class Hole extends Tile {
    public int colour;
    public enum GridPosition {
        TL, BL, TR, BR
    }
    public GridPosition gridPosition;

    public Hole(int x, int y, int colour, GridPosition gridPosition) {
        super(x, y);
        this.colour = colour;
        this.gridPosition = gridPosition;
    }

    public static String toString(Hole hole) {
        String s = "Hole at %s, colour %d";
        return String.format(s, Arrays.toString(new float[] {hole.getX(), hole.getY()}), hole.colour);
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