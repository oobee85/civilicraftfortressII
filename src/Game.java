import java.awt.Color;
import java.awt.Graphics;
import java.awt.List;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.LinkedList;

public class Game {
	private int ticks;
	private int turn;
	private Point worldSize;
	public Tile[][] world;
	int x;
	int y;
	protected static int tileSize = 10;
	private int money;
	private Position viewOffset;
	private Position hoveredTile;
	private Area hoveredArea;
	private BuildMode currentMode;
	private int rotate = 0;

	
	public enum BuildMode{
		NOMODE,
		ROAD,
		WALL;
		
	};

	public Game(int w, int h, Point wSize) {
		worldSize = wSize;
		x = wSize.x;
		y = wSize.y;
		money = 100;
		hoveredTile = new Position(-1,-1);
		hoveredArea = new Area(0,0,0,0);
		viewOffset = new Position(0, 0);
		currentMode = BuildMode.NOMODE;
		
		world = new Tile[(int) worldSize.getX()][(int) worldSize.getY()];
		genTerrain();

	}

	private void grid(Graphics g) {
//		System.out.println("Drawing Grid");
		g.setColor(Color.BLUE);
		for (int i = 0; i <= x; i++) {
			for (int j = 0; j <= y; j++) {
//				g.drawLine(i * tileSize, 0, i * tileSize, j * tileSize);
//				g.drawLine(0, j * tileSize, i * tileSize, j * tileSize);
			}
		}
		g.setColor(Color.BLACK);
	}

	private double getRandomNormal(int tries) {
		double rand = 0;
		for (int i = 0; i < tries; i++) {
			rand += Math.random();
		}
		return rand / tries;
	}

	private void genTerrain() {
		System.out.println("gen terr");
		LinkedList<double[][]> noises = new LinkedList<>();

		for (int octave = 2; octave <= x; octave *= 2) {
			double[][] noise1 = new double[octave][octave];
			for (int i = 0; i < noise1.length; i++) {
				for (int j = 0; j < noise1[0].length; j++) {
					noise1[i][j] = getRandomNormal(5);
				}
			}
			noises.add(noise1);
		}

		double[][] combinedNoise = new double[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				double rand = 0;
				int divider = world.length;
				double multiplier = 1;
				for (double[][] noise : noises) {
					divider /= 2;
					multiplier /= 2;
					rand += multiplier * noise[i / divider][j / divider];
				}
				combinedNoise[i][j] = rand;
			}
		}

		// TODO make smoothing filter bigger so it looks more smooth.
		double[][] smoothed = new double[x][y];
		// apply smoothing filter
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				if (i == 0 || j == 0 || i == x - 1 || j == y - 1) {
					smoothed[i][j] = combinedNoise[i][j];
				} else {
					double sum = combinedNoise[i][j] + combinedNoise[i + 1][j] + combinedNoise[i][j + 1]
							+ combinedNoise[i][j - 1] + combinedNoise[i - 1][j];
					smoothed[i][j] = sum / 5;
				}
			}
		}

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {

				Position p = new Position(i, j);
				Terrain t;
				if (smoothed[i][j] > 0.55) {
					t = Terrain.DIRT;
				} else {
					t = Terrain.GRASS;
				}

				Tile tile = new Tile(null, p, t);
				world[i][j] = tile;

			}
		}
		makeRoad();

	}

	private void makeRoad() {
		int topTile = (int) (Math.random() * world.length);
		int botTile = (int) (Math.random() * world.length);
		
		Position start = new Position(topTile, 0);
		Position end = new Position(botTile, world.length-1);
		
		for(double t = 0; t < 1; t += 0.1 / world.length) {
			Position current = start.multiply(t).add(end.multiply(1-t));
			world[current.getIntX()][current.getIntY()].setHasRoad(true,rotate);
		}
		
		double castleDistance = getRandomNormal(5);
		Position halfway = start.multiply(castleDistance).add(end.multiply(1-castleDistance));
		world[halfway.getIntX()][halfway.getIntY()].setStructure(new Structure());
	}

	public void draw(Graphics g) {

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				Tile t = world[i][j];
				t.draw(g);
			}
		}
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				Tile t = world[i][j];
				
				if(currentMode == BuildMode.WALL) {
					String bm = "wall";
					t.drawEntities(g, bm);
				}
				t.drawEntities(g, null);
				
				if(i==hoveredTile.getIntX() && j==hoveredTile.getIntY()) {
					t.highlight(g);
				}
				if(hoveredArea.contains(i, j)) {
					t.highlight(g);
				}
				
			}
		}
		
		
		grid(g);
	}

	public static void printPoint(Point p) {
		System.out.println("Point: (" + p.x + ", " + p.y + ")");
	}

	public Position getTileAtPixel(Position pixel) {
		Position tile = pixel.add(viewOffset).divide(tileSize);
		return tile;
	}

	public void mouseOver(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
//		System.out.println("Mouse is on tile " + tile);
		hoveredTile = tile;
	}
	public void selectBox(int x1, int y1, int x2, int y2) {
		Position p1 = getTileAtPixel(new Position(x1,y1));
		Position p2 = getTileAtPixel(new Position(x2,y2));
		hoveredArea = new Area(p1.getIntX(),p1.getIntY(), p2.getIntX(), p2.getIntY());
		
	}
	public void mouseClick(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
		System.out.println(currentMode);
		
		if(currentMode == BuildMode.ROAD) {
			
			world[tile.getIntX()][tile.getIntY()].buildRoad(true);
		}else if(currentMode == BuildMode.WALL) {
			
			world[tile.getIntX()][tile.getIntY()].setHasWall(true,rotate);
			
		}
		
	}
	
	public void zoomView(int scroll, int mx, int my) {
		int newTileSize;
		if(scroll > 0) {
			newTileSize = (int) ((tileSize - 1) * 0.95);
		}
		else {
			newTileSize = (int) ((tileSize + 1) * 1.05);
		}
		if (newTileSize > 0) {
			Position tile = getTileAtPixel(new Position(mx, my));
			tileSize = newTileSize;
			Position focalPoint = tile.multiply(tileSize).subtract(viewOffset);
			viewOffset.x -= mx - focalPoint.x;
			viewOffset.y -= my - focalPoint.y;
		}
	}

	public void shiftView(int dx, int dy) {

		viewOffset.x += dx;
		viewOffset.y += dy;
//		System.out.println(viewOffset.x + "curview" + viewOffset.y);
	}

	public void updateGame() {
		ticks++;

	}

	public int getMoney() {
		return money;
	}
	public void setBuildMode(BuildMode b) {
		if(currentMode == b) {
			currentMode = BuildMode.NOMODE;
		}else {
			currentMode = b;
		}
		
	}
	public void rotateBlock() {
		
		if(rotate ==3) {
			rotate = 0;
			System.out.println("reset rotate");
		}else {
			System.out.println("rotating");
			rotate++;
		}
		
	}
	
	public BuildMode getMode() {
		return currentMode;
		
	}
	public void resetHoveredArea() {
		hoveredArea = new Area(0,0,0,0);
	}

	protected void drawGame(Graphics g) {
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());

		draw(g);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		Toolkit.getDefaultToolkit().sync();
	}

}
