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
import networking.view.*;
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
	private Image harvestIcon = Utils.loadImage("resources/Images/interfaces/harvest.png");
	private Image guardIcon = Utils.loadImage("resources/Images/interfaces/guard.png");
	private Image autoBuildIcon = Utils.loadImage("resources/Images/interfaces/autobuild.png");
	private Image flag = Utils.loadImage("resources/Images/interfaces/flag.png");
	private Image moonIcon = Utils.loadImage("resources/Images/interfaces/moon.png");
	private Image sunIcon = Utils.loadImage("resources/Images/interfaces/sun.png");
	
	private int skipUntilTick;
	public volatile BufferedImage terrainImage;
	public volatile BufferedImage minimapImage;
	public volatile BufferedImage heightMapImage;
	private ConcurrentLinkedQueue<Thing> selectedThings = new ConcurrentLinkedQueue<Thing>();
	private UnitType selectedUnitToSpawn;
	private BuildingType selectedBuildingToSpawn;
	private BuildingType selectedBuildingToPlan;
	private boolean summonPlayerControlled = true;
	public static final CombatStats combatBuffs = new CombatStats(0, 0, 0, 0, 0, 0, 0);

	public static final ArrayList<UnitType> unitTypeList = new ArrayList<>();
	public static final HashMap<String, UnitType> unitTypeMap = new HashMap<>();
	public static final ArrayList<BuildingType> buildingTypeList = new ArrayList<>();
	public static final HashMap<String, BuildingType> buildingTypeMap = new HashMap<>();
	public static final ArrayList<ResearchType> researchTypeList = new ArrayList<>();
	public static final HashMap<String, ResearchType> researchTypeMap = new HashMap<>();

	
	private int money;
	private TileLoc hoveredTile;
	public boolean showHeightMap;
	private boolean shiftEnabled = false;
	private boolean controlEnabled = false;
	private boolean aControl = false;
	
	private int fastModeTileSize = 1;
	
	private GUIController guiController;

	public static final int NUM_DEBUG_DIGITS = 3;
	public static int ticks;
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
		showHeightMap = false;
		
		Loader.doTargetingMappings();
		for(Faction f : World.factions) {
			f.setupResearch();
		}
		
		World.PLAYER_FACTION.addItem(ItemType.WOOD, 200);
		World.PLAYER_FACTION.addItem(ItemType.STONE, 200);
		World.PLAYER_FACTION.addItem(ItemType.FOOD, 200);
		World.CYCLOPS_FACTION.addItem(ItemType.FOOD, 50);
		World.UNDEAD_FACTION.addItem(ItemType.FOOD, 999999);
		
//		resources.get(ItemType.IRON_ORE).addAmount(200);
//		resources.get(ItemType.COPPER_ORE).addAmount(200);
//		resources.get(ItemType.HORSE).addAmount(200);
//		resources.get(ItemType.FOOD).addAmount(2000);
//		resources.get(ItemType.WOOD).addAmount(2000);
//		resources.get(ItemType.ROCK).addAmount(2000);
		
	}
	
	public void weatherEvents() {

		
		if(days > 10 && Math.random() < 0.00001) {
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
		
		
		if(ticks % (world.DAY_DURATION + world.NIGHT_DURATION) == 0) {
			dayEvents();
			days ++;
		}
		if((ticks + world.DAY_DURATION) % (world.DAY_DURATION + world.NIGHT_DURATION) == 0) {
			nightEvents();
			nights ++;
		}
		weatherEvents();
		// GUI updates
		world.updateTerrainChange(world);
		guiController.updateGUI();
	}
	private void makeAnimal(Tile tile, UnitType unitType, int number) {
		for(Tile t: Utils.getTilesInRadius(tile, world, Math.max(1, (int)(Math.sqrt(number))-2))) {
			if(number > 0) {
				world.spawnAnimal(unitType, t, World.NO_FACTION);
				number --;
			}
			else {
				break;
			}
		}
		
	}
	private void dayEvents() {
		//all the forced spawns
		if(days % 5 == 0) {
			for(int i = 0; i < days/5; i++) {
				world.spawnLavaGolem();
				world.spawnIceGiant();
			}
			System.out.println(days/5 + " lava & ice giants");
			
		}
		if(days % 20 == 0) {
			meteorStrike();
		}
		if(days % 8 == 0) {
			for(int i = 0; i < days/8; i++) {
				world.spawnOgre();
			}
			System.out.println(days/8 + " ogres");
			
		}
		if(days % 10 == 0) {
			for(int i = 0; i < days/10; i++) {
				world.spawnSkeletonArmy();
			}
			System.out.println(days/10 + " skeletons");
		}
		if(days % 20 == 0) {
			spawnCyclops();
			System.out.println("cyclops");
		}
		if(days % 15 == 0) {
			world.spawnAnimal(Game.unitTypeMap.get("PARASITE"), world.getTilesRandomly().getFirst(), World.NO_FACTION);
			System.out.println("parasite");
		}
		
		
		if(days >= 10) {
			int number = (int)(Math.random() / Season.FREEZING_TEMPURATURE * days/10);
			for(int i = 0; i < number; i++) {
				world.spawnIceGiant();
			}
			System.out.println(number + " ice giants");
		}
		if(days >= 5) {
			int number = (int)(Math.random()*days/4);
			for(int i = 0; i < number; i++) {
				world.spawnEnt();
			}
			System.out.println(number + " ents");
			
		}
		if(days >= 1) {
			int number = (int)(Season.MELTING_TEMPURATURE + Math.random()*days);
			makeAnimal(world.getTilesRandomly().getFirst(), Game.unitTypeMap.get("FLAMELET"), number);
			System.out.println(number + " flamelets");
		}
		
		if(Math.random() < 0.2) {
			makeAnimal(world.getTilesRandomly().getFirst(), Game.unitTypeMap.get("WATER_SPIRIT"), 4);
			System.out.println(4 + " water spirits");
		}
//		if(ticks >= 3000 && Math.random() < 0.0005) {
//			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
//		}
	}
	private void nightEvents() {
		if(days >= 10) {
			if(Math.random() > 0.5) {
				world.spawnWerewolf();
			}
		}
	}
	public void addCombatBuff(CombatStats cs) {
		combatBuffs.combine(cs);
	}
	public CombatStats getCombatBuffs() {
		return combatBuffs;
	}
	public void addResources() {
		for(ItemType itemType : ItemType.values()) {
			World.PLAYER_FACTION.addItem(itemType, 1000);
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
	public void shadowWordDeath(int num){
		for(int i = 0; i < num; i++) {
			world.spawnOgre();
			world.spawnDragon();
			world.spawnWerewolf();
			world.spawnEnt();
			world.spawnLavaGolem();
			world.spawnIceGiant();
			world.spawnSkeletonArmy();
			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), World.NO_FACTION);
			spawnCyclops();
		}
		for(int i = 0; i < num/2; i++) {
			spawnEverything();
		}
		
		
	}
	public void spawnEverything() {
		List<Tile> tiles = world.getTilesRandomly();
		for(UnitType type : Game.unitTypeList) {
			world.spawnAnimal(type, tiles.remove(0), World.NO_FACTION);
		}
	}
	public void generateWorld(int size, boolean easymode) {
		world = new World();
		Attack.world = world;
		world.generateWorld(size);
		makeRoads(easymode);
		updateTerrainImages();
		if(easymode) {
			addResources();
		}
		world.clearDeadAndAddNewThings();
	}
	public void spawnCyclops() {
		LinkedList<Tile> tiles = world.getTilesRandomly();
		Tile tile = tiles.peek();
		for(Tile t : tiles) {
			if(t.getTerrain() == Terrain.ROCK && t.getLocation().x > 3 && t.getLocation().y > 3 && t.getLocation().x < world.getWidth()-3 && t.getLocation().y < world.getHeight()-3) {
				tile = t;
				break;
			}
		}
		summonThing(world.get(new TileLoc(tile.getLocation().x, tile.getLocation().y)), null, Game.buildingTypeMap.get("WATCHTOWER"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x-1, tile.getLocation().y-1)), null, Game.buildingTypeMap.get("GRANARY"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x+1, tile.getLocation().y-1)), null, Game.buildingTypeMap.get("BARRACKS"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x+1, tile.getLocation().y+1)), null, Game.buildingTypeMap.get("WINDMILL"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x-1, tile.getLocation().y+1)), null, Game.buildingTypeMap.get("MINE"), World.CYCLOPS_FACTION);
		
		//makes the walls
		for(int i = 0; i < 6; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if(i == 3) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall = world.get(new TileLoc(tile.getLocation().x-3 + i, tile.getLocation().y-3));
			summonThing(wall, null, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x+3, tile.getLocation().y-3 + i));
			summonThing(wall, null, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x+3 - i, tile.getLocation().y+3));
			summonThing(wall, null, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x-3, tile.getLocation().y+3 - i));
			summonThing(wall, null, type, World.CYCLOPS_FACTION);
		}
		for(int i = -1; i < 2; i ++) {
			for(int j = -1; j < 2; j ++) {
				Tile temp = world.get(new TileLoc(tile.getLocation().x + i, tile.getLocation().y + j));
				Animal cyclops = world.spawnAnimal(Game.unitTypeMap.get("CYCLOPS"), temp, World.CYCLOPS_FACTION);
				cyclops.setPassiveAction(PlannedAction.GUARD);
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
						Tile tile = world.get(new TileLoc(building.getTile().getLocation().x+i, building.getTile().getLocation().y+j));
						if(tile != null && tile.getIsTerritory() == World.NO_FACTION) {
							tile.setTerritory(building.getFaction());
							world.addToTerritory(tile, building.getFaction());
						}
					}
				}
			}
			building.tick(world);
		}
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
					Building road = new Building(Game.buildingTypeMap.get("STONE_ROAD"), t, World.NO_FACTION);
					road.setRemainingEffort(0);
					t.setRoad(road);
					world.newBuildings.add(road);
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
		
		makeStartingCastleAndUnits(easymode);
	}
	private void makeStartingCastleAndUnits(boolean easymode) {
		LinkedList<HasImage> thingsToPlace = new LinkedList<>();
		thingsToPlace.add(Game.buildingTypeMap.get("CASTLE"));
		thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
		if(easymode) {
			thingsToPlace.add(Game.buildingTypeMap.get("BARRACKS"));
			thingsToPlace.add(Game.buildingTypeMap.get("WORKSHOP"));
			thingsToPlace.add(Game.buildingTypeMap.get("BLACKSMITH"));
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
						&& (current.getTerrain() != Terrain.ROCK || type != Game.buildingTypeMap.get("CASTLE"))) {
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
	
	private void drawTile(Graphics g, Tile theTile, double lowest, double highest) {
		int column = theTile.getLocation().x;
		int row = theTile.getLocation().y;
		int drawx = column * GameView.tileSize;
		int drawy = (int) (row * GameView.tileSize);
		int draww = GameView.tileSize;
		int drawh = GameView.tileSize;
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
			
			if(theTile.getIsTerritory() != World.NO_FACTION) {
//				g.setColor(Color.black);
//				g.fillRect(x, y, w, h); 
				g.setColor(theTile.getIsTerritory().color);
				
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
				drawBuilding(theTile.getRoad(), g, drawx, drawy, draww, drawh);
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
					g.drawImage(item.getType().getImage(imagesize), drawx + GameView.tileSize/4,
							drawy + GameView.tileSize/4, GameView.tileSize/2, GameView.tileSize/2, null);
				}
			}
			if(theTile.getPlant() != null) {
				Plant p = theTile.getPlant();
				g.drawImage(p.getImage(GameView.tileSize), drawx, drawy, draww, drawh, null);
			}
			
			if(theTile.getBuilding() != null) {
				drawBuilding(theTile.getBuilding(), g, drawx, drawy, draww, drawh);
			}
			for(Unit unit : theTile.getUnits()) {
				g.drawImage(unit.getImage(GameView.tileSize), drawx, drawy, draww, drawh, null);
				if(unit.getIsHarvesting() == true) {
					g.drawImage(harvestIcon, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.isGuarding() == true) {
					g.drawImage(guardIcon, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
				if(unit.getAutoBuild() == true) {
					g.drawImage(autoBuildIcon, drawx+draww/4, drawy+drawh/4, draww/2, drawh/2, null);
				}
			}
		}
	}
	public void drawBuilding(Building building, Graphics g, int drawx, int drawy, int draww, int drawh) {
		if(building.getFaction() == World.PLAYER_FACTION) {
			HashSet<Tile> buildingVision = world.getNeighborsInRadius(building.getTile(), building.getType().getVisionRadius());
			for(Tile invision : buildingVision) {
				invision.setInVisionRange(true);
			}
		}
		
		BufferedImage bI = Utils.toBufferedImage(building.getImage(0));
		if(building.isBuilt() == false) {
			//draws the transparent version
			Utils.setTransparency(g, 0.5f);
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(bI, drawx, drawy, draww, drawh, null);
			Utils.setTransparency(g, 1f);
			//draws the partial image
			double percentDone = 1 - building.getRemainingEffort()/building.getType().getBuildingEffort();
			int imageRatio =  Math.max(1, (int) (bI.getHeight() * percentDone));
			int partialHeight = Math.max(1, (int) (GameView.tileSize * percentDone));
			bI = bI.getSubimage(0, bI.getHeight() - imageRatio, bI.getWidth(), imageRatio);
			g.drawImage(bI, drawx, drawy - partialHeight + drawh, draww, partialHeight , null);
			g.drawImage(buildIcon, drawx + GameView.tileSize/4, drawy + GameView.tileSize/4, draww*3/4, drawh*3/4, null);
		}
		else {
			g.drawImage(bI, drawx, drawy, draww, drawh, null);
		}
	}
	
	public void draw(Graphics g, int panelWidth, int panelHeight, Position viewOffset) {
		
		// Try to only draw stuff that is visible on the screen
		int lowerX = Math.max(0, viewOffset.divide(GameView.tileSize).getIntX() - 2);
		int lowerY = Math.max(0, viewOffset.divide(GameView.tileSize).getIntY() - 2);
		int upperX = Math.min(world.getWidth(), lowerX + panelWidth/GameView.tileSize + 4);
		int upperY = Math.min(world.getHeight(), lowerY + panelHeight/GameView.tileSize + 4);
		
		if(GameView.tileSize < fastModeTileSize) {
			if(showHeightMap) {
				g.drawImage(heightMapImage, 0, 0, GameView.tileSize*world.getWidth(), GameView.tileSize*world.getHeight(), null);
			}
			else {
				g.drawImage(terrainImage, 0, 0, GameView.tileSize*world.getWidth(), GameView.tileSize*world.getHeight(), null);
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
			
			for(Building building : world.buildings) {
				drawHealthBar(g, building);
				drawHitsplat(g, building);
			}
			for(Plant plant : world.plants) {
				drawHealthBar(g, plant);
				drawHitsplat(g, plant);
			}
			for(Unit unit : world.units) {
				drawHealthBar(g, unit);
				drawHitsplat(g, unit);
			}
			
			for(Projectile p : world.projectiles) {
				int extra = (int) (GameView.tileSize * p.getExtraSize());
				g.drawImage(p.getShadow(0), p.getTile().getLocation().x * GameView.tileSize, p.getTile().getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
				g.drawImage(p.getImage(0), p.getTile().getLocation().x * GameView.tileSize - extra/2, p.getTile().getLocation().y * GameView.tileSize - p.getHeight() - extra/2, GameView.tileSize + extra, GameView.tileSize + extra, null);
			}
			
			for(Thing thing : selectedThings) {
				// draw selection circle
				g.setColor(Utils.getTransparentColor(World.PLAYER_FACTION.color, 150));
//				Utils.setTransparency(g, 0.8f);
				Graphics2D g2d = (Graphics2D)g;
				Stroke currentStroke = g2d.getStroke();
				int strokeWidth = GameView.tileSize/12;
				g2d.setStroke(new BasicStroke(strokeWidth));
				g.drawOval(thing.getTile().getLocation().x * GameView.tileSize + strokeWidth/2, thing.getTile().getLocation().y * GameView.tileSize + strokeWidth/2, GameView.tileSize-1 - strokeWidth, GameView.tileSize-1 - strokeWidth);
				g2d.setStroke(currentStroke);
//				Utils.setTransparency(g, 1f);

				// draw spawn location for buildings
				if(thing instanceof Building) {
					Building building = (Building) thing;
					if(building.getSpawnLocation() != building.getTile()) {
						g.drawImage(spawnLocationImage, building.getSpawnLocation().getLocation().x * GameView.tileSize, building.getSpawnLocation().getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
					}
				}
				
				if (thing instanceof Unit) {
					Unit unit = (Unit) thing;
					// draw attacking target
					drawTarget(g, unit);
					// draw path 
					LinkedList<Tile> path = unit.getCurrentPath();
					if(path != null) {
						g.setColor(Color.green);
						TileLoc prev = unit.getTile().getLocation();
						for(Tile t : path) {
							if(prev != null) {
								g.drawLine(prev.x * GameView.tileSize + GameView.tileSize/2, prev.y * GameView.tileSize + GameView.tileSize/2, 
										t.getLocation().x * GameView.tileSize + GameView.tileSize/2, t.getLocation().y * GameView.tileSize + GameView.tileSize/2);
							}
							prev = t.getLocation();
						}
					}
					// draw destination flags
					for(PlannedAction plan : unit.actionQueue) {
						Tile targetTile = plan.targetTile == null ? plan.target.getTile() : plan.targetTile;
						g.drawImage(flag, targetTile.getLocation().x * GameView.tileSize, targetTile.getLocation().y * GameView.tileSize, GameView.tileSize, GameView.tileSize, null);
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
							int x = t.getLocation().x * GameView.tileSize;
							int y = t.getLocation().y * GameView.tileSize;
							int w = GameView.tileSize;
							int h = GameView.tileSize;

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

			int indicatorSize = GameView.tileSize/12;
			int offset = 4;
			HashMap<Tile, Integer> visited = new HashMap<>();
			for(Unit unit : world.units) {
				int count = 0;
				if(visited.containsKey(unit.getTile())) {
					count = visited.get(unit.getTile());
				}
				visited.put(unit.getTile(), count+1);
					
				//draws a square for every player unit on the tile
				int xx = unit.getTile().getLocation().x * GameView.tileSize + offset;
				int yy = unit.getTile().getLocation().y * GameView.tileSize + (indicatorSize + offset)*count + offset;
				g.setColor(unit.getFaction().color);
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
						g.fillRect(i * GameView.tileSize, j * GameView.tileSize, GameView.tileSize, GameView.tileSize);
					}
				}
			}
			
			if (selectedBuildingToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedBuildingToSpawn.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * GameView.tileSize, hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (selectedBuildingToPlan != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedBuildingToPlan.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * GameView.tileSize, hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			if (selectedUnitToSpawn != null) {
				Utils.setTransparency(g, 0.5f);
				Graphics2D g2d = (Graphics2D)g;
				BufferedImage bI = Utils.toBufferedImage(selectedUnitToSpawn.getImage(0));
				g2d.drawImage(bI, hoveredTile.x * GameView.tileSize, hoveredTile.y * GameView.tileSize, GameView.tileSize, GameView.tileSize , null);
				Utils.setTransparency(g, 1f);
			}
			
			if(DEBUG_DRAW) {
				if(GameView.tileSize >= 36) {
					int[][] rows = new int[upperX - lowerX][upperY - lowerY];
					int fontsize = GameView.tileSize/4;
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
			g.drawRect(hoveredTile.x * GameView.tileSize, hoveredTile.y * GameView.tileSize, GameView.tileSize-1, GameView.tileSize-1);
			g.drawRect(hoveredTile.x * GameView.tileSize + 1, hoveredTile.y * GameView.tileSize + 1, GameView.tileSize - 3, GameView.tileSize - 3);
		}
	}

	public void drawHitsplat(Graphics g, Thing thing) {

		int splatWidth = (int) (GameView.tileSize*.5);
		int splatHeight = (int) (GameView.tileSize*.5);
		
		thing.updateHitsplats();
		Hitsplat[] hitsplats = thing.getHitsplatList();
		
		for(int m = 0; m < hitsplats.length; m++) {
			if(hitsplats[m] == null) {
				continue;
			}
			double damage = hitsplats[m].getDamage();
			int i = hitsplats[m].getSquare();
			
			int x = (int) ((thing.getTile().getLocation().x * GameView.tileSize) );
			int y = (int) ((thing.getTile().getLocation().y * GameView.tileSize) );
			
			if(i == 1) {
				x = (int) ((thing.getTile().getLocation().x * GameView.tileSize) + GameView.tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y * GameView.tileSize) + GameView.tileSize*0.5);
			}
			if(i == 2) {
				x = (int) ((thing.getTile().getLocation().x * GameView.tileSize) + GameView.tileSize*0.5);
				y = (int) ((thing.getTile().getLocation().y * GameView.tileSize) );
			}
			if( i == 3) {
				x = (int) ((thing.getTile().getLocation().x * GameView.tileSize) );
				y = (int) ((thing.getTile().getLocation().y * GameView.tileSize) + GameView.tileSize*0.5);
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
			
			int fontSize = GameView.tileSize/4;
			g.setFont(new Font(damageFont.getFontName(), Font.BOLD, fontSize));
			int width = g.getFontMetrics().stringWidth(text);
			g.setColor(Color.WHITE);
			g.drawString(text, x + splatWidth/2 - width/2, (int) (y+fontSize*1.5));
		}
	}
	public void drawTarget(Graphics g, Unit unit) {
		if(unit.getTarget() != null) {
			Thing target = unit.getTarget();
			int x = (int) ((target.getTile().getLocation().x * GameView.tileSize + GameView.tileSize*1/10) );
			int y = (int) ((target.getTile().getLocation().y * GameView.tileSize + GameView.tileSize*1/10) );
			int w = (int) (GameView.tileSize*8/10);
			int hi = (int)(GameView.tileSize*8/10);
			g.drawImage(targetImage, x, y, w, hi, null);
		}
	}
	
	public void drawHealthBar(Graphics g, Thing thing) {
		if( GameView.tileSize <= 30) {
			return;
		}
		if(Game.ticks - thing.getTimeLastDamageTaken() < 20 || thing.getTile().getLocation().equals(hoveredTile)) {
			int x = thing.getTile().getLocation().x * GameView.tileSize + 1;
			int y = thing.getTile().getLocation().y * GameView.tileSize + 1;
			int w = GameView.tileSize - 1;
			int h = GameView.tileSize / 4 - 1;
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
//	private void doResearch() {
//		for(Faction faction : World.factions) {
//			if(faction.researchTarget != null) {
//				faction.researchTarget.spendResearch(10);
//				if(faction.researchTarget.isUnlocked()) {
//					guiController.updateGUI();
//				}
//			}
//		}
//	}
	
	public void craftItem(ItemType type, int amount) {
		BuildingType requiredBuilding = Game.buildingTypeMap.get(type.getBuilding());
		for(Building building : world.buildings) {
			if(building.getType() == requiredBuilding && building.getFaction() == World.PLAYER_FACTION) {
				for(int i = 0; i < amount && World.PLAYER_FACTION.canAfford(type.getCost()); i++) {
					World.PLAYER_FACTION.payCost(type.getCost());
					World.PLAYER_FACTION.addItem(type, 1);
				}
				return;
			}
		}
	}

	public void setResearchTarget(ResearchType researchType) {
		World.PLAYER_FACTION.setResearchTarget(researchType);
	}
	
	private Thing summonThing(Tile tile, UnitType unitType, BuildingType buildingType, Faction faction) {
		
		if(unitType != null) {
			if(faction == World.PLAYER_FACTION) {
				Unit unit = new Unit(unitType, tile, faction);
				world.newUnits.add(unit);
				tile.addUnit(unit);
				unit.setTimeToAttack(0);
				return unit;
			} else {
				return world.spawnAnimal(unitType, tile, faction);
			}
		}
		else if(buildingType != null) {
			if(tile.getBuilding() != null) {
				tile.getBuilding().setDead(true);
			}
			Building building = new Building(buildingType, tile, faction);
			building.setRemainingEffort(0);
			world.newBuildings.add(building);
			if(buildingType.isRoad()) {
				tile.setRoad(building);
			}
			else {
				tile.setBuilding(building);
			}
			return building;
		}
		return null;
	}

	public void mouseOver(Position tilepos) {
		hoveredTile = new TileLoc(tilepos.getIntX(), tilepos.getIntY());
	}
	public void leftClick(Position tilepos) {
		Tile tile = world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		
		System.out.println("left clicked on " + tile);
		if(tile == null) {
			return;
		}
		
		//if a-click and the tile has a building or unit
		if(aControl == true) {
			attackCommand(tile);
			aControl = false;
		}
		// spawning unit or building
		else if (selectedUnitToSpawn != null || selectedBuildingToSpawn != null) {
			Thing summoned = summonThing(tile, selectedUnitToSpawn, selectedBuildingToSpawn, summonPlayerControlled ? World.PLAYER_FACTION : World.NO_FACTION);
			if(summoned.getFaction() == World.PLAYER_FACTION) {
				if(shiftEnabled == false) {
					deselectEverything();
				}
				selectThing(summoned);
			}
		}
		//planning building
		else if (selectedBuildingToPlan != null) {
			Building plannedBuilding = buildBuilding(selectedBuildingToPlan, tile);
			if(plannedBuilding != null) {
				for(Thing thing : selectedThings) {
					if(thing instanceof Unit) {
						Unit unit = (Unit) thing;
						if(!shiftEnabled) {
							unit.clearPlannedActions();
						}
						if(unit.getType().isBuilder()) {
							unit.queuePlannedAction(new PlannedAction(plannedBuilding, true));
						}
					}
				}
			}
		}
		//select units on tile
		else {
			toggleSelectionOnTile(tile);
		}
		if(shiftEnabled == false) {
			selectedUnitToSpawn = null;
			selectedBuildingToSpawn = null;
			selectedBuildingToPlan = null;
		}
	}
	public void rightClick(Position tilepos) {
		Tile targetTile = world.get(new TileLoc(tilepos.getIntX(), tilepos.getIntY()));
		if(targetTile == null) {
			return;
		}
		
		for(Thing thing : selectedThings) {
			if(thing instanceof Building) {
				((Building) thing).setSpawnLocation(targetTile);
			}
			else if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				if(!shiftEnabled) {
					unit.clearPlannedActions();
				}
				if(unit.getType().isBuilder()) {
					Building targetBuilding = targetTile.getBuilding();
					if(targetBuilding == null) {
						targetBuilding = targetTile.getRoad();
					}
					if(targetBuilding != null && (targetBuilding.getFaction() == unit.getFaction() || targetBuilding.getType().isRoad()) && !targetBuilding.isBuilt()) {
						unit.queuePlannedAction(new PlannedAction(targetBuilding, true));
					}
					else {
						unit.queuePlannedAction(new PlannedAction(targetTile));
					}
				}
				else {
					Thing targetThing = null;
					for(Unit tempUnit : targetTile.getUnits()) {
						if(tempUnit == unit) {
							continue;
						}
						if(tempUnit.getFaction() != unit.getFaction() || aControl) {
							targetThing = tempUnit;
						}
					}
					if(targetThing == null && targetTile.getBuilding() != null
							&& (targetTile.getBuilding().getFaction() != unit.getFaction() || aControl)) {
						targetThing = targetTile.getBuilding();
					}
					if(targetThing != null) {
						unit.queuePlannedAction(new PlannedAction(targetThing));
					}
					else {
						unit.queuePlannedAction(new PlannedAction(targetTile));
					}
				}
			}
		}
		aControl = false;
		if(shiftEnabled == false) {
			selectedUnitToSpawn = null;
			selectedBuildingToSpawn = null;
			selectedBuildingToPlan = null;
		}
	}
	private void attackCommand(Tile tile) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit) thing;
				Thing targetThing = null;
				for(Unit tempUnit : tile.getUnits()) {
					if(tempUnit == unit) {
						continue;
					}
					targetThing = tempUnit;
					break;
				}
				if(targetThing == null) {
					targetThing = tile.getBuilding();
				}
				if(targetThing == null) {
					targetThing = tile.getRoad();
				}
				if(targetThing != null) {
					// initially set the target even though it might 
					if(!shiftEnabled) {
						unit.clearPlannedActions();
					}
					unit.queuePlannedAction(new PlannedAction(targetThing));
				}
			}
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
		if(!selectedThings.isEmpty()) {
			aControl = enabled;
		}
	}
	
	private void selectThing(Thing thing) {
		thing.setIsSelected(true);
		selectedThings.add(thing);
		if(thing instanceof Unit) {
			guiController.selectedUnit((Unit)thing, true);
		}
		else if(thing instanceof Building) {
			guiController.selectedBuilding((Building)thing, true);
		}
	}

	public void toggleSelectionOnTile(Tile tile) {
		
		//deselects everything if shift or control isnt enabled
		if (shiftEnabled == false && !controlEnabled) {
			deselectEverything();
		}
		
		//selects the building on the tile
		Building building = tile.getBuilding();
		if(building != null && building.getFaction() == World.PLAYER_FACTION && tile.getUnitOfFaction(World.PLAYER_FACTION) == null) {
			selectThing(building);
		}
		//goes through all the units on the tile and checks if they are selected
		for(Unit candidate : tile.getUnits()) {
			// clicking on tile w/o shift i.e only selects top unit
			if (candidate.getFaction() == World.PLAYER_FACTION) {
				selectThing(candidate);
				//shift enabled -> selects whole stack
				//shift disabled -> selects top unit
				if (!shiftEnabled) {
					break;
				}
			}
		}
	}
	public void toggleAutoBuild() {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					unit.setAutoBuild(!unit.getAutoBuild());
				}
			}
		}
	}
	public void toggleGuarding() {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				unit.setGuarding(!unit.isGuarding());
			}
		}
	}
	public void setHarvesting() {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					unit.setHarvesting(!unit.getIsHarvesting());
				}
			}
		}
	}
	public void workerRoad(BuildingType type) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					for(Tile tile : Utils.getTilesInRadius(unit.getTile(), world, 4)) {
						if(world.territory.get(tile) != World.PLAYER_FACTION) {
							continue;
						}
						Building building = buildBuilding(type, tile);
						if(building != null) {
							unit.queuePlannedAction(new PlannedAction(building, true));
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
			thing.setIsSelected(false);
			if(thing instanceof Unit) {
				guiController.selectedUnit((Unit)thing, false);
			}
		}
		selectedThings.clear();
		selectThing(keep);
	}

	public void deselectEverything() {
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
	public void selectAllUnits() {
		for(Unit unit : world.units) {
			if(unit.getFaction() == World.PLAYER_FACTION) {
				selectThing(unit);
			}
		}
	}

	public void spawnUnit(boolean show) {
		guiController.selectedSpawnUnit(show);
	}
	
	public void unitStop() {
		for (Thing thing : selectedThings) {
			if (thing instanceof Unit) {
				((Unit) thing).clearPlannedActions();
			}
		}
	}
	
	private void unitTick() {
		Iterator<Unit> it = world.units.descendingIterator();
		while(it.hasNext()) {
			Unit unit = it.next();
			unit.updateState();
			unit.planActions(world);
			unit.doMovement();
			unit.doAttacks(world);
			unit.doPassiveThings(world);
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
		if(bt.isRoad() && tile.getRoad() != null) {
			return false;
		}
		if (!bt.isRoad() && tile.getHasBuilding()) {
			return false;
		}
		if(!World.PLAYER_FACTION.canAfford(bt.getCost())) {
			return false;
		}
		if (bt == Game.buildingTypeMap.get("IRRIGATION") && tile.canPlant() == false) {
			return false;
		}
		return true;

	}

	public Building buildBuilding(BuildingType bt, Tile tile) {
		if(canBuild(bt, tile) == true) {
			World.PLAYER_FACTION.payCost(bt.getCost());
			Building building = new Building(bt, tile, World.PLAYER_FACTION);
			world.plannedBuildings.add(building);
			building.setPlanned(true);
			building.setHealth(1);
			if(bt.isRoad()) {
				tile.setRoad(building);
			}
			else {
				tile.setBuilding(building);
			}
			return building;
		}
		return null;
	}
	public void setBuildingToPlan(BuildingType bt) {
		selectedBuildingToPlan = bt;
	}
	public void setSummonPlayerControlled(boolean playerControlled) {
		summonPlayerControlled = playerControlled;
	}

	public void setThingToSpawn(UnitType ut, BuildingType bt) {
		if(ut != null) {
			selectedUnitToSpawn = ut;
		}
		if(bt != null) {
			selectedBuildingToSpawn = bt;
		}
		
	}
	
	public void tryToBuildUnit(UnitType u) {
		System.out.println("Trying to build " + u.name());
		for(Thing thing : selectedThings) {
			if(selectedThings != null && thing instanceof Building ) {
				Building building = (Building)thing;
				for(String ut : building.getType().unitsCanBuild()) {
					System.out.println("Can build " + ut);
					if(u == Game.unitTypeMap.get(ut)) {
						buildUnit(u, thing.getTile());
					}
				}
				
			}
		}
		
	}
	
	private void buildUnit(UnitType u, Tile tile) {
		System.out.println("building " + u);
		if(World.PLAYER_FACTION.canAfford(u.getCost())) {
			System.out.println("can afford " + u);
			Unit unit = new Unit(u, tile, World.PLAYER_FACTION);
			if (tile.isBlocked(unit)) {
				return;
			}
			World.PLAYER_FACTION.payCost(u.getCost());
			tile.getBuilding().setBuildingUnit(unit);
			System.out.println("built " + u);
		}
	}

	public int getMoney() {
		return money;
	}
	

	
	public Color getBackgroundColor() {
		double ratio = World.getDaylight();
		int c = (int)(ratio * 255);
		return new Color(c, c, c);
	}
	
	public void setShowHeightMap(boolean show) {
		this.showHeightMap = show;
	}
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
		World.PLAYER_FACTION.researchEverything();
		guiController.updateGUI();
	}
	
	public boolean shouldFastForward() {
		return ticks < skipUntilTick;
	}
}
