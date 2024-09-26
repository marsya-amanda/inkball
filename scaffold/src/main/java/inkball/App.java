package inkball;

import org.checkerframework.checker.units.qual.C;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONObject;
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
    public static boolean isDrawing = false;
    public float[] start = new float[2];
    public float[] end = new float[2];
    public float [] remove = new float[2];
    public static int mouseRadius = 5;
    public int lastLine = 0;
    public int numDrawnLines = -1;

    private Tile[][] board;
    private ArrayList<Ball> balls = new ArrayList<Ball>();
    private ArrayList<Line> allLines = new ArrayList<Line>();
    private ArrayList<ArrayList<Line>> drawnLines = new ArrayList<>(1);
    private ArrayList<Spawner> spawners = new ArrayList<Spawner>();
    private ArrayList<Hole> holes = new ArrayList<Hole>();
    private HashMap<String, PImage> sprites = new HashMap();

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

        //get time limit
        timeLimit = this.json.getJSONArray("levels").getJSONObject(gameLevel - 1).getInt("time");
        lastSecond = timeLimit; //in seconds
        
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
        //Add borders
        //this.addBorders();

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
        for (int i = 0; i < tempHeight; i++) {
            for (int j = 0; j < tempWidth; j++) { //not using length of file bc file might be too long
                if (this.board[i][j] != null) { //if board has something in it, skip
                    continue;
                }

                //BLANK
                if (lines.get(i).get(j).equals(" ")) {
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
                                Hole.GridPosition gp = this.getPosition(k, l);
                                /*if (gp == null) {
                                    this.board[i][j] = new Blank(j, i);
                                    break;
                                }*/
                                if (k == 1 && l == 1) {
                                    this.holes.add(new Hole (j+l, i+k, colour, gp)); // add the one w center coords
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
                    this.balls.add(new Ball(j*CELLSIZE, i*CELLSIZE+TOPBAR, 0));
                    this.spawners.add(new Spawner(j, i));
                }

                //BALL
                else if (lines.get(i).get(j).equals("B")) {
                    this.board[i][j] = new Blank(j, i);
                    if (App.getColourCode(lines, i, j) != -1) {
                        int colour = getColourCode(lines, i, j);
                        this.balls.add(new Ball(j*CELLSIZE, i*CELLSIZE+TOPBAR, colour));
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

    public void addBorders() {
//        float[] topLeftCorner = new float[] {CELLSIZE, TOPBAR+CELLSIZE}; // H0, V0
//        float[] topRightCorner = new float[] {WIDTH-CELLSIZE, TOPBAR+CELLSIZE}; // H1, V0
//        float[] bottomLeftCorner = new float[] {CELLSIZE, HEIGHT-CELLSIZE}; // H0, V1
//        float[] bottomRightCorner = new float[] {WIDTH-CELLSIZE, HEIGHT-CELLSIZE}; // H1, V1
//
//        boolean visible = true;
//        this.lineSegments.add(new Line(topLeftCorner, topRightCorner, visible)); //TOP
//        this.lineSegments.add(new Line(topLeftCorner, bottomLeftCorner, visible)); //LEFT
//        this.lineSegments.add(new Line(bottomLeftCorner, bottomRightCorner, visible)); // BOTTOM
//        this.lineSegments.add(new Line(bottomRightCorner, topRightCorner, visible)); //RIGHT

        // line for non-border tiles
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (this.getBoard()[i][j].getClass() == Wall.class) {
                    Wall wall = (Wall) this.getBoard()[i][j];
                    this.addWallLineSegment(wall);
                }
            }
        }
    }

    public void addWallLineSegment(Wall wall) {
        float[] topLeftCorner = new float[] {wall.getX()*CELLSIZE - 2, wall.getY() * CELLSIZE + TOPBAR - 2}; // H0, V0
        float[] topRightCorner = new float[] {(wall.getX()+1)*CELLSIZE + 2, wall.getY() * CELLSIZE + TOPBAR - 2}; // H1, V0
        float[] bottomLeftCorner = new float[] {wall.getX()*CELLSIZE - 2, (wall.getY()+1) * CELLSIZE + TOPBAR + 2}; // H0, V1
        float[] bottomRightCorner = new float[] {(wall.getX()+1)*CELLSIZE + 2, (wall.getY()+1)*CELLSIZE + TOPBAR + 2};

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
        double distance = Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
        return distance;
        /*Class<?> componentType = P1.getClass().getComponentType();
        if (Number.class.isAssignableFrom(componentType)) {
            float distance = Math.sqrt(Math.pow((P2[1].floatvalue() - P1[1].floatvalue()), 2) + Math.pow((P2[0].floatvalue() - P1[0].floatvalue()), 2));
            return distance;
        }
        if (componentType == int.class || componentType == double.class || componentType == float.class) {
            float distance = Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
            return distance;
        }*/

        //return null;
    }

    public static double getDistance(double[] P1, float[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        double distance = Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
        return distance;
    }

    public static double getDistance(double[] P1, double[] P2) {
        if (P1 == null || P2 == null || P1.length != 2 || P2.length != 2) {
            throw new IllegalArgumentException("Wrong points!");
        }
        double distance = Math.sqrt(Math.pow((P2[1] - P1[1]), 2) + Math.pow((P2[0] - P1[0]), 2));
        return distance;
    }

    public void addDrawnLine(Line line, int num) {
        if (line.getP1()[1] < TOPBAR || line.getP2()[1] < TOPBAR) {
            return;
        }

        this.allLines.add(line);
        this.drawnLines.get(num).add(line);

    }

    public void removeLine(float[] toRemove) {
        System.out.println("calling removeline"); //calling when no right click
        ArrayList<Line> removedLine = new ArrayList<>();
        for (int i = (this.drawnLines.size() - 1); i >= 0; i--) {
            for (int j = (this.drawnLines.get(i).size() - 1); j >= 0; j--) {
                if (mouseOnLine(toRemove, this.drawnLines.get(i).get(j).getP1(), this.drawnLines.get(i).get(j).getP2())) {
                    removedLine = this.drawnLines.get(i);
                    this.drawnLines.remove(i);
                    i = this.drawnLines.size();
                    break;
                }
            }
        }

        if (removedLine.size() == 0) {
            return;
        }

        System.out.println(removedLine);
        //int removed = 0;
        for (int i = 0; i < this.allLines.size(); i++) {
            if (removedLine.get(0).equals(this.allLines.get(i))) {
                System.out.println("line segment found!");
                this.allLines.subList(i, removedLine.size() + 1).clear(); //removedLine.size() + 1, because upper bound exclusive
                return;
            }
        }
        this.numDrawnLines--;

//        int removed = 0;
//        for (int i = this.allLines.size() - 1; i >= 0; i--) {
//            for (int j = removedLine.size() - 1; j >= 0; j--) {
//                if (this.allLines.get(i).equals(removedLine.get(j))) {
//                    this.allLines.remove(i);
//                    i = this.allLines.size();
//                    removed++;
//                    break;
//                }
//            }
//
//        }

        /*if (removed == removedLine.size()) {
            System.out.println("deleted line"); //not passing
            this.numDrawnLines--;
        }*/
    }

    public static boolean mouseOnLine(float[] mouseXY, float[] lineP1, float[] lineP2) {
        double distP1 = App.getDistance(mouseXY, lineP1);
        double distP2 = App.getDistance(mouseXY, lineP2);
        double distP1P2 = App.getDistance(lineP1, lineP2);

        return distP1 + distP2 < mouseRadius + distP1P2; //mouse radius is 5
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (event.getKeyCode() == 114) { //is "r"
            setup();
        }
        if (event.getKeyCode() == 32) {
            gameState = GameState.PAUSED;
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
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (mouseButton == LEFT) {
            if (!isDrawing) {
                this.start = new float[] {mouseX, mouseY};
                this.numDrawnLines++;
                isDrawing = true;
            }

            else if (this.lastLine == 0 || frameCount - this.lastLine == 1) {
                this.end = new float[] {mouseX, mouseY};
            }

            this.lastLine = frameCount;
        }

        else if (mouseButton == RIGHT) {
            this.remove = new float[] {mouseX, mouseY};

            if (this.numDrawnLines == 0) {
                return;
            }

            this.removeLine(this.remove);
        }

    }
	
	@Override
    public void mouseDragged(MouseEvent e) {

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

                if (this.drawnLines.size() == numDrawnLines) {
                    this.drawnLines.add(new ArrayList<Line>());
                }

                if (frameCount - this.lastLine == 1 || this.lastLine == 0) {
                    this.lastLine = frameCount;
                    this.addDrawnLine(new Line(start, end, 0, true), numDrawnLines);
                    this.start = new float[] {mouseX, mouseY};
                    //System.out.println(Arrays.toString(this.start)); // printing
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
        int mouseX = e.getX();
        int mouseY = e.getY();

        if (mouseButton == LEFT) {
            if (isDrawing) {
                this.end = new float[] {mouseX, mouseY};
                this.addDrawnLine(new Line(this.start, this.end, 0, true), numDrawnLines);

                this.lastLine = frameCount;
                this.drawnLines.add(new ArrayList<Line>());
                isDrawing = false;
                System.out.println("line " + this.numDrawnLines + " drawn!");
                //this.numDrawnLines++;
            }
            /*else {
                System.out.println("not drawing"); //not passing: good
            }*/
            //this.end = new float[] {-1, -1};
        }

//        else if (mouseButton == RIGHT) {
//            this.remove = new float[] {mouseX, mouseY};
//            isDrawing = false;
//
//            if (numDrawnLines == 0) {
//                return;
//            }
//            this.removeLine(this.remove);
//            //this.numDrawnLines--;
//            System.out.println("line removed!");
//        }
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        background(206);

        //----------------------------------
        //display Board for current level:
        //----------------------------------
        //TODO

        for (int i = 0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                this.board[i][j].draw(this);
            }
        }

        for (Ball ball : this.balls) {
            ball.draw(this);
            for (Hole hole : this.holes) {
                if (ball.meetHole(hole)) {
                    ball.meetHole(hole);
                    break;
                }
            }
            boolean hasCollided = false;
            for (int i = 0; i < this.allLines.size(); i++) {
                Line line = this.allLines.get(i);
                line.draw(this);
                if (ball.willCollide(line)) {
                    ball.interact(line, this);
                    hasCollided = true;
                    break;
                }
            }

            if (!hasCollided) {
                ball.moveOne();
            }
        }

        //----------------------------------
        //display score
        //----------------------------------
        //TODO

        //----------------------------------
        // display timer & score
        //----------------------------------
        // 1) Timer
        if (frameCount - (timeLimit - lastSecond) * 30 == FPS && gameState == GameState.PLAYING) {
            lastSecond--;
        }
        textSize(22);
        fill(0);
        textAlign(CENTER, CENTER);
        text("Time: " + lastSecond, WIDTH-80, App.TOPBAR-22);

        // 2) Score
        textSize(22);
        fill(0);
        textAlign(CENTER, CENTER);
        text("Score: " + 6, WIDTH-80, App.TOPBAR-44);
        
		//----------------------------------
        //----------------------------------
		//display game end message
        /*if (timeLimit <= 0) {
            gameState = GameState.OVER;
            textSize(30);
            fill(0);
            textAlign(CENTER, CENTER);
            text("== GAME OVER ==", 20, 20);
            setup();
        }
        if (gameState == GameState.WIN) {
            textSize(30);
            fill(0);
            textAlign(CENTER, CENTER);
            text("== YOU WON ==", 20, 20);
            setup();
        }*/
    }


    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
