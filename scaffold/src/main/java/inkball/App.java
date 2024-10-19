package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import com.google.gson.Gson;

import java.util.*;
import java.lang.*;
import java.io.*;

/**
 *  <p>This the main class which represents the game window,
 *  inheriting the PApplet class from the Processing library to handle drawing graphics. </p>
 *  The App instance stores data like the game board components, the current game level and time.
 *  This class is responsible for overall game logic such as
 *  real-time line-drawing, pause/win/lose/restart scenarios, and map modifications.
 * Protected modifiers are to adhere to encapsulation whilst allowing the corresponding
 * testing class AppTest to access relevant attributes.
 */
public class App extends PApplet {

    protected boolean isTesting = false;

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;

    public String configPath;
    public JSONObject json;

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Represents the game state mode. Used for game logic in App.
     */
    protected enum GameState {
        PLAYING, PAUSED, WIN, OVER
    }

    protected static int gameLevel = 1;
    protected int maxLevel = 1;

    protected GameState gameState = GameState.PLAYING;

    protected static int timeLimit = 0;
    protected static int lastSecond = 0;

    /**
     * Represents the working score of the current game session.
     * This gets reset to 0 when the game is restarted.
     */
    static double scoreTemp = 0.0d;

    /**
     * Represents the score accumulated from finished levels.
     * This does not get reset to 0 whem the game is restarted.
     */
    private static double scoreFinal = 0.0d;

    /**
     * Represents a HashMap which contains the score to be added for one correct ball capture, according to colour.
     */
    HashMap<String, Integer> scoreIncrease = new HashMap<>();


    /**
     * Represents a HashMap which contains the score to be added for one incorrect ball capture, according to colour.
     */
    HashMap<String, Integer> scoreDecrease = new HashMap<>();


    /**
     * Represents multiplier for every correct ball capture.
     */
    float modScoreIncrease = 1; // package-private for testing

    /**
     * Represents multiplier for every incorrect ball capture.
     */
    float modScoreDecrease = 1;

    /**
     * Represents the interval at which balls in the queue are spawned at. Unit in seconds.
     * Does not change throughout the game
     */
    protected int spawnInterval = 0;

    /**
     * Represents balls that are yet to be released into the game board.
     */
    Ball[] ballQueue; // package-private for testing and because it is meant to be changed
    protected int maxBallQueue;

    /**
     * Represents the current timer, relative to the spawn interval.
     */
    protected static float ballTimer = 1.0f;

    protected static boolean ctrlPressed = false;
    protected static boolean isDrawing = false;

    /**
     * Contains the starting coordinates of a mouse press/drag.
     * For line-drawing and line-grouping logic.
     */
    protected float[] start = new float[2];

    /**
     * Contains the ending coordinates of a mouse press/drag.
     * For line-drawing and line-grouping logic.
     */
    protected float[] end = new float[2];

    private final static int mouseRadius = 5;

    /**
     * Marks the frame at which the last line was drawn at.
     */
    protected int lastLine = 0;

    protected Tile[][] board;
    protected ArrayList<Ball> balls = new ArrayList<>();
    protected ArrayList<Wall> walls = new ArrayList<>();
    protected ArrayList<Line> allLines = new ArrayList<>();
    protected ArrayList<ArrayList<Line>> drawnLines = new ArrayList<>();

    /**
     * Represents the line that is currently being drawn.
     * Elements are added to other line collections, then cleared when the mouse is released.
     */
    protected ArrayList<ArrayList<Line>> tempLines = new ArrayList<>();
    protected ArrayList<Spawner> spawners = new ArrayList<>();
    protected ArrayList<Hole> holes = new ArrayList<>();
    protected HashMap<String, PImage> sprites = new HashMap<>();

    /**
     * Coordinates of the first tile that makes up the game win spiral animation.
     */
    protected int[] firstSpiral = new int[] {0, 0};

    /**
     * Coordinates of the second tile that makes up the game win spiral animation.
     */
    protected int[] secondSpiral = new int[] {WIDTH/CELLSIZE - 1, (HEIGHT-TOPBAR)/CELLSIZE - 1};

    /**
     * Gets a PImage object from sprite HashMap if exists,
     * adds a PImage object from the resource directory otherwise.
     * This method expects to always returns a PImage object.
     * @param s filename of resource
     * @return PImage object of the resource
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s);
        if (result == null) {
            result = loadImage(this.getClass().getResource(s+".png").getPath().toLowerCase(Locale.ROOT).replace("%20", " "));
            sprites.put(s, result);
        }
        return result;
    }

    public Tile[][] getBoard() {
        return this.board;
    }

    /**
     * @return ArrayList corresponding to one continuous line
     */
    public ArrayList<Line> getLineSegments() {
        return this.allLines;
    }

    public ArrayList<Ball> getBalls() {
        return this.balls;
    }

    public HashMap<String, Integer> getScoreIncrease() {
        return this.scoreIncrease;
    }

    public HashMap<String, Integer> getScoreDecrease() {
        return this.scoreDecrease;
    }

    public float getModScoreIncrease() {
        return this.modScoreIncrease;
    }

    public float getModScoreDecrease() {
        return this.modScoreDecrease;
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Initializes and sets up the game environment. This method overrides PApplet's setup method.<br>
     *
     * <p>This method performs the following major tasks:</p>
     * <ol>
     *   <li>Loads and processes configuration data from a JSON file.</li>
     *   <li>Sets up game parameters such as frame rate, time limit, and score modifiers.</li>
     *   <li>Initializes score increase and decrease mappings for different colors.</li>
     *   <li>Loads necessary sprite resources.</li>
     *   <li>Creates the game board.</li>
     *   <li>Sets up the initial ball queue based on configuration.</li>
     * </ol>
     */
	@Override
    public void setup() {
        if (!isTesting) {
            frameRate(FPS);
            this.json = loadJSONObject(configPath);
        }
        this.gameState = GameState.PLAYING;

        try {
            maxLevel = this.json.getJSONArray("levels").size();
        }
        catch (Exception e) {
            maxLevel = 1;
        }

        try {
            timeLimit = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getInt("time");
        }
        catch (Exception e) {
            timeLimit = 0;
        }

        if (timeLimit <= 0) {
            timeLimit = 0;
        }

        timeLimit += lastSecond; // add any remaining time
        lastSecond = timeLimit; //in seconds

        try {
            this.spawnInterval = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getInt("spawn_interval");
        }
        catch (Exception e) {
            this.spawnInterval = 1;
        }

        try {
            this.modScoreIncrease = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getFloat("score_increase_from_hole_capture_modifier");
        }
        catch (Exception e) {
            this.modScoreIncrease = 1; // Default value is no multiplier
        }

        try {
            this.modScoreDecrease = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getFloat("score_decrease_from_wrong_hole_modifier");
        }
        catch (Exception e) {
            this.modScoreDecrease = 1; // Default value is no multiplier
        }

        JSONObject scoreIncJSON = new JSONObject();
        try {
            scoreIncJSON = this.json.getJSONObject("score_increase_from_hole_capture");
        }
        catch (Exception e) {
            scoreIncJSON.put("grey", 50); // Set defaults
            scoreIncJSON.put("orange", 50);
            scoreIncJSON.put("blue", 50);
            scoreIncJSON.put("green", 50);
            scoreIncJSON.put("yellow", 50);
        }

        for (Object key : scoreIncJSON.keys()) {
            String keyStr = (String) key;
            Integer scoreInc = scoreIncJSON.getInt(keyStr);
            scoreIncrease.put(keyStr, scoreInc);
        }

        JSONObject scoreDecJSON = new JSONObject();
        try {
            scoreDecJSON = this.json.getJSONObject("score_decrease_from_wrong_hole");
        }
        catch (Exception e){
            scoreDecJSON.put("grey", 25); // Set defaults
            scoreDecJSON.put("orange", 25);
            scoreDecJSON.put("blue", 25);
            scoreDecJSON.put("green", 25);
            scoreDecJSON.put("yellow", 25);
        }

        for (Object key : scoreIncJSON.keys()) {
            String keyStr = (String) key;
            Integer scoreInc = scoreDecJSON.getInt(keyStr);
            scoreDecrease.put(keyStr, scoreInc);
        }

        
        // Get all sprites
        String[] sprites = new String[] {
                "entrypoint",
                "inkball_spritesheet",
                "tile"
        };

        for (String sprite : sprites) {
            this.getSprite(sprite);
        }

        for (int i = 0; i <= 4; i++) {
            this.getSprite("ball"+i);
            this.getSprite("hole"+i);
            this.getSprite("wall"+i);
            this.getSprite("wall"+i+"-damaged");
        }
        //

        // Create board
        this.board = new Tile[(HEIGHT - TOPBAR)/CELLHEIGHT][WIDTH/CELLSIZE];

        // Get layout
        this.setLayout();

        // ADD BALL QUEUE
        JSONArray ballsJSON = new JSONArray();
        try {
            ballsJSON = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getJSONArray("balls");
        }
        catch (Exception e) {
            List<String> balls = Arrays.asList("grey", "grey", "grey", "grey", "grey");
            Gson gson = new Gson();
            ballsJSON = JSONArray.parse(gson.toJson(balls));
        }

        this.maxBallQueue = ballsJSON.size() + this.balls.size();

        this.ballQueue = new Ball[this.maxBallQueue];

        for (int i = 0; i < ballsJSON.size(); i++) {
            int colour = this.colourToInt(ballsJSON.getString(i));
            this.ballQueue[i] = new Ball(19 + 28 * i, 21, colour);
        }
    }

    /**
     * Gets the layout file from the JSONObject json.
     *
     * @return the contents of the file in nested ArrayList of strings (if layout file exists), null otherwise.<br>
     *
     * Note: When testing, a dummy file is created instead with the name "for-texting.txt".
     * This file is deleted at the end of the method call.
     */
    public ArrayList<ArrayList<String>> getFileContents() {
        File JSONfile = null;
        try {
            JSONfile = new File(this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getString("layout"));
        }
        catch (Exception e) {
            System.out.println("JSON file not found");
            if (!this.isTesting) return null;
        }

        Scanner scan = null;
        if (!this.isTesting) {
            try {
                scan = new Scanner(JSONfile);
            } catch (FileNotFoundException e) {
                if (!this.isTesting) return null;
            }
        }

        else {
            JSONfile = new File("for-testing.txt");
            try {
                JSONfile.createNewFile();
            }
            catch (IOException e) {
                return null;
            }
            FileWriter fw = null;
            try {
                fw = new FileWriter("for-testing.txt");
            }
            catch (IOException e) {
                return null;
            }
            try {
                fw.write("XXH67  H");
                fw.close();
            }
            catch (IOException e) {
                return null;
            }
            JSONfile = new File("for-testing.txt");
            try {
                scan = new Scanner(JSONfile);
            }
            catch (FileNotFoundException e) {
                return null;
            }
        }

        ArrayList<ArrayList<String>> lines = new ArrayList<>();
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            ArrayList<String> lineChars = new ArrayList<>();
            for (int i = 0; i < line.length(); i++) {
                String c = Character.toString(line.charAt(i));
                lineChars.add(c);
            }
            lines.add(lineChars);
        }

        scan.close();

        if (this.isTesting) JSONfile.delete();

        return lines;
    }

    /**
     * Sets up the game board layout based on file contents and initializes game objects.<br>
     *
     * <p>This method performs the following major tasks:</p>
     * <ol>
     *   <li>Reads the layout file contents.</li>
     *   <li>Iterates through the board grid, creating game objects based on file symbols.</li>
     *   <li>Handles cases of insufficient file data by filling with blank tiles.</li>
     *   <li>Initializes walls, holes, spawners, and balls according to the layout.</li>
     *   <li>Adds border lines to the game board.</li>
     * </ol>
     *
     * Note: <p>Layout symbols and their meanings:</p>
     * <ul>
     *   <li>" " (space): Blank tile</li>
     *   <li>"X": Wall (color 0)</li>
     *   <li>"1"-"4": Colored walls</li>
     *   <li>"H": Hole (followed by color code)</li>
     *   <li>"S": Spawner</li>
     *   <li>"B": Ball (followed by color code)</li>
     * </ul>
     *
     * <p>Special considerations:</p>
     * <ul>
     *   <li>Handles cases where file data is shorter than expected board dimensions.</li>
     *   <li>Skips already populated board positions.</li>
     *   <li>Positions balls slightly offset from grid for centered appearance.</li>
     * </ul>
     */
    public void setLayout() {
        ArrayList<ArrayList<String>> lines = null;
        try {
            lines = this.getFileContents();
        }
        catch (Exception e) {
            System.exit(1);
        }

        //CREATE OBJECTS BASED ON FILE
        for (int i = 0; i < this.board.length; i++) {

            // Handle insufficient number of lines in file
            if (i + 1 > lines.size()) {
                for (int k = i; k < this.board.length; k++) {
                    for (int l = 0; l < this.board[0].length; l++) {
                        if (this.board[k][l] != null) {
                            continue;
                        }
                        this.board[k][l] = new Blank(l, k);
                    }
                }
                continue;
            }

            for (int j = 0; j < this.board[0].length; j++) {

                // HANDLE insufficient number of characters in a line
                if (j + 1 > lines.get(i).size()) {
                    for (int k = j; k < this.board[i].length; k++) {
                        if (this.board[i][k] != null) {
                            continue;
                        }
                        this.board[i][k] = new Blank(k, i); //skips the first box
                    }
                    break;
                }

                if (this.board[i][j] != null) { //if board has something in it, skip
                    continue;
                }

                //BLANK

                else if (lines.get(i).get(j).equals(" ")) {
                    this.board[i][j] = new Blank(j, i);
                }

                //WALL 0
                else if (lines.get(i).get(j).equals("X")) {
                    this.board[i][j] = new Wall(j, i, 0);
                    this.walls.add(new Wall(j, i, 0));
                }

                //WALL 1-4
                else if (lines.get(i).get(j).equals("1") || lines.get(i).get(j).equals("2") || lines.get(i).get(j).equals("3") || lines.get(i).get(j).equals("4")) {
                    //make above more succinct
                    int colour = Integer.parseInt(lines.get(i).get(j));
                    this.board[i][j] = new Wall(j, i, colour);
                    this.walls.add(new Wall(j, i, colour));
                }

                //HOLE
                else if (lines.get(i).get(j).equals("H")) {
                    if (this.getColourCode(lines, i, j) == -1) {
                        this.board[i][j] = new Blank(j, i);
                        this.board[i][j+1] = new Blank(j+1, i); //handle out of bounds
                    }
                    else {
                        int colour = this.getColourCode(lines, i, j);
                        for (int k = 0; k <= 1; k++) {
                            for (int l = 0; l <= 1; l++) {
                                Hole.GridPosition gp = this.getPosition(l, k);
                                /*if (gp == null) {
                                    this.board[i][j] = new Blank(j, i);
                                    break;
                                }*/
                                if (k == 1 && l == 1) {
                                    this.holes.add(new Hole (j+l, i+k, colour, gp)); // add the one w center coords
                                    //System.out.println("Added hole with center coords at " + gp + " " + Integer.toString(j+l) + ", " + (i+k));
                                    //System.out.println(Hole.toString(new Hole (j+l, i+k, colour, gp)));
                                }
                                this.board[i+k][j+l] = new Hole(j+l, i+k, colour, gp);
                            }
                        }
                    }
                }

                //SPAWNER
                else if (lines.get(i).get(j).equals("S")) {
                    this.board[i][j] = new Spawner(j, i);
                    //this.balls.add(new Ball(j*CELLSIZE, i*CELLSIZE+TOPBAR, 0));
                    this.spawners.add(new Spawner(j, i));
                }

                //BALL
                else if (lines.get(i).get(j).equals("B")) {
                    this.board[i][j] = new Blank(j, i);
                    if (this.getColourCode(lines, i, j) != -1) {
                        int colour = this.getColourCode(lines, i, j);
                        this.balls.add(new Ball(j*CELLSIZE + 4, i*CELLSIZE+TOPBAR + 4, colour)); //add 4 so spawns in the middle
                    }
                }

                else {
                    this.board[i][j] = new Blank(j, i);
                }
            }
        }

        // ADD LINES FROM WALLS
        this.addBorders();

    }

    /**
     * Restarts the game. Clears existing elements and resets the score to 0
     * or the score from the previous level when applicable.
     */
    public void restart() {
        frameCount = 0;
        lastSecond = 0;
        ballTimer = 1;
        this.balls = new ArrayList<>();
        this.allLines = new ArrayList<>();
        this.drawnLines = new ArrayList<>();
        this.tempLines = new ArrayList<>();
        this.spawners = new ArrayList<>();
        this.holes = new ArrayList<>();
        this.lastLine = 0;
        isDrawing = false;
        this.ballQueue = new Ball[0];

        if (gameLevel > this.json.getJSONArray("levels").size()) {
            gameLevel = 1;
        }

        setup();
    }

    /**
     * Modifies the input coordinates so that it follows a clockwise spiral.
     * This method will always return an integer array
     * @param spiral the xy-coordinate position of the object meant to move in a spiral.
     * @return integer array of coordinates after being moved in the spiral direction.
     */
    public int[] moveSpiral(int[] spiral) {
        if (spiral[1] == 0 && spiral[0] < this.board[0].length - 1) {
            spiral[0]++;
        }
        else if (spiral[0] == this.board[0].length - 1 && spiral[1] < this.board.length - 1) {
            spiral[1]++;
        }
        else if (spiral[1] == this.board.length - 1 && spiral[0] <= this.board[0].length - 1 && spiral[0] > 0) {
            spiral[0]--;
        }
        else if (spiral[0] == 0 && spiral[1] <= this.board.length - 1 && spiral[1] > 0) {
            spiral[1]--;
        }
        return spiral;
    }

    /**
     * Manages the spawning of balls in the game based on a queue system.<br>
     *
     * This method is responsible for:
     * <ol>
     *     <li>Checking if there are balls in the queue to spawn.</li>
     *  <li>Spawning a new ball at regular intervals from a random spawner.</li>
     *  <li>Managing the ball queue after spawning.</li>
     * </ol>
     * The method uses frameCount and spawnInterval to determine when to spawn a new ball.
     * Balls are spawned from random spawner locations.
     * After spawning, the queue is reorganized, removing the spawned ball and shifting others.
     * Returns early if ballQueue has not been properly initialised or is empty (ballQueue[0] is null).
     */
    public void spawnBalls() {
        if (this.ballQueue.length == 0) {
            return;
        }

        if (this.ballQueue[0] == null) {
            return;
        }

        if (frameCount % (this.spawnInterval * FPS) == 0) {
            ballTimer = 1.1f;

            int colour = this.ballQueue[0].getColour();
            Random rand = new Random();
            int i = rand.nextInt(this.spawners.size());
            Spawner spawner = this.spawners.get(i);
            this.balls.add(new Ball(spawner.getX() * CELLSIZE + 4, spawner.getY() * CELLSIZE + TOPBAR + 4, colour));
            this.ballQueue[0] = null;

            if (this.ballQueue.length > 1) {
                ArrayList<Ball> tempQueue = new ArrayList<>();
                for (Ball ball : this.ballQueue) {
                    if (ball != null) {
                        tempQueue.add(ball);
                    }
                }

                this.ballQueue = new Ball[this.maxBallQueue]; // make queue null
                for (int j = 0; j < tempQueue.size(); j++) {
                    this.ballQueue[j] = tempQueue.get(j);
                }

                for (int j = 0; j < this.ballQueue.length - 1; j++) {
                    if (this.ballQueue[j] == null) {
                        return;
                    }
                    int c = this.ballQueue[j].getColour();
                    this.ballQueue[j] = new Ball(19 + 28 * j, 21, c); // move ball coordinates
                }
            }
        }
    }

    /**
     * Returns the integer representation of the input using the colour scale of Inkball:
     * grey = 0, orange = 1, blue = 2, green = 3, yellow = 4, others = 0.
     * @param colourStr string representation of a colour
     * @return integer representation of colourStr
     */
    public int colourToInt(String colourStr) {
        if (colourStr.equals("grey")) {
            return 0;
        }
        if (colourStr.equals("orange")) {
            return 1;
        }
        if (colourStr.equals("blue")) {
            return 2;
        }
        if (colourStr.equals("green")) {
            return 3;
        }
        if (colourStr.equals("yellow")) {
            return 4;
        }
        return 0;
    }

    /**
     *Adds border lines to the game area and wall segments for all Wall tiles. <br>
     * This method performs two main tasks:
     * <ol><li>Creates four invisible border lines around the entire game area.</li>
     * <li>Adds line segments for each Wall tile in the game board.</li></ol>
     * Border lines are created slightly outside the visible game area (32 pixels)
     * to ensure complete coverage. These lines are invisible (isDrawn = false)
     * and have a color code of 0.
     * For each Wall tile in the board, addWallLineSegment() is called to add
     * its individual line segments.
     */
    public void addBorders() {

        float[] topLeftCorner = new float[] {-32, TOPBAR-32}; // H0, V0
        float[] topRightCorner = new float[] {WIDTH+32, TOPBAR-32}; // H1, V0
        float[] bottomLeftCorner = new float[] {-32, HEIGHT+32}; // H0, V1
        float[] bottomRightCorner = new float[] {WIDTH+32, HEIGHT+32}; // H1, V1

        boolean isDrawn = false;
        this.allLines.add(new Line(topLeftCorner, topRightCorner, 0, isDrawn)); //TOP
        this.allLines.add(new Line(topLeftCorner, bottomLeftCorner, 0, isDrawn)); //LEFT
        this.allLines.add(new Line(bottomLeftCorner, bottomRightCorner, 0, isDrawn)); // BOTTOM
        this.allLines.add(new Line(bottomRightCorner, topRightCorner, 0, isDrawn)); //RIGHT

        // line for non-border tiles
        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[0].length; j++) {
                if (this.getBoard()[i][j].getClass() == Wall.class) { //null pointer?
                    Wall wall = (Wall) this.getBoard()[i][j];
                    this.addWallLineSegment(wall);
                }
            }
        }

    }

    /**
     * Gets the line segments representing the top, left, bottom, and right sides of the wall
     * and adds the segments to app.allLines
     * @param wall The Wall object for which to generate line segments.
     */
    public void addWallLineSegment(Wall wall) {
        this.allLines.addAll(Arrays.asList(this.getWallLineSegments(wall)));
    }

    /**
     * Generates an array of Line objects representing the four sides of a Wall.
     *
     * @param wall The Wall object for which to generate line segments.
     * @return An array of four Line objects representing the top, left, bottom, and right sides of the wall.
     *         Each Line object contains:<br>
     *         - Start and end coordinates adjusted for cell size and top bar offset<br>
     *         - The color of the wall<br>
     *         - A boolean indicating if the line is drawn (set to false by default)
     **/
    public Line[] getWallLineSegments(Wall wall) {
        Line[] lines = new Line[4];

        float[] topLeftCorner = new float[] {wall.getX()*CELLSIZE, wall.getY() * CELLSIZE + TOPBAR}; // H0, V0
        float[] topRightCorner = new float[] {(wall.getX()+1)*CELLSIZE, wall.getY() * CELLSIZE + TOPBAR}; // H1, V0
        float[] bottomLeftCorner = new float[] {wall.getX()*CELLSIZE, (wall.getY()+1) * CELLSIZE + TOPBAR}; // H0, V1
        float[] bottomRightCorner = new float[] {(wall.getX()+1)*CELLSIZE, (wall.getY()+1)*CELLSIZE + TOPBAR};

        int colour = wall.getColour();
        boolean isDrawn = false;
        lines[0] = new Line(topLeftCorner, topRightCorner, colour, isDrawn); //TOP
        lines[1] = new Line(topLeftCorner, bottomLeftCorner, colour, isDrawn); //LEFT
        lines[2] = new Line(bottomLeftCorner, bottomRightCorner, colour, isDrawn); // BOTTOM
        lines[3] = new Line(bottomRightCorner, topRightCorner, colour, isDrawn);

        return lines;
    }

    /**
     * Retrieves the color code from a 2D ArrayList of Strings at the specified position.
     *
     * @param lines The 2D ArrayList containing color information.
     * @param i The row index in the ArrayList.
     * @param j The column index in the ArrayList. The color code is expected to be at j+1.
     * @return The color code as an integer. Returns -1 if:
     *         - The specified position is out of bounds
     *         - The color string is empty
     *         - The color string cannot be parsed as an integer
     *         - The parsed integer is not in the range 0-4
     * @throws IndexOutOfBoundsException if the specified indices are out of range (caught internally)
     * @throws NumberFormatException if the color string cannot be parsed as an integer (caught internally)
     */
    public int getColourCode (ArrayList<ArrayList<String>> lines, int i, int j) {
        String colour = " ";
        try {
            colour = lines.get(i).get(j+1);
        }
        catch (IndexOutOfBoundsException e) {
            return -1;
        }

        if (colour.equals(" ")) {
            return -1;
        }

        int colourCode = -1;
        try {
            colourCode = Integer.parseInt(colour);
        }
        catch (NumberFormatException e) {
            return -1;
        }
        if (colourCode < 0 || colourCode > 4) {
            return -1;
        }
        return colourCode;
    }

    /**
     * Determines the grid position of a hole based on given coordinates.
     *
     * @param k The x-coordinate in a 2x2 grid (0 or 1)
     * @param l The y-coordinate in a 2x2 grid (0 or 1)
     * @return A Hole.GridPosition enum value representing the position:
     *         - TL (TopLeft) for (0,0)
     *         - TR (TopRight) for (1,0)
     *         - BL (BottomLeft) for (0,1)
     *         - BR (BottomRight) for (1,1)
     *         Returns null if the coordinates are out of the 2x2 range.
     */
    public Hole.GridPosition getPosition(int k, int l) {
        if (k == 0 && l == 0) {
            return Hole.GridPosition.TL;
        }
        else if (k == 0 && l == 1) {
            return Hole.GridPosition.BL;
        }
        else if (k == 1 && l == 0) {
            return Hole.GridPosition.TR;
        }
        else if (k == 1 && l == 1) {
            return Hole.GridPosition.BR;
        }
        else {
            return null;
        }
    }

    /**
     * Overloaded method: Determines the distance between two points with float-coordinates.
     *
     * @param P1 The coordinates of the first point.
     * @param P2 The coordinates of the second point.
     * @return A double value representing the distance between both points.
     *
     * @throws IllegalArgumentException if:
     * <ul>
     *     <li>P1 is null and/or;</li>
     *     <li>P2 is null and/or;</li>
     *     <li>P1 is not in xy-coordinate format and/or;</li>
     *     <li>P2 is not in xy-coordinate.</li>
     * </ul>
     */
    public static double getDistance(float[] P1, float[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        return Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
    }

    /**
     * Overloaded method: Determines the distance between two points, the first with double-coordinates
     * and the second with float-coordinates
     *
     * @param P1 The coordinates of the first point.
     * @param P2 The coordinates of the second point.
     * @return A double value representing the distance between both points.
     *
     * @throws IllegalArgumentException if:
     *          - P1 is null and/or;
     *          - P2 is null and/or;
     *          - P1 is not in xy-coordinate format and/or;
     *          - P2 is not in xy-coordinate format.
     */
    public static double getDistance(double[] P1, float[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        return Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
    }

    /**
     * Adds user-drawn lines to tempLines.
     * <p>Does not add the line if line is drawn on the window top bar.
     * Adds line segment to the most recent line group in tempLines.</p>
     *
     * @param line The line segment to be added to tempLines.
     *
     */
    public void addDrawnLine(Line line) {
        if (line.getP1()[1] < TOPBAR || line.getP2()[1] < TOPBAR) {
            return;
        }

        if  (this.tempLines.isEmpty()) {
            this.tempLines.add(new ArrayList<>());
        }

        this.tempLines.get(this.tempLines.size() - 1).add(line);
    }

    /**
     * Overloaded method: Removes a line from the game based on the given coordinates.
     *
     * @param toRemove An array of floats representing the coordinates of the point to remove.
     *                 Expected to be in the format [x, y].<br>
     *
     * Note: The method searches for a line that contains the given point, starting from the most
     * recently drawn lines (end of the list) and moving backwards. Once found, it removes
     * the entire list containing that line from both drawnLines and tempLines.
     *
     * If no line is found containing the given point, the method returns without making any changes.
     */
    public void removeLine(float[] toRemove) {
        ArrayList<Line> removedLine = new ArrayList<>();

        outerLoop:
        for (int i = (this.drawnLines.size() - 1); i >= 0; i--) {
            for (int j = (this.drawnLines.get(i).size() - 1); j >= 0; j--) {
                if (this.mouseOnLine(toRemove, this.drawnLines.get(i).get(j).getP1(), this.drawnLines.get(i).get(j).getP2())) {
                    removedLine = this.drawnLines.get(i);
                    break outerLoop;
                }
            }
        }

        if (removedLine.isEmpty()) {
            return;
        }

        this.tempLines.remove(removedLine);
        this.drawnLines.remove(removedLine);
    }

    /**
     * Removes a specific line and its associated group from the game.
     *
     * @param toRemove The Line object to be removed.<br>
     *
     * Note: The method searches for the specified line in drawnLines, starting from the most
     * recently drawn lines (end of the list) and moving backwards. Once found, it removes
     * the entire group (List) containing that line from both drawnLines and tempLines.
     *
     * If the specified line is not found, the method returns without making any changes.
     */
    public void removeLine(Line toRemove) {
        ArrayList<Line> removedLine = new ArrayList<>();

        outerLoop:
        for (int i = this.drawnLines.size() - 1; i >= 0; i--) {
            for (int j = (this.drawnLines.get(i).size() - 1); j >= 0; j--) {
                if (this.drawnLines.get(i).get(j).equals(toRemove)) {
                    removedLine = this.drawnLines.get(i);
                    break outerLoop;
                }
            }
        }

        if (removedLine.isEmpty()) {
            return;
        }

        this.tempLines.remove(removedLine);
        this.drawnLines.remove(removedLine);
    }

    /**
     * Determines if the mouse is on or near a line segment.
     *
     * @param mouseXY An array of two floats representing the mouse coordinates [x, y].
     * @param lineP1 An array of two floats representing the coordinates of one endpoint of the line segment [x1, y1].
     * @param lineP2 An array of two floats representing the coordinates of the other endpoint of the line segment [x2, y2].
     * @return true if the mouse is considered to be on the line segment, false otherwise.
     *
     */
    public boolean mouseOnLine(float[] mouseXY, float[] lineP1, float[] lineP2) {
        double distP1 = App.getDistance(mouseXY, lineP1);
        double distP2 = App.getDistance(mouseXY, lineP2);
        double distP1P2 = App.getDistance(lineP1, lineP2);

        return distP1 + distP2 < mouseRadius + distP1P2; //mouse radius is 5
    }

    /**
     * Determines the walls associated with a potential collision between a ball and a line.
     *
     * @param ball The Ball object involved in the potential collision.
     * @param line The Line object involved in the potential collision.
     * @return An array of Wall objects. The first element is the closest wall to the collision point from app.walls,
     *         and the second element is the wall at the same board position as the closest wall.
     *         Returns null if no collision is detected or if no associated walls are found.
     *
     * The method first checks for a collision using the ball's willCollide method.
     * If a collision is detected, it finds the closest wall to the collision point.
     * It then attempts to find a second wall at the same board position as the closest wall.
     */
    public Wall[] getWallAssociated(Ball ball, Line line) {
        float[] collisionPoint = ball.willCollide(line);
        if (collisionPoint == null) {
            return null;
        }

        //System.out.println("collision point found"); //here now
        Wall[] wallsAssociated = new Wall[2];
        Wall closestWall = null;
        float minDistance = Float.MAX_VALUE;

        for (Wall wall : this.walls) {
            float wallCenterX = wall.getX() * CELLSIZE + CELLSIZE / 2;
            float wallCenterY = wall.getY() * CELLSIZE + TOPBAR + CELLSIZE / 2;
            float distance = (float) App.getDistance(collisionPoint, new float[]{wallCenterX, wallCenterY});

            if (distance < minDistance) {
                minDistance = distance; //find wall that is closest
                closestWall = wall;
            }
        }

        if (closestWall == null) {
            return null;
        }

        wallsAssociated[0] = closestWall;

        try {
            wallsAssociated[1] = (Wall) (this.board[wallsAssociated[0].getY()][wallsAssociated[0].getX()]);
            return wallsAssociated;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Removes a specified wall from the game, including its line segments and board representation.
     *
     * @param wall The Wall object to be removed from the game.
     *
     * The method performs the following steps:
     * 1. Retrieves the line segments associated with the wall.
     * 2. Searches for these line segments in the allLines list.
     * 3. If found, removes the line segments from allLines.
     * 4. Removes the wall from the walls collection.
     * 5. Replaces the wall in the board array with a new Blank object.
     */
    public void removeWall (Wall wall) {
        List<Line> linesToRemove = Arrays.asList(this.getWallLineSegments(wall));
        int removeInd = -1;
        //System.out.println(linesToRemove);
        boolean removed = false;
        for (int i = 0; i < this.allLines.size() - 4; i++) {
            if (this.allLines.subList(i, i + 4).equals(linesToRemove)) { // +4 because upper bound exclusive
                removeInd = i;
                removed = true;
                break;
            }
        }
        if (removed) {
            this.allLines.subList(removeInd, removeInd + 4).clear();
            this.walls.remove(wall);
            this.board[wall.getY()][wall.getX()] = new Blank(wall.getX(), wall.getY());
        }
    }

    /**
     * Handles key press events in the game. This method overrides the keyPressed method from the parent PApplet class.
     *
     * @param event The KeyEvent object containing information about the key press.<br>
     *
     * <p>The method performs the following actions based on key presses:
     * - 'r' key (code 82): Restarts the game, resets timer, and sets game state to PLAYING.
     * - Spacebar (code 32): Toggles between PAUSED and PLAYING states.
     * - Ctrl key (code 17): Sets ctrlPressed flag to true.</p>
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (event.getKeyCode() == 82) { //is "r"
            lastSecond = 0;
            gameState = GameState.PLAYING;
            scoreTemp = scoreFinal;
            restart();
        }
        if (event.getKeyCode() == 32) {
            if (gameState == GameState.PAUSED) {
                gameState = GameState.PLAYING;
            }
            else if (gameState == GameState.PLAYING) {
                gameState = GameState.PAUSED;
            }
        }

        if (event.getKeyCode() == 17) { // if control
            ctrlPressed = true;
        }
    }

    /**
     * Handles key released events in the game. This method overrides the
     * keyReleased method from the parent PApplet class. <br>
     *
     * The method sets ctrlPressed flag to true when the Ctrl key (code 17) is released.
     */
	@Override
    public void keyReleased(){
        if (keyCode == 17) {
            ctrlPressed = false;
        }
    }

    /**
     * Handles mouse press events in the game. This method overrides the
     * mousePressed method from the parent PApplet class. <br>
     *
     * @param e The MouseEvent object containing information about the mouse press.<br>
     *<br>
     * The method performs the following actions based on mouse presses:
     * 1. Left click (button 37):
     * <ul>
     *          <li>With Ctrl modifier: Removes a line at the clicked position.</li>
     *          <li>Without Ctrl: Starts or continues drawing a line.</li>
     * </ul>
     * 2. Right click (button 39): Removes a line at the clicked position.<br>
     * The method only processes mouse events if the game state is PLAYING.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        // create a new player-drawn line object
        if (gameState != GameState.PLAYING) {
            return;
        }

        int mouseX = e.getX();
        int mouseY = e.getY();

        if (e.getButton() == 37) { // Button is left
            if (e.getModifiers() == 2) { // Modifier is CTRL
                float[] toRemove = new float[] {mouseX, mouseY};

                this.removeLine(toRemove);
                return;
            }

            if (!isDrawing) {
                this.start = new float[] {mouseX, mouseY};
                //this.drawnLines.add(new ArrayList<>());
                this.tempLines.add(new ArrayList<>());
            }

            else if (this.lastLine == 0 || frameCount - this.lastLine == 1) {
                this.end = new float[] {mouseX, mouseY};
            }

            isDrawing = true;
            this.lastLine = frameCount;
        }

        else if (e.getButton() == 39) {
            float[] toRemove = new float[] {mouseX, mouseY};

            this.removeLine(toRemove);
        }

    }

    /**
     * Handles mouse drag events in the game. This method overrides the mouseDragged method from the parent PApplet class.
     *
     * @param e The MouseEvent object containing information about the mouse drag.<br><br>
     *
     * The method performs the following actions:<br>
     * - Only processes events if the game state is PLAYING.<br>
     * For left mouse button drags (button 37):<br>
     *   - Continues drawing a line if already drawing.<br>
     *   - Starts drawing a new line if not already drawing.<br>
     *   - Updates the end point of the current line segment.<br>
     *   - Adds a new line segment if enough frames have passed since the last segment.<br><br>
     *
     * The method uses the addDrawnLine method to add new line segments.
     */
	@Override
    public void mouseDragged(MouseEvent e) {
        if (gameState != GameState.PLAYING) {
            return;
        }

		// remove player-drawn line object if right mouse button is held
		// and mouse position collides with the line
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (e.getButton() == 37) { //Left button dragged
            if (mouseX == this.start[0] && mouseY == this.start[1]) {
                return;
            }

            else if (isDrawing) {
                //note handle null
                this.end = new float[] {mouseX, mouseY};

                if (frameCount - this.lastLine == 1 || this.lastLine == 0) {
                    this.lastLine = frameCount;

                    this.addDrawnLine(new Line(start, end, 0, true));
                    this.start = new float[] {mouseX, mouseY};
                }
            }

            else {
                isDrawing = true;
                this.lastLine = frameCount;
                this.start = new float[] {mouseX, mouseY};
            }
        }
    }

    /**
     * Handles mouse release events in the game. This method overrides the mouseReleased method from the parent class.
     *
     * @param e The MouseEvent object containing information about the mouse release. <br>
     *
     * <p>The method performs the following actions:
     * - Only processes events if the game state is PLAYING.<br>
     * For left mouse button releases:<br>
     *   - Finalizes the current line being drawn if isDrawing is true.<br>
     *   - Adds the completed line to drawnLines.<br>
     *   - Updates lastLine with the current frameCount.<br>
     *   - Sets isDrawing to false, ending the drawing process.</p>
     *
     * The method uses the addDrawnLine method to add the final line segment.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameState != GameState.PLAYING) {
            return;
        }

        int mouseX = e.getX();
        int mouseY = e.getY();

        if (e.getButton() == LEFT) {
            if (isDrawing) {
                this.end = new float[]{mouseX, mouseY};
                this.addDrawnLine(new Line(this.start, this.end, 0, true));
                //System.out.println("just drew a line! current number of lines: " + this.drawnLines.size());
                //this.allLines.addAll(this.drawnLines.get(this.drawnLines.size() - 1)); //other way
                if (!this.tempLines.isEmpty()) {
                    this.drawnLines.add(this.tempLines.get(this.tempLines.size() - 1));
                }
                this.lastLine = frameCount;
                isDrawing = false;
            }
        }
    }

    /**
     * Executes main game logic and facilitates interaction between game components.<br><br>
     * Methods used: Wall::damage, Ball::willCollide, Ball::interact, Ball::meetHole, Ball:: moveOne,
     * removeLine, removeWall, getWallAssociated
     */
    public void drawAll() {
        for (int i = this.balls.size() - 1; i >= 0; i--) {
            Ball ball = this.balls.get(i);
            ball.draw(this);

            boolean hasCollided = false;
            for (int j = this.allLines.size() - 1; j >= 0; j--) {
                Line line = this.allLines.get(j);
                if (ball.willCollide(line) != null) {
                    hasCollided = true;
                    if (this.getWallAssociated(ball, line) != null) {
                        Wall[] wallsAssociated = this.getWallAssociated(ball, line);
                        (wallsAssociated[0]).damage(ball);
                        (wallsAssociated[1]).damage(ball);
                        if (wallsAssociated[0].getHP() == 0) {
                            //System.out.println("removed wall");
                            this.removeWall(wallsAssociated[0]); // not REMOVING LINES, removing wrong walls
                        }
                        //ball.interact(line, this);
                    }
                    ball.interact(line);
                    break;
                }

            }

            outerLoop:
            for (int j = this.drawnLines.size() - 1; j >= 0; j--) {
                for (int k = this.drawnLines.get(j).size() - 1; k >= 0; k--) {
                    if (ball.willCollide(this.drawnLines.get(j).get(k)) != null) {
                        ball.interact(this.drawnLines.get(j).get(k));
                        hasCollided = true;
                        this.removeLine(this.drawnLines.get(j).get(k));
                        break outerLoop;
                    }
                }
            }

            if (!hasCollided) {
                ball.moveOne();
            }

            //Interact with line first
            for (Hole hole : this.holes) {
                if (getDistance(ball.getBallCenter(), hole.getHoleCenter()) < 32) {
                    ball.meetHole(hole, this);
                    break;
                }
                else {
                    ball.setBallRadius(12);
                }
            }
        }
    }


    /**
     * Draw all elements in the game by current frame. Draws timer and score components. Calls the drawAll method.
     * Calls spawnBalls and handles different gameState scenarios, including the win spiral animation.
     */
	@Override
    public void draw() {
        if (!isTesting) {
            background(206);
            this.spawnBalls();
        }

        //----------------------------------
        //display Board for current level:
        //----------------------------------
        //TODO

        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                this.board[i][j].draw(this);
            }
        }


        if (gameState == GameState.WIN) {
            textSize(30);
            fill(0);
            textAlign(CENTER, CENTER);
            //frameCount = (timeLimit - lastSecond) * 30;
            Wall firstSpiral = new Wall(this.firstSpiral[0], this.firstSpiral[1], 4);
            Wall secondSpiral = new Wall(this.secondSpiral[0], this.secondSpiral[1], 4);
            firstSpiral.draw(this);
            secondSpiral.draw(this);
            if ((frameCount - lastSecond * FPS) % 2 == 0) {
                this.moveSpiral(this.firstSpiral);
                this.moveSpiral(this.secondSpiral);
            }
        }

        //----------------------------------
        // display top bar
        //----------------------------------
        // 1) Timer
        if (timeLimit != 0) {
            if (frameCount - (timeLimit - lastSecond) * 30 == FPS && gameState == GameState.PLAYING) {
                lastSecond--;
                if (this.ballQueue[0] != null) {
                    ballTimer -= 0.1f;
                }
                else {
                    ballTimer = 1.1f;
                }
            }
            if (gameState == GameState.PAUSED) {
                frameCount = (timeLimit - lastSecond) * 30;
            }
            if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                textSize(22);
                fill(0);
                textAlign(CENTER, CENTER);
                text("Time: " + lastSecond, WIDTH-80, App.TOPBAR-22);
            }
        }

        // 2) Score
        if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            textSize(22);
            fill(0);
            textAlign(CENTER, CENTER);
            text("Score: " + (int) scoreTemp, WIDTH - 80, App.TOPBAR - 44);
        }

        // if PAUSED
        if (gameState != GameState.PLAYING) {
            textSize(22);
            fill(0);
            textAlign(CENTER, CENTER);
            if (gameState == GameState.PAUSED) {
                text("*** PAUSED ***", WIDTH / 2, TOPBAR / 2);
            }
            else if (gameState == GameState.WIN && gameLevel > maxLevel) {
                text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);
            }
            else if (gameState == GameState.OVER) {
                text("=== TIME'S UP ===", WIDTH / 2, TOPBAR / 2);
            }

            for (Ball ball : this.balls) {
                ball.draw(this);
            }

            for (ArrayList<Line> line : this.drawnLines) {
                for (Line l : line) {
                    l.draw(this);
                }
            }

            for (Line line : this.allLines) {
                line.draw(this);
            }

            if (gameState == GameState.WIN && gameLevel <= maxLevel) {
                if (Arrays.equals(this.firstSpiral, new int[]{this.board[0].length - 1, this.board.length - 1}) && Arrays.equals(this.secondSpiral, new int[]{0, 0})) {
                    restart();
                }
            }
        }

        if (gameState == GameState.WIN || gameState == GameState.OVER) {
            return;
        }

        // 3) Ball queue
        strokeWeight(0);
        rect(14, 16, 5 * 28 + 4, 34); // max 5 balls in queue
        fill(0);
          for (int i = 0; i < this.ballQueue.length; i++) {
            if (i > 4) {
                break;
            }
            if (this.ballQueue[i] == null) {
                continue;
            }
            this.ballQueue[i].draw(this);
        }

        // 3b) Ball timer
        if (this.ballQueue[0] != null) {
            textSize(18);
            fill(0);
            textAlign(LEFT, TOP);
            if (ballTimer != 1.1f) {
                String s = String.format("%.1f", ballTimer);
                text(s, 164, 22);
            }
        }

        if (gameState == GameState.PAUSED) {// looks ugly

            for (ArrayList<Line> line : this.drawnLines) {
                for (Line l : line) {
                    l.draw(this);
                }
            }

            for (Line line : this.allLines) {
                line.draw(this);
            }

            return;
        }

        //----------------------------------
        // draw lines in real time
        //----------------------------------
        strokeWeight(10);
        for (ArrayList<Line> lineList : this.tempLines) {
            for (Line line : lineList) {
                line.draw(this);
            }
        }

        this.drawAll();
        
		//----------------------------------
        // game end
        //----------------------------------
        if (this.ballQueue[0] == null && this.balls.isEmpty() && gameState != GameState.WIN) {
            gameState = GameState.WIN;
            gameLevel++;
            scoreTemp += (int) (lastSecond / 0.067);
            scoreFinal += scoreTemp;
        }

        else if (frameCount >= timeLimit * FPS) {
            gameState = GameState.OVER;
        }
    }


    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
