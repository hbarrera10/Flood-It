import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // main constructor
  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }

  // constructor
  Cell(int x, int y, Color color, boolean flooded, Cell left, Cell top, Cell right, Cell bottom) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  // convenience constructor
  Cell() {
    this(0, 0, null, false, null, null, null, null);
  }

  // convenience constructor
  Cell(int x, int y, Color color) {
    this.color = color;
    this.x = x;
    this.y = y;
  }

  // draws the image of a cell
  WorldImage image() {
    return new RectangleImage(20, 20, OutlineMode.SOLID, this.color);
  }

  // references adjacent cells to the left
  void setLeft(Cell left) {
    this.left = left;
  }

  // references adjacent cells to the right
  void setRight(Cell right) {
    this.right = right;
  }

  // references adjacent cells to the top
  void setTop(Cell top) {
    this.top = top;
  }

  // references adjacent cells to the bottom
  void setBottom(Cell bottom) {
    this.bottom = bottom;
  }

  // floods this cell with the given color
  void floodCell(Color c, ArrayList<Cell> workingList) {
    this.color = c;
    this.flooded = true;
    ArrayList<Cell> temp = new ArrayList<Cell>();
    temp.add(this.top);
    temp.add(this.left);
    temp.add(this.right);
    temp.add(this.bottom);
    for (Cell neighbor : temp) {
      if (neighbor != null) {
        neighbor.needsFlood(c, workingList);
      }
    }
  }

  // checks if this square is both flooded and of the supplied color, used to
  // check for a winstate
  boolean check(Color c) {
    return this.color.equals(c) && this.flooded;
  }

  // adds this cell to the working list if it is flooded XOR the same color
  // provided
  void needsFlood(Color c, ArrayList<Cell> workingList) {
    if ((this.color.equals(c) ^ this.flooded) && !workingList.contains(this)) {
      workingList.add(this);
    }
  }

  // returns true if the posn is in bounds
  boolean inBounds(Posn pos) {
    return Math.abs((80 + 20 * this.x) - pos.x) < 10 && Math.abs((80 + 20 * this.y) - pos.y) < 10;
  }
}

//represents FloodItWorld
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> grid;
  int time = 0;
  int movesLimit;
  int colorsUsed;
  Random rand;
  Color floodColor;
  ArrayList<Cell> floodNext = new ArrayList<Cell>();

  boolean lost;
  boolean won;

  public int gridSize;
  public static int WHEIGHT = 900;
  public static int WWIDTH = 600;

  // array list of the colors to be used in the game
  ArrayList<Color> randColor = new ArrayList<Color>(Arrays.asList(Color.RED, Color.GREEN,
      Color.BLUE, Color.YELLOW, Color.GRAY, Color.CYAN, Color.ORANGE, Color.PINK));

  // constructor
  FloodItWorld(int colorsUsed, int dimens) {
    if (colorsUsed > 8) {
      throw new IllegalArgumentException("Number of colors exceeds 8");
    }
    this.colorsUsed = colorsUsed;
    gridSize = dimens;
    this.grid = new ArrayList<Cell>(gridSize);
    this.rand = new Random();
    gridGen();
    floodNext.add(grid.get(0));
    this.movesLimit = this.maxMovesGen();
    this.lost = false;
    this.won = false;
  }

  // constructor
  FloodItWorld(int colorsUsed, int dimens, Random rand) {
    if (colorsUsed > 8) {
      throw new IllegalArgumentException("Number of colors exceeds 8");
    }
    this.colorsUsed = colorsUsed;
    gridSize = dimens;
    this.grid = new ArrayList<Cell>(gridSize);
    this.rand = rand;
    gridGen();
    floodNext.add(grid.get(0));
    this.movesLimit = this.maxMovesGen();
    this.lost = false;
    this.won = false;
  }

  // starts the flooding process when the mouse is clicked
  public void onMouseClicked(Posn pos) {
    for (Cell c : grid) {
      if (c.inBounds(pos)) {
        this.floodColor = c.color;
        this.floodNext.add(this.grid.get(0));
        this.movesLimit--;
      }
    }
  }

  // generates the number of max moves
  int maxMovesGen() {
    return this.gridSize + this.colorsUsed + 3;
  }

  // takes in a key and resets the scene if the key is R
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.reset();
    }
  }

  // resets the scene
  public void reset() {
    this.grid = new ArrayList<Cell>();
    gridGen();
    floodNext = new ArrayList<Cell>();
    floodNext.add(grid.get(0));
    this.movesLimit = this.maxMovesGen();
    this.lost = false;
    this.won = false;
    this.time = 0;
  }

  // updates the world on tick
  public void onTick() {
    time++;
    this.flood();
    this.lost = this.checkLoss();
    this.won = this.checkWin();
  }

  // checks to see if the game has been lost
  public boolean checkLoss() {
    return this.movesLimit <= 0;
  }

  // checks to see if the game has been won
  public boolean checkWin() {
    boolean completeChecker = true;
    for (Cell c : grid) {
      completeChecker = completeChecker && c.check(floodColor);

    }
    return completeChecker;
  }

  // floods every cell in floodnext and then updates floodnext with the cells that
  // need to be flooded on the next tick
  public void flood() {
    ArrayList<Cell> workingList = new ArrayList<Cell>();
    for (Cell c : floodNext) {
      c.floodCell(floodColor, workingList);
    }
    this.floodNext = new ArrayList<Cell>(workingList);
  }

  // creates a scene
  public WorldScene makeScene() {
    if (this.won) {
      return this.endScene("You Win :)");
    }
    else if (this.lost) {
      return this.endScene("You Lose :(");
    }
    WorldScene gameScene = new WorldScene(900, 600);
    
    //enhancing graphics with a background image
    gameScene.placeImageXY(new FromFileImage("src/Northeastern_Huskies_logo.png"), 250, 300);

    // this will put each cell on the grid
    for (Cell a : grid) {
      gameScene.placeImageXY(a.image(), 80 + 20 * a.x, 80 + 20 * a.y);
    }

    // places "FLOOD IT!"
    gameScene.placeImageXY(new TextImage("FLOOD IT!", 50, FontStyle.BOLD, 
        new Color(135, 206, 235)), WWIDTH - 400, 40);

    // tells the user how many moves are left
    gameScene.placeImageXY(new TextImage("Moves Remaining: " + (this.movesLimit), 
        25, FontStyle.BOLD, new Color(135, 206, 235)), WWIDTH - 350, 475);

    // time displayed on screen
    gameScene.placeImageXY(new TextImage("Time Spent: " + time / 10 + " seconds", 25,
        FontStyle.BOLD, new Color(135, 206, 235)), WWIDTH - 350, 450);

    return gameScene;
  }

  // returns the game at the end scene with the supplied message
  public WorldScene endScene(String message) {
    WorldScene gameScene = new WorldScene(900, 600);
    gameScene.placeImageXY(new TextImage(message, 50, FontStyle.BOLD, new Color(0, 0, 0)),
        WWIDTH / 3, 150);
    gameScene.placeImageXY(
        new TextImage("Press r to play again", 24, FontStyle.BOLD, new Color(0, 0, 0)), WWIDTH / 3,
        200);

    return gameScene;
  }

  // generates a random grid for the game
  void gridGen() {
    if (grid != null) {
      for (int r = 0; r < this.gridSize; r++) {
        for (int c = 0; c < this.gridSize; c++) {
          boolean isCorner = (r == 0 && c == 0);
          Cell cell = new Cell(r, c, this.randColor.get(this.rand.nextInt(this.colorsUsed)),
              isCorner);
          grid.add(cell);
        }
      }
    }
    for (Cell cell : grid) {
      int cellIndex = grid.indexOf(cell);
      int row = cellIndex / this.gridSize;
      int col = cellIndex % this.gridSize;

      // connects to left neighbor if not leftmost cell in row
      if (col > 0) {
        cell.setLeft(grid.get(cellIndex - 1));
      }

      // connects to right neighbor if not rightmost cell in row
      if (col < this.gridSize - 1) {
        cell.setRight(grid.get(cellIndex + 1));
      }

      // connects to top neighbor if not top row
      if (row > 0) {
        cell.setTop(grid.get(cellIndex - this.gridSize));
      }

      // connects to bottom neighbor if not bottom row
      if (row < this.gridSize - 1) {
        cell.setBottom(grid.get(cellIndex + this.gridSize));
      }
    }
    this.floodColor = this.grid.get(0).color;// field of field
  }
}

//represents tests and examples for FloodIt
class ExamplesFloodIt {

  Cell r;
  Cell g;
  Cell b;
  FloodItWorld world1;
  FloodItWorld world2;
  FloodItWorld world3;

  ArrayList<Cell> w2Array;
  Cell w20;
  Cell w21;
  Cell w22;
  Cell w23;

  void initFlood() {
    r = new Cell(0, 0, Color.RED);
    g = new Cell(0, 0, Color.GREEN);
    b = new Cell(0, 0, Color.BLUE);

    world1 = new FloodItWorld(5, 1, new Random(25));
    world2 = new FloodItWorld(2, 2, new Random(58));
    world3 = new FloodItWorld(2, 3, new Random(100));

    w20 = new Cell(0, 0, Color.GREEN, true);
    w21 = new Cell(0, 1, Color.GREEN);
    w22 = new Cell(1, 0, Color.GREEN);
    w23 = new Cell(1, 1, Color.RED);

    w20.setBottom(w22);
    w20.setRight(w21);

    w21.setLeft(w20);
    w21.setBottom(w23);

    w22.setTop(w20);
    w22.setRight(w23);

    w23.setTop(w21);
    w23.setLeft(w22);

    w2Array = new ArrayList<Cell>(Arrays.asList(w20, w21, w22, w23));

  }

  // tests image
  void testImage(Tester t) {
    this.initFlood();
    t.checkExpect(r.image(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED));
    t.checkExpect(g.image(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.GREEN));
    t.checkExpect(b.image(), new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE));
  }

  // tests setBottom
  void testSetBottom(Tester t) {
    this.initFlood();
    this.r.setBottom(b);
    this.b.setBottom(g);
    this.g.setBottom(r);
    t.checkExpect(r.bottom == b, true);
    t.checkExpect(b.bottom == g, true);
    t.checkExpect(g.bottom == r, true);
    this.r.setBottom(g);
    t.checkExpect(r.bottom == g, true);
  }

  // tests setTop
  void testSetTop(Tester t) {
    this.initFlood();
    this.r.setTop(b);
    this.b.setTop(g);
    this.g.setTop(r);
    t.checkExpect(r.top == b, true);
    t.checkExpect(b.top == g, true);
    t.checkExpect(g.top == r, true);
    this.r.setTop(g);
    t.checkExpect(r.top == g, true);
  }

  // tests setLeft
  void testSetLeft(Tester t) {
    this.initFlood();
    this.r.setLeft(b);
    this.b.setLeft(g);
    this.g.setLeft(r);
    t.checkExpect(r.left == b, true);
    t.checkExpect(b.left == g, true);
    t.checkExpect(g.left == r, true);
    this.r.setLeft(g);
    t.checkExpect(r.left == g, true);
  }

  // tests setRight
  void testSetRight(Tester t) {
    this.initFlood();
    this.r.setRight(b);
    this.b.setRight(g);
    this.g.setRight(r);
    t.checkExpect(r.right == b, true);
    t.checkExpect(b.right == g, true);
    t.checkExpect(g.right == r, true);
    this.r.setRight(g);
    t.checkExpect(r.right == g, true);
  }

  // tests floodCell
  void testFloodCell(Tester t) {
    this.initFlood();
    ArrayList<Cell> temp = new ArrayList<Cell>(0);
    this.w20.floodCell(Color.GREEN, temp);
    t.checkExpect(w20.color, Color.GREEN);
    t.checkExpect(temp.get(0), this.w21);
    t.checkExpect(temp.get(1), this.w22);
    temp = new ArrayList<Cell>(0);
    this.w20.floodCell(Color.RED, temp);
    t.checkExpect(w20.color, Color.RED);
    temp = new ArrayList<Cell>(0);
    this.w21.floodCell(Color.RED, temp);
    this.w22.floodCell(Color.RED, temp);
    t.checkExpect(this.w21.flooded, true);
    t.checkExpect(this.w23.flooded, false);
    t.checkExpect(temp.contains(this.w23), true);
  }

  // tests Check
  boolean testCheck(Tester t) {
    this.initFlood();
    this.world2.onTick();
    return t.checkExpect(w20.check(Color.RED), false) && t.checkExpect(w21.check(Color.RED), false)
        && t.checkExpect(w20.check(Color.GREEN), true);
  }

  // tests needsColor
  void testNeedsColor(Tester t) {
    this.initFlood();
    ArrayList<Cell> temp = new ArrayList<Cell>(0);
    this.w20.needsFlood(Color.GREEN, temp);
    t.checkExpect(temp.contains(w20), false);
    this.w20.needsFlood(Color.RED, temp);
    t.checkExpect(temp.contains(w20), true);
    this.w23.needsFlood(Color.GREEN, temp);
    t.checkExpect(temp.contains(w23), false);
    this.w23.needsFlood(Color.RED, temp);
    t.checkExpect(temp.contains(w23), true);
  }

  boolean testInBounds(Tester t) {
    this.initFlood();
    return t.checkExpect(this.w20.inBounds(new Posn(80, 80)), true)
        && t.checkExpect(this.w21.inBounds(new Posn(90, 80)), false)
        && t.checkExpect(this.w20.inBounds(new Posn(90, 80)), false);
  }

  // tests gridGen
  void testGridGen(Tester t) {
    this.initFlood();
    this.g.flooded = true;
    t.checkExpect(this.world1.grid, new ArrayList<Cell>(Arrays.asList(this.g)));
    t.checkExpect(this.world2.grid, this.w2Array);
  }

  // tests makeScene
  void testMakeScene(Tester t) {
    this.initFlood();
    WorldScene temp = new WorldScene(900, 600);
    temp.placeImageXY(new FromFileImage("src/Northeastern_Huskies_logo.png"), 250, 300);
    temp.placeImageXY(g.image(), 80, 80);
    temp.placeImageXY(new TextImage("FLOOD IT!", 50, FontStyle.BOLD, 
        new Color(135, 206, 235)), 600 / 3, 40);
    temp.placeImageXY(
        new TextImage("Moves Remaining: " + (9), 25, FontStyle.BOLD, 
            new Color(135, 206, 235)), 600 - 350, 475);
    temp.placeImageXY(
        new TextImage("Time Spent: " + 0 / 10 + " seconds", 25, FontStyle.BOLD, 
            new Color(135, 206, 235)), 600 - 350, 450);
    t.checkExpect(this.world1.makeScene(), temp);
    temp = new WorldScene(900, 600);
    temp.placeImageXY(new FromFileImage("src/Northeastern_Huskies_logo.png"), 250, 300);
    temp.placeImageXY(w20.image(), 80, 80);
    temp.placeImageXY(w21.image(), 100, 80);
    temp.placeImageXY(w22.image(), 80, 100);
    temp.placeImageXY(w23.image(), 100, 100);
    temp.placeImageXY(new TextImage("FLOOD IT!", 50, FontStyle.BOLD, 
        new Color(135, 206, 235)), 600 / 3, 40);
    temp.placeImageXY(
        new TextImage("Moves Remaining: " + (7), 25, FontStyle.BOLD, 
            new Color(135, 206, 235)), 600 - 350, 475);
    temp.placeImageXY(
        new TextImage("Time Spent: " + 0 / 10 + " seconds", 25, FontStyle.BOLD, 
            new Color(135, 206, 235)), 600 - 350, 450);
    t.checkExpect(this.world2.makeScene(), temp);
    temp = new WorldScene(900, 600);
    world2.lost = true;
    temp.placeImageXY(new TextImage("You Lose :(", 50, FontStyle.BOLD, 
        new Color(0, 0, 0)), 600 / 3, 150);
    temp.placeImageXY(
        new TextImage("Press r to play again", 24, FontStyle.BOLD, 
            new Color(0, 0, 0)), 600 / 3, 200);
    t.checkExpect(this.world2.makeScene(), temp);
    temp = new WorldScene(900, 600);
    world2.won = true;
    temp.placeImageXY(new TextImage("You Win :)", 50, FontStyle.BOLD, 
        new Color(0, 0, 0)), 600 / 3, 150);
    temp.placeImageXY(
        new TextImage("Press r to play again", 24, FontStyle.BOLD, 
            new Color(0, 0, 0)), 600 / 3, 200);
    t.checkExpect(this.world2.makeScene(), temp);

  }

  // tests onMouseClicked
  void testOnMouseClicked(Tester t) {
    this.initFlood();
    world2.onMouseClicked(new Posn(80, 80));
    t.checkExpect(world2.floodColor, Color.GREEN);
    world2.onMouseClicked(new Posn(80, 800));
    t.checkExpect(world2.floodColor, Color.GREEN);
    world2.onMouseClicked(new Posn(100, 100));
    t.checkExpect(world2.floodColor, Color.RED);
    t.checkExpect(world2.movesLimit, 5);
  }

  // tests maxMovesGen
  boolean testMaxMovesGen(Tester t) {
    this.initFlood();
    return t.checkExpect(this.world1.maxMovesGen(), 9)
        && t.checkExpect(this.world2.maxMovesGen(), 7)
        && t.checkExpect(this.world3.maxMovesGen(), 8);
  }

  // tests onKeyEvent
  void testOnKeyEvent(Tester t) {
    this.initFlood();
    world1.onKeyEvent("p");
    t.checkExpect(this.world1.grid.get(0).color, Color.GREEN);
    world1.onKeyEvent("r");
    t.checkExpect(this.world1.grid.get(0).color, Color.YELLOW);
  }

  // tests reset
  boolean testReset(Tester t) {
    this.initFlood();
    world2.movesLimit = 800;
    world2.lost = true;
    world2.won = true;
    world2.time = 110;
    world2.reset();
    return t.checkExpect(world2.movesLimit, 7) && t.checkExpect(world2.lost, false)
        && t.checkExpect(world2.won, false) && t.checkExpect(world2.time, 0);
  }

  // tests checkLoss
  void testCheckLoss(Tester t) {
    this.initFlood();
    t.checkExpect(this.world2.checkLoss(), false);
    this.world2.movesLimit = 0;
    t.checkExpect(this.world1.checkLoss(), false);
    t.checkExpect(this.world2.checkLoss(), true);
  }

  // tests checkWin
  void testCheckWin(Tester t) {
    this.initFlood();
    this.world1.movesLimit = 0;
    this.world2.movesLimit = 0;
    t.checkExpect(this.world1.checkWin(), true);
    t.checkExpect(this.world2.checkWin(), false);
  }

  void testFlood(Tester t) {
    this.initFlood();
    this.world2.floodColor = Color.GREEN;
    t.checkExpect(world2.floodNext.get(0), this.w20);
    this.world2.flood();
    t.checkExpect(world2.floodNext.contains(w20), false);
    t.checkExpect(world2.floodNext.get(0), this.w21);
    t.checkExpect(world2.floodNext.get(1), this.w22);
    this.world2.flood();
    t.checkExpect(world2.floodNext.contains(w20), false);
    t.checkExpect(world2.floodNext.contains(w21), false);
    t.checkExpect(world2.floodNext.contains(w22), false);
    t.checkExpect(world2.floodNext.size(), 0);

    this.world2.floodColor = Color.RED;
    world2.floodNext.add(world2.grid.get(0));
    this.world2.flood();
    t.checkExpect(world2.grid.get(0).color, Color.RED);
    t.checkExpect(world2.floodNext.contains(w20), false);
    t.checkExpect(world2.floodNext.get(0), world2.grid.get(1));
    t.checkExpect(world2.floodNext.get(1), world2.grid.get(2));
    this.world2.flood();
    t.checkExpect(world2.floodNext.contains(w20), false);
    t.checkExpect(world2.floodNext.contains(w21), false);
    t.checkExpect(world2.floodNext.contains(w22), false);
    t.checkExpect(world2.floodNext.get(0), world2.grid.get(3));
  }

  // tests endScene
  void testEndScene(Tester t) {
    this.initFlood();
    ArrayList<String> messages = new ArrayList<String>();
    messages.add("hi");
    messages.add("win");
    messages.add("lose");
    messages.add("the devil went down to georgia and he was lookin for a soul to steal");

    for (String s : messages) {
      WorldScene gameScene = new WorldScene(900, 600);
      gameScene.placeImageXY(new TextImage(s, 50, FontStyle.BOLD, 
          new Color(0, 0, 0)), 600 / 3, 150);
      gameScene.placeImageXY(
          new TextImage("Press r to play again", 24, FontStyle.BOLD, 
              new Color(0, 0, 0)), 600 / 3, 200);
      t.checkExpect(gameScene, this.world1.endScene(s));
    }
  }

  // tests onTick
  void testOnTick(Tester t) {
    this.initFlood();
    t.checkExpect(world2.time, 0);
    t.checkExpect(world2.floodNext.get(0), world2.grid.get(0));
    this.world2.onTick();
    t.checkExpect(world2.time, 1);
    t.checkExpect(world2.floodNext.get(0), world2.grid.get(1));
    this.initFlood();
    world2.movesLimit = 0;
    this.world2.onTick();
    t.checkExpect(world2.lost, true);
    t.checkExpect(world1.won, false);
    this.world1.onTick();
    t.checkExpect(world1.won, true);
  }

  // creates the FloodItWorld and runs it for manual testing
  void testFloodItWorld(Tester t) {
    this.initFlood();
    FloodItWorld starterWorld = new FloodItWorld(8, 16);
    int sceneSize = 500;
    starterWorld.bigBang(sceneSize, sceneSize, .1f);
  }
}