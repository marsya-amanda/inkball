package inkball;

import processing.core.PImage;

/**
 * Child class of Tile. Has no inward functionality or specific logic handling.
 */
public class Spawner extends Tile {
    public Spawner (int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite("entrypoint");
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }
}