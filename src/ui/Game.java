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
	HashMap<ItemType, Item> resources = new HashMap<ItemType, Item>();
	LinkedList<Building> buildings = new LinkedList<Building>();
	LinkedList<Structure> structures = new LinkedList<Structure>();
	
	
	public static int tileSize = 10;
//	public boolean selectedUnit = false;
	private int money;
	private Position viewOffset;
	private TileLoc hoveredTile;
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
		hoveredTile = new TileLoc(-1,-1);
//		hoveredArea = new Area(0,0,0,0);
		viewOffset = new Position(0, 0);
		currentMode = BuildMode.NOMODE;
		showHeightMap = false;
		
		for(ItemType resourceType : ItemType.values()) {
			Item resource = new Item(0, resourceType);
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
				world[world.volcano].liquidAmount += .05;
			}
			Liquid.propogate(world);
			changedTerrain = true;
		}
		
		Wildlife.tick(world);
		world.updatePlantDamage();
		world.updateUnitDamage();
		if(ticks%5 == 0) {
			updateBuildingAction();
			changedTerrain = true;
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
			if(building.getBuildingType() == BuildingType.MINE && building.getTile().getHasResource() == true) {
				resources.get(building.getTile().getResourceType().getResourceType()).addAmount(1);
			}
			if(building.getBuildingType() == BuildingType.IRRIGATION && building.getTile().canPlant() == true) {
				resources.get(ItemType.WHEAT).addAmount(1);
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
		for(Tile tile : world.getTiles()) {
			tile.setHeight(1 - tile.getHeight());
		}
		updateTerrainImages();
	}
	
	private double computeCost(Tile current, Tile next, Tile target) {
		double distanceCosts = 1;
		if(next.getRoadType() == null) {
			double deltaHeight = 10000 * Math.abs(current.getHeight() - next.getHeight());
			distanceCosts += next.getTerrain().getRoadCost()
							+ deltaHeight * deltaHeight
							+ 1000000*next.liquidAmount*next.liquidType.getDamage();
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
			t.setRoad(RoadType.ROAD_STONE, "asdf");
		}
	}

	private void makeRoads() {
		double highest = -1000;
		Tile highestTile = null;
		double lowest = +1000;
		Tile lowestTile = null;
		for(Tile tile: world.getTiles()) {
			if(tile.getHeight() > highest) {
				highestTile = tile;
				highest = tile.getHeight();
			}
			if(tile.getHeight() < lowest) {
				lowestTile = tile;
				lowest = tile.getHeight();
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
			if(tile.getRoadType() == null)
				continue;
			
			Set<Direction> directions = new HashSet<>();
			TileLoc loc = tile.getLocation();
			List<Tile> neighbors = Utils.getNeighbors(tile, world);
			for(Tile t : neighbors) {
				if(t.getRoadType() == null)
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
			world[loc].setRoad(RoadType.ROAD_STONE, s);
		}
	}
	private void makeCastle() {
		for(Tile tile :world.getTilesRandomly()) {
			if(tile.getRoadType() != null && tile.canBuild() == true && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
				buildUnit(UnitType.WORKER, tile);
				Structure s = new Structure(StructureType.CASTLE, tile);
				tile.setStructure(s);
				structures.add(s);
				
				break;
			}
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
			double highest = 0;
			double lowest = 1;
			if(showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						highest = Math.max(highest, world[new TileLoc(i, j)].getHeight());
						lowest = Math.min(lowest, world[new TileLoc(i, j)].getHeight());
					}
				}
			}
			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile t = world[new TileLoc(i, j)];
					int x = t.getLocation().x * Game.tileSize;
					int y = t.getLocation().y * Game.tileSize;
					int w = Game.tileSize;
					int h = Game.tileSize;
					
					if(t.getHasStructure() == true) {
						setTerritory(new TileLoc(i,j));
					}
					
					if(showHeightMap) {
						t.drawHeightMap(g, (world[new TileLoc(i, j)].getHeight() - lowest) / (highest - lowest));
					}
					else {
						g.drawImage(t.getTerrain().getImage(Game.tileSize), x, y, w, h, null);
//						t.drawEntities(g, currentMode);
						
						if(t.getHasResource()) {
							g.drawImage(t.getResourceType().getImage(Game.tileSize), x, y, w, h, null);
						}
						if(t.getIsTerritory()) {
							g.setColor(Tile.TERRITORY_COLOR);
							Utils.setTransparency(g, 0.5f);
							g.fillRect(x, y, w, h); 
							Utils.setTransparency(g, 1);
						}
						if (t.getRoadType() != null) {
							g.drawImage(t.getRoadImage(), x, y, w, h, null);
						}
						if(t.liquidType != LiquidType.DRY) {
							double alpha = Utils.getAlphaOfLiquid(t.liquidAmount);
//							 transparency liquids
							Utils.setTransparency(g, alpha);
							g.setColor(t.liquidType.getColor(Game.tileSize));
							g.fillRect(x, y, w, h);
							Utils.setTransparency(g, 1);
							
							int size = (int) Math.min(Math.max(Game.tileSize*t.liquidAmount / 0.2, 1), Game.tileSize);
							g.setColor(t.liquidType.getColor(Game.tileSize));
							g.fillRect(x + Game.tileSize/2 - size/2, y + Game.tileSize/2 - size/2, size, size);
							g.drawImage(t.liquidType.getImage(Game.tileSize), x + Game.tileSize/2 - size/2, y + Game.tileSize/2 - size/2, size, size, null);
						}
					}
				}
			}
			for(Plant p : world.plantsLand) {
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, p);
			}
			for(Plant p : world.plantsAquatic) {
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, p);
			}
			for(Building b : this.buildings) {
				g.drawImage(b.getImage(0), b.getTile().getLocation().x * Game.tileSize, b.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, b);
			}
			for(Structure s : this.structures) {
				g.drawImage(s.getImage(0), s.getTile().getLocation().x * Game.tileSize, s.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, s);
			}
			for(Animal animal : Wildlife.getAnimals()) {
				g.drawImage(animal.getImage(0), animal.getTile().getLocation().x * Game.tileSize, animal.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, animal);
			}
			for(Unit unit : world.units) {
				if(unit.getIsSelected()) {
					g.setColor(Color.pink);
					Utils.setTransparency(g, 0.8f);
					Graphics2D g2d = (Graphics2D)g;
					Stroke currentStroke = g2d.getStroke();
					int strokeWidth = Game.tileSize /8;
					g2d.setStroke(new BasicStroke(strokeWidth));
					g.drawOval(unit.getTile().getLocation().x * Game.tileSize + strokeWidth/2, unit.getTile().getLocation().y * Game.tileSize + strokeWidth/2, Game.tileSize-1 - strokeWidth, Game.tileSize-1 - strokeWidth);
					g2d.setStroke(currentStroke);
					Utils.setTransparency(g, 1f);
				}
				g.drawImage(unit.getImage(0), unit.getTile().getLocation().x * Game.tileSize, unit.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, unit);
			}
			if(!showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						double brightness = world.getDaylight() + world[new TileLoc(i, j)].getBrightness();
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int)(255 * (1 - brightness))));
						g.fillRect(i * Game.tileSize, j * Game.tileSize, Game.tileSize, Game.tileSize);
					}
				}
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
							g.drawString(String.format("H=%." + NUM_DEBUG_DIGITS + "f", world[new TileLoc(i, j)].getHeight()), x, y + (++rows[i][j])*fontsize);
							
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
			g.setColor(new Color(0, 0, 0, 64));
			g.drawRect(hoveredTile.x * Game.tileSize, hoveredTile.y * Game.tileSize, Game.tileSize-1, Game.tileSize-1);
			g.drawRect(hoveredTile.x * Game.tileSize + 1, hoveredTile.y * Game.tileSize + 1, Game.tileSize - 3, Game.tileSize - 3);
		}
	}

	public void drawHealthBar(Graphics g, Thing thing) {
		if( Game.tileSize <= 30) {
			return;
		}
		if(Game.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(hoveredTile)) {
			int x = thing.getTile().getLocation().x * Game.tileSize + 1;
			int y = thing.getTile().getLocation().y * Game.tileSize + 1;
			int w = Game.tileSize - 1;
			int h = Game.tileSize / 4 - 1;
			int greenBarWidth = (int) (thing.getHealth() / thing.getMaxHealth() * (w - 4));
			int greenBarHeight = h - 4;
			if (thing.isSideHealthBar()) {
				int temp = w;
				w = h;
				h = temp;
				temp = greenBarWidth;
				greenBarWidth =greenBarHeight;
				greenBarHeight = temp;
			}
			g.setColor(Color.BLACK);
			g.fillRect(x, y, w, h);
			
			g.setColor(Color.RED);
			g.fillRect(x + 2, y + 2, w - 4, h - 4);

			g.setColor(Color.GREEN);
			g.fillRect(x + 2, y + 2, greenBarWidth, greenBarHeight);
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
		hoveredTile = new TileLoc(tile.getIntX(), tile.getIntY());
	}
	
	public void mouseClick(int mx, int my) {
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		System.out.println(currentMode);
		Tile tile = world[loc];
		if(currentMode == BuildMode.NOMODE && tile.getHasUnit() == true) {
			toggleUnitSelectOnTile(tile);
		}
		if(currentMode == BuildMode.NOMODE) {
			setDestination(mx, my);
		}
	}
	public void toggleUnitSelectOnTile(Tile tile) {
		if(selectedUnit == tile.getUnit()) {
			workerView();
			deselectUnit();
			return;
		}
		else if(selectedUnit != null) {
			workerView();
			deselectUnit();
		}
		selectedUnit = tile.getUnit();
		selectedUnit.setIsSelected(true);
		workerView();
		
	}
	private void workerView() {
		if(selectedUnit != null && selectedUnit.getUnitType() == UnitType.WORKER) {
			guiController.toggleWorkerView();
		}
	}
	public void deselectUnit() {
		System.out.println("deselecting unit");
		if(selectedUnit != null) {
			selectedUnit.setIsSelected(false);
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
			unit.tick();
			if(unit.getTargetTile() == null) {
				continue;
			}
			if(unit.readyToMove()) {
				Tile currentTile = unit.getTile();
				double bestDistance = Integer.MAX_VALUE;
				Tile bestTile = currentTile;
				
				for(Tile tile : Utils.getNeighbors(currentTile, world)) {
					if(tile.getHasUnit()) {
						continue;
					}
					if(tile.getHasBuilding() == true && tile.getBuilding().getBuildingType().canMoveThrough() == false) {
						continue;
					}
					double distance = tile.getLocation().distanceTo(unit.getTargetTile().getLocation() );
					if(distance < bestDistance) {
						bestDistance = distance;
						bestTile = tile;
					}
					
				}
				unit.moveTo(bestTile);
			}
		}
	}
	
	public void buildBuilding(BuildingType bt) {
		if(selectedUnit != null && selectedUnit.getUnitType() == UnitType.WORKER) {
			if(selectedUnit.getTile().getHasBuilding() == false && selectedUnit.getTile().getHasStructure() == false) {
				if (bt == BuildingType.IRRIGATION && selectedUnit.getTile().canPlant() == false) {
					return;
				}
				Building building = new Building(bt, selectedUnit.getTile());
				selectedUnit.getTile().setBuilding(building);
				buildings.add(building);

			}
			
		}
		
	}
	public void buildRoad(RoadType rt) {
		if(selectedUnit != null && selectedUnit.getUnitType() == UnitType.WORKER) {
			String s = "";
			s += Direction.NORTH;
			selectedUnit.getTile().setRoad(rt, s);
		}
		
	}
	public void buildStructure(StructureType st) {
		if(selectedUnit != null && selectedUnit.getUnitType() == UnitType.WORKER) {
			if(selectedUnit.getTile().getHasBuilding() == false && selectedUnit.getTile().getHasStructure() == false) {
				Structure structure = new Structure(st, selectedUnit.getTile());
				selectedUnit.getTile().setStructure(structure);
				structures.add(structure);
			}
			
		}
		
	}
	public void buildUnit(UnitType u, Tile tile) {
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
	public void exitWorkerView() {
		guiController.toggleWorkerView();
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
	public int getResourceAmount(ItemType resourceType) {
		return resources.get(resourceType).getAmount();
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
		double ratio = world.getDaylight();
		int c = (int)(ratio * 255);
		return new Color(c, c, c);
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}
	public UnitType getSelectedUnit() {
		return selectedUnit.getUnitType();
	}
}
