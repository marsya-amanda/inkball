package inkball;

import processing.core.PImage;

/**
 * Child class of Tile. Represents a "Blank" Tile.
 * Has no inward functionality or specific interaction with other components.
 */
public class Blank extends Tile {

    public Blank(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite("tile");
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }
}