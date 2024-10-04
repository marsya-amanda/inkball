package inkball;

import processing.core.PImage;

public class Wall extends Tile {
    private final int colour;
    private int hp;

    public Wall(int x, int y, int colour) {
        super(x, y);
        this.colour = colour;
        this.hp = 3;
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite("wall"+this.colour);
        if (this.hp <= 3 && this.hp > 1) {
            tile = app.getSprite("wall"+this.colour);
        }
        else if (this.hp < 2) {
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

    public void damage(Ball ball) {
        if (this.colour == 0) {
            this.hp--;
            return;
        }
        if (this.colour == ball.getColour()) {
            this.hp--;
        }
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