package world;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.*;

import javax.imageio.*;

import game.*;
import game.actions.*;
import networking.message.*;
import networking.server.*;
import ui.*;
import utils.*;
import wildlife.*;
import world.liquid.*;

public class World {
	
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
	public int eruptingUntil = 0;
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
			tile.getAir().setHumidity(info.getAir().getHumidity());
			tile.setResource(info.getResource());

			tile.setTerrain(info.getTerrain());
			tile.setModifier(info.getModifier());
			tile.liquidAmount = info.liquidAmount;
			tile.liquidType = info.liquidType;
			
			
		}
		System.out.println("updating tiles");
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
//			double temperature = t.getTemperature();
//			WeatherEvent weather = new WeatherEvent(t, targetTile, t.getLocation().distanceTo(target)*WeatherEventType.RAIN.getSpeed() + (int)(Math.random()*50), 0.00002, LiquidType.WATER);;
//			t.setWeather(weather);
//			worldData.addWeatherEvent(weather);
		}
	}
	
	public void updateVolcano() {
	  if (ticks < eruptingUntil) {
	    this.get(volcano).liquidType = LiquidType.LAVA;
	    this.get(volcano).liquidAmount += 300;

	    int[] numProjectiles = new int[ProjectileType.values().length]; 
	    numProjectiles[ProjectileType.LAVA_BALL.ordinal()] = 2;
	    numProjectiles[ProjectileType.ROCK.ordinal()] = 10;
	    numProjectiles[ProjectileType.WIZARD_BALL.ordinal()] = 1;
	    int typeIndex = 0;
	    TileLoop: for(Tile tile : this.getTilesRandomly()) {
	      while(numProjectiles[typeIndex] == 0) {
	        typeIndex++;
	        if(typeIndex >= numProjectiles.length) {
	          break TileLoop;
	        }
	      }
	      Projectile meteor = new Projectile(ProjectileType.values()[typeIndex], this.get(volcano), tile, null, 100);
	      worldData.addProjectile(meteor);
	      --numProjectiles[typeIndex];
	    }
	  }
	}
	
	public void eruptVolcano() {
		System.out.println("eruption");
		eruptingUntil = ticks + (int)(Math.random()*100 + 50);
		spawnAnimal(Game.unitTypeMap.get("BALROG"), get(volcano), getFaction(NO_FACTION_ID), null);
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
			animal.queuePlannedAction(PlannedAction.moveTo(target));
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
		if(!animalType.isAquatic() && world.get(loc).liquidAmount > world.get(loc).liquidType.getMinimumDamageAmount()/2 ) {
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
			Tile target = null;
			int radius = (int) (Utils.getRandomNormal(2) * 30 + 5);
			System.out.println("meteor at: " + t.getLocation().x() + ", " + t.getLocation().y());
			while(target == null) {
				for(int i = 0; i < 20; i++) {
//					System.out.println(i);
					if(t.getLocation().x() + i < tiles.length) {
						target = tiles[t.getLocation().x() + i][t.getLocation().y()];
					}
				}
			}
			Projectile meteor = new Projectile(ProjectileType.METEOR, t, target, null, 10000, false, 200);
			worldData.addProjectile(meteor);
//			spawnExplosionCircle(t, radius, 5000);
//			int rockRadius = radius / 5;
//			spawnRock(t, rockRadius);
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
		// TODO use Utils function instead
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
				double Energy = damage*10 / (distanceFromCenter + 1);
				t.addEnergy(Energy/2);
				t.getAir().addEnergy(Energy/2);
				
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
//	public void spawnExplosion(Tile tile, int radius, int damage, DamageType type) {
//	
//		for(Tile t : getNeighborsInRadius(tile, radius)) {
//			
//			if(type == DamageType.HEAT) {
//				t.replaceOrAddDurationModifier(
//						GroundModifierType.FIRE, 
//						10 + (int)(Math.random()*damage/5),
//						worldData);
//			}
//			
////			GroundModifier fire = new GroundModifier(GroundModifierType.FIRE, t, 10 + (int)(Math.random()*damage/5));
////			worldData.addGroundModifier(fire);
////			t.setModifier(fire);
//			if(t.hasBuilding() == true) {
//				t.getBuilding().takeDamage(damage, type);
//			}
//			for(Unit unit : t.getUnits()) {
//				unit.takeDamage(damage, type);
//			}
//			if(t.getPlant() != null) {
//				t.getPlant().takeDamage(damage, type);
//			}
//		}
//
//	}
	
	
	// UNUSED
	public void updateDesertChange(Tile tile, boolean start) {
		int failTiles = 0;
		int numDesertNeighbors = 0;
		int numGrassNeighbor = 0;
		boolean grassNeighbor = false;
		
		//if it doesnt roll chance to change terrain, return
		if(Math.random() >= Constants.CHANCE_TO_SWITCH_TERRAIN && start == false) {
			return;
		}
		
		for(Tile t : tile.getNeighbors()) {
			//counts the tiles are too humid to be desert
			if(t.getAir().getHumidity() > Constants.DESERT_HUMIDITY) {
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
		if (tile.getAir().getHumidity() < terrain.getMinMax().x) {
			if (terrain == Terrain.GRASS) {
				tile.setTerrain(Terrain.DIRT);
				tile.setTickLastTerrainChange(World.ticks);
			}
			
			//start variable indicates if the function was called when map was made
			//when start == true, we allow desert to be generated anywhere possible
			if (start == true) {
				
				//if there is only 1 ineligible desert tile, the dirt turns to sand
				if (terrain == Terrain.DIRT && failTiles < 2) {
					tile.setTerrain(Terrain.SAND);
					tile.setTickLastTerrainChange(World.ticks);
				}
			//when start == false, we only allow desert to spread if nearby other desert tiles
			}else if (start == false) {
				if (terrain == Terrain.DIRT && failTiles < 2 && numDesertNeighbors >= 2 && numGrassNeighbor == 0) {
					tile.setTerrain(Terrain.SAND);
					tile.setTickLastTerrainChange(World.ticks);
				}
			}
		}
		if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount >= 0.05) {
			return;
		}
		//if the humidity is more than the max terrain humidity
		if (tile.getAir().getHumidity() > terrain.getMinMax().y ) {
			if (terrain == Terrain.DIRT && tile.canGrow() && numGrassNeighbor >= 3) {
				tile.setTerrain(Terrain.GRASS);
				tile.setTickLastTerrainChange(World.ticks);
				
			//if there are too many failed tiles to support desert
			} else if(terrain == Terrain.SAND && failTiles >= 2) {
				tile.setTerrain(Terrain.DIRT);
				tile.setTickLastTerrainChange(World.ticks);
			}
		}
		
		//if there arent enough desert neighbors
		if(terrain == Terrain.SAND && numDesertNeighbors < 2) {
			tile.setTerrain(Terrain.DIRT);
			tile.setTickLastTerrainChange(World.ticks);
		}
		
		//if there is a neighbor that is grass
		if(terrain == Terrain.SAND && grassNeighbor == true) {
			tile.setTerrain(Terrain.DIRT);
			tile.setTickLastTerrainChange(World.ticks);
		}
		
		
	}
	
	
	

	
	
	public void setTileMass() {
		for(Tile tile : getTiles()) {
			
			Air air = tile.getAir();
//			Air atmosphere = tile.getAtmosphere();
			double pressure = air.getPressure();
			double volume = Constants.VOLUMEPERTILE;
			double temperature = tile.getAir().getTemperature();
			
			double moles = (pressure*volume) / Constants.R * (temperature + Math.abs(Constants.MINTEMP));
//			air.setMass(moles);
//			air.setMass(STARTINGMASS);
			air.setMass(5);
//			atmosphere.setMass(STARTINGMASS/2);
			
		}
	}
	private void initializeTileEnergy() {
		double defaultEnergy = Constants.DEFAULTENERGY + 2500;
		double defaultAirEnergy = defaultEnergy / 3.1;
		for(Tile tile: getTilesRandomly()) {
			double energy = defaultEnergy;
			double airEnergy = defaultAirEnergy;
			
			Air air = tile.getAir();
			double altitudeMultiplier = Math.sqrt(Math.sqrt( Math.sqrt(Constants.SEALEVEL) / Math.sqrt(tile.getHeight()) ));
			if(altitudeMultiplier > 0.5 && altitudeMultiplier < 1) {
				energy *= (altitudeMultiplier);
				airEnergy *= Math.sqrt(altitudeMultiplier);
				
			}
//			double maxVol = tile.getAir().getMaxVolumeLiquid();
//			tile.getAir().setVolumeLiquid(maxVol/1.5 + Math.random());
//			tile.getAtmosphere().setVolumeLiquid(maxVol/5);
			tile.setEnergy(energy);
			
			air.setEnergy(airEnergy);
		}
	}
	
	public void initializeAirSimulationStuff() {
		setTileMass();
		initializeTileEnergy();
	}
	
	public void doAirSimulationStuff() {
		LinkedList<Tile> tiles = getTilesRandomly();
		AirSimulation.updateAirStuff(tiles);
		AirSimulation.updateEnergyToTemperature(tiles);
		AirSimulation.updateEnergy(tiles);
		AirSimulation.updateEnergyToTemperature(tiles);
		AirSimulation.blackBodyRadiation(tiles);
		AirSimulation.updateEnergyToTemperature(tiles);
		AirSimulation.updateAirMovement(tiles, width, height);
		AirSimulation.updateEnergyToTemperature(tiles);
	}
	
	/**
	 * Updates terrain types such as grass growing or dying
	 * also does desertification and plants taking damage
	 */
	public void updateTerrainChange(boolean start) {
		if(World.ticks % 50 == 0) {
			for(Building building: worldData.getBuildings()) {
				Tile t = building.getTile();
				Plant p = t.getPlant();
				if(p != null) {
					p.takeDamage((int)p.getHealth(), DamageType.PHYSICAL);
					worldData.addDeadThing(p);
				}
				if(building.getType().isHarvestable() == false) {
					continue;
				}
				
				if(t.getTerrain() == Terrain.GRASS || p != null) {
					t.setTerrain(Terrain.DIRT);
					
				}
			}
		}
		
		for(Tile tile : getTiles()) {
			if(tile.getResource() != null) {
				tile.getResource().tick(World.ticks);
			}
			
			
			if(World.ticks - tile.getTickLastTerrainChange() <= Constants.MIN_TIME_TO_SWITCH_TERRAIN) {
				continue;
			}
//			updateDesertChange(tile, start);
			Terrain terrain = tile.getTerrain();
			
			if(start == true && tile.getHeight() <= 300 && tile.canPlant()) {
				tile.setTerrain(Terrain.GRASS);
				tile.setTickLastTerrainChange(World.ticks);
			}
			
			
			
			
			// kills plants if its too hot or cold
			if(tile.canPlant() && (tile.getTemperature() >= Constants.LETHALHOTTEMP || tile.getTemperature() <= Constants.LETHALCOLDTEMP)) {
				if(Math.random() < Constants.CHANCE_TO_SWITCH_TERRAIN) {
					
					tile.setTerrain(Terrain.DIRT);
					tile.setTickLastTerrainChange(World.ticks);
					Plant plant = tile.getPlant();
					if(plant != null && Math.random() < Constants.CHANCE_TO_SWITCH_TERRAIN) {
						plant.takeDamage((int)plant.getHealth(), DamageType.HEAT);
						worldData.addDeadThing(plant);
					}
				}
			}
			
			
			//turns grass to dirt if tile has a cold liquid || the temperature is cold
//			if(terrain == Terrain.GRASS && tile.canGrow() == false) {
//				if(Math.random() < Constants.CHANCE_TO_SWITCH_TERRAIN) {
//					tile.setTerrain(Terrain.DIRT);
//					tile.setTickLastTerrainChange(World.ticks);
//				}
//			}
			
			if(terrain == Terrain.GRASS && tile.canGrow() == false && Math.random() < Constants.CHANCE_TO_SWITCH_TERRAIN/10000) {
				tile.setTerrain(Terrain.DIRT);
				tile.setTickLastTerrainChange(World.ticks);
			}
			
//			
//			if(tile.liquidType == LiquidType.LAVA &&(tile.getTerrain() == Terrain.GRASS || tile.getTerrain() == Terrain.DIRT) && tile.liquidAmount >= 0.05) {
//				if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
////					tile.setTerrain(Terrain.SAND);
//				}
//			}
			if(tile.checkTerrain(Terrain.DIRT)) {
//				boolean adjacentGrass = false;
//				for(Tile neighbor : Utils.getNeighbors(tile, this)) {
//					if(neighbor.getTerrain() == Terrain.GRASS) {
//						adjacentGrass = true;
//					}
//					
//				}
				double threshold = 0;
				
				if(tile.liquidType == LiquidType.WATER) {
					threshold = Constants.CHANCE_TO_SWITCH_TERRAIN/1000;
				}
//				if(adjacentGrass) {
//					threshold += 0.0001;
//				}
				
				if(tile.canGrow() == true) {
					if(Math.random() < threshold) {
						tile.setTerrain(Terrain.GRASS);
						tile.setTickLastTerrainChange(World.ticks);
					}
				}
				
				
				
			}
		}
		
	}
	
	private void spreadPlants() {
		
		if(worldData.getPlants().size() >= 4000) {
			return;
		}
		for(Plant plant : worldData.getPlants()) {
			
			if(plant.getTile().canGrow() == false) {
				continue;
			}
			if(plant.getType() == Game.plantTypeMap.get("TREE")) {
				if(Math.random() < 0.05) {
					for(Tile tile : plant.getTile().getNeighbors()) {
						if(tile.getPlant() == null && tile.canGrow()) {
							tile.setHasPlant(new Plant(plant.getType(), tile, getFaction(NO_FACTION_ID)));
							worldData.addPlant(tile.getPlant());
							break;
						}
					}
				}
			}
			if(plant.getType() == Game.plantTypeMap.get("BERRY")) {
				if(Math.random() < 0.01) {
					for(Tile tile : plant.getTile().getNeighbors()) {
						if(tile.getPlant() == null && tile.canGrow()) {
							tile.setHasPlant(new Plant(plant.getType(), tile, getFaction(NO_FACTION_ID)));
							worldData.addPlant(tile.getPlant());
							break;
						}
					}
				}
			}
			
		}
		
		
	}
	
	public void grow() {
		spreadPlants();
		
		for(Tile tile : getTilesRandomly()) {
			if(tile.getPlant() != null) {
				continue;
			}
			
			if(tile.getTerrain() == Terrain.SAND) {
				if(Math.random() < 0.01) {
					Plant plant = new Plant(Game.plantTypeMap.get("CACTUS"), tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(plant);
					worldData.addPlant(plant);
				}
			}
			
			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(tile.isCold() == false && Math.random() < 0.01) {
					Plant plant = new Plant(Game.plantTypeMap.get("CATTAIL"), tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(plant);
					worldData.addPlant(plant);
				}
			}
			if(tile.liquidType != null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
				if (tile.canGrow() && Math.random() < 0.001) {
					tile.setHasPlant(new Plant(Game.plantTypeMap.get("BERRY"), tile, getFaction(NO_FACTION_ID)));
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
//			tile.liquidAmount += 0.1;
//			weather.addStrength(-0.1);
//			tile.getAir().addVolume(-0.1);
			
			
		}
	}
	
	public void doProjectileUpdates(boolean simulated) {

		for(Projectile projectile : worldData.getProjectiles()) {
			projectile.tick();
			if(projectile.getTargetTile() == null) {
				continue;
			}
//			if(projectile.getType() == ProjectileType.METEOR_WAVE) {
//				double modifier = 1 - (projectile.getTile().getTemperature()/MAXTEMP);
//				projectile.getTile().addEnergy(100 * modifier);
//			}
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
//					if(projectile.getType().getRadius() <= 2) {
//						spawnExplosion(projectile.getTile(), projectile.getType().getRadius(), projectile.getDamage(), DamageType.HEAT);
//					}else {
						spawnExplosionCircle(projectile.getTile(), projectile.getType().getRadius(), projectile.getDamage());
//					}
					
				} 
				else {
					for(Unit unit : projectile.getTile().getUnits()) {
						unit.takeDamage(projectile.getDamage(), DamageType.PHYSICAL);
						unit.aggro(projectile.getSource());
					}
					if(projectile.getTile().getPlant() != null) {
						projectile.getTile().getPlant().takeDamage(projectile.getDamage(), DamageType.PHYSICAL);
					}
					if(projectile.getTile().hasBuilding() == true) {
						projectile.getTile().getBuilding().takeDamage(projectile.getDamage(), DamageType.PHYSICAL);
					}
				}
				if(projectile.getType() == ProjectileType.METEOR) {
					// meteor leaves a lil dent
					projectile.getTile().setHeight(projectile.getTile().getHeight() - 10);
					spawnAnimal(Game.unitTypeMap.get("INFERNAL"), projectile.getTile(), getFaction(World.NO_FACTION_ID), null);
				}
				if(projectile.getType() == ProjectileType.LAVA_BALL) {
					projectile.getTile().liquidType = LiquidType.LAVA;
					projectile.getTile().liquidAmount += 100;
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
//			System.out.println("Tick " + World.ticks + ", " + worldData.toString());
		}
	}
	
	public void updatePlantDamage() {
		for(Plant plant : worldData.getPlants()) {
			int[] damage = plant.getTile().computeTileDamage();
			for(int i = 0; i < damage.length; i++) {
				plant.takeDamage(damage[i], DamageType.values()[i]);
			}
			
//			Tile tile = plant.getTile();
//			if(tile.isCold()) {
//				plant.takeDamage(1);
//			}
//			if (plant.isAquatic() && tile.liquidType == LiquidType.WATER) {
//				if (tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
//
//					double difInLiquids = tile.liquidType.getMinimumDamageAmount() - tile.liquidAmount;
//					double damageTaken = difInLiquids * tile.liquidType.getDamage();
//					int roundedDamage = (int) (damageTaken + 1);
//					if (roundedDamage >= 1) {
//						plant.takeDamage(roundedDamage);
//					}
//				}
//			} else {
//				int totalDamage = 0;
//				double modifierDamage = 0;
//				
//				//adds the damage of groundmodifier
//				if(tile.getModifier() != null) {
//					modifierDamage += tile.getModifier().getType().getDamage();
//				}
//				
//				
//				if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
//					if(plant.isAquatic() == false || tile.liquidType != LiquidType.WATER) {
//						//adds damage of liquids
//						double liquidDamage = tile.liquidAmount * tile.liquidType.getDamage();
//						totalDamage += liquidDamage;
//					}
//				}
//				if(plant.isAquatic() && tile.liquidType != LiquidType.WATER) {
//					totalDamage += 5;
//				}
//				if(tile.getTerrain().isPlantable(tile.getTerrain()) == false && plant.getType().isDesertResistant() == false) {
//					totalDamage += 5;
//				}
//				
//				if(tile.getTerrain() == Terrain.GRASS && plant.getType().isDesertResistant() == true) {
//					totalDamage += 5;
//				}
//				totalDamage = (int) (modifierDamage+totalDamage);
//				if(totalDamage >= 1) {
//					plant.takeDamage(totalDamage);
//				}
//			}
			
		}	
	}

	public void genPlants() {
		for(Tile tile : getTiles()) {
			//generates cactus
			if(tile.getTerrain() == Terrain.SAND) {
				if(Math.random() < 0.01) {
					Plant plant = new Plant(Game.plantTypeMap.get("CACTUS"), tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(plant);
					worldData.addPlant(plant);
				}
			}
			//generates land plants
			if(tile.checkTerrain(Terrain.GRASS) && tile.getRoad() == null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount() / 2 && Math.random() < Constants.BUSH_RARITY) {
				double o = Math.random();
				if(o < Game.plantTypeMap.get("BERRY").getRarity()) {
					makePlantVein(tile, Game.plantTypeMap.get("BERRY"), 6);
//					Plant p = new Plant(PlantType.BERRY, tile, getFaction(NO_FACTION_ID));
//					tile.setHasPlant(p);
//					worldData.addPlant(tile.getPlant());
				}
			}
			//tile.liquidType.WATER &&
			//generates water plants
			if( Math.random() < Constants.WATER_PLANT_RARITY) {
				double o = Math.random();
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()  && o < Game.plantTypeMap.get("CATTAIL").getRarity()) {
					Plant p = new Plant(Game.plantTypeMap.get("CATTAIL"), tile, getFaction(NO_FACTION_ID));
					tile.setHasPlant(p);
					worldData.addPlant(tile.getPlant());
				}
			}
		}
	}
	public void makePlantVein(Tile t, PlantType type, int veinSize) {
		HashMap<Tile, Double> visited = new HashMap<>();

		PriorityQueue<Tile> search = new PriorityQueue<>((x, y) -> {
			double distancex = visited.get(x);
			double distancey = visited.get(y);
			if (distancey < distancex) {
				return 1;
			} else if (distancey > distancex) {
				return -1;
			} else {
				return 0;
			}
		});
		visited.put(t, 0.0);
		search.add(t);

		while (veinSize > 0 && !search.isEmpty()) {
			Tile potential = search.poll();

			for (Tile ti : potential.getNeighbors()) {
				if (visited.containsKey(ti)) {
					continue;
				}
				visited.put(ti, ti.getLocation().distanceTo(t.getLocation()) + Math.random() * 10);
				search.add(ti);
			}
			if(type == Game.plantTypeMap.get("TREE")) {
				if ((potential.canPlant() || type.isDesertResistant()) && potential.getPlant() == null && potential.getTerrain() != Terrain.DIRT) {
					Plant plant = new Plant(type, potential, getFaction(NO_FACTION_ID));
					potential.setHasPlant(plant);
					worldData.addPlant(plant);
					veinSize--;
				}
			}else
			// if plant can live on the tile
			if ((potential.canPlant() || type.isDesertResistant()) && potential.getPlant() == null) {
				
				Plant plant = new Plant(type, potential, getFaction(NO_FACTION_ID));
				potential.setHasPlant(plant);
				worldData.addPlant(plant);
				veinSize--;
			}
		}

	}

	public void makeForest() {
		
		for(Tile t : tileListRandom) {
			double tempDensity = Constants.FOREST_DENSITY;
			if(t.getTerrain() == Terrain.DIRT) {
				tempDensity /= 2;
			}
			if (t.canPlant() && t.getRoad() == null && t.liquidAmount < t.liquidType.getMinimumDamageAmount() / 2)
				if (Math.random() < tempDensity) {
					makePlantVein(t, Game.plantTypeMap.get("TREE"), 30);
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
	
	public void reseedTerrain(long seed) {
		for(Tile tile : getTiles()) {
			tile.setTerrain(Terrain.DIRT);
		}
		
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		float[][] heightMap = Generation.generateHeightMap(seed, smoothingRadius, width, height);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		volcano = Generation.makeVolcano(this, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		Generation.addCliff(this, heightMap);

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
		double rockCutoff = tiles.get((int)((1-rockpercentage)*tiles.size())).getHeight();
		double dirtCutoff = tiles.get((int)((1-.5)*tiles.size())).getHeight();
		for(Tile tile : getTiles()) {
			if(tile.getTerrain() != Terrain.DIRT) {
				continue;
			}
			Terrain t;
			if (tile.getHeight() > rockCutoff) {
				t = Terrain.ROCK;
			}
			else if (tile.getHeight() > dirtCutoff) {
				t = Terrain.DIRT;
			}
			else {
				t = Terrain.GRASS;
			}
			tile.setTerrain(t);
		}
	}
	
	public void generateWorld() {
		reseedTerrain(PerlinNoise.DEFAULT_SEED);

		int numTiles = width*height;
		Generation.makeLake(numTiles * 1, this);
		Generation.makeLake(numTiles * 2, this);
		Generation.makeLake(numTiles * 4, this);
		Generation.makeLake(numTiles * 8, this);
		System.out.println("Simulating water for 100 iterations");
		for(int i = 0; i < 100; i++) {
			LiquidSimulation.propogate(this);
		}
		initializeAirSimulationStuff();
		doAirSimulationStuff();
		Tile t = getTilesRandomly().peek();
		Generation.makeBiome(t, Terrain.SAND, 1000, 0, new Terrain[]{Terrain.GRASS, Terrain.DIRT});
		updateTerrainChange(true);
		Generation.generateResources(this);
		this.genPlants();
		this.makeForest();
		if(!Settings.DISABLE_WILDLIFE_SPAWNS) {
			Generation.generateWildLife(this);
		}
		System.out.println("Finished generating " + width + "x" + height + " world with " + tileList.size() + " tiles.");
	}
	
	public BufferedImage[] createTerrainImage(Faction faction) {
		BufferedImage[] mapImages = new BufferedImage[MapMode.values().length];
		mapImages[MapMode.LIGHT.ordinal()] = computeTileBrightness(faction);
		double brighnessModifier = getDaylight();
		
		HashMap<Terrain, Color> terrainColors = Utils.computeTerrainAverageColor();
		BufferedImage terrainImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage minimapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage bigTerrainImage = new BufferedImage(terrainImage.getWidth()*2, terrainImage.getHeight()*2+1, terrainImage.getType());
		BufferedImage bigLightImage = new BufferedImage(terrainImage.getWidth()*2, terrainImage.getHeight()*2+1, terrainImage.getType());

		for(Tile tile : this.getTiles()) {
			Color minimapColor = terrainColors.get(tile.getTerrain());
			Color terrainColor = terrainColors.get(tile.getTerrain());
			if(tile.getResource() != null && faction.areRequirementsMet(tile.getResource().getType())) {
				terrainColor = tile.getResource().getType().getMipMap().getColor(0);
				minimapColor = tile.getResource().getType().getMipMap().getColor(0);
			}
			if(tile.getRoad() != null) {
				terrainColor = tile.getRoad().getType().getMipMap().getColor(0);
				minimapColor = tile.getRoad().getType().getMipMap().getColor(0);
			}
			if(tile.liquidAmount > 0) {
				double alpha = Utils.getAlphaOfLiquid(tile.liquidAmount);
				minimapColor = Utils.blendColors(tile.liquidType.getMipMap().getColor(0), minimapColor, alpha);
				terrainColor = Utils.blendColors(tile.liquidType.getMipMap().getColor(0), terrainColor, alpha);
			}
			if(tile.getPlant() != null) {
				terrainColor = tile.getPlant().getMipMap().getColor(0);
				minimapColor = tile.getPlant().getMipMap().getColor(0);
			}
			if(tile.hasBuilding()) {
				terrainColor = tile.getBuilding().getMipMap().getColor(0);
				minimapColor = tile.getBuilding().getMipMap().getColor(0);
			}
			GroundModifier modifier = tile.getModifier(); 
			if(modifier != null) {
				minimapColor = modifier.getType().getMipMap().getColor(0);
				terrainColor = Utils.blendColors(modifier.getType().getMipMap().getColor(0), terrainColor, 0.9);
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
			
			int oddColumn = tile.getLocation().x() % 2;
			bigTerrainImage.setRGB(tile.getLocation().x()*2, tile.getLocation().y()*2 + oddColumn, terrainColor.getRGB());
			bigTerrainImage.setRGB(tile.getLocation().x()*2+1, tile.getLocation().y()*2 + oddColumn, terrainColor.getRGB());
			bigTerrainImage.setRGB(tile.getLocation().x()*2+1, tile.getLocation().y()*2+1 + oddColumn, terrainColor.getRGB());
			bigTerrainImage.setRGB(tile.getLocation().x()*2, tile.getLocation().y()*2+1 + oddColumn, terrainColor.getRGB());
			
			int alpha = (int) (255 * (1 - (brighnessModifier + tilebrightness)));
			alpha = (alpha > 255) ? 255 : ((alpha < 0) ? 0 : alpha);
			int nightColor = new Color(0, 0, 0, alpha).getRGB();
			bigLightImage.setRGB(tile.getLocation().x()*2, tile.getLocation().y()*2 + oddColumn, nightColor);
			bigLightImage.setRGB(tile.getLocation().x()*2+1, tile.getLocation().y()*2 + oddColumn, nightColor);
			bigLightImage.setRGB(tile.getLocation().x()*2+1, tile.getLocation().y()*2+1 + oddColumn, nightColor);
			bigLightImage.setRGB(tile.getLocation().x()*2, tile.getLocation().y()*2+1 + oddColumn, nightColor);
		}
		for (int x = 0; x < bigLightImage.getWidth(); x++) {
			bigLightImage.setRGB(x, 0, bigLightImage.getRGB(x, 1));
			bigLightImage.setRGB(x, bigLightImage.getHeight()-1, 
					bigLightImage.getRGB(x, bigLightImage.getHeight() - 2));
		}
		for(AttackedNotification notification : faction.getAttackedNotifications()) {
			minimapImage.setRGB(notification.tile.getLocation().x(), notification.tile.getLocation().y(), Color.red.getRGB());
			terrainImage.setRGB(notification.tile.getLocation().x(), notification.tile.getLocation().y(), Color.red.getRGB());
		}
		
		double highHeight = Double.MIN_VALUE;
		double lowHeight = Double.MAX_VALUE;
		double highPressure = Double.MIN_VALUE;
		double lowPressure = Double.MAX_VALUE;
		double highTemperature = Constants.MINTEMP;
		double lowTemperature = Constants.MAXTEMP;
		double highHumidity = Double.MIN_VALUE;
		double lowHumidity = Double.MAX_VALUE;
		for(Tile tile : getTiles() ) {
			highHeight = Math.max(highHeight, tile.getHeight());
			lowHeight = Math.min(lowHeight, tile.getHeight());
			highPressure = Math.max(highPressure, tile.getAir().getPressure());
			lowPressure = Math.min(lowPressure, tile.getAir().getPressure());
			highTemperature = Math.max(highTemperature, tile.getAir().getTemperature());
			lowTemperature = Math.min(lowTemperature, tile.getAir().getTemperature());
			highHumidity = Math.max(highHumidity, tile.getAir().getHumidity());
			lowHumidity = Math.min(lowHumidity, tile.getAir().getHumidity());
		}
		
		mapImages[MapMode.TERRAIN_BIG.ordinal()] = bigTerrainImage;
		mapImages[MapMode.TERRAIN.ordinal()] = terrainImage;
		mapImages[MapMode.FLOW2.ordinal()] = terrainImage;
		mapImages[MapMode.MINIMAP.ordinal()] = minimapImage;
		mapImages[MapMode.LIGHT_BIG.ordinal()] = bigLightImage;
		for(MapMode mode : MapMode.HEATMAP_MODES) {
			BufferedImage image = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_4BYTE_ABGR);
			for(Tile tile : getTiles() ) {
				float ratio = 0;
				if(mode == MapMode.HEIGHT) {
					ratio = (float) ((tile.getHeight() - lowHeight) / (highHeight - lowHeight));
				}
				else if(mode == MapMode.PRESSURE) {
					ratio = (float) ((tile.getAir().getPressure() - lowPressure) / (highPressure - lowPressure));
				}
				else if(mode == MapMode.TEMPURATURE) {
					ratio = (float) ((tile.getAir().getTemperature() - lowTemperature) / (highTemperature - lowTemperature));
				}
				else if(mode == MapMode.HUMIDITY) {
					ratio = (float) ((tile.getAir().getHumidity() - lowHumidity) / (highHumidity - lowHumidity));
				}
				ratio = Math.max(Math.min(ratio, 1), 0);
				Color c = new Color(ratio, 0, 1-ratio);
				image.setRGB(tile.getLocation().x(), tile.getLocation().y(), c.getRGB());
			}
			mapImages[mode.ordinal()] = image;
		}
		return mapImages;
	}
	
	/**
	 * computes all tile brightnesses and creates brightness image
	 */
	private BufferedImage computeTileBrightness(Faction faction) {
		int w = getWidth();
		int h = getHeight();
		BufferedImage rawImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		for(Tile tile : getTiles() ) {
			double brightness = 0;
			if (tile.getThingOfFaction(faction) != null
					|| tile.isInVision()) {
				brightness += 1;
			}
			else if (tile.getFaction() == faction) {
				brightness += 0.4;
			}
			brightness = Math.max(brightness, tile.getTerrain().getBrightness());
			brightness = Math.max(brightness, tile.liquidAmount * tile.liquidType.getBrightness());
			if (tile.getModifier() != null) {
				brightness = Math.max(brightness, tile.getModifier().getType().getBrightness());
			}
			byte brightnessByte = (byte) Math.min(255, brightness*255);
			int rgb = (brightnessByte << 24) |  (brightnessByte << 16) | (brightnessByte << 8) | brightnessByte;
			rawImage.setRGB(tile.getLocation().x(), tile.getLocation().y(), rgb);
		}
		rawImage = ImageCreation.convertToHexagonal(rawImage);
		
		int r = 27;
		BufferedImage rawImagePlusEdges = new BufferedImage(rawImage.getWidth() + r*2, rawImage.getHeight() + r*2, rawImage.getType());
		Graphics g = rawImagePlusEdges.getGraphics();
		for(int i = 0; i < r; i++) {
			g.drawImage(rawImage, i, i, null);
			g.drawImage(rawImage, r*2 - i, i, null);
			g.drawImage(rawImage, r*2 - i, r*2 - i, null);
			g.drawImage(rawImage, i, r*2 - i, null);
		}
		g.drawImage(rawImage, r, r, null);
		g.dispose();
		float[] kernelData = new float[r*r];
		for(int y = 0; y < r; y++) {
			for(int x = 0; x < r; x++) {
				int dist = (x - r/2)*(x - r/2) + (y - r/2)*(y - r/2);
				if(dist == 0) {
					kernelData[x + y*r] = 1;
				}
				else if(dist <= r*r/4){
					kernelData[x + y*r] = (float) (1f / dist);
				}
			}
		}
		Kernel kernel = new Kernel(r, r, kernelData);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
		BufferedImage blurred = op.filter(rawImagePlusEdges, null);
		blurred = blurred.getSubimage(r, r, rawImage.getWidth(), rawImage.getHeight());
		blurred = ImageCreation.convertFromHexagonal(blurred);
		for(Tile tile : getTiles()) {
			int brightness = blurred.getRGB(tile.getLocation().x(), tile.getLocation().y()) & 0xFF;
			tile.setBrightness(brightness / 255.0);
		}
		return blurred;
	}

	public int ticksUntilDay() {
		int currentDayOffset = World.ticks%(Constants.DAY_DURATION + Constants.NIGHT_DURATION);
		int skipAmount = (Constants.DAY_DURATION + Constants.NIGHT_DURATION - Constants.TRANSITION_PERIOD) - currentDayOffset;
		if(skipAmount < 0) {
			skipAmount += Constants.DAY_DURATION + Constants.NIGHT_DURATION;
		}
		return skipAmount;
	}
	
	public static boolean isNightTime() {
		return getDaylight() < 0.4;
	}
	public static int getCurrentDayOffset() {
		return (World.ticks + Constants.TRANSITION_PERIOD) % (Constants.DAY_DURATION + Constants.NIGHT_DURATION);
	}

	private static double precomputedDaylight;
	private static int precomputedDaylightTick = -1;
	
	public static double getDaylight() {
		if(Game.DISABLE_NIGHT) {
			return 1;
		}
		if(World.ticks != precomputedDaylightTick) {
			recomputeDaylight();
		}
		return precomputedDaylight;
	}
	private static void recomputeDaylight() {
		double ratio = 1;
		int currentDayOffset = getCurrentDayOffset();
		if(currentDayOffset < Constants.TRANSITION_PERIOD) {
			ratio = 0.5 + 0.5 * currentDayOffset / Constants.TRANSITION_PERIOD;
		}
		else if(currentDayOffset < Constants.DAY_DURATION - Constants.TRANSITION_PERIOD) {
			ratio = 1;
		}
		else if(currentDayOffset < Constants.DAY_DURATION + Constants.TRANSITION_PERIOD) {
			ratio = 0.5 - 0.5 * (currentDayOffset - Constants.DAY_DURATION) / Constants.TRANSITION_PERIOD;
		}
		else if(currentDayOffset < Constants.DAY_DURATION + Constants.NIGHT_DURATION - Constants.TRANSITION_PERIOD) {
			ratio = 0;
		}
		else {
			ratio = 0.5 - 0.5 * (Constants.DAY_DURATION + Constants.NIGHT_DURATION - currentDayOffset) / Constants.TRANSITION_PERIOD;
		}
		precomputedDaylight = ratio;
		precomputedDaylightTick = World.ticks;
	}
	
}
