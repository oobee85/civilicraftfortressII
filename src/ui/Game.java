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
	private Unit selectedUnit;
	HashMap<ResourceType, Resource> resources = new HashMap<ResourceType, Resource>();
	LinkedList<Building> buildings = new LinkedList<Building>();
	LinkedList<Structure> structures = new LinkedList<Structure>();
	
	
	public static int tileSize = 10;
//	public boolean selectedUnit = false;
	private int money;
	private Position viewOffset;
	private Position hoveredTile;
	private BuildMode currentMode;
	private boolean showHeightMap;
	
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
//		hoveredArea = new Area(0,0,0,0);
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
			if(world.volcano != null) {
				world[world.volcano].liquidType = LiquidType.LAVA;
				world[world.volcano].liquidAmount += .1;
			}
			Liquid.propogate(world);
			changedTerrain = true;
		}
		
		Wildlife.tick(world);
		world.updatePlantDamage();
		world.updateUnitDamage();
		if(ticks%5 == 0) {
			updateBuildingAction();
			
		}
		moveUnits();
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
		makeRoads();
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
			if(building.getBuildingType() == BuildingType.IRRIGATION && building.getTile().canPlant() == true) {
				resources.get(ResourceType.WHEAT).addAmount(1);
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
	
	private double computeCost(Tile current, Tile two, Tile target) {
		double distanceCosts = 1;
		if(!two.getHasRoad()) {
			double deltaHeight = 10000 * Math.abs(world.getHeight(current.getLocation()) - world.getHeight(two.getLocation()));
			distanceCosts += two.getTerrain().getRoadCost()
							+ deltaHeight * deltaHeight
							+ 1000000*two.liquidAmount*two.liquidType.getDamage();
		}
		return distanceCosts;
	}
	
	private class Path {
		double cost;
		LinkedList<Tile> tiles = new LinkedList<>();
		public Path() {
			cost = 0;
		}
		public void addTile(Tile tile, double addedCost) {
			tiles.add(tile);
			cost += addedCost;
		}
		public Tile getHead() {
			return tiles.getLast();
		}
		public Path clone() {
			Path p = new Path();
			for(Tile t : tiles) {
				p.addTile(t, 0);
			}
			p.cost = cost;
			return p;
		}
		public double getCost() {
			return cost;
		}
		public LinkedList<Tile> getTiles() {
			return tiles;
		}
		@Override
		public String toString() {
			String s = "";
			for(Tile t : tiles) {
				s += t.getLocation() + ", ";
			}
			return s;
		}
	}
	
	private void makeRoadBetween(Tile start, Tile target) {
		PriorityQueue<Path> search = new PriorityQueue<>((x, y) ->  { 
			if(y.getCost() < x.getCost()) {
				return 1;
			}
			else if(y.getCost() > x.getCost()) {
				return -1;
			}
			else {
				return 0;
			}
		});
		
		Path startingPath = new Path();
		startingPath.addTile(start, 0);
		search.add(startingPath);
		
		double bestCost = Double.MAX_VALUE;
		Path selectedPath = null;
		HashMap<Tile, Double> visited = new HashMap<>();
		visited.put(startingPath.getHead(), startingPath.getCost());
		
		int iterations = 0;
		while(!search.isEmpty()) {
			iterations++;
			Path currentPath = search.remove();
			Tile currentTile = currentPath.getHead();
			if(currentTile == target && currentPath.getCost() < bestCost) {
				selectedPath = currentPath;
				bestCost = currentPath.getCost();
				continue;
			}
			if(currentPath.getCost() > bestCost) {
				// if current cost is already more than the best cost
				continue;
			}
			List<Tile> neighbors = Utils.getNeighbors(currentTile, world);
			for(Tile neighbor : neighbors) {
				double cost = computeCost(currentTile, neighbor, target);
				Path p = currentPath.clone();
				p.addTile(neighbor, cost);
				if(visited.containsKey(neighbor)) {
					if(p.getCost() > visited[currentTile]) {
						// Already visited this tile at a lower cost
						continue;
					}
				}
				visited.put(neighbor, p.getCost());
				search.add(p);
			}
		}
		System.out.println("road iterations: " + iterations);
		
		for(Tile t : selectedPath.getTiles()) {
			t.setRoad(true, "asdf");
		}
	}

	private void makeRoads() {
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

		makeRoadBetween(world[new TileLoc(world.getWidth()-1, 0)], world[new TileLoc(0, world.getHeight()-1)]);
		makeRoadBetween(world[new TileLoc(0, 0)], world[new TileLoc(world.getWidth()-1, world.getHeight()-1)]);
		makeRoadBetween(highestTile, lowestTile);
		turnRoads();
		
		makeCastle();
	}
	private void turnRoads() {
		for(Tile tile : world.getTiles()) {
			if(!tile.getHasRoad())
				continue;
			
			Set<Direction> directions = new HashSet<>();
			TileLoc loc = tile.getLocation();
			List<Tile> neighbors = Utils.getNeighbors(tile, world);
			for(Tile t : neighbors) {
				if(!t.getHasRoad())
					continue;
				Direction d = Direction.getDirection(loc, t.getLocation());
				if(d != null)
					directions.add(d);
			}
			String s = "";
			for(Direction d : Direction.values()) {
				if(directions.contains(d)) {
					s += d;
				}
			}
			world[loc].setRoad(true, s);
		}
	}
	private void makeCastle() {
		for(Tile tile :world.getTiles()) {
			if(tile.getHasRoad() == true && tile.canBuild() == true && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
				buildStructure(StructureType.CASTLE, tile);
				break;
			}
		}
	}
	
	
//	private void turnRoads(TileLoc current, TileLoc prev) {
//		if(current.x-1 < 0 || current.x+1 >= world.getWidth()) {
//			return;
//		}
//		if(world[current].canBuild()==true) {
//			
//			// makes turns bot left -> top right
//			TileLoc left = new TileLoc(current.x-1, current.y);
//			if(world[left].canBuild()==true) {
//				if (left.x == prev.x && current.y == prev.y) {
//					world[left].setRoad(true, "left_down");
//					world[current].setRoad(true, "right_up");
//				} else if (left.x == prev.x && current.x + 1 == prev.y) {
//					world[left].setRoad(true, "left_down");
//					world[current].setRoad(true, "right_up");
//				}	
//			}
//			
//
//			// makes turns bot right -> top left
//			TileLoc right = new TileLoc(current.x+1, current.y);
//			if(world[right].canBuild()==true) {
//				if (right.x == prev.x && current.y == prev.y) {
//					world[right].setRoad(true, "right_down");
//					world[current].setRoad(true, "left_up");
//				} else if (right.x == prev.x && current.y + 1 == prev.y) {
//					world[right].setRoad(true, "right_down");
//					world[current].setRoad(true, "left_up");
//				}
//			}
//			
//
//			if (world[current].getHasRoad() == false) {
//				world[current].setRoad(true, "top_down");
//			}
//			
//			
////			Tile t =  world[current.getIntX()][current.getIntY()];
////			if(
//////					world[current.getIntX()][current.getIntY()].getHasRoad() == true && 
////					t.getTerrain().isBridgeable(t.getTerrain()) == true) {
////				System.out.println("bridging");
////				world[current.getIntX()][current.getIntY()].setHasBuilding(Buildings.BRIDGE);
////			}
//
//		}
//
//	}
	
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
//					if(hoveredArea.contains(i, j)) {
//						t.highlight(g);
//					}
					
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
				buildBuilding(BuildingType.WALL_STONE, tile);
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
		if(currentMode == BuildMode.NOMODE && tile.getHasUnit() == true) {
			if(selectedUnit != null && !selectedUnit.equals(tile.getUnit()) ) {
				selectedUnit.selectUnit(false);
				selectedUnit = tile.getUnit();
				selectedUnit.selectUnit(true);
			}else
			if(tile.getUnit().getIsSelected() == false) {
				selectedUnit = tile.getUnit();
				selectedUnit.selectUnit(true);
//				tile.getUnit().selectUnit(true);
			}else {
				deselectUnit();
			}
			
			
		}
		if(currentMode == BuildMode.NOMODE) {
			setDestination(mx, my);
		}
		
		
	}
	public void deselectUnit() {
		System.out.println("deselecting unit");
		if(selectedUnit != null) {
			selectedUnit.selectUnit(false);
			selectedUnit = null;
		}
		
	}
	
	public void setDestination(int mx, int my) {
		
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		Tile destination = world[loc];
		
		if(selectedUnit != null && destination != null ) {
			selectedUnit.setTargetTile(destination);
		}
		
	}
	
	private void moveUnits() {
		
		for(Unit unit : world.units) {
			if(unit.getTargetTile() == null) {
				continue;
			}
			Tile currentTile = unit.getTile();
			double bestDistance = Integer.MAX_VALUE;
			Tile bestTile = currentTile;
			
			for(Tile tile : Utils.getNeighbors(currentTile, world)) {
				
					double distance = tile.getLocation().distanceTo(unit.getTargetTile().getLocation() );
					if(distance < bestDistance) {
						bestDistance = distance;
						bestTile = tile;
					}
				
			}
			bestTile.setUnit(unit);
			unit.setTile(bestTile);
			
			currentTile.setUnit(null);
			
		}
		
		
		
		
		
		
		
	}
	
	private void buildBuilding(BuildingType bt, Tile tile) {
		if(tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
			Building building = new Building(bt, tile);
			tile.setBuilding(building);
			buildings.add(building);
		}
		
	}
	private void buildStructure(StructureType st, Tile tile) {
		if(tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
			Structure structure = new Structure(st, tile);
			tile.setStructure(structure);
			structures.add(structure);
		}
		
	}
	public void buildUnit(UnitType u) {
		Tile tile = structures.get(0).getTile();
		Unit unit = new Unit(u , tile);
		tile.setUnit(unit);
		world.units.add(unit);
		
	}
	
	public void doubleClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
		if(world[loc].getStructure() != null && world[loc].getStructure().getStructureType() == StructureType.CASTLE) {
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
	public int getTileSize() {
		return tileSize;
	}
	
	public BuildMode getMode() {
		return currentMode;
		
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
	public Color getBackgroundColor() {
		int currentDayOffset = ticks%(World.DAY_DURATION + World.NIGHT_DURATION);
		double ratio = 1;
		if(currentDayOffset < World.TRANSITION_PERIOD) {
			ratio = 0.5 + 0.5*currentDayOffset/World.TRANSITION_PERIOD;
		}
		else if(currentDayOffset < World.DAY_DURATION - World.TRANSITION_PERIOD) {
			ratio = 1;
		}
		else if(currentDayOffset < World.DAY_DURATION + World.TRANSITION_PERIOD) {
			ratio = 0.5 - 0.5*(currentDayOffset - World.DAY_DURATION)/World.TRANSITION_PERIOD;
		}
		else if(currentDayOffset < World.DAY_DURATION + World.NIGHT_DURATION - World.TRANSITION_PERIOD) {
			ratio = 0;
		}
		else {
			ratio = 0.5 - 0.5*(World.DAY_DURATION + World.NIGHT_DURATION - currentDayOffset)/World.TRANSITION_PERIOD;
		}
		int c = (int)(ratio * 255);
		return new Color(c, c, c);
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}
}
