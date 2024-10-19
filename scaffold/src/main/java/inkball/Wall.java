package inkball;

import processing.core.PImage;

/**
 * This is the child class of Tile. It is responsible for modifying its own attributes.
 * Methods added to implement the extension, Bricks.
 */
public class Wall extends Tile {
    private final int colour;
    /**
     * This represents the "health points" of the wall. Used to determine which image is used to draw the Wall.
     */
    private int hp;

    /**
     * Constructor for Wall object.
     * HP is set to default at 4, to allow for 3 hits/damage before it reaches 0 and is removed.
     * @param x The x-coordinate of the Wall, in units of 1 Cell size(=32)
     * @param y The y-coordinate of the Wall, in units of 1 Cell size, and only relative to the game board instead of app window.
     * @param colour The colour of the Wall, represented by an integer.
     *               0 = grey, 1 = orange, 2 = blue, 3 = green, 4 = yellow.<br>
     *
     * Note: This method assumes that the colour given follows the colour dictionary of Inkball.
     */
    public Wall(int x, int y, int colour) {
        super(x, y);
        this.colour = colour;
        this.hp = 4;
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite("wall"+this.colour);
        if (this.hp <= 4 && this.hp > 2) {
            tile = app.getSprite("wall"+this.colour);
        }
        else if (this.hp < 3) {
            tile = app.getSprite("wall"+this.colour+"-damaged");
        }
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }

    public int getColour() {
        return this.colour;
    }

    public int getHP() {
        return this.hp;
    }

    /**
     * Reduces the HP attribute value by 1 when:<br>
     * - The Wall colour is grey;<br>
     * - The Ball and Wall are the same colour.
     *
     * @param ball The Ball object that is colliding with the Wall object.
     */
    public void damage(Ball ball) {
        if (this.colour == 0) {
            this.hp--;
            return;
        }
        if (this.colour == ball.getColour()) {
            this.hp--;
        }
    }

    /**
     * This returns a string representation for the colour of the Wall object.
     *
     * @return String representing the colour of the Wall object, which is represented by an integer:<br>
     *              0 = grey, 1 = orange, 2 = blue, 3 = green, 4 = yellow.<br>
     *
     * Note: If the Wall object has a colour attribute that is
     * not within the range, this method will return "grey" by default.
     */
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