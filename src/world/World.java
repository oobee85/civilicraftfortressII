package world;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.*;

import game.*;
import game.liquid.*;
import networking.message.*;
import networking.server.*;
import ui.*;
import utils.*;
import wildlife.*;

public class World {

	public static final int TICKS_PER_ENVIRONMENTAL_DAMAGE = 10;
	public static final double TERRAIN_SNOW_LEVEL = 1;
	public static final double DESERT_HUMIDITY = 1;
	public static final int DAY_DURATION = 500;
	public static final int NIGHT_DURATION = 350;
	public static final int TRANSITION_PERIOD = 100;
	private static final double CHANCE_TO_SWITCH_TERRAIN = 0.05;
	
	private static final double BUSH_RARITY = 0.005;
	private static final double WATER_PLANT_RARITY = 0.05;
	private static final double FOREST_DENSITY = 0.1;
	
	private LinkedList<Tile> tileList;
	private LinkedList<Tile> tileListRandom;
	
	private static final int NUM_LIQUID_SIMULATION_PHASES = 9;
	private ArrayList<ArrayList<Tile>> liquidSimulationPhases = new ArrayList<>(NUM_LIQUID_SIMULATION_PHASES);
	private Tile[][] tiles;
	
	public volatile ConcurrentHashMap<Tile, Faction> territory = new ConcurrentHashMap<>();
	private int width;
	private int height;
	
	public static final int NO_FACTION_ID = 0;
	public static final int CYCLOPS_FACTION_ID = 1;
	public static final int UNDEAD_FACTION_ID = 2;
	private ArrayList<Faction> factions = new ArrayList<>();
	
	private WorldData worldData;
	
	public TileLoc volcano;
	public int numCutTrees = 10;
	public static int nights = 0;
	public static int days = 1;
	public static volatile int ticks;
	
	public World(int width, int height) {
		worldData = new WorldData();
		this.width = width;
		this.height = height;
		tileList = new LinkedList<>();
		tileListRandom = new LinkedList<>();
		tiles = new Tile[width][height];
		liquidSimulationPhases.clear();
		for(int i = 0; i < NUM_LIQUID_SIMULATION_PHASES; i++) {
			liquidSimulationPhases.add(new ArrayList<>());
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = new Tile(new TileLoc(i, j), Terrain.DIRT);
				tileList.add(tiles[i][j]);
				tileListRandom.add(tiles[i][j]);
				
				// This one only has 5 phases
//				int phase = (i + 5 - (2*j)%5) % 5;
				// but this one has fewer cache invalidations
				int phase = 3*(i%3) + j%3;
				liquidSimulationPhases.get(phase).add(tiles[i][j]);
			}
		}

		for(Tile tile : getTiles()) {
			tile.setNeighbors(getNeighbors(tile));
		}
	}
	public void updateTiles(Tile[] tileInfos) {
		for(Tile info : tileInfos) {
			Tile tile = get(info.getLocation());
			if(tile == null) {
				System.out.println("Tried to update null tile at " + info.getLocation());
			}
			if(tile.getFaction() == null || tile.getFaction().id() != info.getFaction().id()) {
				tile.setFaction(factions.get(info.getFaction().id()));
				addToTerritory(tile);
//				updateBorderTiles();
			}
			tile.setHeight(info.getHeight());
			tile.setHumidity(info.getHumidity());
			tile.setResource(info.getResource());

			tile.setTerrain(info.getTerrain());
			tile.setModifier(info.getModifier());
			tile.liquidAmount = info.liquidAmount;
			tile.liquidType = info.liquidType;
		}
	}

	public Faction getFaction(String name) {
		for(Faction faction : factions) {
			if(faction.name().equals(name)) {
				return faction;
			}
		}
		return null;
	}
	public Faction getFaction(int id) {
		for(Faction faction : factions) {
			if(faction.id() == id) {
				return faction;
			}
		}
		return null;
	}
	public void addFaction(Faction faction) {
		factions.add(faction);
	}
	public ArrayList<Faction> getFactions() {
		return factions;
	}
	
	public int getTerritorySize() {
		return territory.size();
	}
	public void addToTerritory(Tile tile) {
		if(!territory.contains(tile)) {
			territory.put(tile, tile.getFaction());
		}
		
	}
//	public void updateBorderTiles() {
//		
//		for(Tile tile : territory.keySet()) {
//			
//			int numFail = 0;
//			for(Tile neighbor: tile.getNeighbors()) {
////				System.out.println("testing neighbors");
//				if(neighbor.getFaction() != tile.getFaction()) {
////					System.out.println("adding border");
//					addToBorderTerritory(tile);
//					break;
//				}else {
//					numFail ++;
//				}
//				
//			}
//			if(numFail == 4) {
////				System.out.println("removing border");
//				borderTerritory.remove(tile);
//			}
//			
//		}
//	}
//	
//	public void addToBorderTerritory(Tile tile) {
//		if(!borderTerritory.contains(tile)) {
//			borderTerritory.put(tile, tile.getFaction());
//		}
//	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public LinkedList<Tile> getTiles() {
		return tileList;
	}
	public LinkedList<Tile> getTilesRandomly() {
		Collections.shuffle(tileListRandom);
		return tileListRandom;
	}
	public ArrayList<ArrayList<Tile>> getLiquidSimulationPhases() {
		return liquidSimulationPhases;
	}
	
	public Tile get(TileLoc loc) {
		if(loc.x() < 0 || loc.x() >= tiles.length || loc.y() < 0 || loc.y() >= tiles[0].length) {
			return null;
		}
		return tiles[loc.x()][loc.y()];
	}
	
	public LinkedList<Building> getBuildings() {
		return worldData.getBuildings();
	}
	public void addBuilding(Building newBuilding) {
		worldData.addBuilding(newBuilding);
	}
	public LinkedList<Unit> getUnits() {
		return worldData.getUnits();
	}
	public void addUnit(Unit newUnit) {
		worldData.addUnit(newUnit);
	}
	public LinkedList<WeatherEvent> getWeatherEvents() {
		return worldData.getWeatherEvents();
	}
	public void addPlant(Plant newPlant) {
		worldData.addPlant(newPlant);
	}
	public LinkedList<Plant> getPlants() {
		return worldData.getPlants();
	}
	public WorldData getData() {
		return worldData;
	}

	public void drought() {
		for(Tile tile : getTiles()) {
			tile.liquidAmount = 0;
		}
	}
	
	public void rain() {
		
		//makes it so that it doesnt spawn the center of rain in deserts or on the volcano
		Tile rainTile = this.getTilesRandomly().peek();
		while(rainTile.getTerrain() == Terrain.SAND || rainTile.getTerrain() == Terrain.VOLCANO) {
			rainTile = this.getTilesRandomly().peek();
		}
		
		int radius = (int) (Math.random()*20 + 10);
		
		List<Tile> rainTiles = Utils.getTilesInRadius(rainTile, this, radius);
		TileLoc destination = getTilesRandomly().peek().getLocation();
		int dx = destination.x() - rainTile.getLocation().x();
		int dy = destination.y() - rainTile.getLocation().y();
		
		for(Tile t : rainTiles) {
			TileLoc target = new TileLoc(t.getLocation().x() + dx, t.getLocation().y() + dy);
			Tile targetTile = get(target);
			if(targetTile == null) {
				continue;
			}
			double temperature = t.getTempurature();
			WeatherEvent weather = new WeatherEvent(t, targetTile, t.getLocation().distanceTo(target)*WeatherEventType.RAIN.getSpeed() + (int)(Math.random()*50), 0.00002, LiquidType.WATER);;
			t.setWeather(weather);
			worldData.addWeatherEvent(weather);
		}
	}
	
	public void eruptVolcano() {
		System.out.println("eruption");
		this.get(volcano).liquidAmount += 200;
		
//		world[volcano].liquidType = LiquidType.WATER;
//		world[volcano].liquidAmount += 200;
	}
	
	public void spawnOgre(Tile target) {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> 
//		e.getTerrain() == Terrain.ROCK 
		e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(Game.unitTypeMap.get("OGRE"), tile.get(), getFaction(NO_FACTION_ID), target);
		}
	}
	
	public void spawnWerewolf(Tile target) {
		List<Unit> wolves = worldData.getUnits()
				.stream()
				.filter(e -> e.getType() == Game.unitTypeMap.get("WOLF"))
				.collect(Collectors.toList());
		if(wolves.size() == 0) {
			return;
		}
		Unit wolf = wolves.get((int)(Math.random()*wolves.size()));
		wolf.setDead(true);
		Tile t = wolf.getTile();
//		System.out.println("Werewolf at: "+t);
		spawnAnimal(Game.unitTypeMap.get("WEREWOLF"), t, getFaction(NO_FACTION_ID), target);
	}
	
	public void spawnLavaGolem(Tile target) {
		Optional<Tile> tile = getTilesRandomly()
				.stream()
				.filter(e -> 
//				e.getTerrain() == Terrain.VOLCANO 
				e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
				&& e.getFaction() == getFaction(NO_FACTION_ID))
				.findFirst();
		if(tile.isPresent()) {
			spawnAnimal(Game.unitTypeMap.get("LAVAGOLEM"), tile.get(), getFaction(NO_FACTION_ID), target);
		}
	}
	
	public void spawnEnt(Tile target) {
		Optional<Tile> tile = getTilesRandomly().stream().filter(
				e -> e.getTerrain() == Terrain.GRASS 
				&& e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn 
				&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(Game.unitTypeMap.get("ENT"), tile.get(), getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnIceGiant(Tile target) {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> 
//		e.liquidType == LiquidType.SNOW
		e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(Game.unitTypeMap.get("ICE_GIANT"), tile.get(), getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnDragon(Tile target) {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> 
					e.getTerrain() == Terrain.VOLCANO 
					&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(Game.unitTypeMap.get("DRAGON"), tile.get(), getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnStoneGolem(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
		e.getTerrain() == Terrain.ROCK 
		&& e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			spawnAnimal(Game.unitTypeMap.get("STONE_GOLEM"), t, getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnRoc(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
		e.getTerrain() == Terrain.ROCK 
		&& e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			spawnAnimal(Game.unitTypeMap.get("ROC"), t, getFaction(CYCLOPS_FACTION_ID), target);
		}
	}
	public void spawnTermite(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
		e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			spawnAnimal(Game.unitTypeMap.get("TERMITE"), t, getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnBomb(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
		e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			spawnAnimal(Game.unitTypeMap.get("BOMB"), t, getFaction(NO_FACTION_ID), target);
		}
	}
	public void spawnVampire(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
				e.getTerrain() == Terrain.ROCK 
				&& e.getLocation().distanceTo(target.getLocation()) < Game.howFarAwayStuffSpawn
				&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			spawnAnimal(Game.unitTypeMap.get("VAMPIRE"), t, getFaction(UNDEAD_FACTION_ID), target);
		}
	}
	public void spawnSkeletonArmy(Tile target) {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> 
		e.getTerrain() == Terrain.ROCK 
		&& e.getFaction() == getFaction(NO_FACTION_ID)).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			for(Tile tile : t.getNeighbors()) {
				for(int i = 0; i < 3; i++) {
					spawnAnimal(Game.unitTypeMap.get("SKELETON"), tile, getFaction(UNDEAD_FACTION_ID), target);
				}
			}
		}
	}
	public Animal spawnAnimal(UnitType type, Tile tile, Faction faction, Tile target) {
		Animal animal = makeAnimal(type, tile, faction);
		tile.addUnit(animal);
		worldData.addUnit(animal);
		if(target != null) {
			animal.queuePlannedAction(new PlannedAction(target));
		}
		return animal;
	}

	public Animal makeAnimal(UnitType type, Tile tile, Faction faction) {
		if(type == Game.unitTypeMap.get("FLAMELET")) {
			return new Flamelet(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("PARASITE")) {
			return new Parasite(tile, faction, this.get(volcano));
		}
		else if(type == Game.unitTypeMap.get("WEREWOLF")) {
			return new Werewolf(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("WATER_SPIRIT")) {
			return new WaterSpirit(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("ENT")) {
			return new Ent(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("DRAGON")) {
			return new Dragon(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("ICE_GIANT")) {
			return new IceGiant(tile, faction);
		}
		else if(type == Game.unitTypeMap.get("BOMB")) {
			return new Bomb(tile, faction, this);
		}
		else {
			return new Animal(type, tile, faction);
		}
	}
	
	public void makeAnimal(UnitType animalType, World world, TileLoc loc) {
		if(animalType.isAquatic() == false && world.get(loc).liquidAmount > world.get(loc).liquidType.getMinimumDamageAmount()/2 ) {
			return;
		}
		Animal animal = new Animal(animalType, world.get(loc), getFaction(NO_FACTION_ID));
		animal.setTile(world.get(loc));
		worldData.addUnit(animal);
		world.get(loc).addUnit(animal);
	}
	
	public void meteorStrike() {

		Optional<Tile> potential = this.getTilesRandomly().stream().filter(e -> e.getFaction() == getFaction(NO_FACTION_ID))
				.findFirst();
		if (potential.isPresent()) {
			Tile t = potential.get();

			int radius = (int) (Utils.getRandomNormal(2) * 30 + 5);
			System.out.println("meteor at: " + t.getLocation().x() + ", " + t.getLocation().y());
			;
			spawnExplosionCircle(t, radius, 5000);
			int rockRadius = radius / 5;
			spawnRock(t, rockRadius);
		}
	}
	public void spawnRock(Tile tile, int radius) {
		int numTiles = 0;
		for(Tile t : this.getTiles()) {
			int i =  t.getLocation().x();
			int j =  t.getLocation().y();
			int dx = i - tile.getLocation().x();
			int dy = j - tile.getLocation().y();
			double distanceFromCenter = Math.sqrt(dx * dx + dy * dy);
			
			if (distanceFromCenter < radius) {
				t.setTerrain(Terrain.VOLCANO);
				numTiles ++;
			}
		}
		int resource = (int) (Math.random()*ResourceType.values().length);
		ResourceType resourceType = ResourceType.values()[resource];
		Generation.makeOreVein(tile, resourceType, numTiles/2);
	}
	public HashSet<Tile> getNeighborsInRadius(Tile tile, int radius) {
		HashSet<Tile> neighbors = new HashSet<>();
		TileLoc tileLoc = tile.getLocation();
		int x = tileLoc.x();
		int y = tileLoc.y();
		for(int i = Math.max(x - radius, 0) ; i <= x + radius && i < tiles.length ; i ++) {
			for(int j = Math.max(y - radius, 0) ; j <= y + radius && j < tiles[i].length ; j ++) {
				if(tileLoc.distanceTo(new TileLoc(i, j)) >= radius) {
					continue;
				}
				neighbors.add(tiles[i][j]);
			}
		}
		return neighbors;

	}
	public void spawnExplosionCircle(Tile tile, int radius, int damage) {
//		int radius = 35;
		float amplitude = (float)(radius)/100;
		
		for(Tile t : Utils.getTilesInRadius(tile, this, 2*radius)) {
			
			double distanceFromCenter = t.getLocation().euclideanDistance(tile.getLocation());
			
			float delta = 0f;
			double cos = -Math.cos( (distanceFromCenter*(20f/radius)) / (2*Math.PI));
			
			if (distanceFromCenter < radius) {
				Projectile wave = new Projectile(ProjectileType.METEOR_WAVE, tile, t, null, damage);
				if(damage < 500) {
					wave = new Projectile(ProjectileType.FIRE_WAVE, tile, t, null, damage);
				}
				
				tile.addProjectile(wave);
				worldData.addProjectile(wave);
				
				
				
			}
			if(radius >= 10) {
				delta = (float)(amplitude*cos - 0.5*amplitude);
				if (distanceFromCenter >= radius && distanceFromCenter < 2*radius) {
					delta = (float)(amplitude*cos - 0.5*amplitude);
					if(delta < 0) {
						delta = 0f;
					}
				}
				if(delta != 0) {
					t.setHeight(t.getHeight() + delta);
				}
			}
			
		}
	}
	public void spawnExplosion(Tile tile, int radius, int damage) {
	
		for(Tile t : getNeighborsInRadius(tile, radius)) {

			t.replaceOrAddDurationModifier(
					GroundModifierType.FIRE, 
					10 + (int)(Math.random()*damage/5),
					worldData);
			
//			GroundModifier fire = new GroundModifier(GroundModifierType.FIRE, t, 10 + (int)(Math.random()*damage/5));
//			worldData.addGroundModifier(fire);
//			t.setModifier(fire);
			if(t.hasBuilding() == true) {
				t.getBuilding().takeDamage(damage);
			}
			for(Unit unit : t.getUnits()) {
				unit.takeDamage(damage);
			}
			if(t.getPlant() != null) {
				t.getPlant().takeDamage(damage);
			}
		}

	}
	
	public void updateDesertChange(Tile tile, boolean start) {
		int failTiles = 0;
		int numDesertNeighbors = 0;
		int numGrassNeighbor = 0;
		boolean grassNeighbor = false;
		
		//if it doesnt roll chance to change terrain, return
		if(Math.random() >= CHANCE_TO_SWITCH_TERRAIN && start == false) {
			return;
		}
		
		for(Tile t : tile.getNeighbors()) {
			//counts the tiles are too humid to be desert
			if(t.getHumidity() > DESERT_HUMIDITY) {
				failTiles ++;
			}
			//counts up how many neighbors are desert
			if(t.getTerrain() == Terrain.SAND) {
				numDesertNeighbors ++;
			}
			if(t.getTerrain() == Terrain.GRASS) {
				numGrassNeighbor ++;
			}
		}
//		
//		

		// changes terrain if it isnt in the humidity range of the terrain type
		Terrain terrain = tile.getTerrain();
		//if the humidity is less than the minimum terrain humidity
		if (tile.getHumidity() < terrain.getMinMax().x) {
			if (terrain == Terrain.GRASS) {
				tile.setTerrain(Terrain.DIRT);
			}
			
			//start variable indicates if the function was called when map was made
			//when start == true, we allow desert to be generated anywhere possible
			if (start == true) {
				
				//if there is only 1 ineligible desert tile, the dirt turns to sand
				if (terrain == Terrain.DIRT && failTiles < 2) {
					tile.setTerrain(Terrain.SAND);
				}
			//when start == false, we only allow desert to spread if nearby other desert tiles
			}else if (start == false) {
				if (terrain == Terrain.DIRT && failTiles < 2 && numDesertNeighbors >= 2 && numGrassNeighbor == 0) {
					tile.setTerrain(Terrain.SAND);
				}
			}
		}
		
		//if the humidity is more than the max terrain humidity
		if (tile.getHumidity() > terrain.getMinMax().y ) {
			if (terrain == Terrain.DIRT && tile.canGrow() && numGrassNeighbor >= 2) {
				tile.setTerrain(Terrain.GRASS);
				
			//if there are too many failed tiles to support desert
			} else if(terrain == Terrain.SAND && failTiles >= 2) {
				tile.setTerrain(Terrain.DIRT);
			}
		}
		
		//if there arent enough desert neighbors
		if(terrain == Terrain.SAND && numDesertNeighbors < 2) {
			tile.setTerrain(Terrain.DIRT);
		}
		
		//if there is a neighbor that is grass
		if(terrain == Terrain.SAND && grassNeighbor == true) {
			tile.setTerrain(Terrain.DIRT);
		}
		
		
	}
	public void updateTerrainChange(boolean start) {
		for(Tile tile : getTiles()) {
			
			
			if(tile.getResource() != null) {
				tile.getResource().tick(World.ticks);
			}
			tile.updateHumidity(World.ticks);
			
			updateDesertChange(tile, start);
			
			
			
			
			//turns grass to dirt if tile has a cold liquid || the temperature is cold
			if(tile.checkTerrain(Terrain.GRASS) && tile.isCold() && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
					tile.setTerrain(Terrain.DIRT);
				}
			}
			
			if(tile.checkTerrain(Terrain.BURNED_GROUND) && tile.liquidType != LiquidType.LAVA) {
				double chance = 0.05;
				if(Math.random() < chance) {
					tile.setTerrain(Terrain.DIRT);
				}
			}
			if(tile.checkTerrain(Terrain.DIRT)) {
				boolean adjacentGrass = false;
				boolean adjacentWater = false;
				for(Tile neighbor : Utils.getNeighbors(tile, this)) {
					if(neighbor.checkTerrain(Terrain.GRASS)) {
						adjacentGrass = true;
					}
					if(neighbor.liquidType == LiquidType.WATER) {
						adjacentWater = true;
					}
				}
				double threshold = CHANCE_TO_SWITCH_TERRAIN;
				if(tile.liquidType == LiquidType.WATER) {
					threshold += 0.001;
				}
				if(adjacentGrass) {
					threshold += 0.01;
				}
				if(adjacentWater) {
					threshold += 0.01;
				}
				if(adjacentGrass && adjacentWater) {
					threshold += 0.1;
				}
				if(tile.isCold() == false && Math.random() < tile.liquidAmount*threshold*tile.getHumidity() && tile.liquidType != LiquidType.ICE) {
					tile.setTerrain(Terrain.GRASS);
				}
			}
		}
		
	}
	
	private void spreadForest() {
		
		if(worldData.getPlants().size() >= 3000) {
			return;
		}
		for(Plant plant : worldData.getPlants()) {
			
			if(plant.getTile().isCold() == true) {
				continue;
			}
			if(plant.getPlantType() == PlantType.FOREST1) {
				if(Math.random() < 0.02) {
					for(Tile tile : plant.getTile().getNeighbors()) {
						if(tile.getPlant() == null && tile.canPlant()) {
							tile.setHasPlant(new Plant(PlantType.FOREST1, tile, getFaction(NO_FACTION_ID)));
							worldData.addPlant(tile.getPlant());
							break;
						}
					}
				}
			}
		}
		
		
	}
	public void grow() {
		spreadForest();
		
		for(Tile tile : getTilesRandomly()) {
			
			if(tile.canPlant() == false || tile.isCold() == true) {
				continue;
			}
			
			if(tile.getPlant() != null) {
				continue;
			}
			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(Math.random() < 0.01) {
					Plant plant = new Plant(PlantType.CATTAIL, tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(plant);
					worldData.addPlant(plant);
				}
			}
			if(tile.liquidType != null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
				if (Math.random() < 0.001) {
					tile.setHasPlant(new Plant(PlantType.BERRY, tile, getFaction(NO_FACTION_ID)));
					worldData.addPlant(tile.getPlant());
				}
			}
			

		}
	}
	public void doWeatherUpdate() {
		for(WeatherEvent weather : worldData.getWeatherEvents()) {
			weather.tick();
			Tile tile = weather.getTile();
			if(weather.getTargetTile() == null) {
				continue;
			}
			if (weather.readyToMove()) {
				weather.moveToTarget();
			}
			
			if(tile.liquidType == LiquidType.LAVA) {
				continue;
			}
			if(tile.liquidType != weather.getLiquidType() && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()/2) {
				continue;
			}
			tile.liquidType = weather.getLiquidType();
			tile.liquidAmount += weather.getStrength();
			
			
		}
	}
	
	public void doProjectileUpdates(boolean simulated) {

		for(Projectile projectile : worldData.getProjectiles()) {
			projectile.tick();
			if(projectile.getTargetTile() == null) {
				continue;
			}
			if (projectile.readyToMove()) {
				projectile.moveToTarget();
				if(projectile.getType().getGroundModifierType() != null) {
//					if(projectile.getTile().getModifier() != null) {
//						if(projectile.getTile().getModifier().getType() != projectile.getType().getGroundModifierType()) {
//							projectile.getTile().getModifier().finish();
//							GroundModifier gm = new GroundModifier(projectile.getType().getGroundModifierType(), projectile.getTile(), projectile.getType().getGroundModifierDuration());
//							projectile.getTile().setModifier(gm);
//							worldData.addGroundModifier(gm);
//						}
//						else {
//							projectile.getTile().getModifier().addDuration(projectile.getType().getGroundModifierDuration());
//						}
//					}
//					else {
//						GroundModifier gm = new GroundModifier(projectile.getType().getGroundModifierType(), projectile.getTile(), projectile.getType().getGroundModifierDuration());
//						projectile.getTile().setModifier(gm);
//						worldData.addGroundModifier(gm);
//					}
					projectile.getTile().replaceOrAddDurationModifier(
							projectile.getType().getGroundModifierType(), 
							projectile.getType().getGroundModifierDuration(),
							worldData);
				}
			}
			if(!simulated && projectile.reachedTarget()) {
				if(projectile.getType().isExplosive()) {
					if(projectile.getType().getRadius() <= 2) {
						spawnExplosion(projectile.getTile(), projectile.getType().getRadius(), (int)projectile.getDamage());
					}else {
						spawnExplosionCircle(projectile.getTile(), projectile.getType().getRadius(), (int)projectile.getDamage());
					}
					
				} 
				else {
					for(Unit unit : projectile.getTile().getUnits()) {
						unit.takeDamage(projectile.getDamage());
						unit.aggro(projectile.getSource());
					}
					if(projectile.getTile().getPlant() != null) {
						projectile.getTile().getPlant().takeDamage(projectile.getDamage());
					}
					if(projectile.getTile().hasBuilding() == true) {
						projectile.getTile().getBuilding().takeDamage(projectile.getDamage());
					}
				}
			}
		}
	}
	
	public void clearDeadAndAddNewThings() {
		// FACTIONS
		for(Faction f : factions) {
			f.clearExpiredAttackedNotifications();
		}
		
		worldData.filterDeadUnits();
		worldData.filterDeadGroundModifiers();
		worldData.filterDeadWeatherEvents();
		worldData.filterDeadBuildings();
		worldData.filterDeadPlants();
		worldData.filterDeadProjectiles();
		
		if(World.ticks % 200 == 1) {
			System.out.println("Tick " + World.ticks + ", " + worldData.toString());
		}
	}
	
	public void updatePlantDamage() {
		for(Plant plant : worldData.getPlants()) {
			Tile tile = plant.getTile();
			
//			if(tile.isCold()) {
//				plant.takeDamage(1);
//			}
			if (plant.isAquatic() && tile.liquidType == LiquidType.WATER) {
				if (tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {

					double difInLiquids = tile.liquidType.getMinimumDamageAmount() - tile.liquidAmount;
					double damageTaken = difInLiquids * tile.liquidType.getDamage();
					int roundedDamage = (int) (damageTaken + 1);
					if (roundedDamage >= 1) {
						plant.takeDamage(roundedDamage);
					}
				}
			} else {
				int totalDamage = 0;
				double modifierDamage = 0;
				
				//adds the damage of groundmodifier
				if(tile.getModifier() != null) {
					modifierDamage += tile.getModifier().getType().getDamage();
				}
				
				
				if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
					if(plant.isAquatic() == false || tile.liquidType != LiquidType.WATER) {
						//adds damage of liquids
						double liquidDamage = tile.liquidAmount * tile.liquidType.getDamage();
						totalDamage += liquidDamage;
					}
				}
				if(tile.getTerrain().isPlantable(tile.getTerrain()) == false) {
					totalDamage += 5;
				}
				
				totalDamage = (int) (modifierDamage+totalDamage);
				if(totalDamage >= 1) {
					plant.takeDamage(totalDamage);
				}
			}
		}	
	}

	public void genPlants() {
		for(Tile tile : getTiles()) {
			//generates land plants
			if(tile.checkTerrain(Terrain.GRASS) && tile.getRoad() == null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount() / 2 && Math.random() < BUSH_RARITY) {
				double o = Math.random();
				if(o < PlantType.BERRY.getRarity()) {
					Plant p = new Plant(PlantType.BERRY, tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(p);
					worldData.addPlant(tile.getPlant());
				}
			}
			//tile.liquidType.WATER &&
			//generates water plants
			if( Math.random() < WATER_PLANT_RARITY) {
				double o = Math.random();
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()  && o < PlantType.CATTAIL.getRarity()) {
					Plant p = new Plant(PlantType.CATTAIL, tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(p);
					worldData.addPlant(tile.getPlant());
				}
			}
		}
	}


	public void makeForest() {
		
		for(Tile t : tileListRandom) {
			double tempDensity = FOREST_DENSITY;
			if(t.getTerrain() == Terrain.DIRT) {
				tempDensity /= 2;
			}
			if (t.canPlant() && t.getRoad() == null && t.liquidAmount < t.liquidType.getMinimumDamageAmount() / 2)
				if (Math.random() < tempDensity) {
					Plant plant = new Plant(PlantType.FOREST1, t, getFaction(NO_FACTION_ID));
					t.setHasPlant(plant);
					worldData.addPlant(plant);
				}
		}
		
	}
	
	public List<Tile> getNeighbors(Tile tile) {
		int x = tile.getLocation().x();
		int y = tile.getLocation().y();
//		int minX = Math.max(0, tile.getLocation().x() - 1);
//		int maxX = Math.min(this.getWidth()-1, tile.getLocation().x() + 1);
//		int minY = Math.max(0, tile.getLocation().y()-1);
//		int maxY = Math.min(this.getHeight()-1, tile.getLocation().y() + 1);

		LinkedList<TileLoc> possibleNeighbors = new LinkedList<>();
		possibleNeighbors.add(new TileLoc(x - 1, y));
		possibleNeighbors.add(new TileLoc(x + 1, y));
		possibleNeighbors.add(new TileLoc(x, y - 1));
		possibleNeighbors.add(new TileLoc(x, y + 1));
//		possibleNeighbors.add(new TileLoc(x + 1, y + 1));
		if(x%2 == 1) {
			possibleNeighbors.add(new TileLoc(x - 1, y + 1));
			possibleNeighbors.add(new TileLoc(x + 1, y + 1));
		}
		else {
			possibleNeighbors.add(new TileLoc(x - 1, y - 1));
			possibleNeighbors.add(new TileLoc(x + 1, y - 1));
		}
		LinkedList<Tile> tiles = new LinkedList<>();
		for(TileLoc possible : possibleNeighbors) {
			Tile t = this.get(possible);
			if(t != null) {
				tiles.add(t);
			}
		}
//		for(int i = minX; i <= maxX; i++) {
//			for(int j = minY; j <= maxY; j++) {
//				if(i == x || j == y) {
//					if(i != x || j != y) {
//						if(this.get(new TileLoc(i, j)) != null) {
//							tiles.add(this.get(new TileLoc(i, j)));
//						}
//					}
//				}
//			}
//		}
		Collections.shuffle(tiles); 
		return tiles;
	}
	
	public void generateWorld() {
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		float[][] heightMap = Generation.generateHeightMap(smoothingRadius, width, height);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		volcano = Generation.makeVolcano(this, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);

		for(Tile tile : getTiles()) {
			tile.setFaction(getFaction(NO_FACTION_ID));
			tile.setHeight(heightMap[tile.getLocation().x()][tile.getLocation().y()]);
		}

		LinkedList<Tile> tiles = getTilesRandomly();
		Collections.sort(tiles, new Comparator<Tile>() {
			@Override
			public int compare(Tile o1, Tile o2) {
				return o1.getHeight() > o2.getHeight() ? 1 : -1;
			}
		});
		double rockpercentage = 0.30;
		double cutoff = tiles.get((int)((1-rockpercentage)*tiles.size())).getHeight();
		for(Tile tile : getTiles()) {
			if(tile.getTerrain() == Terrain.DIRT) {
				Terrain t;
				if (tile.getHeight() > cutoff) {
					t = Terrain.ROCK;
				}
				else if (tile.getHeight() > 0.4) {
					t = Terrain.DIRT;
				}
				else {
					t = Terrain.GRASS;
				}
//				else {
//					t = Terrain.WATER;
//				}
				tile.setTerrain(t);
			}
		}

		int numTiles = width*height;
		Generation.makeLake(numTiles * 1.0/100, this);
		Generation.makeLake(numTiles * 1.0/200, this);
		Generation.makeLake(numTiles * 1.0/400, this);
		Generation.makeLake(numTiles * 1.0/800, this);
		System.out.println("Simulating water for 100 iterations");
		for(int i = 0; i < 100; i++) {
			Liquid.propogate(this);
			updateTerrainChange(true);
		}
		
		
		Generation.generateResources(this);
		this.genPlants();
		this.makeForest();
		Generation.generateWildLife(this);
		System.out.println("Finished generating " + width + "x" + height + " world with " + tileList.size() + " tiles.");
	}
	

	public BufferedImage[] createTerrainImage(Faction faction) {
		double brighnessModifier = getDaylight();
		HashMap<Terrain, Color> terrainColors = new HashMap<>();
		for(Terrain t : Terrain.values()) {
			BufferedImage image = Utils.toBufferedImage(t.getImage(0));
			int sumr = 0;
			int sumg = 0;
			int sumb = 0;
			for(int i = 0; i < image.getWidth(); i++) {
				for(int j = 0; j < image.getHeight(); j++) {
					Color c = new Color(image.getRGB(i, j));
					sumr += c.getRed();
					sumg += c.getGreen();
					sumb += c.getBlue();
				}
			}
			int totalNumPixels = image.getWidth()*image.getHeight();
			Color average = new Color(sumr/totalNumPixels, sumg/totalNumPixels, sumb/totalNumPixels);
			terrainColors.put(t, average);
		}
		BufferedImage terrainImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage minimapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);

		Graphics minimapGraphics = minimapImage.getGraphics();
		Graphics terrainGraphics = terrainImage.getGraphics();
		for(Tile tile : this.getTiles()) {
			Color minimapColor = terrainColors.get(tile.getTerrain());
			Color terrainColor = terrainColors.get(tile.getTerrain());
			if(tile.getResource() != null && faction.areRequirementsMet(tile.getResource().getType())) {
				terrainColor = tile.getResource().getType().getColor(0);
				minimapColor = tile.getResource().getType().getColor(0);
			}
			if(tile.getRoad() != null) {
				terrainColor = tile.getRoad().getType().getColor(0);
				minimapColor = tile.getRoad().getType().getColor(0);
			}
			if(tile.liquidAmount > 0) {
				double alpha = Utils.getAlphaOfLiquid(tile.liquidAmount);
				minimapColor = Utils.blendColors(tile.liquidType.getColor(0), minimapColor, alpha);
				terrainColor = Utils.blendColors(tile.liquidType.getColor(0), terrainColor, alpha);
			}
			if(tile.getPlant() != null) {
				terrainColor = tile.getPlant().getColor(0);
				minimapColor = tile.getPlant().getColor(0);
			}
			if(tile.hasBuilding()) {
				terrainColor = tile.getBuilding().getColor(0);
				minimapColor = tile.getBuilding().getColor(0);
			}
			if(tile.getModifier() != null) {
				minimapColor = tile.getModifier().getType().getColor(0);
				terrainColor = Utils.blendColors(tile.getModifier().getType().getColor(0), terrainColor, 0.9);
			}
			if(tile.getFaction() != getFaction(NO_FACTION_ID)) {
				minimapColor = Utils.blendColors(tile.getFaction().color(), minimapColor, 0.3);
				terrainColor = Utils.blendColors(tile.getFaction().color(), terrainColor, 0.3);
			}
			double tilebrightness = tile.getBrightness(faction);
			minimapColor = Utils.blendColors(minimapColor, Color.black, brighnessModifier + tilebrightness);
			terrainColor = Utils.blendColors(terrainColor, Color.black, brighnessModifier + tilebrightness);
			
			minimapImage.setRGB(tile.getLocation().x(), tile.getLocation().y(), minimapColor.getRGB());
			terrainImage.setRGB(tile.getLocation().x(), tile.getLocation().y(), terrainColor.getRGB());
		}
		for(AttackedNotification notification : faction.getAttackedNotifications()) {
			minimapImage.setRGB(notification.tile.getLocation().x(), notification.tile.getLocation().y(), Color.red.getRGB());
			terrainImage.setRGB(notification.tile.getLocation().x(), notification.tile.getLocation().y(), Color.red.getRGB());
		}
		minimapGraphics.dispose();
		terrainGraphics.dispose();
		
		BufferedImage heightMapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		for(Tile tile : getTiles() ) {
			int r = Math.max(Math.min((int)(255*tile.getHeight()), 255), 0);
			Color c = new Color(r, 0, 255-r);
			heightMapImage.setRGB(tile.getLocation().x(), tile.getLocation().y(), c.getRGB());
		}
		BufferedImage humidityMapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		
		double highHumidity = Double.MIN_VALUE;
		double lowHumidity = Double.MAX_VALUE;
		for(Tile tile : getTiles() ) {
			highHumidity = Math.max(highHumidity, tile.getHumidity());
			lowHumidity = Math.min(lowHumidity, tile.getHumidity());
		}
		
		for(Tile tile : getTiles() ) {
			float humidityRatio = (float) ((tile.getHumidity() - lowHumidity) / (highHumidity - lowHumidity));
			float insidePara = ((humidityRatio - 0.5f)*1.74f);
			float almostRatio = (insidePara*insidePara*insidePara*insidePara*insidePara + 0.5f);
			int r = Math.max(Math.min((int)(255*almostRatio), 255), 0);
			Color c = new Color(255 - r, 0, r);
			humidityMapImage.setRGB(tile.getLocation().x(), tile.getLocation().y(), c.getRGB());
		}
		return new BufferedImage[] { 
				ImageCreation.convertToHexagonal(terrainImage), 
				ImageCreation.convertToHexagonal(minimapImage), 
				ImageCreation.convertToHexagonal(heightMapImage),
				ImageCreation.convertToHexagonal(humidityMapImage) };
		
	}

	public int ticksUntilDay() {
		int currentDayOffset = World.ticks%(DAY_DURATION + NIGHT_DURATION);
		int skipAmount = (DAY_DURATION + NIGHT_DURATION - TRANSITION_PERIOD) - currentDayOffset;
		if(skipAmount < 0) {
			skipAmount += DAY_DURATION + NIGHT_DURATION;
		}
		return skipAmount;
	}
	
	public static boolean isNightTime() {
		return getDaylight() < 0.4;
	}
	public static int getCurrentDayOffset() {
		return (World.ticks + TRANSITION_PERIOD)%(DAY_DURATION + NIGHT_DURATION);
	}
	public static double getDaylight() {
		if(Game.DISABLE_NIGHT) {
			return 1;
		}
		double ratio = 1;
		int currentDayOffset = getCurrentDayOffset();
		if(currentDayOffset < TRANSITION_PERIOD) {
			ratio = 0.5 + 0.5*currentDayOffset/TRANSITION_PERIOD;
		}
		else if(currentDayOffset < DAY_DURATION - TRANSITION_PERIOD) {
			ratio = 1;
		}
		else if(currentDayOffset < DAY_DURATION + TRANSITION_PERIOD) {
			ratio = 0.5 - 0.5*(currentDayOffset - DAY_DURATION)/TRANSITION_PERIOD;
		}
		else if(currentDayOffset < DAY_DURATION + NIGHT_DURATION - TRANSITION_PERIOD) {
			ratio = 0;
		}
		else {
			ratio = 0.5 - 0.5*(DAY_DURATION + NIGHT_DURATION - currentDayOffset)/TRANSITION_PERIOD;
		}
		return ratio;
	}
	
}
