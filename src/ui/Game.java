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
	private Image moonIcon = Utils.loadImage("resources/Images/interfaces/moon.png");
	private Image sunIcon = Utils.loadImage("resources/Images/interfaces/sun.png");
	
	private int skipUntilTick;
	private volatile BufferedImage terrainImage;
	private volatile BufferedImage minimapImage;
	private volatile BufferedImage heightMapImage;
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

	public static final ArrayList<UnitType> unitTypeList = new ArrayList<>();
	public static final HashMap<String, UnitType> unitTypeMap = new HashMap<>();
	
	HashMap<BuildingType, ResearchRequirement> buildingResearchRequirements = new HashMap<>();
	HashMap<UnitType, ResearchRequirement> unitResearchRequirements = new HashMap<>();
	HashMap<ItemType, ResearchRequirement> craftResearchRequirements = new HashMap<>();
	
	private Research researchTarget;
	
	private int money;
	private Position viewOffset;
	private TileLoc hoveredTile;
	private boolean showHeightMap;
	private boolean shiftEnabled = false;
	private boolean controlEnabled = false;
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
	public static int days = 1;
	public static int nights = 0;
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
		for(UnitType type : Game.unitTypeList) {
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
		if(Math.random() < 0.0005) {
			world.spawnAnimal(Game.unitTypeMap.get("WATER_SPIRIT"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
		}
		if(ticks >= 1000 && Math.random() < 0.001) {
			world.spawnAnimal(Game.unitTypeMap.get("FLAMELET"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
		}
		if(ticks >= 3000 && Math.random() < 0.0005) {
			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
		}
		if(ticks >= 6000 && Math.random() < 0.0005 ) {
			world.spawnWerewolf();
		}
		if(ticks >= 3000 && Math.random() < 0.0005 ) {
			world.spawnLavaGolem();
			world.spawnIceGiant();
		}
		if(ticks >= 6000 && Math.random() < 0.0001) {
			world.spawnAnimal(Game.unitTypeMap.get("PARASITE"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
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
	public int getDays() {
		return days;
	}
	public int getNights() {
		return nights;
	}
	public void gameTick() {
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		ticks++;

		if(ticks%10 == 0) {
			doResearch();
		}
		if(ticks%20 == 0) {
			updateTerritory();
		}
		
		Liquid.propogate(world);
		
		// Remove dead things
		world.clearDeadAndAddNewThings();
		
		buildingTick();
		unitTick();
		world.doProjectileUpdates();
		if(ticks%World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			world.updatePlantDamage();
		}
		groundModifierTick();
		
		randomEvents();
		if(ticks % (world.DAY_DURATION + world.NIGHT_DURATION) == 0) {
			days ++;
		}
		if((ticks + world.DAY_DURATION) % (world.DAY_DURATION + world.NIGHT_DURATION) == 0) {
			nights ++;
		}
		// GUI updates
		world.updateTerrainChange(world);
		guiController.updateGUI();
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
	public Image getTimeImage(boolean day) {
		if(day) {
			return sunIcon;
		}
		else {
			return moonIcon;
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
		for(UnitType type : Game.unitTypeList) {
			world.spawnAnimal(type, tiles.remove(0), World.NEUTRAL_FACTION);
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
		world.clearDeadAndAddNewThings();
	}
	public void spawnOrcs() {
		int ORC_FACTION = 2;
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
		summonThing(barracks, null, BuildingType.BARRACKS, ORC_FACTION);
		
		//makes the walls
		for(int i = 0; i < 6; i++) {
			BuildingType type = BuildingType.WALL_WOOD;
			if(i == 3) {
				type = BuildingType.GATE_WOOD;
			}
			Tile wall = world.get(new TileLoc(tile.getLocation().x-3 + i, tile.getLocation().y-3));
			summonThing(wall, null, type, ORC_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x+3, tile.getLocation().y-3 + i));
			summonThing(wall, null, type, ORC_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x+3 - i, tile.getLocation().y+3));
			summonThing(wall, null, type, ORC_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x-3, tile.getLocation().y+3 - i));
			summonThing(wall, null, type, ORC_FACTION);
		}
		for(int i = -1; i < 2; i ++) {
			for(int j = -1; j < 2; j ++) {
				tloc = new TileLoc(tile.getLocation().x + i, tile.getLocation().y + j);
				Tile temp = world.get(tloc);
				world.spawnAnimal(Game.unitTypeMap.get("CYCLOPS"), temp, ORC_FACTION);
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
			double culture = building.getCulture();
			double area = culture * Building.CULTURE_AREA_MULTIPLIER;
			double radius = Math.sqrt(area);
			int r = (int)Math.ceil(radius);
			for (int i = -r; i <= r; i++) {
				for (int j = -r; j <= r; j++) {
					double distanceFromCenter = Math.sqrt(i*i + j*j);
					if(distanceFromCenter < radius) {
						TileLoc tileloc = new TileLoc(building.getTile().getLocation().x+i, building.getTile().getLocation().y+j);
						Tile tile = world.get(tileloc);
						if(tile != null && tile.getIsTerritory() == World.NEUTRAL_FACTION) {
							tile.setTerritory(building.getFaction());
							world.addToTerritory(tile, building.getFaction());
						}
					}
				}
			}
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
			if(building.getType() == BuildingType.GRANARY) {
				items.get(ItemType.FOOD).addAmount(2);
				building.resetTimeToHarvest();
			}
			if(building.getType() == BuildingType.WINDMILL) {
				items.get(ItemType.FOOD).addAmount(10);
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
			
			if(building.getType() == BuildingType.FARM && building.getTile().hasUnit(Game.unitTypeMap.get("HORSE"))) {
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
		
		makeStartingCastleAndUnits(easymode);
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
	private void makeStartingCastleAndUnits(boolean easymode) {
		LinkedList<HasImage> thingsToPlace = new LinkedList<>();
		thingsToPlace.add(BuildingType.CASTLE);
		thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
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
					Building s = new Building(type, current, World.PLAYER_FACTION);
					current.setBuilding(s);
					world.newBuildings.add(s);
					s.setRemainingEffort(0);
					thing = null;
				}
			}
			else if(thing instanceof UnitType) {
				UnitType type = (UnitType)thing;
				if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
					summonThing(current, type, null, World.PLAYER_FACTION);
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
	
	private void drawTile(Graphics g, Tile theTile, double lowest, double highest) {
		int column = theTile.getLocation().x;
		int row = theTile.getLocation().y;
		int drawx = column * Game.tileSize;
		int drawy = (int) (row * Game.tileSize);
		int draww = Game.tileSize;
		int drawh = Game.tileSize;
		int imagesize = draww < drawh ? draww : drawh;
		
		if(showHeightMap) {
			theTile.drawHeightMap(g, (world.get(new TileLoc(column, row)).getHeight() - lowest) / (highest - lowest));
		}
		else {
			g.drawImage(theTile.getTerrain().getImage(imagesize), drawx, drawy, draww, drawh, null);
//			t.drawEntities(g, currentMode);
			
			if(theTile.getResource() != null) {
				g.drawImage(theTile.getResource().getType().getImage(imagesize), drawx, drawy, draww, drawh, null);
			}
			
			if(theTile.getIsTerritory() != World.NEUTRAL_FACTION) {
//				g.setColor(Color.black);
//				g.fillRect(x, y, w, h); 
				g.setColor(world.getFactionColor(theTile.getIsTerritory()));
				
				Utils.setTransparency(g, 0.5f);
				for(Tile tile : theTile.getNeighbors()) {
					if(tile.getIsTerritory() != theTile.getIsTerritory()) {
						TileLoc tileLoc = tile.getLocation();
						
						if(tileLoc.x == theTile.getLocation().x ) {
							if(tileLoc.y < theTile.getLocation().y) {
								g.fillRect(drawx, drawy, draww, 10); 
							}
							if(tileLoc.y > theTile.getLocation().y) {
								g.fillRect(drawx, drawy + drawh - 10, draww, 10); 
							}
							
						}
						if(tileLoc.y == theTile.getLocation().y ) {
							if(tileLoc.x < theTile.getLocation().x) {
								g.fillRect(drawx, drawy, 10, drawh); 
							}
							if(tileLoc.x > theTile.getLocation().x) {
								g.fillRect(drawx + draww - 10, drawy, 10, drawh); 
							}
						}
						
					}
				}
				Utils.setTransparency(g, 1);
			}
			if (theTile.getRoad() != null) {
				g.drawImage(theTile.getRoadImage(), drawx, drawy, draww, drawh, null);
				if(theTile.getRoad().isBuilt() == false) {
					g.drawImage(buildIcon, drawx, drawy, draww, drawh, null);
				}
			}
			
			if(theTile.liquidType != LiquidType.DRY) {
				double alpha = Utils.getAlphaOfLiquid(theTile.liquidAmount);
//				 transparency liquids
				Utils.setTransparency(g, alpha);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawx, drawy, draww, drawh);
				Utils.setTransparency(g, 1);
				
				int imageSize = (int) Math.min(Math.max(draww*theTile.liquidAmount / 0.2, 1), draww);
				g.setColor(theTile.liquidType.getColor(imagesize));
				g.fillRect(drawx + draww/2 - imageSize/2, drawy + drawh/2 - imageSize/2, imageSize, imageSize);
				g.drawImage(theTile.liquidType.getImage(imagesize), drawx + draww/2 - imageSize/2, drawy + draww/2 - imageSize/2, imageSize, imageSize, null);
			}
			
			
			
			if(theTile.getModifier() != null) {
				Utils.setTransparency(g, 0.9);
				g.drawImage(theTile.getModifier().getType().getImage(imagesize), drawx, drawy, draww, drawh, null);
				Utils.setTransparency(g, 1);
			}
			
			if (!theTile.getItems().isEmpty()) {
				for (Item item : theTile.getItems()) {
//					if (item != null && t != null) {
						g.drawImage(item.getType().getImage(imagesize), drawx + Game.tileSize/4,
								drawy + Game.tileSize/4, Game.tileSize/2, Game.tileSize/2, null);
//					}
				}
			}
			
		}
		if(theTile.getPlant() != null) {
			Plant p = theTile.getPlant();
			g.drawImage(p.getImage(Game.tileSize), drawx, drawy, draww, drawh, null);
			drawHealthBar(g, p);
			drawHitsplat(g, p);
		}
		
		if(theTile.getBuilding() != null) {
			Building b = theTile.getBuilding();
			if(b.getFaction() == World.PLAYER_FACTION) {
				HashSet<Tile> buildingVision = world.getNeighborsInRadius(b.getTile(), b.getType().getVisionRadius());
				for(Tile invision : buildingVision) {
					invision.setInVisionRange(true);
				}
			}
			
			BufferedImage bI = Utils.toBufferedImage(b.getImage(0));
			//draws the transparent version
			Utils.setTransparency(g, 0.5f);
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(bI, drawx, drawy, draww, drawh, null);
			Utils.setTransparency(g, 1f);
			
			//draws the partial image
			double percentDone = 1 - b.getRemainingEffort()/b.getType().getBuildingEffort();
			int imageRatio =  Math.max(1, (int) (bI.getHeight() * percentDone));
			int partialHeight = Math.max(1, (int) (Game.tileSize * percentDone));
			bI = bI.getSubimage(0, bI.getHeight() - imageRatio, bI.getWidth(), imageRatio);
			g.drawImage(bI, drawx, drawy - partialHeight + drawh, draww, partialHeight , null);
			drawHealthBar(g, b);
			drawHitsplat(g, b);
			if(b.isBuilt() == false) {
				g.drawImage(buildIcon, drawx + Game.tileSize/4, drawy + Game.tileSize/4, draww*3/4, drawh*3/4, null);
			}
		}
		for(Unit unit : theTile.getUnits()) {
			g.drawImage(unit.getImage(Game.tileSize), drawx, drawy, draww, drawh, null);
			drawHealthBar(g, unit);
			drawHitsplat(g, unit);
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
					Tile tile = world.get(new TileLoc(i, j));
					if(tile == null)
						continue;
					drawTile(g, tile, lowest, highest);
				}
			}
			
			for(Building planned : world.plannedBuildings) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(planned.getImage(0));
				g2d.drawImage(bI, planned.getTile().getLocation().x * Game.tileSize, planned.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			
			for(Projectile p : world.projectiles) {
				int extra = (int) (Game.tileSize * p.getExtraSize());
				g.drawImage(p.getShadow(0), p.getTile().getLocation().x * Game.tileSize, p.getTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * Game.tileSize - extra/2, p.getTile().getLocation().y * Game.tileSize - p.getHeight() - extra/2, Game.tileSize + extra, Game.tileSize + extra, null);
			}
			
			for(Thing thing : selectedThings) {
				// draw selection circle
				g.setColor(playerColor);
				Utils.setTransparency(g, 0.8f);
				Graphics2D g2d = (Graphics2D)g;
				Stroke currentStroke = g2d.getStroke();
				int strokeWidth = Game.tileSize/12;
				g2d.setStroke(new BasicStroke(strokeWidth));
				g.drawOval(thing.getTile().getLocation().x * Game.tileSize + strokeWidth/2, thing.getTile().getLocation().y * Game.tileSize + strokeWidth/2, Game.tileSize-1 - strokeWidth, Game.tileSize-1 - strokeWidth);
				g2d.setStroke(currentStroke);
				Utils.setTransparency(g, 1f);

				// draw spawn location for buildings
				if(thing instanceof Building) {
					Building building = (Building) thing;
					if(building.getSpawnLocation() != building.getTile()) {
						g.drawImage(spawnLocationImage, building.getSpawnLocation().getLocation().x * Game.tileSize, building.getSpawnLocation().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
					}
				}
				
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					// draw attacking target
					drawTarget(g, unit);
					// draw path and destination flag
					if(unit.getTargetTile() != null) {
						LinkedList<Tile> path = unit.getCurrentPath();
						if(path != null) {
							g.setColor(Color.green);
							TileLoc prev = unit.getTile().getLocation();
							for(Tile t : path) {
								if(prev != null) {
									g.drawLine(prev.x * Game.tileSize + Game.tileSize/2, prev.y * Game.tileSize + Game.tileSize/2, 
											t.getLocation().x * Game.tileSize + Game.tileSize/2, t.getLocation().y * Game.tileSize + Game.tileSize/2);
								}
								prev = t.getLocation();
							}
						}
						g.drawImage(flag, unit.getTargetTile().getLocation().x * Game.tileSize, unit.getTargetTile().getLocation().y * Game.tileSize, Game.tileSize, Game.tileSize, null);
					}
					int range = unit.getType().getCombatStats().getAttackRadius();
					if(range == 1) {
						range = -1;
					}
					// draws the attack range for units
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

			int indicatorSize = Game.tileSize/12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for(Unit unit : world.units) {
				int count = 0;
				if(visited.containsKey(unit.getTile())) {
					count = visited.get(unit.getTile());
				}
				visited.put(unit.getTile(), count+1);
					
				//draws a square for every player unit on the tile
				int xx = unit.getTile().getLocation().x * Game.tileSize + offset;
				int yy = unit.getTile().getLocation().y * Game.tileSize + (indicatorSize + offset)*count + offset;
				g.setColor(world.getFactionColor(unit.getFaction()));
				g.fillRect(xx, yy, indicatorSize, indicatorSize);
				g.setColor(Color.BLACK);
				g.drawRect(xx, yy, indicatorSize, indicatorSize);
				count++;
			}
			
			
			if(!showHeightMap) {
				for (int i = lowerX; i < upperX; i++) {
					for (int j = lowerY; j < upperY; j++) {
						Tile tile = world.get(new TileLoc(i, j));
						if(tile == null)
							continue;
						double brightness = world.getDaylight() + tile.getBrightness(World.PLAYER_FACTION);
						brightness = Math.max(Math.min(brightness, 1), 0);
						g.setColor(new Color(0, 0, 0, (int)(255 * (1 - brightness))));
						g.fillRect(i * Game.tileSize, j * Game.tileSize, Game.tileSize, Game.tileSize);
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
							strings.add(String.format("TEMP" + "=%." + NUM_DEBUG_DIGITS + "f", tile.getTempurature()));
							
							if(tile.liquidType != LiquidType.DRY) {
								strings.add(String.format(tile.liquidType.name().charAt(0) + "=%." + NUM_DEBUG_DIGITS + "f", tile.liquidAmount));
							}
							
							
							if(tile.getModifier() != null) {
								strings.add("GM=" + tile.getModifier().timeLeft());
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
			g.drawString(text, x + splatWidth/2 - width/2, (int) (y+fontSize*1.5));
		}
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
			drawHealthBar2(g, thing, x, y, w, h, 2, thing.getHealth() / thing.getMaxHealth());
		}
	}
	public static void drawHealthBar2(Graphics g, Thing thing, int x, int y, int w, int h, int thickness, double ratio) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);
		
		g.setColor(Color.RED);
		g.fillRect(x + thickness, y + thickness, w - thickness*2, h - thickness*2);

		int greenBarWidth = (int) (ratio * (w - thickness*2));
		g.setColor(Color.GREEN);
		g.fillRect(x + thickness, y + thickness, greenBarWidth, h - thickness*2);
	}
	private void updateTerritory() {
		for(Building building : world.buildings) {
			building.updateCulture();
		}
	}
	private void doResearch() {
		if(researchTarget != null) {
			researchTarget.spendResearch(10);
			if(researchTarget.isUnlocked()) {
				guiController.updateGUI();
			}
		}
	}
	
	public void craftItem(ItemType type) {
		
		for (Entry<ItemType, Integer> mapElement : type.getCost().entrySet()) {
			ItemType key = mapElement.getKey();
			Integer value = mapElement.getValue();

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

	
	private void summonThing(Tile tile, UnitType unitType, BuildingType buildingType, int faction) {
		
		if(unitType != null) {
			System.out.println("spawn unit" + unitType.toString() +tile.getLocation());
			if(faction == World.PLAYER_FACTION) {
				Unit unit = new Unit(unitType, tile, faction);
				world.newUnits.add(unit);
				tile.addUnit(unit);
				unit.setTimeToAttack(0);
			} else {
				world.spawnAnimal(unitType, tile, faction);
			}
		}
		else if(buildingType != null) {
			if(tile.getBuilding() != null) {
				tile.getBuilding().takeDamage(tile.getBuilding().getType().getHealth() + 1);
			}
			System.out.println("spawn building" + buildingType.toString() + tile.getLocation());
			Building building = new Building(buildingType, tile, faction);
			building.setRemainingEffort(0);
			tile.setBuilding(building);
			world.newBuildings.add(building);
		}
		
	}
	
	/**
	 * @return true if chose target, false otherwise
	 */
	public boolean toggleTargetEnemy(Tile tile) {
		boolean choseTarget = false;
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				Thing targetThing = tile.getUnits().peek();
				for(Unit tempUnit : tile.getUnits()) {
					if(tempUnit.getFaction() != unit.getFaction()) {
						targetThing = tempUnit;
					}
				}
				if(targetThing == null && tile.getBuilding() != null) {
					targetThing = tile.getBuilding();
				}
				
				if(targetThing != null) {
					// sets the target if the target isn't itself (clicking a lets you attack allies)
					if(targetThing != unit && (aControl == true || targetThing.getFaction() != unit.getFaction())) {
						unit.setTarget(targetThing);
						choseTarget = true;
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
						choseTarget = true;
					}
				} 
				
			}
		}
		return choseTarget;
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

		System.out.println("left clicked on " + loc);
		
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
			summonThing(tile, selectedUnitToSpawn, null, summonPlayerControlled ? 1 : 0);
			if(shiftEnabled == false) {
				selectedUnitToSpawn = null;
				selectedBuildingToSpawn = null;
				selectedBuildingToPlan = null;
			}
			
		}
		
		//spawning building
		if (selectedBuildingToSpawn != null) {
			summonThing(tile, null, selectedBuildingToSpawn, summonPlayerControlled ? 1 : 0);
			if(shiftEnabled == false) {
				selectedUnitToSpawn = null;
				selectedBuildingToSpawn = null;
				selectedBuildingToPlan = null;
			}
		}
		
		//planning building
		if (selectedBuildingToPlan != null) {
			System.out.println("planning building " + selectedBuildingToPlan.toString() + loc.toString());
			if (selectedBuildingToPlan == BuildingType.IRRIGATION && tile.canPlant() == false) {
				return;
			}
			if(tile.getHasBuilding() == true) {
				return;
			}
			if(buildBuilding(selectedBuildingToPlan, tile)) {
				for(Thing thing : selectedThings) {
					if(thing instanceof Unit) {
						Unit unit = (Unit) thing;
						if(unit.getType().isBuilder()) {
							unit.setTargetTile(tile);
						}
					}
				}
			}
			
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
		
		boolean choseTarget = toggleTargetEnemy(tile);
		if(!choseTarget) {
			//sets the target tile if there isnt an enemy
			setDestination(mx, my);
		}
	}
	
	public void shiftControl(boolean enabled) {
		shiftEnabled = enabled;
		if(enabled == false) {
			selectedBuildingToSpawn = null;
			selectedUnitToSpawn = null;
			selectedBuildingToPlan = null;
		}
	}
	public void controlPressed(boolean enabled) {
		controlEnabled = enabled;
	}
	public boolean isControlDown() {
		return controlEnabled;
	}
	public void aControl(boolean enabled) {
		aControl = enabled;
	}
	

	public void toggleSelectionOnTile(Tile tile) {
//		Thing selectionCandidate = tile.getPlayerControlledThing();
		Building building = tile.getBuilding();
		
		//deselects everything if shift isnt enabled
		if (shiftEnabled == false && !controlEnabled) {
			deselectThings();
		}
		
		//selects the building on the tile
		if(building != null && building.getFaction() == World.PLAYER_FACTION && tile.getUnitOfFaction(World.PLAYER_FACTION) == null) {
			guiController.selectedBuilding(building, true);
			building.setIsSelected(true);
			selectedThings.add(building);
		}
		//goes through all the units on the tile and checks if they are selected
		for(Unit candidate : tile.getUnits()) {
			// clicking on tile w/o shift i.e only selects top unit
			if (candidate.getFaction() == World.PLAYER_FACTION) {
				if (shiftEnabled) {
					//shift enabled -> selects whole stack
					candidate.setIsSelected(true);
					guiController.selectedUnit(candidate, true);
					selectedThings.add(candidate);
				}
				else {
					//shift disabled -> selects top unit
					candidate.setIsSelected(true);
					guiController.selectedUnit(candidate, true);
					selectedThings.add(candidate);
					break;
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
				
				if(unit.getType().isBuilder()) {
					for(Entry<Tile, Integer> entry : world.territory.entrySet()) {
						if(entry.getValue() != World.PLAYER_FACTION) {
							continue;
						}
						Tile tile = entry.getKey();
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
		selectedThings.remove(deselect);
		deselect.setIsSelected(false);
		if(deselect instanceof Unit) {
			guiController.selectedUnit((Unit)deselect, false);
		}
	}
	public void deselectOtherThings(Thing keep) {
		for (Thing thing : selectedThings) {
			if(thing != keep) {
				thing.setIsSelected(false);
				if(thing instanceof Unit) {
					guiController.selectedUnit((Unit)thing, false);
				}
			}
		}
		selectedThings.clear();
		selectedThings.add(keep);
	}

	public void deselectThings() {
		for (Thing thing : selectedThings) {
			if (thing != null) {
				thing.setIsSelected(false);
				
				if (thing instanceof Unit) {
					guiController.selectedUnit((Unit) thing, false);
				}
				if (thing instanceof Building) {
					guiController.selectedBuilding((Building) thing, false);
				}

			}
			selectedThings.remove(thing);
		}
		selectedThings.clear();
		selectedBuildingToPlan = null;
		selectedBuildingToSpawn = null;
		selectedUnitToSpawn = null;
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
		Item food = this.items.get(ItemType.FOOD);
		Iterator<Unit> it = world.units.descendingIterator();
		double cost = 0;
		while(it.hasNext()) {
			Unit unit = it.next();
			unit.updateState();
			unit.planActions(world);
			unit.doMovement(items);
			unit.doAttacks(world);
			unit.doPassiveThings(world);
			if(Game.ticks % 60 == 0 && unit.getFaction() == World.PLAYER_FACTION) {
				if(food.getAmount() < cost) {
					unit.setDead(true);
				}
				else {
					food.addAmount(-(int)cost);
				}
				cost += 0.2;
			}
		}
	}
	
	private void groundModifierTick() {
		for(GroundModifier modifier : world.groundModifiers) {
			if(modifier.updateTime()) {
//				if(modifier.getType() == GroundModifierType.SNOW) {
//					Tile tile = modifier.getTile();
//					if(tile.liquidType != LiquidType.WATER) {
//						tile.liquidType = LiquidType.WATER;
//						tile.liquidAmount = 0;
//					} 
//					tile.liquidAmount += 0.01;
//				}
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

	public boolean buildBuilding(BuildingType bt, Tile tile) {
		
		//if passed in a tile, it builds it on the tile
		if(tile != null) {
			if(canBuild(bt, tile) == true) {
				chargePrice(bt);
				Building building = new Building(bt, tile, World.PLAYER_FACTION);
				tile.setBuilding(building);
				world.plannedBuildings.add(building);
				building.setPlanned(true);
				building.setHealth(1);
				return true;
			}
		}
		
		//if not passed in tile, builds the building on each worker
		boolean built = false;
		for (Thing thing : selectedThings) {
			if (thing != null && thing instanceof Unit && ((Unit) thing).getUnitType().isBuilder()) {
				if(canBuild(bt, thing.getTile()) == true) {
					chargePrice(bt);
					Building building = new Building(bt, thing.getTile(), World.PLAYER_FACTION);
					thing.getTile().setBuilding(building);
					world.newBuildings.add(building);
					building.setPlanned(false);
					building.setHealth(1);
					built = true;
				}
			}
		}
		return built;

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
			if (thing != null && thing instanceof Unit && ((Unit) thing).getUnitType().isBuilder()) {
				for (Entry<ItemType, Integer> mapElement : rt.getCost().entrySet()) {
					ItemType key = (ItemType) mapElement.getKey();
					Integer value = (Integer) mapElement.getValue();

					if (items.get(key).getAmount() < value) {
						return;
					}
				}

				for (Entry<ItemType, Integer> mapElement : rt.getCost().entrySet()) {
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
				for(String ut : building.getType().unitsCanBuild()) {
					if(u == Game.unitTypeMap.get(ut)) {
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
		
		Unit unit = new Unit(u, tile, World.PLAYER_FACTION);
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
		int boxw = (int) (panelWidth * w / Game.tileSize / world.getWidth());
		int boxh = (int) (panelHeight * h / Game.tileSize / world.getHeight());
//		System.out.println(boxx);
		g.setColor(Color.yellow);
		g.drawRect(x + boxx, y + boxy, boxw, boxh);
	}
	protected void drawGame(Graphics g) {
		
		g.translate(-viewOffset.getIntX(), -viewOffset.getIntY());
		draw(g);
		g.translate(viewOffset.getIntX(), viewOffset.getIntY());
		if(researchTarget != null && !researchTarget.isUnlocked()) {
			g.setFont(KUIConstants.infoFont);
			double completedRatio = 1.0 * researchTarget.getPointsSpent() / researchTarget.getRequiredPoints();
			String progress = String.format(researchTarget + " %d/%d", researchTarget.getPointsSpent(), researchTarget.getRequiredPoints());
			KUIConstants.drawProgressBar(g, Color.blue, Color.gray, Color.white, completedRatio, progress, 4, this.panelHeight - 30 - 4, this.panelWidth/3, 30);
		}
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
