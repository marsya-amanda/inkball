package inkball;

import processing.core.PImage;

public class Wall extends Tile {
    private final int colour;

    public Wall(int x, int y, int colour) {
        super(x, y);
        this.colour = colour;
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite("wall"+colour);
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }

    public int getColour() {
        return this.colour;
    }

    public String colourToString() {
        if (this.colour == 0) {
            return "grey";
        }
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