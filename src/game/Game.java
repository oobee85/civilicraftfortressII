package game;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import game.liquid.*;

import java.util.*;

import networking.message.*;
import networking.server.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	
	public static boolean DEBUG = false;

	public static final ArrayList<UnitType> unitTypeList = new ArrayList<>();
	public static final HashMap<String, UnitType> unitTypeMap = new HashMap<>();
	public static final ArrayList<BuildingType> buildingTypeList = new ArrayList<>();
	public static final HashMap<String, BuildingType> buildingTypeMap = new HashMap<>();
	public static final ArrayList<PlantType> plantTypeList = new ArrayList<>();
	public static final HashMap<String, PlantType> plantTypeMap = new HashMap<>();
	public static final ArrayList<ResearchType> researchTypeList = new ArrayList<>();
	public static final HashMap<String, ResearchType> researchTypeMap = new HashMap<>();

	public static boolean USE_BIDIRECTIONAL_A_STAR = true;
	public static boolean DISABLE_NIGHT = false;
	// disables enemies and volcano
	public static boolean DISABLE_ENEMY_SPAWNS = true;
	public static boolean DISABLE_VOLCANO_ERUPT = true;
	
	private GUIController guiController;
	public static final int howFarAwayStuffSpawn = 30;
	public World world;
	
	public Game(GUIController guiController) {
		this.guiController = guiController;
		Loader.doTargetingMappings();
	}
	
	public void saveToFile() {
		WorldInfo worldInfo = Utils.extractWorldInfo(world);
		Utils.saveToFile(worldInfo, "save1.civ", false);
	}
	
	public void weatherEvents() {
		if(World.days > 10 && Math.random() < 0.00001) {
			meteorStrike();
		}
		world.rains();
		
		// rain event
//		if(Math.random() < 0.002) {
//			world.rain();
//		}
//		if(Math.random() < 0.01) {
//			world.grow();
//		}
		
		if(world.volcano != null) {
			world.get(world.volcano).liquidType = LiquidType.LAVA;
			if(World.days >= 10 && Math.random() < 0.0001 && !DISABLE_VOLCANO_ERUPT) {
				eruptVolcano();
			}
		}
	}
	public GUIController getGUIController() {
		return guiController;
	}
	public int getDays() {
		return World.days;
	}
	public int getNights() {
		return World.nights;
	}
	public void simulatedGameTick() {
		// Do the things that can be simulated client-side for smoother game play
		World.ticks++;
		if(World.ticks%20 == 0) {
			updateTerritory();
		}
		Liquid.propogate(world);
		// Remove dead things
		for(Unit unit : world.getUnits()) {
			if(unit.isDead()) {
				for (Item item : unit.getType().getDeadItem()) {
					unit.getTile().addItem(item);
				}
			}
			else {
				unit.getTile().getItems().clear();
				PlannedAction plan = unit.actionQueue.peek();
				if(plan != null) {
					if(plan.isDone(unit.getTile())) {
						unit.actionQueue.poll();
					}
				}
			}
		}
		world.clearDeadAndAddNewThings();

//		buildingTick();
//		unitTick();

		world.doProjectileUpdates(true);
		world.doWeatherUpdate();
	}
	public void gameTick() {
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		World.ticks++;
		if(World.ticks%20 == 0) {
			updateTerritory();
		}
		
		if(World.ticks % 2 == 0) {
			Liquid.propogate(world);
		}

		world.clearDeadAndAddNewThings();
		
		buildingTick();
		unitTick();
		world.doProjectileUpdates(false);
		if(World.ticks%World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			world.updatePlantDamage();
		}
		
		if(World.ticks % (World.DAY_DURATION + World.NIGHT_DURATION) == 0) {
			if(!DISABLE_ENEMY_SPAWNS) {
				dayEvents();
			}
			World.days ++;
		}
		if((World.ticks + World.DAY_DURATION) % (World.DAY_DURATION + World.NIGHT_DURATION) == 0) {
			if(!DISABLE_ENEMY_SPAWNS) {
				nightEvents();
			}
			World.nights ++;
		}
		world.doWeatherUpdate();
		weatherEvents();
		// GUI updates
		world.updateTerrainChange(false);
	}
	private void makeAnimal(Tile tile, UnitType unitType, int number) {
		for(Tile t: Utils.getTilesInRadius(tile, world, Math.max(1, (int)(Math.sqrt(number))-1))) {
			if(number > 0) {
				world.spawnAnimal(unitType, t, world.getFaction(World.NO_FACTION_ID), null);
				number --;
			}
			else {
				break;
			}
		}
	}
	
	private Tile getTargetTileForSpawns() {
		Tile targetTile = world.getTilesRandomly().peek();

		for (Faction faction : world.getFactions()) {
			if (faction.getDifficulty() <= 0) {
				continue;
			}

			LinkedList<Tile> factionTiles = new LinkedList<Tile>();
			for (Tile t : world.territory.keySet()) {
				if (t.getFaction() == faction) {
					factionTiles.add(t);
				}
			}
			if (factionTiles.isEmpty() == false) {
				targetTile = factionTiles.get((int) (Math.random() * factionTiles.size()));
			}
		}
		return targetTile;
	}
	private void dayEvents() {
		double day = Math.sqrt(World.days);
		Tile targetTile = getTargetTileForSpawns();
		
		//all the forced spawns
		if(World.days % 5 == 0) {
			world.spawnLavaGolem(targetTile);
			world.spawnIceGiant(targetTile);
			System.out.println(day + " lava & ice giants");
			
		}
		if(World.days % 10 == 0) {
			meteorStrike();
		}
		if(World.days % 8 == 0) {
			world.spawnOgre(targetTile);
			System.out.println(day + " ogres");
			
		}
		if(World.days % 10 == 0) {
			world.spawnSkeletonArmy(targetTile);
			System.out.println(day + " skeletons");
		}
		if(World.days % 20 == 0) {
			spawnCyclops();
			System.out.println("cyclops");
		}
		if(World.days % 15 == 0) {
			world.spawnAnimal(Game.unitTypeMap.get("PARASITE"), world.getTilesRandomly().getFirst(), world.getFaction(World.NO_FACTION_ID), null);
			System.out.println("parasite");
		}
		
		
		//random spawns
		if(World.days >= 10 && Math.random() > 0.1) {
//			int number = (int)(Math.random() * Season.FREEZING_TEMPURATURE * day);
//			for(int i = 0; i < number; i++) {
			world.spawnIceGiant(targetTile);
//			}
//			System.out.println(number + " ice giants");
		}
		if(World.days >= 10 && Math.random() > 0.1) {
//			int number = (int)(Math.random() * Season.FREEZING_TEMPURATURE * day/2);
//			number = 1;
//			for(int i = 0; i < number; i++) {
			world.spawnStoneGolem(targetTile);
//			}
//			System.out.println(number + " stone golem");
		}
		if(World.days >= 10 && Math.random() > 0.2) {
//			int number = (int)(Math.random() * day/2);
//			for(int i = 0; i < number; i++) {
			world.spawnRoc(targetTile);
//			}
//			System.out.println(number + " roc");
		}
		
		if(World.days >= 5 && Math.random() > 0.2) {
//			int number = (int)(Math.random()*day/2);
//			for(int i = 0; i < number; i++) {
			world.spawnEnt(targetTile);
//			}
//			System.out.println(number + " ents");
			
		}
		if(World.days >= 5 && Math.random() > 0.1) {
//			int number = (int)(Math.random() * day);
//			for(int i = 0; i < number; i++) {
			world.spawnTermite(targetTile);
//			}
//			System.out.println(number + " termite");
		}
		if(World.days >= 5 && Math.random() > 0.5) {
//			int number = (int)(Math.random() * day);
//			for(int i = 0; i < number; i++) {
			world.spawnBomb(targetTile);
//			}
//			System.out.println(number + " bomb");
		}

		Tile spawnTile = targetTile;
		for(Tile t: world.getTilesRandomly()) {
			if(t.getLocation().distanceTo(targetTile.getLocation()) < howFarAwayStuffSpawn 
					&& t.getFaction() == world.getFaction(World.NO_FACTION_ID)) {
				spawnTile = t;
			}
		}
		if(World.days >= 1 && Math.random() > 0.5) {
			int number = (int)(Math.random()*day);
			makeAnimal(spawnTile, Game.unitTypeMap.get("FLAMELET"), number);
			System.out.println(number + " flamelets");
		}
		
		if(Math.random() < 0.2) {
			makeAnimal(spawnTile, Game.unitTypeMap.get("WATER_SPIRIT"), 4);
			System.out.println(4 + " water spirits");
		}
//		if(ticks >= 3000 && Math.random() < 0.0005) {
//			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), World.NEUTRAL_FACTION);
//		}
	}
	private void nightEvents() {
		double day = Math.sqrt(World.days);
		Tile targetTile = getTargetTileForSpawns();
		if(World.days >= 10) {
			if(Math.random() > 0.5) {
				world.spawnWerewolf(targetTile);
			}
		}
		if(World.days >= 10) {
			int number = (int)(Math.random() * day);
			for(int i = 0; i < number; i++) {
				world.spawnVampire(targetTile);
			}
			System.out.println(number + " vampire");
		}
	}
	public void addResources(Faction faction) {
		for(ItemType itemType : ItemType.values()) {
			faction.getInventory().addItem(itemType, 1000);
		}
		
	}
	public void eruptVolcano() {
		world.eruptVolcano();
	}
	public void meteorStrike(){
		world.meteorStrike();
	}
	public void shadowWordDeath(int num){
		Tile t = world.getTilesRandomly().getFirst();
		for(int i = 0; i < num; i++) {
			world.spawnOgre(t);
			world.spawnDragon(t);
			world.spawnWerewolf(t);
			world.spawnEnt(t);
			world.spawnLavaGolem(t);
			world.spawnIceGiant(t);
			world.spawnSkeletonArmy(t);
			world.spawnStoneGolem(t);
			world.spawnRoc(t);
			world.spawnVampire(t);
			world.spawnBomb(t);
//			world.spawnAnimal(Game.unitTypeMap.get("BOMB"), world.getTilesRandomly().getFirst(), world.getFaction(World.NO_FACTION_ID), null);
			spawnCyclops();
		}
		for(int i = 0; i < num/2; i++) {
			spawnEverything();
		}
	}

	public void shadowWordPain(int num){
		Faction undead = world.getFaction(World.UNDEAD_FACTION_ID);
		Faction cyclops = world.getFaction(World.CYCLOPS_FACTION_ID);
//		for(int i = 0; i < 40; i++) {
//			world.spawnDragon(null);
//		}
		for(int x = 0; x < world.getWidth(); x++) {
			
			Tile tile;
			tile = world.get(new TileLoc(x, 0));
			world.spawnAnimal(Game.unitTypeMap.get("STONE_GOLEM"), tile, undead, null);
			world.spawnAnimal(Game.unitTypeMap.get("TERMITE"), tile, undead, null);
			tile = world.get(new TileLoc(x, 1));
			world.spawnAnimal(Game.unitTypeMap.get("VAMPIRE"), tile, undead, null);
			tile = world.get(new TileLoc(x, 2));
			world.spawnAnimal(Game.unitTypeMap.get("ICE_GIANT"), tile, undead, null);
			for(int y = 0; y < 3; y++) {
				tile = world.get(new TileLoc(x, y + 3));
				world.spawnAnimal(Game.unitTypeMap.get("SKELETON"), tile, undead, null);
			}
			
			tile = world.get(new TileLoc(x, world.getHeight() - 2));
			world.spawnAnimal(Game.unitTypeMap.get("ROC"), tile, cyclops, null);
			tile = world.get(new TileLoc(x, 5));
			world.spawnAnimal(Game.unitTypeMap.get("HORSEARCHER"), tile, undead, null);
			tile = world.get(new TileLoc(x, 6));
			world.spawnAnimal(Game.unitTypeMap.get("KNIGHT"), tile, undead, null);
			tile = world.get(new TileLoc(x, 7));
			world.spawnAnimal(Game.unitTypeMap.get("CATAPULT"), tile, undead, null);
			tile = world.get(new TileLoc(x, 8));
			world.spawnAnimal(Game.unitTypeMap.get("LONGBOWMAN"), tile, undead, null);
			tile = world.get(new TileLoc(x, 9));
			world.spawnAnimal(Game.unitTypeMap.get("ARCHER"), tile, undead, null);
		}
		for(int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if(x > 4 && x < world.getWidth() - 3) {
				if(x % 6 == 0) {
					tile = world.get(new TileLoc(x, world.getHeight() - 16));
					spawnCyclops(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		for(int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if(x > 4 && x < world.getWidth() - 3) {
				if(x % 6 == 3) {
					tile = world.get(new TileLoc(x, world.getHeight() - 10));
					spawnCyclops(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		for(int x = 0; x < world.getWidth(); x++) {
			Tile tile;
			if(x > 4 && x < world.getWidth() - 3) {
				if(x % 6 == 0) {
					tile = world.get(new TileLoc(x, world.getHeight() - 5));
					spawnCyclops(tile);
					world.spawnAnimal(Game.unitTypeMap.get("OGRE"), tile, cyclops, null);
					world.spawnAnimal(Game.unitTypeMap.get("TREBUCHET"), tile, cyclops, null);
				}
			}
		}
		int y = world.getHeight() - 18;
		for(int x = 0; x < 5; x++) {
			summonBuilding(world.get(new TileLoc(x, y)), Game.buildingTypeMap.get("WALL_WOOD"), cyclops);
			summonBuilding(world.get(new TileLoc(world.getWidth() - x, y)), Game.buildingTypeMap.get("WALL_WOOD"), cyclops);
		}
		
	}
	
	public void spawnEverything() {
		List<Tile> tiles = world.getTilesRandomly();
		
		Iterator<Tile> iterator = tiles.iterator();
		for(UnitType type : Game.unitTypeList) {
			if(type.name() == "TWIG") {
				System.out.println("twig");
				continue;
			}else {
				world.spawnAnimal(type, iterator.next(), world.getFaction(World.NO_FACTION_ID), null);
			}
			
		}
	}
	
	public void initializeWorld(int width, int height) {
		world = new World(width, height);
	}
	public void generateWorld(int width, int height, boolean easymode, List<PlayerInfo> players) {
		initializeWorld(width, height);

		Faction NO_FACTION = new Faction("NONE", false, false);
		world.addFaction(NO_FACTION);
		
		Faction CYCLOPS_FACTION = new Faction("CYCLOPS", false, true);
		CYCLOPS_FACTION.getInventory().addItem(ItemType.FOOD, 50);
		world.addFaction(CYCLOPS_FACTION);
		
		Faction UNDEAD_FACTION = new Faction("UNDEAD", false, true);
		UNDEAD_FACTION.getInventory().addItem(ItemType.FOOD, 999999);
		world.addFaction(UNDEAD_FACTION);
		
		Attack.world = world;
		world.generateWorld();
		makeRoads(easymode);
		world.clearDeadAndAddNewThings();
//		spawnCyclops();
//		meteorStrike();
		makeStartingCastleAndUnits(easymode, players);
	}
	public void spawnCyclops() {
		LinkedList<Tile> tiles = world.getTilesRandomly();
		Tile tile = tiles.peek();
		for(Tile t : tiles) {
			if(t.getTerrain() == Terrain.ROCK && t.getLocation().x() > 3 && t.getLocation().y() > 3 && t.getLocation().x() < world.getWidth()-3 && t.getLocation().y() < world.getHeight()-3) {
				tile = t;
				break;
			}
		}
		spawnCyclops(tile);
	}
	public void spawnCyclops(Tile tile) {
		summonBuilding(world.get(new TileLoc(tile.getLocation().x(), tile.getLocation().y())), Game.buildingTypeMap.get("WATCHTOWER"), world.getFaction(World.CYCLOPS_FACTION_ID));
		summonBuilding(world.get(new TileLoc(tile.getLocation().x()-1, tile.getLocation().y()-1)), Game.buildingTypeMap.get("GRANARY"), world.getFaction(World.CYCLOPS_FACTION_ID));
		summonBuilding(world.get(new TileLoc(tile.getLocation().x()+1, tile.getLocation().y()-1)), Game.buildingTypeMap.get("BARRACKS"), world.getFaction(World.CYCLOPS_FACTION_ID));
		summonBuilding(world.get(new TileLoc(tile.getLocation().x()+1, tile.getLocation().y()+1)), Game.buildingTypeMap.get("WINDMILL"), world.getFaction(World.CYCLOPS_FACTION_ID));
		summonBuilding(world.get(new TileLoc(tile.getLocation().x()-1, tile.getLocation().y()+1)), Game.buildingTypeMap.get("MINE"), world.getFaction(World.CYCLOPS_FACTION_ID));
		
		//makes the walls
		for(int i = 0; i < 5; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if(i == 2) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall;
			wall = world.get(new TileLoc(tile.getLocation().x()+3, tile.getLocation().y()-2 + i));
			summonBuilding(wall, type, world.getFaction(World.CYCLOPS_FACTION_ID));
			wall = world.get(new TileLoc(tile.getLocation().x()-3, tile.getLocation().y()-2 + i));
			summonBuilding(wall, type, world.getFaction(World.CYCLOPS_FACTION_ID));
		}
		for(int i = 1; i < 6; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if(i == 3) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall;
			int yoffset = i;
			if(i > 3) {
				yoffset = (6 - yoffset);
			}
			yoffset += tile.getLocation().x()%2;
			yoffset /= 2;
			
			wall = world.get(new TileLoc(tile.getLocation().x()-3 + i, tile.getLocation().y()-2 - yoffset));
			summonBuilding(wall, type, world.getFaction(World.CYCLOPS_FACTION_ID));

			yoffset = yoffset + (tile.getLocation().x() + i)%2 - 2 - (tile.getLocation().x()%2);
			wall = world.get(new TileLoc(tile.getLocation().x()-3 + i, tile.getLocation().y()+4 + yoffset));
			summonBuilding(wall, type, world.getFaction(World.CYCLOPS_FACTION_ID));
		}
		
		for(int i = -1; i < 2; i ++) {
			for(int j = -1; j < 2; j ++) {
				Tile temp = world.get(new TileLoc(tile.getLocation().x() + i, tile.getLocation().y() + j));
				if(temp == null) {
					continue;
				}
				Animal cyclops = world.spawnAnimal(Game.unitTypeMap.get("CYCLOPS"), temp, world.getFaction(World.CYCLOPS_FACTION_ID), null);
				cyclops.setPassiveAction(PlannedAction.GUARD);
			}
		}
	}

	public void buildingTick() {
		
		for(Building building : world.getBuildings()) {
			TileLoc loc = building.getTile().getLocation();
			double culture = building.getCulture();
			double area = culture * Building.CULTURE_AREA_MULTIPLIER;
			double radius = Math.sqrt(area);
			int r = (int)Math.ceil(radius);
			for (int i = -r; i <= r; i++) {
				for (int j = -r; j <= r; j++) {
					int distance = loc.distanceTo(new TileLoc(loc.x() + i, loc.y() + j));
//					double distanceFromCenter = Math.sqrt(i*i + j*j);
					if(distance < radius) {
						Tile tile = world.get(new TileLoc(building.getTile().getLocation().x()+i, building.getTile().getLocation().y()+j));
						if(tile != null && tile.getFaction() == world.getFaction(World.NO_FACTION_ID)) {
							tile.setFaction(building.getFaction());
							world.addToTerritory(tile);
//							world.updateBorderTiles();
						}
					}
				}
			}
			building.tick(world);
			if(building.isFull()) {
//				spawnCaravan(building);
			}
			
		}
	}
	
	
	public void flipTable() {
		for(Tile tile : world.getTiles()) {
			tile.setHeight(1 - tile.getHeight());
		}
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
					Building road = new Building(Game.buildingTypeMap.get("STONE_ROAD"), t, world.getFaction(World.NO_FACTION_ID));
					road.setRemainingEffort(0);
					t.setRoad(road);
					world.addBuilding(road);
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
	}
	private boolean isValidSpawnLocation(Tile spawnTile, int radius) {
		List<Tile> tiles = Utils.getTilesInRadius(spawnTile, world, radius);
		for(Tile t : tiles) {
			if(t == spawnTile) {
				continue;
			}
			if(!isValidSpawnTileForBuilding(t, Game.buildingTypeMap.get("CASTLE"))) {
				return false;
			}
		}
		return true;
	}
	private boolean isValidSpawnTileForBuilding(Tile tile, BuildingType type) {
		return tile.canBuild() == true 
				&& !tile.hasBuilding()
				&& tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()
				&& tile.getLocation().distanceTo(world.volcano) > 30
				&& (tile.getTerrain() != Terrain.ROCK || type != Game.buildingTypeMap.get("CASTLE"));
	}
	private void makeStartingCastleAndUnits(boolean easymode, List<PlayerInfo> players) {
		double spacePerPlayer = (double)world.getWidth()/players.size();
		int index = 0;
		for(PlayerInfo player : players) {
			Faction newFaction = new Faction(player.getName(), true, true, player.getColor());
			newFaction.getInventory().addItem(ItemType.WOOD, 200);
			newFaction.getInventory().addItem(ItemType.STONE, 200);
			newFaction.getInventory().addItem(ItemType.FOOD, 200);
			world.addFaction(newFaction);
			
			LinkedList<Object> thingsToPlace = new LinkedList<>();
			thingsToPlace.add(Game.buildingTypeMap.get("CASTLE"));
			thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
//			thingsToPlace.add(Game.unitTypeMap.get("WARRIOR"));
//			thingsToPlace.add(Game.buildingTypeMap.get("BLACKSMITH"));
//			thingsToPlace.add(Game.unitTypeMap.get("HORSEARCHER"));
//			thingsToPlace.add(Game.unitTypeMap.get("KNIGHT"));
			if(easymode) {
				thingsToPlace.add(Game.buildingTypeMap.get("BARRACKS"));
				thingsToPlace.add(Game.buildingTypeMap.get("WORKSHOP"));
				thingsToPlace.add(Game.buildingTypeMap.get("BLACKSMITH"));
				addResources(newFaction);
			}
			Tile spawnTile = world.get(new TileLoc((int) (index*spacePerPlayer + spacePerPlayer/2), world.getHeight()/2));
			int minRadius = 20;
			while(!isValidSpawnLocation(spawnTile, minRadius)) {
				spawnTile = world.get(new TileLoc((int) (index*spacePerPlayer + spacePerPlayer/2), (int) (Math.random()*world.getHeight())));
				minRadius = Math.max(0, minRadius-1);
			};
			
			HashSet<Tile> visited = new HashSet<>();
			LinkedList<Tile> tovisit = new LinkedList<>();
			
			tovisit.add(spawnTile);
			visited.add(spawnTile);
			
			while(!thingsToPlace.isEmpty()) {
				Tile current = tovisit.removeFirst();
				Object thingType = thingsToPlace.getFirst();
				if(thingType instanceof BuildingType) {
					BuildingType type = (BuildingType)thingType;
					if (isValidSpawnTileForBuilding(current, type)) {
						summonBuilding(current, type, newFaction);
						thingType = null;
						
					}
				}
				else if(thingType instanceof UnitType) {
					if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
						summonUnit(current, (UnitType)thingType, newFaction);
						thingType = null;
					}
				}
				if(thingType == null) {
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
			index++;
		}
	}
	
	
	private void updateTerritory() {
		for(Building building : world.getBuildings()) {
			building.updateCulture();
		}
	}
	
	public Thing summonUnit(Tile tile, UnitType unitType, Faction faction) {
		if(tile == null) {
			return null;
		}
		if(faction.isPlayer()) {
			Unit unit = new Unit(unitType, tile, faction);
			world.addUnit(unit);
			tile.addUnit(unit);
			unit.setTimeToAttack(0);
			return unit;
		} else {
			return world.spawnAnimal(unitType, tile, faction, null);
		}
	}
	public Thing summonBuilding(Tile tile, BuildingType buildingType, Faction faction) {
		if(tile == null) {
			return null;
		}
		if(tile.getBuilding() != null) {
			tile.getBuilding().setDead(true);
		}
		Building building = new Building(buildingType, tile, faction);
		building.setRemainingEffort(0);
		world.addBuilding(building);
		if(buildingType.isRoad()) {
			tile.setRoad(building);
		}
		else {
			tile.setBuilding(building);
		}
		return building;
	}
	public Thing summonPlant(Tile tile, PlantType plantType, Faction faction) {
		if(tile == null) {
			return null;
		}
		if(tile.getPlant() != null) {
			tile.getPlant().setDead(true);
		}
		Plant plant = new Plant(plantType, tile, faction);
		world.addPlant(plant);
		tile.setHasPlant(plant);
		return plant;
	
	}
	public Thing summonThing(Tile tile, Object thingType, Faction faction) {
		if(thingType instanceof UnitType) {
			return summonUnit(tile, (UnitType)thingType, faction);
		}
		else if(thingType instanceof BuildingType) {
			return summonBuilding(tile, (BuildingType)thingType, faction);
		}
		else if(thingType instanceof PlantType) {
			return summonPlant(tile, (PlantType)thingType, faction);
		}
		else {
			System.err.println("ERROR tried to summon invalid type: " + thingType);
			return null;
		}
	}
	
	public void spawnWeather(Tile center, int radius) {
		HashSet<Tile> tiles = world.getNeighborsInRadius(center, radius);
		for(Tile t: tiles) {
			t.liquidType = LiquidType.WATER;
			t.liquidAmount += 5;
//			double distance = t.getLocation().distanceTo(center.getLocation());
//			float height = (float) (t.getHeight() + (radius - distance) / (radius) * 0.1);
//			t.setHeight(height);
		}
	}
	public void setTerritory(Tile center, int radius, Faction faction) {
		HashSet<Tile> tiles = world.getNeighborsInRadius(center, radius);
		for(Tile t: tiles) {
			t.setFaction(faction);
			world.addToTerritory(t);
		}
	}

	public void toggleAutoBuild(ConcurrentLinkedQueue<Thing> selectedThings) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					unit.setAutoBuild(!unit.getAutoBuild());
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
	
	private void unitTick() {
		for(Unit unit : world.getUnits()) {
			unit.updateState();
			unit.planActions(world);
			unit.doMovement();
			unit.doAttacks(world);
			unit.doPassiveThings(world);
		}
	}

	private boolean canBuild(Unit unit, BuildingType bt, Tile tile) {
		if(bt.isRoad() && tile.getRoad() != null) {
			return false;
		}
		if (!bt.isRoad() && tile.hasBuilding()) {
			return false;
		}
		if(!unit.getFaction().canAfford(bt.getCost())) {
			return false;
		}
		if (bt == Game.buildingTypeMap.get("IRRIGATION") && tile.canPlant() == false) {
			return false;
		}
		return true;

	}

	public Building planBuilding(Unit unit, BuildingType bt, Tile tile) {
		if(canBuild(unit, bt, tile) == true) {
			unit.getFaction().payCost(bt.getCost());
			Building building = new Building(bt, tile, unit.getFaction());
			world.addBuilding(building);
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
		else if(bt.isRoad() && tile.getRoad() != null) {
			if(bt == tile.getRoad().getType()) {
				return tile.getRoad();
			}
		}
		else if(!bt.isRoad() && tile.getBuilding() != null) {
			if(bt == tile.getBuilding().getType()) {
				return tile.getBuilding();
			}
		}
		return null;
	}
	
	public Color getBackgroundColor() {
		double ratio = World.getDaylight();
		int c = (int)(ratio * 255);
		return new Color(c, c, c);
	}
	
	public void researchEverything(Faction faction) {
		faction.researchEverything();
		guiController.updateGUI();
	}
}
