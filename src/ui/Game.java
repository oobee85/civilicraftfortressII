package ui;
import java.awt.*;
import java.util.List;
import java.awt.image.*;
import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	public static final int NUM_DEBUG_DIGITS = 3;
	public static int ticks;
	private BufferedImage terrainImage;
	private BufferedImage minimapImage;
	private BufferedImage heightMapImage;
	ArrayList<Position> structureLoc = new ArrayList<Position>();
	ArrayList<Unit> selectUnits = new ArrayList<Unit>();
	HashMap<ResourceType, Resource> resources = new HashMap<ResourceType, Resource>();
	LinkedList<Building> buildings = new LinkedList<Building>();
	LinkedList<Structure> structures = new LinkedList<Structure>();
	
	public static int tileSize = 10;
	public boolean selectedUnit = false;
	private int money;
	private int ironOre;
	private int copperOre;
	private Position viewOffset;
	private Position hoveredTile;
	private Area hoveredArea;
	private BuildMode currentMode;
	private boolean showHeightMap;
	private int rotate = 0;
	
	private volatile int panelWidth;
	private volatile int panelHeight;
	private int fastModeTileSize = 10;
	
	private GUIController guiController;
	
	public static boolean DEBUG_DRAW = false;
	
	public World world;
	
	public Game(GUIController guiController) {
		this.guiController = guiController;
		money = 100;
		hoveredTile = new Position(-1,-1);
		hoveredArea = new Area(0,0,0,0);
		viewOffset = new Position(0, 0);
		currentMode = BuildMode.NOMODE;
		showHeightMap = false;
		
		for(ResourceType resourceType : ResourceType.values()) {
			Resource resource = new Resource(0, resourceType);
			resources.put(resourceType, resource);
		}
		
	}
	
	public void gameTick() {
		boolean changedTerrain = false;
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		ticks++;
		
		if(ticks%20 == 0) {
			updateTerritory();
			
		}
//		if(Math.random() < 0.01) {
//			for(int x = 0; x < world2.getWidth(); x++) {
//				for(int y = 0; y < world2.getHeight(); y++) {
//					world[x][y].liquidAmount *= 0.5;
//				}
//			}
//			makeLake(100);
//			changedTerrain = true;
//		}
		
		
		if(ticks == 1) {
			world.rain();
		}
		// rain event
		if(Math.random() < 0.001) {
			world.rain();
		}
		if(Math.random() < 0.01) {
			world.grow();
		}
		if(ticks%1 == 0) {
			world[world.volcano].liquidType = LiquidType.LAVA;
			world[world.volcano].liquidAmount += .1;
			Liquid.propogate(world);
			changedTerrain = true;
		}
		
		Wildlife.tick(world);
		world.updatePlantDamage();
		if(ticks%5 == 0) {
			updateBuildingAction();
		}
		
		updateBuildingDamage();
		updateStructureDamage();
		
		guiController.updateGUI();
		if(changedTerrain) {
			updateTerrainImages();
		}
		
		
		
	}
	
	public void generateWorld(MapType mapType, int size) {
		world = new World();
		world.generateWorld(mapType, size);
		makeRoad();
		updateTerrainImages();
	}
	
	public void updateTerrainImages() {
		BufferedImage[] images = world.createTerrainImage();
		this.terrainImage = images[0];
		this.minimapImage = images[1];
		this.heightMapImage = images[2];
	}

	public void updateBuildingAction() {
		
		for(Building building : buildings) {
			if(building.getBuildingType() == BuildingType.MINE && building.getTile().getHasOre() == true) {
				resources.get(building.getTile().getOre().getResourceType()).addAmount(1);
			}
		}
		
	}
	

	public void updateBuildingDamage() {
		LinkedList<Building> buildingsNew = new LinkedList<Building>();

		for (Building building : buildings) {
			Tile tile = building.getTile();
			if (tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
				building.takeDamage(damageTaken);

			}
		}	
		
		for (Building building : buildings) {

			Tile tile = building.getTile();
			if (building.isDead() == true) {
				tile.setBuilding(null);
			} else {
				buildingsNew.add(building);
			}
		}
		buildings = buildingsNew;
		
	}
	public void updateStructureDamage() {
		LinkedList<Structure> structuresNew = new LinkedList<Structure>();

		for (Structure structure : structures) {
			Tile tile = structure.getTile();
			if (tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				double damageTaken = tile.liquidAmount * tile.liquidType.getDamage();
				structure.takeDamage(damageTaken);

			}
		}	
		
		for (Structure structure : structures) {

			Tile tile = structure.getTile();
			if (structure.isDead() == true) {
				tile.setStructure(null);
			} else {
				structuresNew.add(structure);
			}
		}
		structures = structuresNew;
		
	}
	
	public void setViewSize(int width, int height) {
		panelWidth = width;
		panelHeight = height;
	}

	
	public void flipTable() {
		for(int x = 0; x < world.heightMap.length; x++) {
			for(int y = 0; y < world.heightMap[0].length; y++) {
				world.heightMap[x][y] = Math.max(Math.min(1-world.heightMap[x][y], 1), 0);
			}
		}
		updateTerrainImages();
	}
	

	private void makeRoad() {
		double highest = -1000;
		Tile highestTile = null;
		double lowest = +1000;
		Tile lowestTile = null;
		for(Tile tile: world.getTiles()) {
			if(world.getHeight(tile.getLocation()) > highest) {
				highestTile = tile;
				highest = world.getHeight(tile.getLocation());
			}
			if(world.getHeight(tile.getLocation()) < lowest) {
				lowestTile = tile;
				lowest = world.getHeight(tile.getLocation());
			}
		}
		
		TileLoc startTile = new TileLoc((int) (Math.random() * world.getWidth()), (int) (Math.random() * world.getHeight()));
		startTile = highestTile.getLocation();
		TileLoc targetTile = lowestTile.getLocation();
		
		
		TileLoc current = startTile;
		TileLoc previous = null;
		TileLoc previous2 = null;
		while(true) {
//			world[current].setRoad(true, "left_down");
			
			List<Tile> neighbors = Utils.getNeighbors(world[current], world);
			TileLoc best = null;
			double bestValue = Double.MAX_VALUE;
			for(Tile tile : neighbors) {
				double delta = Math.abs(world.getHeight(tile.getLocation()) - world.getHeight(current));
				double distance = tile.getLocation().distanceTo(targetTile);
				double value = delta*20 + distance;
				if(!tile.getHasRoad() && !tile.getLocation().equals(previous) && !tile.getLocation().equals(previous2) && value < bestValue) {
					bestValue = value;
					best = tile.getLocation();
				}
			}
			if(best == null) {
				break;
			}
			current = best;
			
			if(previous != null  && previous2 != null) {
				boolean[] directions = new boolean[4];
				String s = "";
				if(previous2.y == previous.y + 1) {
					directions[2] = true;
				}
				if(previous2.x == previous.x + 1) {
					directions[1] = true;
				}
				if(previous2.y == previous.y - 1) {
					directions[0] = true;
				}
				if(previous2.x == previous.x - 1) {
					directions[3] = true;
				}
				if(current.y == previous.y + 1) {
					directions[2] = true;
				}
				if(current.x == previous.x + 1) {
					directions[1] = true;
				}
				if(current.y == previous.y - 1) {
					directions[0] = true;
				}
				if(current.x == previous.x - 1) {
					directions[3] = true;
				}
				for(int direction = 0; direction < directions.length; direction++) {
					if(directions[direction]) {
						s += Utils.DIRECTION_STRINGS[direction];
					}
				}
				System.out.println(current + s);
				world[previous].setRoad(true, s);
			}
			if(previous == targetTile) {
				break;
			}
			previous2 = previous;
			previous = current;
		}
	}
	private void makeCastle(Position start, Position end) {
		double castleDistance = Utils.getRandomNormal(5);
		Position halfway = start.multiply(castleDistance).add(end.multiply(1-castleDistance));
		TileLoc loc = new TileLoc(halfway.getIntX(), halfway.getIntY());
		Tile tile = world[loc];
//		Structure struct = new Structure(StructureType.CASTLE, halfway);
		if(tile.canBuild() == true) {
			buildStructure(StructureType.CASTLE, tile);
//			world[halfway.getIntX()][halfway.getIntY()].setStructure(struct);
//			structureLoc.add(halfway);
		}else {
			makeCastle(start, end);
		}
		
		
	}
	
	private void turnRoads(TileLoc current, TileLoc prev) {
		if(current.x-1 < 0 || current.x+1 >= world.getWidth()) {
			return;
		}
		if(world[current].canBuild()==true) {
			
			// makes turns bot left -> top right
			TileLoc left = new TileLoc(current.x-1, current.y);
			if(world[left].canBuild()==true) {
				if (left.x == prev.x && current.y == prev.y) {
					world[left].setRoad(true, "left_down");
					world[current].setRoad(true, "right_up");
				} else if (left.x == prev.x && current.x + 1 == prev.y) {
					world[left].setRoad(true, "left_down");
					world[current].setRoad(true, "right_up");
				}	
			}
			

			// makes turns bot right -> top left
			TileLoc right = new TileLoc(current.x+1, current.y);
			if(world[right].canBuild()==true) {
				if (right.x == prev.x && current.y == prev.y) {
					world[right].setRoad(true, "right_down");
					world[current].setRoad(true, "left_up");
				} else if (right.x == prev.x && current.y + 1 == prev.y) {
					world[right].setRoad(true, "right_down");
					world[current].setRoad(true, "left_up");
				}
			}
			

			if (world[current].getHasRoad() == false) {
				world[current].setRoad(true, "top_down");
			}
			
			
//			Tile t =  world[current.getIntX()][current.getIntY()];
//			if(
////					world[current.getIntX()][current.getIntY()].getHasRoad() == true && 
//					t.getTerrain().isBridgeable(t.getTerrain()) == true) {
//				System.out.println("bridging");
//				world[current.getIntX()][current.getIntY()].setHasBuilding(Buildings.BRIDGE);
//			}

		}

	}
	
	public void draw(Graphics g) {
		
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, viewOffset.divide(tileSize).getIntX() - 2);
		int lowerY = Math.max(0, viewOffset.divide(tileSize).getIntY() - 2);
		int upperX = Math.min(world.getWidth(), lowerX + panelWidth/tileSize + 4);
		int upperY = Math.min(world.getHeight(), lowerY + panelHeight/tileSize + 4);
		
		if(Game.tileSize < fastModeTileSize) {
			if(showHeightMap) {
				g.drawImage(heightMapImage, 0, 0, Game.tileSize*world.getWidth(), Game.tileSize*world.getHeight(), null);
			}
			else {
				g.drawImage(terrainImage, 0, 0, Game.tileSize*world.getWidth(), Game.tileSize*world.getHeight(), null);
			}
		}
		else {
			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile t = world[new TileLoc(i, j)];
					
					if(t.getHasStructure() == true) {
						setTerritory(new TileLoc(i,j));
					}
					if(i==hoveredTile.getIntX() && j==hoveredTile.getIntY()) {
						t.highlight(g);
					}
					if(hoveredArea.contains(i, j)) {
						t.highlight(g);
					}
					
					if(showHeightMap) {
						t.drawHeightMap(g, world.heightMap[i][j]);
					}
					else {
						t.setRecentTick(ticks);
						t.draw(g, currentMode);
					}
					
				}
			}
			for(Animal animal : Wildlife.getAnimals()) {
				g.drawImage(animal.getImage(0), animal.getTile().getLocation().x * Game.tileSize, animal.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				animal.getTile().drawHealthBar(g, animal);
			}
			if(DEBUG_DRAW) {
				if(Game.tileSize >= 36) {
					int[][] rows = new int[world.getWidth()][world.getHeight()];
					int fontsize = Game.tileSize/4;
					fontsize = Math.min(fontsize, 13);
					Font font = new Font("Consolas", Font.PLAIN, fontsize);
					g.setFont(font);
					int stringWidth = g.getFontMetrics().stringWidth(String.format("??=%." + NUM_DEBUG_DIGITS + "f", 0.0));
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							TileLoc loc = new TileLoc(i, j);
							Tile tile = world[loc];
							int x = i * Game.tileSize + 2;
							int y = j * Game.tileSize + fontsize/2;
							
							g.setColor(Color.black);
							int numrows = 2;
							if(world[loc].liquidType == LiquidType.DRY) {
								numrows = 1;
							}
							g.fillRect(x, y + 2, stringWidth, numrows*fontsize);
							g.setColor(Color.green);
							g.drawString(String.format("H=%." + NUM_DEBUG_DIGITS + "f", world.heightMap[i][j]), x, y + (++rows[i][j])*fontsize);
							
							if(world[loc].liquidType != LiquidType.DRY) {
								g.drawString(String.format(world[loc].liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount), x, y + (++rows[i][j])*fontsize);
							}
						}
					}
					for(Animal animal : Wildlife.getAnimals()) {
						animal.getTile().drawDebugStrings(g, animal.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Plant plant : world.plantsLand) {
						plant.getTile().drawDebugStrings(g, plant.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Plant plant : world.plantsAquatic) {
						plant.getTile().drawDebugStrings(g, plant.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Building building : buildings) {
						building.getTile().drawDebugStrings(g, building.getDebugStrings(), rows, fontsize, stringWidth);
					}
					for(Structure structure : structures) {
						structure.getTile().drawDebugStrings(g, structure.getDebugStrings(), rows, fontsize, stringWidth);
					}
				}
			}
		}
		
	}
	private void updateTerritory() {
		for(Structure structure : structures) {
			structure.updateCulture();
		}
	}
	private void setTerritory(TileLoc p) {
		int culture = world[p].getStructure().getCulture();
		double area = culture * Structure.CULTURE_AREA_MULTIPLIER;
		double radius = Math.sqrt(area);
		expandTerritory(radius, p);	
	}
	private void expandTerritory(double radius, TileLoc p) {
		int r = (int)Math.ceil(radius);
		for (int i=0-r; i <= r; i++) {
			for (int j=0-r; j <= r; j++) {
				double distanceFromCenter = Math.sqrt(i*i + j*j);
				if(distanceFromCenter < radius) {
					if(p.x+i >= 0 && p.x+i < world.getWidth() && p.y+j >= 0 && p.y+j < world.getHeight()) {
						world[new TileLoc(p.x+i, p.y+j)].setTerritory(true);
					}
				}
			}
		}
	}
	
	public void rightClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx,my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
		if(currentMode == BuildMode.NOMODE) {
			if(world[loc].getHasUnit() == true) {
				selectedUnit = true;
				System.out.println("selected unit");
			}
		}
		
	}
	

	public static void printPoint(Point p) {
		System.out.println("Point: (" + p.x + ", " + p.y + ")");
	}

	public Position getTileAtPixel(Position pixel) {
		Position tile = pixel.add(viewOffset).divide(tileSize);
		return tile;
	}
	public Position getPixelForTile(Position tile) {
		return tile.multiply(tileSize).subtract(viewOffset);
	}

	public void mouseOver(int mx, int my) {
		Position tile = getTileAtPixel(new Position(mx, my));
//		System.out.println("Mouse is on tile " + tile);
		hoveredTile = tile;
	}
	public void selectBox(int x1, int y1, int x2, int y2) {
		Position p1 = getTileAtPixel(new Position(x1,y1));
		Position p2 = getTileAtPixel(new Position(x2,y2));
		
			
		hoveredArea = new Area(p1.getIntX(),p1.getIntY(), p2.getIntX()+1, p2.getIntY()+1);
			
		selectTile();
		
	}
	
	private void selectTile() {
		for (int i = 0; i < hoveredArea.getIntX2()-hoveredArea.getIntX1(); i++) {
			for (int j = 0; j < hoveredArea.getIntY2()-hoveredArea.getIntY1(); j++) {
				TileLoc loc = new TileLoc(hoveredArea.getIntX1()+i, hoveredArea.getIntY1()+j);
				world[loc].setHighlight(true);
				if(world[loc].getUnit() != null) {
					selectUnits.add(world[loc].getUnit());
				}
			}
		}
		
	}
	public void mouseClick(int mx, int my) {
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		System.out.println(currentMode);
		Tile tile = world[loc];
		
		if(currentMode == BuildMode.ROAD) {
			if(tile.canBuild() == true) {
				tile.setRoad(true, null);
			}
		}
			
		if(currentMode == BuildMode.BARRACKS) {
			if(tile.canBuild() == true) {
				buildStructure(StructureType.BARRACKS, tile);
			}
		} 
		
		if(currentMode == BuildMode.WALL) {
			if(tile.canBuild() == true) {
				buildBuilding(BuildingType.WALL_BRICK, tile);
			}
		}
		
		if(currentMode == BuildMode.MINE) {
			if(tile.canBuild() == true || tile.getHasOre() == true) {
				buildBuilding(BuildingType.MINE, tile);
			}
		}
		
		if(currentMode == BuildMode.IRRIGATE) {
			if(tile.canBuild() == true && tile.canPlant() == true) {
				buildBuilding(BuildingType.IRRIGATION, tile);
			}
		}
		
		
	}
	private void buildBuilding(BuildingType bt, Tile t) {
		Building building = new Building(bt, t);
		t.setBuilding(building);
		buildings.add(building);
	}
	private void buildStructure(StructureType st, Tile t) {
		Structure structure = new Structure(st, t);
		t.setStructure(structure);
		structures.add(structure);
	}
	public void buildUnit(UnitType u) {
		Tile t = structures.get(0).getTile();
		Unit unit = new Unit(u , t);
		t.setUnit(unit);
		
	}
	
	public void doubleClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
		if(world[loc].isStructure(StructureType.CASTLE) == true ) {
			exitCity();
		}
	}
	public void exitCity() {
		guiController.toggleCityView();
	}
	public void exitTile() {
		guiController.toggleTileView();
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
//			System.out.println("Tilesize: "+tileSize);
		}
	}

	public void shiftView(int dx, int dy) {

		viewOffset.x += dx;
		viewOffset.y += dy;
//		System.out.println(viewOffset.x + "curview" + viewOffset.y);
	}
	public void moveViewTo(double ratiox, double ratioy) {
		Position tile = new Position(ratiox*world.getWidth(), ratioy*world.getHeight());
		Position pixel = tile.multiply(tileSize).subtract(new Position(panelWidth/2, panelHeight/2));
		viewOffset = pixel;
	}


	public int getMoney() {
		return money;
	}
	public int getResourceAmount(ResourceType resourceType) {
		return resources.get(resourceType).getAmount();
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
	public int getTileSize() {
		return tileSize;
	}
	
	public BuildMode getMode() {
		return currentMode;
		
	}
	public void resetHoveredArea() {
		hoveredArea = new Area(0,0,0,0);
	}
	
	protected void drawMinimap(Graphics g, int x, int y, int w, int h) {
		if(showHeightMap) {
			g.drawImage(heightMapImage, x, y, w, h, null);
		}
		else {
			g.drawImage(minimapImage, x, y, w, h, null);
		}
		Position offsetTile = getTileAtPixel(viewOffset);
		int boxx = (int) (offsetTile.x * w / world.getWidth() / 2);
		int boxy = (int) (offsetTile.y * h / world.getHeight() / 2);
		int boxw = (int) (panelWidth/Game.tileSize * w / world.getWidth());
		int boxh = (int) (panelHeight/Game.tileSize * h / world.getHeight());
//		System.out.println(boxx);
		g.setColor(Color.yellow);
		g.drawRect(x + boxx, y + boxy, boxw, boxh);
	}
	protected void drawGame(Graphics g) {
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		Toolkit.getDefaultToolkit().sync();
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}
}
