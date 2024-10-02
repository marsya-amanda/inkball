package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
import processing.data.JSONArray;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.util.*;
import java.lang.*;
import java.io.*;

public class App extends PApplet {

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

    public static Random random = new Random();
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    public enum GameState {
        PLAYING, PAUSED, WIN, OVER;
    }

    public static int gameLevel = 1;

    public GameState gameState = GameState.PLAYING;

    public static int timeLimit = 0;
    public static int lastSecond = 0;

    public static double score = 0.0d;
    public static HashMap<String, Integer> scoreIncrease = new HashMap<>();
    public static HashMap<String, Integer> scoreDecrease = new HashMap<>();
    public float modScoreIncrease = 1;
    public float modScoreDecrease = 1;
    private int spawnInterval = 0;
    public Ball[] ballQueue;
    public int maxBallQueue;
    public static float ballTimer = 1.0f;

    public static boolean isDrawing = false;
    public float[] start = new float[2];
    public float[] end = new float[2];
    public static int mouseRadius = 5;
    public int lastLine = 0;

    private Tile[][] board;
    private ArrayList<Ball> balls = new ArrayList<Ball>();
    private ArrayList<Line> allLines = new ArrayList<Line>();
    private ArrayList<ArrayList<Line>> drawnLines = new ArrayList<>();
    private ArrayList<ArrayList<Line>> tempLines = new ArrayList<>();
    private ArrayList<Spawner> spawners = new ArrayList<Spawner>();
    private ArrayList<Hole> holes = new ArrayList<Hole>();
    private HashMap<String, PImage> sprites = new HashMap();

    public int[] firstSpiral = new int[] {0, 0};
    public int[] secondSpiral = new int[] {WIDTH/CELLSIZE - 1, (HEIGHT-TOPBAR)/CELLSIZE - 1};

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

    public ArrayList<Line> getLineSegments() {
        return this.allLines;
    }

    public ArrayList<Ball> getBalls() {
        return this.balls;
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        this.gameState = GameState.PLAYING;
        //See PApplet javadoc:
        //loadJSONObject(configPath)
        // the image is loaded from relative path: "src/main/resources/inkball/..."
		/*try {
            result = loadImage(URLDecoder.decode(this.getClass().getResource(filename+".png").getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }*/
        this.json = loadJSONObject(configPath);

        //get information from config
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
        System.out.println("Last second: " + lastSecond);
        System.out.println("timeLimit: " + timeLimit);

        this.spawnInterval = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getInt("spawn_interval");

        this.modScoreIncrease = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getFloat("score_increase_from_hole_capture_modifier");
        this.modScoreDecrease = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getFloat("score_decrease_from_wrong_hole_modifier");

        JSONObject scoreIncJSON = this.json.getJSONObject("score_increase_from_hole_capture");
        for (Object key : scoreIncJSON.keys()) {
            String keyStr = (String) key;
            Integer scoreInc = scoreIncJSON.getInt(keyStr);
            scoreIncrease.put(keyStr, scoreInc);
        }

        JSONObject scoreDecJSON = this.json.getJSONObject("score_decrease_from_wrong_hole");
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

        for (int i = 0; i <= 4; i++) {
            this.getSprite("ball"+String.valueOf(i));
            this.getSprite("hole"+String.valueOf(i));
            this.getSprite("wall"+String.valueOf(i));
        }
        //

        // Create board
        this.board = new Tile[(HEIGHT - TOPBAR)/CELLHEIGHT][WIDTH/CELLSIZE];

        // Get layout
        this.setLayout();

        // ADD BALL QUEUE
        JSONArray ballsJSON = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getJSONArray("balls");
        this.maxBallQueue = ballsJSON.size() + this.balls.size();

        this.ballQueue = new Ball[this.maxBallQueue];

        for (int i = 0; i < ballsJSON.size(); i++) {
            int colour = colourToInt(ballsJSON.getString(i));
            this.ballQueue[i] = new Ball(19 + 28 * i, 21, colour);
        }

        //this.drawnLines.add(new ArrayList<Line>());

        //FOR CHECKING
        /*for (int i = 0; i < this.lineSegments.size(); i++) {
            Line line = this.lineSegments.get(i);
            System.out.println(Line.toString(line));
        }*/

        //TESTING WALL COORDS
        /*for (int i = 0; i < this.board.length; i++) {
            System.out.println(Arrays.toString(this.board[i]));
        }*/
        //System.out.println(Arrays.deepToString(this.board)); //some are null

    }

    public void setLayout() {
        File JSONfile = new File(this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getString("layout"));
        Scanner scan = null;
        try {
             scan = new Scanner(JSONfile);
        }
        catch (FileNotFoundException e) {
            System.exit(1);
        }

        //READ TXT LAYOUT FILE
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

        //VALIDATE DIMENSIONS OF LAYOUT FILE VS BOARD
        int tempWidth = 0;
        int tempHeight = 0;
        if (lines.size() >= this.board.length) {
            tempHeight = this.board.length;
        }
        if (lines.size() < this.board.length) {
            tempHeight = lines.size();
        }
        if (lines.get(0).size() >= this.board[0].length) {
            tempWidth = this.board[0].length;
        }
        if (lines.get(0).size() < this.board[0].length) {
            tempWidth = lines.get(0).size();
        }

        //CREATE OBJECTS BASED ON FILE
        for (int i = 0; i < this.board.length; i++) {

            // Handle insufficient number of lines in file
            if (i + 1 > lines.size()) {
                for (int k = i + 1; k < this.board.length; k++) {
                    for (int l = 0; l < this.board[0].length; l++) {
                        this.board[k][l] = new Blank(l, k);
                    }
                }
                break;
            }

            for (int j = 0; j < this.board[0].length; j++) { //not using length of file bc file might be too long

                // HANDLE insufficient number of characters in a line
                if (j + 1 > lines.get(i).size()) {
                    for (int k = j + 1; k < this.board[i].length; k++) {
                        this.board[i][k] = new Blank(k, i);
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
                }

                //WALL 1-4
                else if (lines.get(i).get(j).equals("1") || lines.get(i).get(j).equals("2") || lines.get(i).get(j).equals("3") || lines.get(i).get(j).equals("4")) {
                    //make above more succinct
                    int colour = Integer.parseInt(lines.get(i).get(j));
                    this.board[i][j] = new Wall(j, i, colour);
                }

                //HOLE
                else if (lines.get(i).get(j).equals("H")) {
                    if (getColourCode(lines, i, j) == -1) {
                        this.board[i][j] = new Blank(j, i);
                        this.board[i][j+1] = new Blank(j+1, i); //handle out of bounds
                    }
                    else {
                        int colour = getColourCode(lines, i, j);
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
                    if (App.getColourCode(lines, i, j) != -1) {
                        int colour = getColourCode(lines, i, j);
                        this.balls.add(new Ball(j*CELLSIZE + 4, i*CELLSIZE+TOPBAR + 4, colour)); //add 4 so spawns in the middle
                    }
                }

                else {
                    this.board[i][j] = new Blank(j, i);
                }
            }
        }

//        for (int i = 0; i < this.board.length; i++) {
//            for (int j = 0; j < this.board[0].length; j++) {
//                if (this.board[i][j] == null) {
//                    this.board[i][j] = new Blank(j, i);
//                }
//            }
//        }

        // ADD LINES FROM WALLS
        this.addBorders();

    }

    public void restart() {
        frameCount = 0;
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

    public static int colourToInt(String colourStr) {
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

    public void addWallLineSegment(Wall wall) {

        float[] topLeftCorner = new float[] {wall.getX()*CELLSIZE, wall.getY() * CELLSIZE + TOPBAR}; // H0, V0
        float[] topRightCorner = new float[] {(wall.getX()+1)*CELLSIZE, wall.getY() * CELLSIZE + TOPBAR}; // H1, V0
        float[] bottomLeftCorner = new float[] {wall.getX()*CELLSIZE, (wall.getY()+1) * CELLSIZE + TOPBAR}; // H0, V1
        float[] bottomRightCorner = new float[] {(wall.getX()+1)*CELLSIZE, (wall.getY()+1)*CELLSIZE + TOPBAR};

        int colour = wall.getColour();
        boolean isDrawn = false;
        this.allLines.add(new Line(topLeftCorner, topRightCorner, colour, isDrawn)); //TOP
        this.allLines.add(new Line(topLeftCorner, bottomLeftCorner, colour, isDrawn)); //LEFT
        this.allLines.add(new Line(bottomLeftCorner, bottomRightCorner, colour, isDrawn)); // BOTTOM
        this.allLines.add(new Line(bottomRightCorner, topRightCorner, colour, isDrawn));
    }

    public static int getColourCode (ArrayList<ArrayList<String>> lines, int i, int j) {
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

    public static double getDistance(float[] P1, float[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        return Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
    }

    public static double getDistance(double[] P1, float[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        return Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
    }

    public static double getDistance(double[] P1, double[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        return Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
    }

    public void addDrawnLine(Line line) {
        if (line.getP1()[1] < TOPBAR || line.getP2()[1] < TOPBAR) {
            return;
        }

        if  (this.tempLines.isEmpty()) {
            this.tempLines.add(new ArrayList<>());
        }

        this.tempLines.get(this.tempLines.size() - 1).add(line);
    }

    public void removeLine(float[] toRemove) {
        ArrayList<Line> removedLine = new ArrayList<>();

        outerLoop:
        for (int i = (this.drawnLines.size() - 1); i >= 0; i--) {
            for (int j = (this.drawnLines.get(i).size() - 1); j >= 0; j--) {
                if (mouseOnLine(toRemove, this.drawnLines.get(i).get(j).getP1(), this.drawnLines.get(i).get(j).getP2())) {
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
        //System.out.println("just removed a line! current number of lines: " + this.drawnLines.size());

    }

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

    public static boolean mouseOnLine(float[] mouseXY, float[] lineP1, float[] lineP2) {
        double distP1 = App.getDistance(mouseXY, lineP1);
        double distP2 = App.getDistance(mouseXY, lineP2);
        double distP1P2 = App.getDistance(lineP1, lineP2);

        return distP1 + distP2 < mouseRadius + distP1P2; //mouse radius is 5
    }

    public boolean getBallsStatus() {
        for (Ball ball : this.balls) {
            if (!ball.getIsAbsorbed()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (event.getKeyCode() == 82) { //is "r"
            lastSecond = 0;
            gameState = GameState.PLAYING;
            score = 0;
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
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // create a new player-drawn line object
        if (gameState == GameState.PAUSED) {
            return;
        }

        int mouseX = e.getX();
        int mouseY = e.getY();

        if (mouseButton == LEFT) {
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

        else if (mouseButton == RIGHT) {
            float[] toRemove = new float[] {mouseX, mouseY};

            this.removeLine(toRemove);
        }

    }
	
	@Override
    public void mouseDragged(MouseEvent e) {
        if (gameState == GameState.PAUSED) {
            return;
        }

		// remove player-drawn line object if right mouse button is held
		// and mouse position collides with the line
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (mouseButton == LEFT) {
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

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameState == GameState.PAUSED) {
            return;
        }

        int mouseX = e.getX();
        int mouseY = e.getY();

        if (mouseButton == LEFT) {
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
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        background(206);
        this.spawnBalls();

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
            text("Score: " + (int) score, WIDTH - 80, App.TOPBAR - 44);
        }

        // if PAUSED
        if (gameState != GameState.PLAYING) {
            textSize(22);
            fill(0);
            textAlign(CENTER, CENTER);
            if (gameState == GameState.PAUSED) {
                text("*** PAUSED ***", WIDTH / 2, TOPBAR / 2);
            }
            else if (gameState == GameState.WIN && gameLevel > this.json.getJSONArray("levels").size()) {
                text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);
            }
            else if (gameState == GameState.OVER) {
                text("=== TIME'S UP ===", WIDTH / 2, TOPBAR / 2);
            }

            for (Ball ball : this.balls) {
                ball.draw(this);
            }
            for (Line line : this.allLines) {
                line.draw(this);
            }

            if (gameState == GameState.WIN && gameLevel <= this.json.getJSONArray("levels").size()) {
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
            String s = String.format("%.1f", ballTimer);
            text(s, 164, 22);
        }

        if (gameState == GameState.PAUSED) { // looks ugly
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

        for (int i = this.balls.size() - 1; i >= 0; i--) {
            Ball ball = this.balls.get(i);
            ball.draw(this);

            boolean hasCollided = false;
            for (int j = this.allLines.size() - 1; j >= 0; j--) {
                Line line = this.allLines.get(j);
                if (ball.willCollide(line)) {
                    ball.interact(line, this);
                    hasCollided = true;
                    break;
                }

            }

            outerLoop:
            for (int j = this.drawnLines.size() - 1; j >= 0; j--) {
                for (int k = this.drawnLines.get(j).size() - 1; k >= 0; k--) {
                    if (ball.willCollide(this.drawnLines.get(j).get(k))) {
                        ball.interact(this.drawnLines.get(j).get(k), this);
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
        
		//----------------------------------
        // game end
        //----------------------------------
        if (this.ballQueue[0] == null && this.balls.isEmpty() && gameState != GameState.WIN) {
            gameState = GameState.WIN;
            gameLevel++;
            score += (int) (lastSecond / 0.067);
        }

        else if (frameCount >= timeLimit * FPS) {
            gameState = GameState.OVER;
        }
    }


    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
