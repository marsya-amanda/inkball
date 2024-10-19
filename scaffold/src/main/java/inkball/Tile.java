package inkball;

/**
 * Represents a tile on the game board.
 * This is an abstract parent class with an abstract draw() method.
 */
public abstract class Tile {
    protected int x;
    protected int y;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void draw(App app);

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

}