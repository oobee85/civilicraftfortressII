package ui;
import java.awt.*;
import java.util.List;
import java.util.Map.*;
import java.awt.image.*;
import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	private Font damageFont = new Font("Comic Sans MS", Font.BOLD, 14);
	private Image redHitsplatImage = Utils.loadImage("resources/Images/interfaces/redhitsplat.png");
	private Image blueHitsplatImage = Utils.loadImage("resources/Images/interfaces/bluehitsplat.png");
	private Image greenHitsplatImage = Utils.loadImage("resources/Images/interfaces/greenhitsplat.png");
	private Image targetImage = Utils.loadImage("resources/Images/interfaces/ivegotyouinmysights.png");
	private Image spawnLocationImage = Utils.loadImage("resources/Images/interfaces/queuelocation.png");
	private int skipUntilTick;
	private BufferedImage terrainImage;
	private BufferedImage minimapImage;
	private BufferedImage heightMapImage;
	ArrayList<Position> structureLoc = new ArrayList<Position>();
	private Thing selectedThing;
	private UnitType selectedUnitToSpawn;
	private int numCutTrees = 10;
	private int buildingsUntilOgre = 8;
	
	HashMap<ItemType, Item> resources = new HashMap<ItemType, Item>();
	HashMap<ResearchType, Research> researches = new HashMap<>();
	
	HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	
	private Research researchTarget;
	
	private int money;
	private Position viewOffset;
	private TileLoc hoveredTile;
	private boolean showHeightMap;
	
	private volatile int panelWidth;
	private volatile int panelHeight;
	private int fastModeTileSize = 10;
	
	private GUIController guiController;

	public static final int NUM_DEBUG_DIGITS = 3;
	public static int ticks;
	public static int tileSize;
	public static boolean USE_BIDIRECTIONAL_A_STAR = true;
	public static boolean DEBUG_DRAW = false;
	public static boolean DISABLE_NIGHT = false;
	
	public World world;
	
	public Game(GUIController guiController) {
		this.guiController = guiController;
		money = 100;
		hoveredTile = new TileLoc(-1,-1);
		viewOffset = new Position(0, 0);
		showHeightMap = false;
		
		for(ItemType itemType : ItemType.values()) {
			Item item = new Item(0, itemType);
			if(itemType == ItemType.WOOD || itemType == ItemType.STONE || itemType == ItemType.FOOD) {
				item = new Item(200, itemType);
			}
			resources.put(itemType, item);
		}
		for(ResearchType researchType : ResearchType.values()) {
			Research res = new Research(researchType);
			researches.put(researchType, res);
		}
		for(Research research : researches.values()) {
			for(ResearchType requiredType : research.getType().getChildren()) {
				research.getRequirement().addRequirement(researches.get(requiredType));
			}
		}
		for(BuildingType type : BuildingType.values()) {
			// make a new researchrequirement object
			ResearchRequirement req = new ResearchRequirement();
			// only add requirement if it isnt null
			if(type.getResearchRequirement() != null) {
				// get the research that type requires
				Research typesRequirement = researches.get(type.getResearchRequirement());
				// add the required research to the req
				req.addRequirement(typesRequirement);
			}
			// put it in the hashmap
			buildingResearchRequirements.put(type, req);
		}
		for(UnitType type : UnitType.values()) {
			// make a new researchrequirement object
			ResearchRequirement req = new ResearchRequirement();
			// only add requirement if it isnt null
			if(type.getResearchRequirement() != null) {
				// get the research that type requires
				Research typesRequirement = researches.get(type.getResearchRequirement());
				// add the required research to the req
				req.addRequirement(typesRequirement);
			}
			// put it in the hashmap
			unitResearchRequirements.put(type, req);
		}
		for(ItemType type : ItemType.values()) {
			// make a new researchrequirement object
			ResearchRequirement req = new ResearchRequirement();
			// only add requirement if it isnt null
			if(type.getResearchRequirement() != null) {
				// get the research that type requires
				Research typesRequirement = researches.get(type.getResearchRequirement());
				// add the required research to the req
				req.addRequirement(typesRequirement);
			}
			// put it in the hashmap
			craftResearchRequirements.put(type, req);
		}
		
//		resources.get(ItemType.IRON_ORE).addAmount(200);
//		resources.get(ItemType.COPPER_ORE).addAmount(200);
//		resources.get(ItemType.HORSE).addAmount(200);
//		resources.get(ItemType.FOOD).addAmount(2000);
//		resources.get(ItemType.WOOD).addAmount(2000);
//		resources.get(ItemType.ROCK).addAmount(2000);
		
	}
	
	public void gameTick() {
		boolean changedTerrain = false;
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		ticks++;
		
		if(selectedThing != null && !selectedThing.isPlayerControlled()) {
			deselectThing();
		}
		
		if(ticks%20 == 0) {
			updateTerritory();
			doResearch();
			changedTerrain = true;
		}
		
		if(ticks == 1) {
			world.rain();
		}
		if(ticks >= 10 && Math.random() < 0.0001) {
			world.spawnAnimal(UnitType.WATER_SPIRIT, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 100 && Math.random() < 0.001) {
			world.spawnAnimal(UnitType.FLAMELET, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 1000 && Math.random() < 0.0001) {
			world.spawnWerewolf();
		}
		if(ticks >= 1000 && Math.random() < 0.0001) {
			world.spawnLavaGolem();
		}
		if(ticks >= 2000 && Math.random() < 0.0001) {
			world.spawnAnimal(UnitType.PARASITE, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 100 && Math.random() < (0.00005 * numCutTrees/2)) {
			world.spawnEnt();
		}
		if(buildingsUntilOgre == world.buildings.size()) {
			world.spawnOgre();
			buildingsUntilOgre += buildingsUntilOgre;
		}
		if(ticks >= 1000 && Math.random() < 0.00001) {
			meteorStrike();
		}
		world.tick();
		// rain event
		if(Math.random() < 0.008) {
			world.rain();
		}
		if(Math.random() < 0.01) {
			world.grow();
		}
		if(world.volcano != null) {
			world.get(world.volcano).liquidType = LiquidType.LAVA;
//			world.get(world.volcano].liquidAmount += .01;
			if(Math.random() < 0.0001) {
				eruptVolcano();
			}
		}
		
		Liquid.propogate(world);
		changedTerrain = true;
		
		
		world.updatePlantDamage();
		
		world.updateTerrainChange(world);
		
		changedTerrain = true;
		
		Wildlife.tick(world);
		buildingTick();
		unitTick();
		world.updateUnitDealDamage();
		
		if(ticks%5 == 0) {
			world.updateUnitLiquidDamage();
		}
		
		
		
		guiController.updateGUI();
		if(changedTerrain) {
			updateTerrainImages();
		}
	}
	public void addResources() {
		for(ItemType itemType : ItemType.values()) {
			resources.get(itemType).addAmount(1000);
		}
		
	}
	public void eruptVolcano() {
		world.eruptVolcano();
	}
	public void meteorStrike(){
		world.meteorStrike();
	}
	public void spawnEverything() {
		List<Tile> tiles = world.getTilesRandomly();
		for(UnitType type : UnitType.values()) {
			world.spawnAnimal(type, tiles.remove(0));
		}
	}
	public void generateWorld(MapType mapType, int size, boolean easymode) {
		world = new World();
		world.generateWorld(mapType, size);
		makeRoads(easymode);
		updateTerrainImages();
		if(easymode) {
			for(ItemType itemType : ItemType.values()) {
				resources.get(itemType).addAmount(999);
			}
		}
	}
	public void updateTerrainImages() {
		BufferedImage[] images = world.createTerrainImage();
		this.terrainImage = images[0];
		this.minimapImage = images[1];
		this.heightMapImage = images[2];
	}

	public void updateBuildingAction() {
		
		for(Building building : world.buildings) {
			if(!building.isBuilt()) {
				continue;
			}
			if(!building.readyToHarvest() ) {
				continue;
			}
			if(building.getBuildingType() == BuildingType.MINE && building.getTile().getResource() != null && building.getTile().getResource().getType().isOre() == true) {
				resources.get(building.getTile().getResource().getType().getItemType()).addAmount(1);
				building.getTile().getResource().harvest(1);
				
				
				building.getTile().setHeight(building.getTile().getHeight() - 0.001);
				
				
				if(building.getTile().getResource().getYield() <= 0) {
					building.getTile().setResource(null);
				}
				building.resetTimeToHarvest();
			}
			
			if(building.getBuildingType() == BuildingType.MINE && building.getTile().getTerrain() == Terrain.ROCK) {
				resources.get(ItemType.STONE).addAmount(1);
				building.resetTimeToHarvest();
			}
			if(building.getBuildingType() == BuildingType.IRRIGATION && building.getTile().canPlant() == true) {
				//irrigation produces extra food when placed on water
				if(building.getTile().liquidType == LiquidType.WATER && building.getTile().liquidAmount > 0) {
					int extraFood = (int) (building.getTile().liquidAmount * 100);
					resources.get(ItemType.FOOD).addAmount(1 + extraFood/2);
					
				}else {
					resources.get(ItemType.FOOD).addAmount(1);
				}
				building.resetTimeToHarvest();
			}
			
			if(building.getBuildingType() == BuildingType.SAWMILL) {
				HashSet<Tile> tilesToCut = new HashSet<>();
				tilesToCut.add(building.getTile());
				for(Tile t : building.getTile().getNeighbors()) {
					for(Tile t2 : t.getNeighbors()) {
						tilesToCut.add(t2);
					}
					tilesToCut.add(t);
				}
				for(Tile tile : tilesToCut) {
					if(tile.getPlant() != null && tile.getPlant().getPlantType() == PlantType.FOREST1) {
						tile.getPlant().harvest(1);
						tile.getPlant().takeDamage(1);
						resources.get(ItemType.WOOD).addAmount(1);
						if(tile.getPlant().isDead() ) {
							numCutTrees ++;
						}
					}
				}
				
				building.resetTimeToHarvest();
			}
			
			if(building.getBuildingType() == BuildingType.FARM && building.getTile().hasUnit(UnitType.HORSE)) {
				resources.get(ItemType.HORSE).addAmount(1);
				resources.get(ItemType.FOOD).addAmount(1);
				building.resetTimeToHarvest();
			}
			
			if(building.getTile().getPlant() != null) {
				if(building.getBuildingType() == BuildingType.FARM && building.getTile().getPlant().getPlantType() == PlantType.BERRY) {
					resources.get(ItemType.FOOD).addAmount(1);
					building.getTile().getPlant().takeDamage(1);
					building.resetTimeToHarvest();
				}
			}
			
			// building builds units
			if(building.getBuildingUnit().peek() != null && building.getBuildingUnit().peek().isBuilt() == true) {
				Unit unit = building.getBuildingUnit().peek();
//				building.getTile().addUnit(unit);
				world.newUnits.add(unit);
				building.getBuildingUnit().remove();
			}
		}
		
		
		
	}
	

	public void updateBuildingDamage() {
		LinkedList<Building> buildingsNew = new LinkedList<Building>();
		for (Building building : world.buildings) {
			Tile tile = building.getTile();
			double damage = tile.computeTileDamage(building);
			if(damage > 0 ) {
				building.takeDamage(damage);
			}
			if (building.isDead() == true) {
				tile.setBuilding(null);
			} else {
				buildingsNew.add(building);
			}
		}	
		world.buildings = buildingsNew;
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
					if(p.getCost() > visited.get(currentTile)) {
						// Already visited this tile at a lower cost
						continue;
					}
				}
				visited.put(neighbor, p.getCost());
				search.add(p);
			}
		}
		System.out.println("road iterations: " + iterations);
		
		if(selectedPath != null) {
			for(Tile t : selectedPath.getTiles()) {
				if(t != null) 
					t.setRoad(RoadType.STONE_ROAD, "asdf");
			}
		}
	}

	private void makeRoads(boolean easymode) {
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

		makeRoadBetween(world.get(new TileLoc(world.getWidth()-1, 0)), world.get(new TileLoc(0, world.getHeight()-1)));
		makeRoadBetween(world.get(new TileLoc(0, 0)), world.get(new TileLoc(world.getWidth()-1, world.getHeight()-1)));
		makeRoadBetween(highestTile, lowestTile);
		turnRoads();
		
		makeCastle(easymode);
	}
	private void turnRoad(Tile tile) {
		if(tile.getRoadType() == null) {
			return;
		}
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
		if(s.equals("")) {
			for(Direction d : Direction.values()) {
				s += d;
			}
		}
		world.get(loc).setRoad(RoadType.STONE_ROAD, s);
	}
	private void turnRoads() {
		for(Tile tile : world.getTiles()) {
			if(tile.getRoadType() != null)
				turnRoad(tile);
		}
	}
	private void makeCastle(boolean easymode) {
		LinkedList<HasImage> thingsToPlace = new LinkedList<>();
		thingsToPlace.add(BuildingType.CASTLE);
		thingsToPlace.add(UnitType.WORKER);
		if(easymode) {
			thingsToPlace.add(BuildingType.BARRACKS);
			thingsToPlace.add(BuildingType.WORKSHOP);
			thingsToPlace.add(BuildingType.BLACKSMITH);
		}
		
		HashSet<Tile> visited = new HashSet<>();
		LinkedList<Tile> tovisit = new LinkedList<>();
		
		Tile middle = world.get(new TileLoc(world.getWidth()/2, world.getHeight()/2));
		tovisit.add(middle);
		visited.add(middle);
		
		while(!thingsToPlace.isEmpty()) {
			Tile current = tovisit.removeFirst();
			HasImage thing = thingsToPlace.getFirst();
			if(thing instanceof BuildingType) {
				BuildingType type = (BuildingType)thing;
				if (current.canBuild() == true 
						&& !current.getHasBuilding()
						&& current.liquidAmount < current.liquidType.getMinimumDamageAmount()
						&& (current.getTerrain() != Terrain.ROCK || type != BuildingType.CASTLE)) {
					Building s = new Building(type, current);
					current.setBuilding(s);
					world.buildings.add(s);
					s.setRemainingEffort(0);
					thing = null;
				}
			}
			else if(thing instanceof UnitType) {
				UnitType type = (UnitType)thing;
				if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
					summonUnit(current, type, true);
					thing = null;
				}
			}
			if(thing == null) {
				tovisit.clear();
				visited.clear();
				visited.add(current);
				thingsToPlace.remove();
			}
			
			for(Tile neighbor : current.getNeighbors()) {
				if(!visited.contains(neighbor)) {
					visited.add(neighbor);
					tovisit.add(neighbor);
				}
			}
		}
	}
	public void centerViewOn(Tile tile, int zoom) {
		tileSize = zoom;
		viewOffset.x = (tile.getLocation().x - panelWidth/2/tileSize) * tileSize + tileSize/2;
		viewOffset.y = (tile.getLocation().y - panelHeight/2/tileSize) * tileSize;
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
						Tile tile = world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						highest = Math.max(highest, tile.getHeight());
						lowest = Math.min(lowest, tile.getHeight());
					}
				}
			}
			for (int i = lowerX; i < upperX; i++) {
				for (int j = lowerY; j < upperY; j++) {
					Tile t = world.get(new TileLoc(i, j));
					if(t == null)
						continue;
					int x = t.getLocation().x * Game.tileSize;
					int y = t.getLocation().y * Game.tileSize;
					int w = Game.tileSize;
					int h = Game.tileSize;
					
					if(t.getHasBuilding() == true) {
						setTerritory(new TileLoc(i,j));
					}
					
					if(showHeightMap) {
						t.drawHeightMap(g, (world.get(new TileLoc(i, j)).getHeight() - lowest) / (highest - lowest));
					}
					else {
						g.drawImage(t.getTerrain().getImage(Game.tileSize), x, y, w, h, null);
//						t.drawEntities(g, currentMode);
						
						if(t.getResource() != null) {
							g.drawImage(t.getResource().getType().getImage(Game.tileSize), x, y, w, h, null);
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
						if(t.getModifier() != null) {
							g.drawImage(t.getModifier().getType().getImage(Game.tileSize), x, y, w, h, null);
						}
					}
				}
			}
			for(Plant p : world.plantsLand) {
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, p);
				drawHitsplat(g, p);
			}
			for(Plant p : world.plantsAquatic) {
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, p);
				drawHitsplat(g, p);
			}
			
			for(Building b : this.world.buildings) {
				if(b.getIsSelected()) {
					g.setColor(Color.pink);
					Utils.setTransparency(g, 0.8f);
					Graphics2D g2d = (Graphics2D)g;
					Stroke currentStroke = g2d.getStroke();
					int strokeWidth = Game.tileSize /8;
					g2d.setStroke(new BasicStroke(strokeWidth));
					g.drawOval(b.getTile().getLocation().x * Game.tileSize + strokeWidth/2, b.getTile().getLocation().y * Game.tileSize + strokeWidth/2, Game.tileSize-1 - strokeWidth, Game.tileSize-1 - strokeWidth);
					g2d.setStroke(currentStroke);
					Utils.setTransparency(g, 1f);
				}
				BufferedImage bI = Utils.toBufferedImage(b.getImage(0));
				double percentDone = 1 - b.getRemainingEffort()/b.getBuildingType().getBuildingEffort();
				int h =  Math.max(1, (int) (bI.getHeight() * percentDone));
				int tileh = Math.max(1, (int) (Game.tileSize * percentDone));
				bI = bI.getSubimage(0, bI.getHeight() - h, bI.getWidth(), h);
				g.drawImage(bI, b.getTile().getLocation().x * Game.tileSize, b.getTile().getLocation().y * Game.tileSize - tileh + Game.tileSize, Game.tileSize, tileh , null);
				drawHealthBar(g, b);
				drawHitsplat(g, b);
				if(b.isBuilt() == false) {
					int x = (int) ((b.getTile().getLocation().x * Game.tileSize) + Game.tileSize*.25);
					int y = (int) ((b.getTile().getLocation().y * Game.tileSize) + Game.tileSize*.25);
					int w = (int) (Game.tileSize*.75);
					int hi = (int)(Game.tileSize*.75);
					g.drawImage(Utils.loadImage("resources/Images/interfaces/building.PNG"), x, y, w, hi, null);
				}
			}
			for(Animal animal : Wildlife.getAnimals()) {
				g.drawImage(animal.getImage(0), animal.getTile().getLocation().x * Game.tileSize, animal.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				drawHealthBar(g, animal);
				drawHitsplat(g, animal);
		
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
				drawTarget(g, unit);
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
			}
			if(!showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						double brightness = world.getDaylight() + tile.getBrightness();
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int)(255 * (1 - brightness))));
						g.fillRect(i * Game.tileSize, j * Game.tileSize, Game.tileSize, Game.tileSize);
					}
				}
			}
			if(selectedThing instanceof Building) {
				Building building = (Building) selectedThing;
				if(building.getSpawnLocation() != building.getTile()) {
					g.drawImage(spawnLocationImage, building.getSpawnLocation().getLocation().x * Game.tileSize, building.getSpawnLocation().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				}
			}
			if(DEBUG_DRAW) {
				if(Game.tileSize >= 36) {
					int[][] rows = new int[upperX - lowerX][upperY - lowerY];
					int fontsize = Game.tileSize/4;
					fontsize = Math.min(fontsize, 13);
					Font font = new Font("Consolas", Font.PLAIN, fontsize);
					g.setFont(font);
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile tile = world.get(new TileLoc(i, j));
							List<String> strings = new LinkedList<String>();
							strings.add(String.format("H=%." + NUM_DEBUG_DIGITS + "f", tile.getHeight()));
							if(tile.liquidType != LiquidType.DRY) {
								strings.add(String.format(tile.liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount));
							}
							if(tile.getModifier() != null) {
								strings.add(tile.getModifier().timeLeft() + "");
							}
							rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, strings, rows[i-lowerX][j-lowerY], fontsize);
							
							for(Unit unit : tile.getUnits()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, unit.getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
							if(tile.getPlant() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getPlant().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
							if(tile.getHasBuilding()) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getBuilding().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
							}
						}
					}
				}
			}
			g.setColor(new Color(0, 0, 0, 64));
			g.drawRect(hoveredTile.x * Game.tileSize, hoveredTile.y * Game.tileSize, Game.tileSize-1, Game.tileSize-1);
			g.drawRect(hoveredTile.x * Game.tileSize + 1, hoveredTile.y * Game.tileSize + 1, Game.tileSize - 3, Game.tileSize - 3);
		}
		
	}

	public void drawHitsplat(Graphics g, Thing thing) {

		int splatWidth = (int) (Game.tileSize*.5);
		int splatHeight = (int) (Game.tileSize*.5);
		
		thing.updateHitsplats();
		Hitsplat[] hitsplats = thing.getHitsplatList();
		
		for(int m = 0; m < hitsplats.length; m++) {
			if(hitsplats[m] == null) {
				continue;
			}
			double damage = hitsplats[m].getDamage();
			int i = hitsplats[m].getSquare();
			
			int x = (int) ((thing.getTile().getLocation().x * Game.tileSize) );
			int y = (int) ((thing.getTile().getLocation().y * Game.tileSize) );
			
			if(i == 1) {
				x = (int) ((thing.getTile().getLocation().x * Game.tileSize) + Game.tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y * Game.tileSize) + Game.tileSize*0.5);
			}
			if(i == 2) {
				x = (int) ((thing.getTile().getLocation().x * Game.tileSize) + Game.tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y * Game.tileSize) );
			}
			if( i == 3) {
				x = (int) ((thing.getTile().getLocation().x * Game.tileSize) );
				y = (int) ((thing.getTile().getLocation().y * Game.tileSize) + Game.tileSize*0.5);
			}
			
			String text = String.format("%.0f", damage);

			if(damage > 0) {
				g.drawImage(redHitsplatImage, x, y, splatWidth, splatHeight, null);
			}else if(damage == 0){
				g.drawImage(blueHitsplatImage, x, y, splatWidth, splatHeight, null);
			}
			else if(damage < 0) {
				g.drawImage(greenHitsplatImage, x, y, splatWidth, splatHeight, null);
				text = String.format("%.0f", -thing.getHitsplatDamage());
			}
			
			int fontSize = Game.tileSize/4;
			g.setFont(new Font(damageFont.getFontName(), Font.BOLD, fontSize));
			int width = g.getFontMetrics().stringWidth(text);
			g.setColor(Color.WHITE);
//				g.drawString(text, x-width/2, y+fontSize*4/10);
			
			g.drawString(text, x + splatWidth/2 - width/2, (int) (y+fontSize*1.5));
		}
		
//		if(thing.hasHitsplat()) {
//			thing.updateHitsplats();
//			
//			int x = (int) ((thing.getTile().getLocation().x * Game.tileSize) + Game.tileSize*.5);
//			int y = (int) ((thing.getTile().getLocation().y * Game.tileSize) + Game.tileSize*.5);
//			int w = (int) (Game.tileSize*.5);
//			int hi = (int) (Game.tileSize*.5);
//			
//			String text = String.format("%.0f", thing.getHitsplatDamage());
//			
//			if(thing.getHitsplatDamage() > 0) {
//				g.drawImage(redHitsplatImage, x - w/2, y - hi/2, w, hi, null);
//			}else if(thing.getHitsplatDamage() == 0){
//				g.drawImage(blueHitsplatImage, x - w/2, y - hi/2, w, hi, null);
//			}else if(thing.getHitsplatDamage() < 0) {
//				g.drawImage(greenHitsplatImage, x - w/2, y - hi/2, w, hi, null);
//				text = String.format("%.0f", thing.getHitsplatDamage() * -1);
//			}
//			
//			int fontSize = Game.tileSize/4;
//			g.setFont(new Font(damageFont.getFontName(), Font.BOLD, fontSize));
//			int width = g.getFontMetrics().stringWidth(text);
//			g.setColor(Color.WHITE);
//			
//			g.drawString(text, x-width/2, y+fontSize*4/10);
//		}
	}
	public void drawTarget(Graphics g, Unit unit) {
		if(unit.getTarget() != null) {
			Thing target = unit.getTarget();
			int x = (int) ((target.getTile().getLocation().x * Game.tileSize + Game.tileSize*1/10) );
			int y = (int) ((target.getTile().getLocation().y * Game.tileSize + Game.tileSize*1/10) );
			int w = (int) (Game.tileSize*8/10);
			int hi = (int)(Game.tileSize*8/10);
			g.drawImage(targetImage, x, y, w, hi, null);
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
		for(Building building : world.buildings) {
			building.updateCulture();
		}
	}
	private void doResearch() {
		if(researchTarget != null) {
			researchTarget.spendResearch(50);
			if(researchTarget.isUnlocked()) {
				guiController.updateGUI();
			}
		}
	}
	
	public void craftItem(ItemType type) {
		
		for (Map.Entry mapElement : type.getCost().entrySet()) {
			ItemType key = (ItemType) mapElement.getKey();
			Integer value = (Integer) mapElement.getValue();

			if (resources.get(key).getAmount() < value) {
				return;
			}
		}

		for (Map.Entry mapElement : type.getCost().entrySet()) {
			ItemType key = (ItemType) mapElement.getKey();
			Integer value = (Integer) mapElement.getValue();

			resources.get(key).addAmount(-value);
			resources.get(type).addAmount(1);
		}


			
	}
	
	public void setResearchTarget(ResearchType type) {
		if(researches.get(type).getRequirement().areRequirementsMet()) {
			researchTarget = researches.get(type);
		}
	}
	private void setTerritory(TileLoc p) {
		double culture = world.get(p).getBuilding().getCulture();
		double area = culture * Building.CULTURE_AREA_MULTIPLIER;
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
						world.get(new TileLoc(p.x+i, p.y+j)).setTerritory(true);
						world.addToTerritory(world.get(new TileLoc(p.x+i, p.y+j)));
					}
				}
			}
		}
	}
	
	public void leftClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx,my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
		Tile tile = world.get(loc);
		
		System.out.println("left click");
		if(selectedUnitToSpawn != null) {
			System.out.println("trying to spawn unit" + selectedUnitToSpawn.toString() + loc.toString());
			Unit unit = new Unit(selectedUnitToSpawn, tile, true);
			tile.addUnit(unit);
			world.units.add(unit);
			selectedUnitToSpawn = null;
			return;
		}
		
		toggleUnitSelectOnTile(tile);
		
		
//		guiController.openRightClickMenu(mx, my, world.get(loc]);
	}
	private void summonUnit(Tile tile, UnitType type, boolean playerControlled) {
		System.out.println("trying to spawn unit" + type.toString() +tile.getLocation());
		Unit unit = new Unit(type, tile, playerControlled);
//		tile.addUnit(unit);
		world.newUnits.add(unit);
	}
	public void toggleTargetEnemy(Tile tile) {
		if(selectedThing instanceof Unit) {
			Unit unit = (Unit) selectedThing;
			Unit targetUnit = tile.getUnits().peek();
			if(targetUnit != unit && targetUnit.isPlayerControlled() == false) {
				unit.setTarget(targetUnit);
			}
			
		}
		
	}
	public void setSpawnLocation(Tile tile) {
		if(selectedThing instanceof Building) {
			Building building = (Building) selectedThing;
			building.setSpawnLocation(tile);
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
		hoveredTile = new TileLoc(tile.getIntX(), tile.getIntY());
	}
	
	public void mouseClick(int mx, int my) {
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		Tile tile = world.get(loc);
		
		if(tile.getUnits().isEmpty() == false) {
			toggleTargetEnemy(tile);
		}else {
			setDestination(mx, my);
		}
		setSpawnLocation(tile);
	}

	public void toggleUnitSelectOnTile(Tile tile) {
		Thing selectionCandidate = tile.getPlayerControlledThing();
		if (selectionCandidate == null) {
			return;
		}
		if (selectionCandidate == selectedThing) {
			deselectThing();
			// clicking on current selection
		} else {
			// clicking on new selection
			deselectThing();
			selectionCandidate.setIsSelected(true);
			if (selectionCandidate instanceof Unit) {
				guiController.selectedUnit((Unit) selectionCandidate, true);
			}
			if (selectionCandidate instanceof Building) {
				guiController.selectedBuilding((Building) selectionCandidate, true);
			}
			selectedThing = selectionCandidate;
		}

	}

	public void deselectThing() {
		if (selectedThing != null) {
			selectedThing.setIsSelected(false);
			if (selectedThing instanceof Unit) {

				Unit selectedUnit = (Unit) selectedThing;
				if (selectedUnit.getUnitType() == UnitType.WORKER) {
					guiController.selectedUnit(null, false);
				}
				
				selectedThing = null;
			}
			if (selectedThing instanceof Building) {
				guiController.selectedBuilding((Building) selectedThing, false);
				selectedThing = null;
			}
		
			
		}
	}
	public void spawnUnit(boolean show) {
		guiController.selectedSpawnUnit(show);
	}
	
	public void unitStop() {
		if (selectedThing instanceof Unit) {
			Unit selectedUnit = (Unit) selectedThing;
			selectedUnit.setTarget(null);
			selectedUnit.setTargetTile(null);
		}

	}
	
	public void setDestination(int mx, int my) {
		
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		Tile destination = world.get(loc);
		
		if(selectedThing != null && destination != null ) {
			selectedThing.setTargetTile(destination);
		}
		
	}
	private Building getBuildingToBuild() {
		if(world.buildings.isEmpty()) {
			return null;
		}
		for(Building building : world.buildings) {
			if(building.isBuilt() == false) {
				return building;
			}
		}
		return null;
	}
	
	private void unitTick() {
		
		for (Unit unit : world.units) {
			
			unit.tick();
			if (unit.getType() == UnitType.WORKER && unit.isIdle() == true) {
				Building building = getBuildingToBuild();
				if(building != null && unit.getTargetTile() == null) {
					
					if(building.getTile().getIsTerritory() == true && unit.getTile().getIsTerritory() == true) {
						unit.setTargetTile(building.getTile());
					}
					
				}
				
			}
			if(unit.getTargetTile() == null) {
				continue;
			}
			if (unit.readyToMove()) {
				unit.moveTowards(unit.getTargetTile());
			}
//			if(unit.getTile().getIsTerritory() && unit.getType().isHostile()) {
//				world.addUnitsInTerritory(unit);
//			}
			
		}
//		world.addUnitsInTerritory();
	}
	
	private void buildingTick() {
		updateBuildingDamage();
		updateBuildingAction();
		for (Building building : world.buildings) {
			building.tick();
		}
		
	}
	
	public void buildBuilding(BuildingType bt) {
		
		if(selectedThing != null && selectedThing instanceof Unit && ((Unit)selectedThing).getUnitType() == UnitType.WORKER) {
			if(selectedThing.getTile().getHasBuilding() == false) {
				
				
				for (Map.Entry mapElement : bt.getCost().entrySet()) {
					ItemType key = (ItemType) mapElement.getKey();
					Integer value = (Integer) mapElement.getValue();
					
					if (resources.get(key).getAmount() < value) {
						return;
					}
				}
				if (bt == BuildingType.IRRIGATION && selectedThing.getTile().canPlant() == false) {
					return;
				}
				
				for (Map.Entry mapElement : bt.getCost().entrySet()) {
					ItemType key = (ItemType) mapElement.getKey();
					Integer value = (Integer) mapElement.getValue();
					
					resources.get(key).addAmount(-value);
				}
				
			
				Building building = new Building(bt, selectedThing.getTile());
				selectedThing.getTile().setBuilding(building);
				world.buildings.add(building);
				
				

			}
			
		}
		
	}
	public void setUnit(UnitType type) {
		selectedUnitToSpawn = type;
		
	}
	
	public void buildRoad(RoadType rt) {
		if(selectedThing != null && selectedThing instanceof Unit && ((Unit)selectedThing).getUnitType() == UnitType.WORKER) {
			selectedThing.getTile().setRoad(rt, Direction.NORTH.toString());
			for(Tile tile : Utils.getNeighborsIncludingCurrent(selectedThing.getTile(), world)) {
				turnRoad(tile);
			}
		}
		
	}

	public void tryToBuildUnit(UnitType u) {
		
		if(selectedThing != null && selectedThing instanceof Building ) {
			buildUnit(u, selectedThing.getTile());
		}
	}
	
	private void buildUnit(UnitType u, Tile tile) {
		for (Map.Entry mapElement : u.getCost().entrySet()) {
			ItemType key = (ItemType) mapElement.getKey();
			Integer value = (Integer) mapElement.getValue();
			
			if (resources.get(key).getAmount() < value) {
				return;
			}
		}
		
		Unit unit = new Unit(u, tile, true);
		if (tile.isBlocked(unit)) {
			return;
		}
		
		for (Map.Entry mapElement : u.getCost().entrySet()) {
			ItemType key = (ItemType) mapElement.getKey();
			Integer value = (Integer) mapElement.getValue();
			
			resources.get(key).addAmount(-value);
		}
		tile.getBuilding().setBuildingUnit(unit);
		unit.setTargetTile(tile.getBuilding().getSpawnLocation());
//		tile.addUnit(unit);
//		world.units.add(unit);
	}

	public void doubleClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());

		
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
		zoomViewTo(newTileSize, mx, my);
	}
	public void zoomViewTo(int newTileSize, int mx, int my) {
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
		if(selectedThing != null && selectedThing instanceof Unit) {
			
			return ((Unit)selectedThing).getUnitType();
		}
		return null;
	}
	
	public void fastForwardToDay() {
		skipUntilTick = ticks + world.ticksUntilDay();
	}
	public void toggleFastForward(boolean enabled) {
		if(enabled) {
			skipUntilTick = Integer.MAX_VALUE;
		}
		else {
			skipUntilTick = ticks - 1;
		}
	}
	
	public void researchEverything() {
		for(Research research : researches.values()) {
			researchTarget = research;
			researchTarget.spendResearch(100000);
			if (researchTarget.isUnlocked()) {
				guiController.updateGUI();
			}
		}
	}
	
	public boolean shouldFastForward() {
		return ticks < skipUntilTick;
	}
}
