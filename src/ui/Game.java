package ui;
import java.awt.*;
import java.util.List;
import java.util.Map.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.awt.image.*;
import java.io.*;
import java.net.*;
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
	private Image buildIcon = Utils.loadImage("resources/Images/interfaces/building.PNG");
	private Image flag = Utils.loadImage("resources/Images/interfaces/flag.png");
	private int skipUntilTick;
	private BufferedImage terrainImage;
	private BufferedImage minimapImage;
	private BufferedImage heightMapImage;
	private ConcurrentLinkedQueue<Thing> selectedThings = new ConcurrentLinkedQueue<Thing>();
	private UnitType selectedUnitToSpawn;
	private BuildingType selectedBuildingToSpawn;
	private BuildingType selectedBuildingToPlan;
	private boolean summonPlayerControlled = true;
	private int numCutTrees = 10;
	private int buildingsUntilOgre = 20;
	public static final CombatStats combatBuffs = new CombatStats(0, 0, 0, 0, 0, 0, 0);
	public static final Color playerColor = Color.pink;
	public static final Color neutralColor = Color.lightGray;
	
	HashMap<ItemType, Item> items = new HashMap<ItemType, Item>();
	ArrayList<Research> researchList = new ArrayList<>();
	HashMap<String, Research> researchMap = new HashMap<>();
	
	HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	
	private Research researchTarget;
	
	private int money;
	private Position viewOffset;
	private TileLoc hoveredTile;
	private boolean showHeightMap;
	private boolean shiftEnabled = false;
	private boolean aControl = false;
	
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
			items.put(itemType, item);
		}
		Loader.setupResearch(researchMap, researchList);
		for(BuildingType type : BuildingType.values()) {
			// make a new researchrequirement object
			ResearchRequirement req = new ResearchRequirement();
			// only add requirement if it isnt null
			if(type.getResearchRequirement() != null) {
				// get the research that type requires
				Research typesRequirement = researchMap.get(type.getResearchRequirement());
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
				Research typesRequirement = researchMap.get(type.getResearchRequirement());
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
//			if(type.getResearchRequirement() != null) {
//				// get the research that type requires
//				Research typesRequirement = researches.get(type.getResearchRequirement());
//				// add the required research to the req
//				req.addRequirement(typesRequirement);
//			}
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
	
	public void randomEvents() {

		if(ticks == 1) {
			world.rain();
		}
		if(ticks >= 10 && Math.random() < 0.0005) {
			world.spawnAnimal(UnitType.WATER_SPIRIT, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 1000 && Math.random() < 0.001) {
			world.spawnAnimal(UnitType.FLAMELET, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 3000 && Math.random() < 0.0005) {
			world.spawnAnimal(UnitType.BOMB, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 6000 && Math.random() < 0.0005) {
			world.spawnWerewolf();
		}
		if(ticks >= 3000 && Math.random() < 0.0005) {
			world.spawnLavaGolem();
			world.spawnIceGiant();
		}
		if(ticks >= 6000 && Math.random() < 0.0001) {
			world.spawnAnimal(UnitType.PARASITE, world.getTilesRandomly().getFirst());
		}
		if(ticks >= 3000 && Math.random() < (0.00005 * numCutTrees/5)) {
			world.spawnEnt();
		}
		if(ticks >= 12000 && Math.random() < 0.0001 ) {
			spawnOrcs();
		}
		if(buildingsUntilOgre == world.buildings.size()) {
			world.spawnOgre();
			buildingsUntilOgre += buildingsUntilOgre;
		}
		if(ticks >= 12000 && Math.random() < 0.00001) {
			meteorStrike();
		}

		// rain event
		if(Math.random() < 0.008) {
			world.rain();
		}
		if(Math.random() < 0.01) {
			world.grow();
		}
		
		if(world.volcano != null) {
			world.get(world.volcano).liquidType = LiquidType.LAVA;
			if(Math.random() < 0.0001) {
				eruptVolcano();
			}
		}
	}
	
	public void gameTick() {
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		ticks++;

		if(ticks%20 == 0) {
			updateTerritory();
			doResearch();
		}
		
		Liquid.propogate(world);
		
		// Remove dead things
		world.clearDeadAndAddNewThings();
		
		world.addUnitsInTerritory();
		
		buildingTick();
		unitTick();
		world.doProjectileUpdates();
		if(ticks%World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			world.updatePlantDamage();
		}
		groundModifierTick();
		
		randomEvents();
		
		// GUI updates
		world.updateTerrainChange(world);
		guiController.updateGUI();
		updateTerrainImages();
	}
	public void addCombatBuff(CombatStats cs) {
		combatBuffs.combine(cs);
	}
	public CombatStats getCombatBuffs() {
		return combatBuffs;
	}
	public void addResources() {
		for(ItemType itemType : ItemType.values()) {
			items.get(itemType).addAmount(1000);
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
		Attack.world = world;
		world.generateWorld(mapType, size);
		makeRoads(easymode);
		updateTerrainImages();
		if(easymode) {
			for(ItemType itemType : ItemType.values()) {
				items.get(itemType).addAmount(999);
			}
		}
	}
	public void spawnOrcs() {
		LinkedList<Tile> tiles = world.getTilesRandomly();
		Tile tile = tiles.peek();
		for(Tile t : tiles) {
			if(t.getTerrain() == Terrain.ROCK && t.getLocation().x > 3 && t.getLocation().y > 3 && t.getLocation().x < world.getWidth()-3 && t.getLocation().y < world.getHeight()-3) {
				tile = t;
				break;
			}
		}
		TileLoc tloc = new TileLoc(tile.getLocation().x, tile.getLocation().y);
		Tile barracks = world.get(tloc) ;
//		summonThing(tile, null, BuildingType.BARRACKS, false);
		summonThing(barracks, null, BuildingType.BARRACKS, false);
		
		//makes the walls
		for(int i = 0; i < 6; i ++) {
			
			TileLoc wallLoc = new TileLoc(tile.getLocation().x-3, tile.getLocation().y-3 + i);
			Tile wall = world.get(wallLoc) ;
			if(wallLoc.x <= 0 || wallLoc.y <= 0) {
				continue;
			}
			if(i != 3) {
				summonThing(wall, null, BuildingType.WALL_WOOD, false);
			}
			
			
			wallLoc = new TileLoc(tile.getLocation().x+3, tile.getLocation().y-3 + i);
			wall = world.get(wallLoc) ;
			if(i != 3) {
				summonThing(wall, null, BuildingType.WALL_WOOD, false);
			}
			
			
			wallLoc = new TileLoc(tile.getLocation().x-3 + i, tile.getLocation().y-3);
			wall = world.get(wallLoc) ;
			summonThing(wall, null, BuildingType.WALL_WOOD, false);
			
			wallLoc = new TileLoc(tile.getLocation().x-3 + i, tile.getLocation().y+3);
			wall = world.get(wallLoc) ;
			summonThing(wall, null, BuildingType.WALL_WOOD, false);
		}
		for(int i = -1; i < 2; i ++) {
			for(int j = -1; j < 2; j ++) {
				tloc = new TileLoc(tile.getLocation().x + i, tile.getLocation().y + j);
				Tile temp = world.get(tloc);
				world.spawnAnimal(UnitType.CYCLOPS, temp);
			}
			
		}
		
	}
	public void updateTerrainImages() {
		BufferedImage[] images = world.createTerrainImage();
		this.terrainImage = images[0];
		this.minimapImage = images[1];
		this.heightMapImage = images[2];
	}

	public void buildingTick() {
		
		for(Building building : world.buildings) {
			building.tick();
			if(!building.isBuilt()) {
				continue;
			}
			if(!building.readyToHarvest() ) {
				continue;
			}
			if(building.getType() == BuildingType.MINE && building.getTile().getResource() != null && building.getTile().getResource().getType().isOre() == true) {
				items.get(building.getTile().getResource().getType().getItemType()).addAmount(1);
				building.getTile().getResource().harvest(1);
				
				
				building.getTile().setHeight(building.getTile().getHeight() - 0.001);
				
				
				if(building.getTile().getResource().getYield() <= 0) {
					building.getTile().setResource(null);
				}
				building.resetTimeToHarvest();
			}
			
			if(building.getType() == BuildingType.MINE && building.getTile().getTerrain() == Terrain.ROCK && building.getTile().getResource() == null) {
				items.get(ItemType.STONE).addAmount(1);
				building.resetTimeToHarvest();
			}
			if(building.getType() == BuildingType.IRRIGATION && building.getTile().canPlant() == true) {
				//irrigation produces extra food when placed on water
				if(building.getTile().liquidType == LiquidType.WATER && building.getTile().liquidAmount > 0) {
					int extraFood = (int) (building.getTile().liquidAmount * 50);
					items.get(ItemType.FOOD).addAmount(1 + extraFood);
					
				}else {
					items.get(ItemType.FOOD).addAmount(1);
				}
				building.resetTimeToHarvest();
			}
			
			if(building.getType() == BuildingType.SAWMILL) {
				HashSet<Tile> tilesToCut = new HashSet<>();
				tilesToCut.add(building.getTile());
				
				for(Tile t : world.getNeighborsInRadius((building.getTile()), 3)) {
					tilesToCut.add(t);
				}
				for(Tile tile : tilesToCut) {
					if(tile.getPlant() != null && tile.getPlant().getPlantType() == PlantType.FOREST1) {
						tile.getPlant().harvest(1);
						tile.getPlant().takeDamage(1);
						items.get(ItemType.WOOD).addAmount(1);
						if(tile.getPlant().isDead() ) {
							numCutTrees ++;
						}
					}
				}
				
				building.resetTimeToHarvest();
			}
			
			if(building.getType() == BuildingType.FARM && building.getTile().hasUnit(UnitType.HORSE)) {
				items.get(ItemType.HORSE).addAmount(1);
				items.get(ItemType.FOOD).addAmount(1);
				building.resetTimeToHarvest();
			}
			
			if(building.getTile().getPlant() != null) {
				if(building.getType() == BuildingType.FARM && building.getTile().getPlant().getPlantType() == PlantType.BERRY) {
					items.get(ItemType.FOOD).addAmount(1);
					building.getTile().getPlant().takeDamage(1);
					building.resetTimeToHarvest();
				}
			}
			
			// building builds units
			if(building.getBuildingUnit().peek() != null && building.getBuildingUnit().peek().isBuilt() == true) {
				Unit unit = building.getBuildingUnit().peek();
				building.getTile().addUnit(unit);
				world.newUnits.add(unit);
				building.getBuildingUnit().remove();
			}
		}
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
		if(next.getRoad() == null) {
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
		public Path(Path other) {
			tiles.addAll(other.tiles);
			this.cost = other.cost;
		}
		public void addTile(Tile tile, double addedCost) {
			tiles.add(tile);
			cost += addedCost;
		}
		public Tile getHead() {
			return tiles.getLast();
		}
		public Path clone() {
			return new Path(this);
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
				if(t != null) {
					Road road = new Road(RoadType.STONE_ROAD, t);
					road.setRemainingEffort(0);
					t.setRoad(road, "asdf");
				}
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
		if(tile.getRoad() == null) {
			return;
		}
		Set<Direction> directions = new HashSet<>();
		TileLoc loc = tile.getLocation();
		List<Tile> neighbors = Utils.getNeighbors(tile, world);
		for(Tile t : neighbors) {
			if(t.getRoad() == null)
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
		Road road = new Road(RoadType.STONE_ROAD, world.get(loc));
		road.setRemainingEffort(tile.getRoad().getRemainingEffort());
		world.get(loc).setRoad(road, s);
		
	}
	private void turnRoads() {
		for(Tile tile : world.getTiles()) {
			if(tile.getRoad() != null)
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
					Building s = new Building(type, current, true);
					current.setBuilding(s);
					world.buildings.add(s);
					s.setRemainingEffort(0);
					thing = null;
				}
			}
			else if(thing instanceof UnitType) {
				UnitType type = (UnitType)thing;
				if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
					summonThing(current, type, null, true);
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
					
					
					
					if(t.getHasBuilding() == true && t.getBuilding().getIsPlayerControlled()) {
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
//							g.setColor(Color.black);
//							g.fillRect(x, y, w, h); 
							g.setColor(playerColor);
							
							Utils.setTransparency(g, 0.5f);
							for(Tile tile : t.getNeighbors()) {
								if(tile.getIsTerritory() == false) {
									TileLoc tileLoc = tile.getLocation();
									
									if(tileLoc.x == t.getLocation().x ) {
										if(tileLoc.y < t.getLocation().y) {
											g.fillRect(x, y, w, 10); 
										}
										if(tileLoc.y > t.getLocation().y) {
											g.fillRect(x, y + h - 10, w, 10); 
										}
										
									}
									if(tileLoc.y == t.getLocation().y ) {
										if(tileLoc.x < t.getLocation().x) {
											g.fillRect(x, y, 10, h); 
										}
										if(tileLoc.x > t.getLocation().x) {
											g.fillRect(x + w - 10, y, 10, h); 
										}
									}
									
								}
							}
							Utils.setTransparency(g, 1);
						}
						if (t.getRoad() != null) {
							g.drawImage(t.getRoadImage(), x, y, w, h, null);
							if(t.getRoad().isBuilt() == false) {
								g.drawImage(buildIcon, x, y, w, h, null);
							}
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
							Utils.setTransparency(g, 0.9);
							g.drawImage(t.getModifier().getType().getImage(Game.tileSize), x, y, w, h, null);
							Utils.setTransparency(g, 1);
						}
						
						if (!t.getItems().isEmpty()) {
							for (Item item : t.getItems()) {
//								if (item != null && t != null) {
									g.drawImage(item.getType().getImage(0), t.getLocation().x * Game.tileSize + Game.tileSize/4,
											t.getLocation().y * Game.tileSize + Game.tileSize/4, Game.tileSize/2, Game.tileSize/2, null);
//								}
							}
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
					g.setColor(playerColor);
					Utils.setTransparency(g, 0.8f);
					Graphics2D g2d = (Graphics2D)g;
					Stroke currentStroke = g2d.getStroke();
					int strokeWidth = Game.tileSize /8;
					g2d.setStroke(new BasicStroke(strokeWidth));
					g.drawOval(b.getTile().getLocation().x * Game.tileSize + strokeWidth/2, b.getTile().getLocation().y * Game.tileSize + strokeWidth/2, Game.tileSize-1 - strokeWidth, Game.tileSize-1 - strokeWidth);
					g2d.setStroke(currentStroke);
					Utils.setTransparency(g, 1f);
				}
				HashSet<Tile> buildingVision = world.getNeighborsInRadius(b.getTile(), b.getType().getVisionRadius());
				for(Tile t : buildingVision) {
					if(t.getBuilding() != null && t.getBuilding().getIsPlayerControlled()) {
						t.setInVisionRange(true);
					}
					
				}
				
				BufferedImage bI = Utils.toBufferedImage(b.getImage(0));
				//draws the transparent version
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				g2d.drawImage(bI, b.getTile().getLocation().x * Game.tileSize, b.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
				
				//draws the partial image
				double percentDone = 1 - b.getRemainingEffort()/b.getType().getBuildingEffort();
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
					g.drawImage(buildIcon, x, y, w, hi, null);
				}
			}
			for(Building planned : world.plannedBuildings) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(planned.getImage(0));
				g2d.drawImage(bI, planned.getTile().getLocation().x * Game.tileSize, planned.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			
			for(Unit unit : world.units) {
				if(unit.getIsSelected()) {
					g.setColor(playerColor);
					Utils.setTransparency(g, 0.8f);
					Graphics2D g2d = (Graphics2D)g;
					Stroke currentStroke = g2d.getStroke();
					int strokeWidth = Game.tileSize /8;
					g2d.setStroke(new BasicStroke(strokeWidth));
					g.drawOval(unit.getTile().getLocation().x * Game.tileSize + strokeWidth/2, unit.getTile().getLocation().y * Game.tileSize + strokeWidth/2, Game.tileSize-1 - strokeWidth, Game.tileSize-1 - strokeWidth);
					g2d.setStroke(currentStroke);
					Utils.setTransparency(g, 1f);
					if(unit.getTargetTile() != null) {
						g.drawImage(flag, unit.getTargetTile().getLocation().x * Game.tileSize, unit.getTargetTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
					}
				}
				
				g.drawImage(unit.getImage(0), unit.getTile().getLocation().x * Game.tileSize, unit.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);

				if(unit.getIsSelected()) {
					drawTarget(g, unit);
				}
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
				
				//draws a square for every player unit on the tile
				int num = unit.getTile().getNumPlayerControlledUnits();
				int total = num;
				int other = 0;
				for (int i = 0; i < num; i++) {
					g.setColor(playerColor);
					int x = unit.getTile().getLocation().x * Game.tileSize + 10 * i;
					int y = unit.getTile().getLocation().y * Game.tileSize;
					g.fillRect(x, y, 5, 5);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, 5, 5);
				}
				for(Unit u : unit.getTile().getUnits()) {
					if(u.isPlayerControlled() == false) {
						other ++;
						total ++;
					}
				}
				//draws a square for every other unit
				for (int i = 0; i < other; i++) {
					g.setColor(neutralColor);
					int x = unit.getTile().getLocation().x * Game.tileSize + (10 * num) + (10 * i);
					int y = unit.getTile().getLocation().y * Game.tileSize;
					g.fillRect(x, y, 5, 5);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, 5, 5);

				}

			}
			for(Projectile p : world.projectiles) {
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
			}
			
			
			for(Thing thing : selectedThings) {
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					int range = unit.getType().getCombatStats().getAttackRadius();
					if(range == 1) {
						range = -1;
					}
					// draws the range for units
					for (int i = lowerX; i < upperX; i++) {
						for (int j = lowerY; j < upperY; j++) {
							Tile t = world.get(new TileLoc(i, j));
							if (t == null)
								continue;
							int x = t.getLocation().x * Game.tileSize;
							int y = t.getLocation().y * Game.tileSize;
							int w = Game.tileSize;
							int h = Game.tileSize;

							if (t.getLocation().distanceTo(unit.getTile().getLocation()) <= range) {
								g.setColor(Color.BLACK);
								Utils.setTransparency(g, 0.3f);

								for (Tile tile : t.getNeighbors()) {
									if (tile.getLocation().distanceTo(unit.getTile().getLocation()) > range) {
										TileLoc tileLoc = tile.getLocation();

										if (tileLoc.x == t.getLocation().x) {
											if (tileLoc.y < t.getLocation().y) {
												g.fillRect(x, y, w, 5);
											}
											if (tileLoc.y > t.getLocation().y) {
												g.fillRect(x, y + h - 5, w, 5);
											}

										}
										if (tileLoc.y == t.getLocation().y) {
											if (tileLoc.x < t.getLocation().x) {
												g.fillRect(x, y, 5, h);
											}
											if (tileLoc.x > t.getLocation().x) {
												g.fillRect(x + w - 5, y, 5, h);
											}
										}

									}
								}
								Utils.setTransparency(g, 1);
							}
						}
					}
				}
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
			
			for(Thing thing : selectedThings) {
				if(thing instanceof Building) {
					Building building = (Building) thing;
					if(building.getSpawnLocation() != building.getTile()) {
						g.drawImage(spawnLocationImage, building.getSpawnLocation().getLocation().x * Game.tileSize, building.getSpawnLocation().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
					}
				}
			}
			
			if (selectedBuildingToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedBuildingToSpawn.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * Game.tileSize, hoveredTile.y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (selectedBuildingToPlan != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedBuildingToPlan.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * Game.tileSize, hoveredTile.y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (selectedUnitToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedUnitToSpawn.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * Game.tileSize, hoveredTile.y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
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
							strings.add(String.format("HUM" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getHumidity()));
							
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
							if(tile.getRoad() != null) {
								rows[i-lowerX][j-lowerY] = tile.drawDebugStrings(g, tile.getRoad().getDebugStrings(), rows[i-lowerX][j-lowerY], fontsize);
								
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

			if (items.get(key).getAmount() < value) {
				return;
			}
		}

		for (Map.Entry mapElement : type.getCost().entrySet()) {
			ItemType key = (ItemType) mapElement.getKey();
			Integer value = (Integer) mapElement.getValue();

			items.get(key).addAmount(-value);
		}

		items.get(type).addAmount(1);
			
	}

	public void setResearchTarget(Research research) {
	
		if(research.getRequirement().areRequirementsMet()) {
			for (Entry<ItemType, Integer> mapElement : research.getCost().entrySet()) {
				
				ItemType key = mapElement.getKey();
				Integer value = mapElement.getValue();

				if (items.get(key) != null && items.get(key).getAmount() < value) {
					System.out.println("Not enough " + key);
					return;
				}
			}

			for (Entry<ItemType, Integer> mapElement : research.getCost().entrySet()) {
				ItemType key = mapElement.getKey();
				Integer value = mapElement.getValue();

				items.get(key).addAmount(-value);
			}

			researchTarget = research;
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
	
	
	private void summonThing(Tile tile, UnitType unitType, BuildingType buildingType, boolean playerControlled) {
		
		if(unitType != null) {
			System.out.println("spawn unit" + unitType.toString() +tile.getLocation());
			Unit unit = new Unit(unitType, tile, playerControlled);
			if(playerControlled) {
			}else {
//				unit = new Animal(unitType, tile, playerControlled);
				world.spawnAnimal(unitType, tile);
				return;
			}
			world.newUnits.add(unit);
			tile.addUnit(unit);
			unit.setTimeToAttack(0);
		}
		if(buildingType != null) {
			if(tile.getBuilding() != null) {
				tile.getBuilding().takeDamage(tile.getBuilding().getType().getHealth() + 1);
			}
				
			System.out.println("spawn building" + buildingType.toString() + tile.getLocation());
			Building building = new Building(buildingType, tile, playerControlled);
			building.setRemainingEffort(0);
			tile.setBuilding(building);
			world.buildings.add(building);
		}
		
	}
	
	public void toggleTargetEnemy(Tile tile) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				Thing targetThing = tile.getUnits().peek();
				for(Unit tempUnit : tile.getUnits()) {
					if(!tempUnit.isPlayerControlled()) {
						targetThing = tempUnit;
					}
				}
				if(targetThing == null && tile.getBuilding() != null) {
					targetThing = tile.getBuilding();
				}
				
				if(targetThing != null) {
					// sets the target if the target isn't itself (clicking a lets you attack allies)
					if(targetThing != unit && (aControl == true || targetThing.isPlayerControlled() == false)) {
						unit.setTarget(targetThing);
					}
					//attack move makes unit go to the closest tile that it can attack from
					if(aControl == true) {
						Tile bestTile = targetThing.getTile();
						int radius = unit.getType().getCombatStats().getAttackRadius() + 1;
						for(Tile t : world.getNeighborsInRadius(targetThing.getTile(), radius)) {
							if(t.getLocation().distanceTo(unit.getTile().getLocation()) < bestTile.getLocation().distanceTo(unit.getTile().getLocation())) {
								bestTile = t;
							}
						}
						unit.setTargetTile(bestTile);
					}
				} 
				
			}
		}
		
		
	}
	public void setSpawnLocation(Tile tile) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Building) {
				Building building = (Building) thing;
				building.setSpawnLocation(tile);
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
		hoveredTile = new TileLoc(tile.getIntX(), tile.getIntY());
	}
	public void leftClick(int mx, int my) {
		Position tilepos = getTileAtPixel(new Position(mx,my));
		TileLoc loc = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
		Tile tile = world.get(loc);

		System.out.println("left click");
		
		//if a-click and the tile has a building or unit
		if(aControl == true) {
			if(tile.getHasBuilding() || !tile.getUnits().isEmpty()) {
				toggleTargetEnemy(tile);
				aControl = false;
				return;
			}
		}
		// spawning unit
		if (selectedUnitToSpawn != null) {
			summonThing(tile, selectedUnitToSpawn, null, summonPlayerControlled);
			if(shiftEnabled == false) {
				selectedUnitToSpawn = null;
				selectedBuildingToSpawn = null;
				selectedBuildingToPlan = null;
			}
			
		}
		
		//spawning building
		if (selectedBuildingToSpawn != null) {
			summonThing(tile, null, selectedBuildingToSpawn, summonPlayerControlled);
			if(shiftEnabled == false) {
				selectedUnitToSpawn = null;
				selectedBuildingToSpawn = null;
				selectedBuildingToPlan = null;
			}
		}
		
		//planning building
		if (selectedBuildingToPlan != null) {
			System.out.println("planning building" + selectedBuildingToPlan.toString() + loc.toString());
			if (selectedBuildingToPlan == BuildingType.IRRIGATION && tile.canPlant() == false) {
				return;
			}
			if(tile.getHasBuilding() == true) {
				return;
			}
			buildBuilding(selectedBuildingToPlan, tile);
			
			if(shiftEnabled == false) {
				selectedUnitToSpawn = null;
				selectedBuildingToSpawn = null;
				selectedBuildingToPlan = null;
			}
		}
		
		//select units on tile
		toggleSelectionOnTile(tile);
		return;
		
//		guiController.openRightClickMenu(mx, my, world.get(loc]);
	}
	public void rightClick(int mx, int my) {
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		Tile tile = world.get(loc);
		setSpawnLocation(tile);
		
	
		for(Unit unit : tile.getUnits()) {
			//if there is a non-player unit, cant move onto it
			if(!unit.isPlayerControlled()) {
				//instead attack the non-player unit
				toggleTargetEnemy(tile);
				return;
			}
		}
		//sets the target tile if there isnt an enemy
		setDestination(mx, my);
		
		
	}
	
	public void shiftControl(boolean enabled) {
		shiftEnabled = enabled;
		if(enabled == false) {
			selectedBuildingToSpawn = null;
			selectedUnitToSpawn = null;
			selectedBuildingToPlan = null;
		}
	}
	public void aControl(boolean enabled) {
		aControl = enabled;
	}
	

	public void toggleSelectionOnTile(Tile tile) {
//		Thing selectionCandidate = tile.getPlayerControlledThing();
		ConcurrentLinkedQueue<Unit> unitsOnTile = tile.getUnits();
		Building building = tile.getBuilding();
		
		//deselects everything if shift isnt enabled
		if (shiftEnabled == false) {
			deselectThings();
		}
		
		//selects the building on the tile
		if(building != null && building.getIsPlayerControlled() && tile.getPlayerControlledUnit() == null) {
			if (shiftEnabled == false) {
				deselectThings();
			}
			guiController.selectedBuilding(building, true);
			building.setIsSelected(true);
			selectedThings.add(building);
		}
		//goes through all the units on the tile and checks if they are selected
		for(Unit candidate : unitsOnTile) {
			if (candidate == null) {
				return;
			}
			
			// clicking on tile w/o shift i.e only selects top unit
			if (candidate.isPlayerControlled()) {
				//shift disabled -> selects top unit
				if (shiftEnabled == false) {
					candidate.setIsSelected(true);
					guiController.selectedUnit(candidate, true);
					selectedThings.add(candidate);
					break;
				}else {
					//shift enabled -> selects whole stack
					candidate.setIsSelected(true);
					guiController.selectedUnit(candidate, true);
					selectedThings.add(candidate);
				}
			}
			
			// clicking on tile with one unit
//			if (selectedThings.contains(candidate) && selectedThings.size() == 0) {
//				deselectOneThing(candidate);
//			}
			
			
			
			
			
		}
		
		
		
	}

	public void workerRoad() {
		
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				
				if(unit.getType() == UnitType.WORKER) {
					for(Tile tile : world.territory) {
						if(tile.getRoad() == null) {
							unit.addToPath(tile);
							setPlannedRoad(tile);
							
						}
					}
				}
					
			}
		}
		
	}
	public void explode(Thing thing) {
		if(thing == null) {
			return;
		}
		world.spawnExplosion(thing.getTile(), 1, 10000);
	}
	public void deselectOneThing(Thing deselect) {
		for(Thing thing : selectedThings) {
			if (thing != null) {
				thing.setIsSelected(false);
				if (thing instanceof Unit) {

					Unit selectedUnit = (Unit) thing;
					if (selectedUnit.getUnitType() == UnitType.WORKER) {
						guiController.selectedUnit(null, false);
					}
					
					thing = null;
				}
				if (thing instanceof Building) {
					guiController.selectedBuilding((Building) thing, false);
					thing = null;
				}
			
				
			}
		}
		
	}

	public void deselectThings() {
		for (Thing thing : selectedThings) {
			if (thing != null) {
				thing.setIsSelected(false);
				
				if (thing instanceof Unit) {
					Unit selectedUnit = (Unit) thing;
					if (selectedUnit.getUnitType() == UnitType.WORKER) {
						guiController.selectedUnit(null, false);
					}

				}
				if (thing instanceof Building) {
					guiController.selectedBuilding((Building) thing, false);
				}

			}
			selectedThings.remove(thing);
		}
		selectedThings.clear();
	}

	public void spawnUnit(boolean show) {
		guiController.selectedSpawnUnit(show);
	}
	
	public void unitStop() {
		for (Thing thing : selectedThings) {
			if (thing instanceof Unit) {
				Unit selectedUnit = (Unit) thing;
				selectedUnit.setTarget(null);
				selectedUnit.setTargetTile(null);
			}

		}

	}
	
	public void setDestination(int mx, int my) {
		Position pos = getTileAtPixel(new Position(mx, my));
		TileLoc loc = new TileLoc(pos.getIntX(), pos.getIntY());
		Tile destination = world.get(loc);
		
		for (Thing thing : selectedThings) {
			if(thing != null && destination != null ) {
				thing.setTargetTile(destination);
			}
		}
		
		
	}
	
	
	private void unitTick() {
		for (Unit unit : world.units) {
			unit.updateState();
			unit.planActions(world.units, world.buildings, world.plannedBuildings);
			unit.doMovement(items);
			unit.doAttacks(world);
			unit.doPassiveThings(world);
		}
	}
	
	private void groundModifierTick() {
		for(GroundModifier modifier : world.groundModifiers) {
			if(modifier.updateTime()) {
				if(modifier.getType() == GroundModifierType.SNOW) {
					Tile tile = modifier.getTile();
					if(tile.liquidType != LiquidType.WATER) {
						tile.liquidType = LiquidType.WATER;
						tile.liquidAmount = 0;
					} 
					tile.liquidAmount += 0.01;
				}
			}
		}
	}

	private boolean canBuild(BuildingType bt, Tile tile) {
		if (tile.getHasBuilding() == true) {
			return false;
		}

		for (Entry<ItemType, Integer> mapElement : bt.getCost().entrySet()) {
			ItemType key = mapElement.getKey();
			Integer value = mapElement.getValue();

			if (items.get(key).getAmount() < value) {
				return false;
			}
		}
		if (bt == BuildingType.IRRIGATION && tile.canPlant() == false) {
			return false;
		}

		return true;

	}
	private void chargePrice(BuildingType bt) {
		for (Entry<ItemType, Integer> mapElement : bt.getCost().entrySet()) {
			ItemType key = mapElement.getKey();
			Integer value = mapElement.getValue();

			items.get(key).addAmount(-value);
		}
	}

	public void buildBuilding(BuildingType bt, Tile tile) {
		
		//if passed in a tile, it builds it on the tile
		if(tile != null) {
			if(canBuild(bt, tile) == true) {
				chargePrice(bt);
				Building building = new Building(bt, tile, true);
				tile.setBuilding(building);
				world.plannedBuildings.add(building);
				building.setPlanned(true);
				building.setHealth(1);
				return;
			}
		}
		
		
		//if not passed in tile, builds the building on each worker
		for (Thing thing : selectedThings) {
			if (thing != null && thing instanceof Unit && ((Unit) thing).getUnitType() == UnitType.WORKER) {

				if(canBuild(bt, thing.getTile()) == true) {
					chargePrice(bt);
					Building building = new Building(bt, thing.getTile(), true);
					thing.getTile().setBuilding(building);
					world.buildings.add(building);
					building.setPlanned(false);
					building.setHealth(1);
					
				}
				
			}
		}
		

	}
	public void setBuildingToPlan(BuildingType bt) {
		if(bt != null) {
			selectedBuildingToPlan = bt;
		}
	}
	public void setSummonPlayerControlled(boolean playerControlled) {
		summonPlayerControlled = playerControlled;
	}
	private void setPlannedRoad(Tile t) {
		if(t != null && t.getRoad() == null) {
			for (Map.Entry mapElement : RoadType.STONE_ROAD.getCost().entrySet()) {
				ItemType key = (ItemType) mapElement.getKey();
				Integer value = (Integer) mapElement.getValue();

				if (items.get(key).getAmount() < value) {
					return;
				}
			}

			for (Map.Entry mapElement : RoadType.STONE_ROAD.getCost().entrySet()) {
				ItemType key = (ItemType) mapElement.getKey();
				Integer value = (Integer) mapElement.getValue();

				items.get(key).addAmount(-value);
			}
			
			Road road = new Road(RoadType.STONE_ROAD, t);
			t.setRoad(road, Direction.NORTH.toString());
			
			for (Tile tile : Utils.getNeighborsIncludingCurrent(t, world)) {
				turnRoad(tile);
			}
		}
	}
	
	public void setThingToSpawn(UnitType ut, BuildingType bt) {
		if(ut != null) {
			selectedUnitToSpawn = ut;
		}
		if(bt != null) {
			selectedBuildingToSpawn = bt;
		}
		
	}
	
	
	public void buildRoad(RoadType rt) {
		
		for(Thing thing : selectedThings) {
			if (thing != null && thing instanceof Unit
					&& ((Unit) thing).getUnitType() == UnitType.WORKER) {
				for (Map.Entry mapElement : rt.getCost().entrySet()) {
					ItemType key = (ItemType) mapElement.getKey();
					Integer value = (Integer) mapElement.getValue();

					if (items.get(key).getAmount() < value) {
						return;
					}
				}

				for (Map.Entry mapElement : rt.getCost().entrySet()) {
					ItemType key = (ItemType) mapElement.getKey();
					Integer value = (Integer) mapElement.getValue();

					items.get(key).addAmount(-value);
				}

				Road road = new Road(rt, thing.getTile());
				thing.getTile().setRoad(road, Direction.NORTH.toString());
				
				for (Tile tile : Utils.getNeighborsIncludingCurrent(thing.getTile(), world)) {
					turnRoad(tile);
				}
			}
		}
	}

	public void tryToBuildUnit(UnitType u) {
		
		for(Thing thing : selectedThings) {
			if(selectedThings != null && thing instanceof Building ) {
				Building building = (Building)thing;
				for(UnitType ut : building.getType().unitsCanBuild()) {
					if(ut == u) {
						buildUnit(u, thing.getTile());
					}
				}
				
			}
		}
		
	}
	
	private void buildUnit(UnitType u, Tile tile) {
		for (Entry<ItemType, Integer> mapElement : u.getCost().entrySet()) {
			ItemType key = mapElement.getKey();
			Integer value = mapElement.getValue();
			
			if (items.get(key).getAmount() < value) {
				System.out.println("Not enough " + key);
				return;
			}
		}
		
		Unit unit = new Unit(u, tile, true);
		if (tile.isBlocked(unit)) {
			return;
		}
		
		for (Entry<ItemType, Integer> mapElement : u.getCost().entrySet()) {
			ItemType key = mapElement.getKey();
			Integer value = mapElement.getValue();
			
			items.get(key).addAmount(-value);
		}
		unit.setTargetTile(unit.getTile().getBuilding().getSpawnLocation());
		tile.getBuilding().setBuildingUnit(unit);
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
		return items.get(resourceType).getAmount();
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
//	public UnitType getSelectedUnit() {
//		if(selectedThing != null && selectedThing instanceof Unit) {
//			
//			return ((Unit)selectedThing).getUnitType();
//		}
//		return null;
//	}
	
//	public Thing getSelectedThing() {
//		return selectedThing;
//	}
	public ConcurrentLinkedQueue<Thing> getSelectedThings() {
		return selectedThings;
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
		for(Research research : researchList) {
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
