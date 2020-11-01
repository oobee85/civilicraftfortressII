package ui;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.*;

import game.*;
import liquid.*;
import networking.server.*;
import utils.*;
import wildlife.*;
import world.*;

public class Game {
	
	public static final CombatStats combatBuffs = new CombatStats(0, 0, 0, 0, 0, 0, 0);

	public static final ArrayList<UnitType> unitTypeList = new ArrayList<>();
	public static final HashMap<String, UnitType> unitTypeMap = new HashMap<>();
	public static final ArrayList<BuildingType> buildingTypeList = new ArrayList<>();
	public static final HashMap<String, BuildingType> buildingTypeMap = new HashMap<>();
	public static final ArrayList<ResearchType> researchTypeList = new ArrayList<>();
	public static final HashMap<String, ResearchType> researchTypeMap = new HashMap<>();

	public static boolean USE_BIDIRECTIONAL_A_STAR = true;
	public static boolean DISABLE_NIGHT = false;
	
	private GUIController guiController;

	public World world;
	
	public Game(GUIController guiController) {
		this.guiController = guiController;
		
		Loader.doTargetingMappings();
		
//		resources.get(ItemType.IRON_ORE).addAmount(200);
//		resources.get(ItemType.COPPER_ORE).addAmount(200);
//		resources.get(ItemType.HORSE).addAmount(200);
//		resources.get(ItemType.FOOD).addAmount(2000);
//		resources.get(ItemType.WOOD).addAmount(2000);
//		resources.get(ItemType.ROCK).addAmount(2000);
		
	}
	
	public void weatherEvents() {
		if(World.days > 10 && Math.random() < 0.00001) {
			meteorStrike();
		}
		

		// rain event
		if(Math.random() < 0.004) {
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
	public GUIController getGUIController() {
		return guiController;
	}
	public int getDays() {
		return World.days;
	}
	public int getNights() {
		return World.nights;
	}
	public void gameTick() {
		// Do all the game events like unit movement, time passing, building things, growing, etc
		// happens once every 100ms
		World.ticks++;
		
		if(World.ticks%20 == 0) {
			updateTerritory();
		}
		
		Liquid.propogate(world);
		
		// Remove dead things
		world.clearDeadAndAddNewThings();
		
		buildingTick();
		unitTick();
		world.doProjectileUpdates();
		if(World.ticks%World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			world.updatePlantDamage();
		}
		
		
		if(World.ticks % (World.DAY_DURATION + World.NIGHT_DURATION) == 0) {
			dayEvents();
			World.days ++;
		}
		if((World.ticks + World.DAY_DURATION) % (World.DAY_DURATION + World.NIGHT_DURATION) == 0) {
			nightEvents();
			World.nights ++;
		}
		world.doWeatherUpdate();
		weatherEvents();
		// GUI updates
		world.updateTerrainChange(world);
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
		if(World.days % 5 == 0) {
			for(int i = 0; i < World.days/5; i++) {
				world.spawnLavaGolem();
				world.spawnIceGiant();
			}
			System.out.println(World.days/5 + " lava & ice giants");
			
		}
		if(World.days % 20 == 0) {
			meteorStrike();
		}
		if(World.days % 8 == 0) {
			for(int i = 0; i < World.days/8; i++) {
				world.spawnOgre();
			}
			System.out.println(World.days/8 + " ogres");
			
		}
		if(World.days % 10 == 0) {
			for(int i = 0; i < World.days/10; i++) {
				world.spawnSkeletonArmy();
			}
			System.out.println(World.days/10 + " skeletons");
		}
		if(World.days % 20 == 0) {
			spawnCyclops();
			System.out.println("cyclops");
		}
		if(World.days % 15 == 0) {
			world.spawnAnimal(Game.unitTypeMap.get("PARASITE"), world.getTilesRandomly().getFirst(), World.NO_FACTION);
			System.out.println("parasite");
		}
		
		
		if(World.days >= 10) {
			int number = (int)(Math.random() / Season.FREEZING_TEMPURATURE * World.days/10);
			for(int i = 0; i < number; i++) {
				world.spawnIceGiant();
			}
			System.out.println(number + " ice giants");
		}
		if(World.days >= 5) {
			int number = (int)(Math.random()*World.days/4);
			for(int i = 0; i < number; i++) {
				world.spawnEnt();
			}
			System.out.println(number + " ents");
			
		}
		if(World.days >= 1) {
			int number = (int)(Season.MELTING_TEMPURATURE + Math.random()*World.days);
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
		if(World.days >= 10) {
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
	public void addResources(Faction faction) {
		for(ItemType itemType : ItemType.values()) {
			faction.addItem(itemType, 1000);
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
	
	public void initializeWorld(int width, int height) {
		world = new World(width, height);
	}
	public void generateWorld(int width, int height, boolean easymode, List<PlayerInfo> players) {
		initializeWorld(width, height);
		Attack.world = world;
		world.generateWorld();
		makeRoads(easymode);
		world.clearDeadAndAddNewThings();
		spawnCyclops();
		meteorStrike();
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
		summonThing(world.get(new TileLoc(tile.getLocation().x(), tile.getLocation().y())), Game.buildingTypeMap.get("WATCHTOWER"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x()-1, tile.getLocation().y()-1)), Game.buildingTypeMap.get("GRANARY"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x()+1, tile.getLocation().y()-1)), Game.buildingTypeMap.get("BARRACKS"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x()+1, tile.getLocation().y()+1)), Game.buildingTypeMap.get("WINDMILL"), World.CYCLOPS_FACTION);
		summonThing(world.get(new TileLoc(tile.getLocation().x()-1, tile.getLocation().y()+1)), Game.buildingTypeMap.get("MINE"), World.CYCLOPS_FACTION);
		
		//makes the walls
		for(int i = 0; i < 6; i++) {
			BuildingType type = Game.buildingTypeMap.get("WALL_WOOD");
			if(i == 3) {
				type = Game.buildingTypeMap.get("GATE_WOOD");
			}
			Tile wall = world.get(new TileLoc(tile.getLocation().x()-3 + i, tile.getLocation().y()-3));
			summonThing(wall, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x()+3, tile.getLocation().y()-3 + i));
			summonThing(wall, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x()+3 - i, tile.getLocation().y()+3));
			summonThing(wall, type, World.CYCLOPS_FACTION);
			wall = world.get(new TileLoc(tile.getLocation().x()-3, tile.getLocation().y()+3 - i));
			summonThing(wall, type, World.CYCLOPS_FACTION);
		}
		for(int i = -1; i < 2; i ++) {
			for(int j = -1; j < 2; j ++) {
				Tile temp = world.get(new TileLoc(tile.getLocation().x() + i, tile.getLocation().y() + j));
				Animal cyclops = world.spawnAnimal(Game.unitTypeMap.get("CYCLOPS"), temp, World.CYCLOPS_FACTION);
				cyclops.setPassiveAction(PlannedAction.GUARD);
			}
			
		}
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
						Tile tile = world.get(new TileLoc(building.getTile().getLocation().x()+i, building.getTile().getLocation().y()+j));
						if(tile != null && tile.getFaction() == World.NO_FACTION) {
							tile.setFaction(building.getFaction());
							world.addToTerritory(tile);
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
	}
	private void makeStartingCastleAndUnits(boolean easymode, List<PlayerInfo> players) {
		int index = 0;
		for(PlayerInfo player : players) {
			Faction newFaction = new Faction(player.getName(), true, true, player.getColor());
			newFaction.setupResearch();
			newFaction.addItem(ItemType.WOOD, 200);
			newFaction.addItem(ItemType.STONE, 200);
			newFaction.addItem(ItemType.FOOD, 200);
			world.addFaction(newFaction);
			
			LinkedList<HasImage> thingsToPlace = new LinkedList<>();
			thingsToPlace.add(Game.buildingTypeMap.get("CASTLE"));
			thingsToPlace.add(Game.unitTypeMap.get("WORKER"));
			thingsToPlace.add(Game.unitTypeMap.get("WARRIOR"));
			if(easymode || true) {
				thingsToPlace.add(Game.buildingTypeMap.get("BARRACKS"));
				thingsToPlace.add(Game.buildingTypeMap.get("WORKSHOP"));
				thingsToPlace.add(Game.buildingTypeMap.get("BLACKSMITH"));
				addResources(newFaction);
			}
			Tile spawnTile = world.get(new TileLoc(world.getWidth()/2 + index*40 - players.size()*40/2, world.getHeight()/2));
			HashSet<Tile> visited = new HashSet<>();
			LinkedList<Tile> tovisit = new LinkedList<>();
			
			tovisit.add(spawnTile);
			visited.add(spawnTile);
			
			while(!thingsToPlace.isEmpty()) {
				Tile current = tovisit.removeFirst();
				HasImage thing = thingsToPlace.getFirst();
				if(thing instanceof BuildingType) {
					BuildingType type = (BuildingType)thing;
					if (current.canBuild() == true 
							&& !current.hasBuilding()
							&& current.liquidAmount < current.liquidType.getMinimumDamageAmount()
							&& (current.getTerrain() != Terrain.ROCK || type != Game.buildingTypeMap.get("CASTLE"))) {
						Building s = new Building(type, current, newFaction);
						current.setBuilding(s);
						world.newBuildings.add(s);
						s.setRemainingEffort(0);
						thing = null;
					}
				}
				else if(thing instanceof UnitType) {
					if (current.liquidAmount < current.liquidType.getMinimumDamageAmount()) {
						summonThing(current, thing, newFaction);
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
			index++;
		}
	}
	
	
	private void updateTerritory() {
		for(Building building : world.buildings) {
			building.updateCulture();
		}
	}
	
	public Thing summonThing(Tile tile, HasImage thingType, Faction faction) {
		
		if(thingType instanceof UnitType) {
			UnitType unitType = (UnitType)thingType;
			if(faction.isPlayer()) {
				Unit unit = new Unit(unitType, tile, faction);
				world.newUnits.add(unit);
				tile.addUnit(unit);
				unit.setTimeToAttack(0);
				return unit;
			} else {
				return world.spawnAnimal(unitType, tile, faction);
			}
		}
		else if(thingType instanceof BuildingType) {
			BuildingType buildingType = (BuildingType)thingType;
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
	public void toggleGuarding(ConcurrentLinkedQueue<Thing> selectedThings) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				unit.setGuarding(!unit.isGuarding());
			}
		}
	}
	public void setHarvesting(ConcurrentLinkedQueue<Thing> selectedThings) {
		for(Thing thing : selectedThings) {
			if(thing instanceof Unit) {
				Unit unit = (Unit)thing;
				if(unit.getType().isBuilder()) {
					unit.setHarvesting(!unit.getIsHarvesting());
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

	public void spawnUnit(boolean show) {
		guiController.selectedSpawnUnit(show);
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
	
	
	private void buildUnit(UnitType u, Tile tile, Faction faction) {
		System.out.println("building " + u);
		if(faction.canAfford(u.getCost())) {
			System.out.println("can afford " + u);
			Unit unit = new Unit(u, tile, faction);
			if (tile.isBlocked(unit)) {
				return;
			}
			faction.payCost(u.getCost());
			tile.getBuilding().setProducingUnit(unit);
			System.out.println("built " + u);
		}
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
