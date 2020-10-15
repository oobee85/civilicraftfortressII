package world;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import wildlife.*;

public class World {

	public static final int TICKS_PER_ENVIRONMENTAL_DAMAGE = 5;
	public static final double TERRAIN_SNOW_LEVEL = 1;
//	public static final double DESERT_HUMIDITY = 0.001;
	public static final double DESERT_HUMIDITY = 2;
	public static final int DAY_DURATION = 500;
	public static final int NIGHT_DURATION = 350;
	public static final int TRANSITION_PERIOD = 100;
	private static final double CHANCE_TO_SWITCH_TERRAIN = 0.05;
	private LinkedList<Tile> tileList;
	private LinkedList<Tile> tileListRandom;
	
	private static final int NUM_LIQUID_SIMULATION_PHASES = 9;
	private ArrayList<ArrayList<Tile>> liquidSimulationPhases = new ArrayList<>(NUM_LIQUID_SIMULATION_PHASES);
	private Tile[][] tiles;
	public ConcurrentLinkedQueue<Tile> territory = new ConcurrentLinkedQueue<Tile>();;
	
	private int width;
	private int height;

	public LinkedList<Plant> plants = new LinkedList<Plant>();
	public LinkedList<Plant> newPlants = new LinkedList<Plant>();
	public LinkedList<Unit> units = new LinkedList<Unit>();
	public LinkedList<Unit> newUnits = new LinkedList<Unit>();
	public LinkedList<Building> buildings = new LinkedList<Building>();
	public LinkedList<Building> plannedBuildings = new LinkedList<Building>();
	public LinkedList<Projectile> projectiles = new LinkedList<Projectile>();
	public LinkedList<GroundModifier> groundModifiers = new LinkedList<>();
	private LinkedList<GroundModifier> newGroundModifiers = new LinkedList<>();
	
	public HashSet<Unit> unitsInTerritory = new HashSet<Unit>();
	
	
	
	
	
	private double bushRarity = 0.005;
	private double waterPlantRarity = 0.05;
	private double forestDensity = 0.3;

	public TileLoc volcano;
	
	public World() {
		tileList = new LinkedList<>();
		tileListRandom = new LinkedList<>();
	}
	public int getTerritorySize() {
		return territory.size();
	}
	public void addToTerritory(Tile tile) {
		territory.add(tile);
	}
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
		if(loc.x < 0 || loc.x >= tiles.length || loc.y < 0 || loc.y >= tiles[0].length) {
			return null;
		}
		return tiles[loc.x][loc.y];
	}

	public LinkedList<Unit> getHostileUnitsInTerritory(){
		
		return unitsInTerritory.stream()
				.filter(e -> e.getType().isHostile() && e.isPlayerControlled() == false).collect(Collectors.toCollection(LinkedList::new));
		
	}
	public void drought() {
		for(Tile tile : getTiles()) {
			tile.liquidAmount = 0;
		}
	}
	
	public void rain() {
		
		Tile rainTile = this.getTilesRandomly().peek();
		int radius = (int) (Math.random()*20 + 10);
		
		List<Tile> rainTiles = Utils.getTilesInRadius(rainTile, this, radius);
		
		for(Tile t : rainTiles) {
			if(t.liquidType == LiquidType.LAVA) {
				continue;
			}
			double temperature = t.getTempurature();
			if(temperature < Season.FREEZING_TEMPURATURE) {
				t.liquidType = LiquidType.SNOW;
				t.liquidAmount += 0.01;
			}
			else {
				t.liquidType = LiquidType.WATER;
				t.liquidAmount += 0.005;
			}
		}
		System.out.println("Raining at " + rainTile + "with radius " + radius);
	}
	
	public void eruptVolcano() {
		System.out.println("eruption");
		this.get(volcano).liquidAmount += 500;
		
//		world[volcano].liquidType = LiquidType.WATER;
//		world[volcano].liquidAmount += 200;
	}
	
	public void addUnitsInTerritory() {
		HashSet<Unit> unitsInTerritoryNew = new HashSet<Unit>();
		
		for(Tile tile : territory) {
			if(tile.getUnits() != null) {
				unitsInTerritoryNew.addAll(tile.getUnits());
			}
			
		}
		unitsInTerritory = unitsInTerritoryNew;
//		System.out.println("Units in territory"+ unitsInTerritory.size());
	}
	
	public void spawnOgre() {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> e.getTerrain() == Terrain.ROCK ).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(UnitType.OGRE, tile.get());
		}
	}
	
	public void spawnWerewolf() {
		List<Unit> wolves = units
				.stream()
				.filter(e -> e.getType() == UnitType.WOLF)
				.collect(Collectors.toList());
		if(wolves.size() == 0) {
			return;
		}
		Unit wolf = wolves.get((int)(Math.random()*wolves.size()));
		Tile t = wolf.getTile();
		
		wolf.takeDamage(wolf.getHealth() + 1);
		System.out.println("Werewolf at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		spawnAnimal(UnitType.WEREWOLF, t);
	}
	
	public void spawnLavaGolem() {
		Optional<Tile> tile = getTilesRandomly()
				.stream()
				.filter(e -> e.getTerrain() == Terrain.VOLCANO )
				.findFirst();
		if(tile.isPresent()) {
			spawnAnimal(UnitType.LAVAGOLEM, tile.get());
		}
	}
	
	public void spawnEnt() {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> e.getTerrain() == Terrain.GRASS ).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(UnitType.ENT, tile.get());
		}
	}
	public void spawnIceGiant() {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> e.getModifier() != null && e.liquidType == LiquidType.SNOW).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(UnitType.ICE_GIANT, tile.get());
		}
	}
	public void spawnDragon() {
		Optional<Tile> tile = getTilesRandomly().stream().filter(e -> e.getTerrain() == Terrain.VOLCANO ).findFirst();
		if(tile.isPresent()) {
			spawnAnimal(UnitType.DRAGON, tile.get());
		}
	}
	
	public void spawnSkeletonArmy() {
		Optional<Tile> potential = getTilesRandomly().stream().filter(e -> e.getTerrain() == Terrain.ROCK ).findFirst();
		if(potential.isPresent()) {
			Tile t = potential.get();
			for(Tile tile : t.getNeighbors()) {
				spawnAnimal(UnitType.SKELETON, tile);
			}
		}
	}
	public void spawnAnimal(UnitType type, Tile tile) {
		Animal animal = makeAnimal(type, tile);
		tile.addUnit(animal);
		newUnits.add(animal);
	}

	public Animal makeAnimal(UnitType type, Tile tile) {
		if(type == UnitType.FLAMELET) {
			return new Flamelet(tile, false);
		}
		else if(type == UnitType.OGRE) {
			return new Ogre(tile, false);
		}
		else if(type == UnitType.PARASITE) {
			return new Parasite(tile, false, this.get(volcano));
		}
		else if(type == UnitType.WEREWOLF) {
			return new Werewolf(tile, false);
		}
		else if(type == UnitType.WATER_SPIRIT) {
			return new WaterSpirit(tile, false);
		}
		else if(type == UnitType.LAVAGOLEM) {
			return new LavaGolem(tile, false);
		}
		else if(type == UnitType.ENT) {
			return new Ent(tile, false);
		}
		else if(type == UnitType.DRAGON) {
			return new Dragon(tile, false);
		}
		else if(type == UnitType.ICE_GIANT) {
			return new IceGiant(tile, false);
		}
		else {
			return new Animal(type, tile, false);
		}
	}
	
	
	public void makeAnimal(UnitType animalType, World world, TileLoc loc) {
		if(animalType.isAquatic() == false && world.get(loc).liquidAmount > world.get(loc).liquidType.getMinimumDamageAmount()/2 ) {
			return;
		}
		Animal animal = new Animal(animalType, world.get(loc), false);
		animal.setTile(world.get(loc));
		newUnits.add(animal);
		world.get(loc).addUnit(animal);
		
	}
	
	
	
	public void meteorStrike() {
		
		Tile t = this.getTilesRandomly().getFirst();
		
		int radius = (int) (Math.random()*20 + 5);
		System.out.println("meteor at: "+t.getLocation().x+ ", "+ t.getLocation().y);
		
		spawnExplosionCircle(t, radius, 10000);
		
		
		
	}
	public HashSet<Tile> getNeighborsInRadius(Tile tile, int radius) {
		HashSet<Tile> neighbors = new HashSet<>();
		TileLoc tileLoc = tile.getLocation();
		int x = tileLoc.x;
		int y = tileLoc.y;
		for(int i = Math.max(x - radius, 0) ; i <= x + radius && i < tiles.length ; i ++) {
			for(int j = Math.max(y - radius, 0) ; j <= y + radius && j < tiles[i].length ; j ++) {
				if(Math.abs(i - x) + Math.abs(j - y) >= radius) {
					continue;
				}
				neighbors.add(tiles[i][j]);
			}
		}
		return neighbors;

	}
	public void spawnExplosionCircle(Tile tile, int radius, int damage) {

		for(Tile t : this.getTiles()) {
			int i =  t.getLocation().x;
			int j =  t.getLocation().y;
			int dx = i - tile.getLocation().x;
			int dy = j - tile.getLocation().y;
			double distanceFromCenter = Math.sqrt(dx*dx + dy*dy);
				
				if(distanceFromCenter < radius) {
					if(t.getTerrain() != Terrain.ROCK && t.getTerrain() != Terrain.VOLCANO) {
//						tile.setTerrain(Terrain.BURNED_GROUND);
					}
					GroundModifier fire = new GroundModifier(GroundModifierType.FIRE, t, 10 + (int)(Math.random()*damage/5));
					addGroundModifier(fire);
					t.setModifier(fire);
					if(t.getHasBuilding() == true) {
						t.getBuilding().takeDamage(damage);
					}
					for(Unit unit : t.getUnits()) {
						unit.takeDamage(damage);
					}
					if(t.getPlant() != null) {
						t.getPlant().takeDamage(damage);
					}
					t.liquidAmount = 0;
				}
		}
	}
	public void spawnExplosion(Tile tile, int radius, int damage) {
	
		for(Tile t : getNeighborsInRadius(tile, radius)) {
			GroundModifier fire = new GroundModifier(GroundModifierType.FIRE, t, 10 + (int)(Math.random()*damage/5));
			addGroundModifier(fire);
			t.setModifier(fire);
			if(t.getHasBuilding() == true) {
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
	public void updateTerrainChange(World world) {
		for(Tile tile : getTiles()) {
			tile.updateHumidity(Game.ticks);
			
			//spreads desert tiles
			if(tile.getTerrain().isPlantable(tile.getTerrain()) && tile.getHumidity() <= DESERT_HUMIDITY) {
				int failTiles = 0;
				
				for(Tile t : tile.getNeighbors()) {
					//count how many neighbors is a failed tile
					if(t.getHumidity() > DESERT_HUMIDITY + 0.01 || t.getTerrain() == Terrain.GRASS) {
						failTiles ++;
					}
				}
				//if one neighbor fails, make the tile dirt
				if(failTiles == 1) {
					if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
						tile.setTerrain(Terrain.DIRT);
					}
				//if all neighbors are eligible for desert, convert tile to desert
				}else if(failTiles == 0){
					if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
						tile.setTerrain(Terrain.SAND);
					}
					
				}
			//turns the tile back into dirt if its above desert humidity
			}else if(tile.getTerrain() == Terrain.SAND && tile.getHumidity() > DESERT_HUMIDITY) {
				if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
					tile.setTerrain(Terrain.DIRT);
				}
			}
			//turns grass into dirt if the tile has a cold liquid
			if(tile.checkTerrain(Terrain.GRASS) && (tile.liquidType == LiquidType.SNOW || tile.liquidType == LiquidType.ICE) && tile.liquidAmount * tile.liquidType.getDamage() > 1) {
				if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
					tile.setTerrain(Terrain.DIRT);
				}
				
			}
			//turns tile into dirt if its very cold
			if(tile.checkTerrain(Terrain.GRASS) && tile.getTempurature() < Season.FREEZING_TEMPURATURE * 0.75) {
				if(Math.random() < CHANCE_TO_SWITCH_TERRAIN) {
					tile.setTerrain(Terrain.DIRT);
				}
				
			}
			
			if(tile.checkTerrain(Terrain.BURNED_GROUND) && tile.liquidType != LiquidType.LAVA 
//					&& (tile.getModifier() != null && tile.getModifier().getType() == GroundModifierType.FIRE)
					) {
//				if(tile.getModifier() != null && tile.getModifier().getType() == GroundModifierType.FIRE) {
//					return;
//				}
				double chance = 0.05;
				if(Math.random() < chance) {
					tile.setTerrain(Terrain.DIRT);
				}
			}
			if(tile.checkTerrain(Terrain.DIRT)) {
				boolean adjacentGrass = false;
				boolean adjacentWater = false;
				for(Tile neighbor : Utils.getNeighbors(tile, world)) {
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
				if(tile.getTempurature() > Season.FREEZING_TEMPURATURE && Math.random() < tile.liquidAmount*threshold*tile.getHumidity()) {
					tile.setTerrain(Terrain.GRASS);
				}
			}
		}
		
	}
	
	private void spreadForest() {
		
		for(Plant plant : plants) {
			
			if(plant.getPlantType() == PlantType.FOREST1) {
				if(Math.random() < 0.01) {
					
					for(Tile tile : plant.getTile().getNeighbors()) {
						if(tile.getPlant() == null && tile.canPlant() && tile.getTempurature() > Season.FREEZING_TEMPURATURE ) {
							tile.setHasPlant(new Plant(PlantType.FOREST1, tile));
							newPlants.add(tile.getPlant());
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
			
			if(tile.canPlant() == false && tile.getTempurature() < Season.FREEZING_TEMPURATURE) {
				continue;
			}
			
			if(tile.getPlant() != null) {
				continue;
			}
			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
				if(Math.random() < 0.01) {
					Plant plant = new Plant(PlantType.CATTAIL, tile);
					tile.setHasPlant(plant);
					newPlants.add(plant);
				}
			}
			
			if (Math.random() < 0.001) {
				tile.setHasPlant(new Plant(PlantType.BERRY, tile));
				newPlants.add(tile.getPlant());
			}

		}
	}
	
	public void doProjectileUpdates() {

		for(Projectile projectile : projectiles) {
			projectile.tick();
			if(projectile.getTargetTile() == null) {
				continue;
			}
			if (projectile.readyToMove()) {
				projectile.moveToTarget();
				if(projectile.getType().getGroundModifierType() != null) {
					GroundModifier gm = new GroundModifier(projectile.getType().getGroundModifierType(), projectile.getTile(), (int)projectile.getType().getDamage()/5);
					projectile.getTile().setModifier(gm);
					addGroundModifier(gm);
				}
			}
			if(projectile.reachedTarget()) {
				if(projectile.getType().isExplosive()) {
					spawnExplosion(projectile.getTile(), projectile.getType().getRadius(), (int)projectile.getType().getDamage());
				} 
				else {
					for(Unit unit : projectile.getTile().getUnits()) {
						unit.takeDamage(projectile.getType().getDamage());
						unit.aggro(projectile.getSource());
					}
					if(projectile.getTile().getHasBuilding() == true) {
						projectile.getTile().getBuilding().takeDamage(projectile.getType().getDamage());
					}
				}
			}
		}
	}
	
	public void doUnitAttacks() {
		for (Unit unit : units) {
			boolean attacked = false;
			if(unit.getTarget() != null) {
				attacked = Attack.tryToAttack(unit, unit.getTarget());
			}
			if(!attacked) {
				for(Unit enemyUnit : getHostileUnitsInTerritory()){
					if(!unit.inRange(enemyUnit)) {
						continue;
					}
					Attack.tryToAttack(unit, enemyUnit);
				}
			}
		}
	}

	public void updateUnitColdDamage() {
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();
		
		for (Unit unit : units) {
			Tile tile = unit.getTile();
			int tileDamage = 0;
			
			if (tileDamage != 0) {
				unit.takeDamage(tileDamage);
			}
			if (unit.isDead() == true) {
				tile.removeUnit(unit);
			} else {
				unitsNew.add(unit);
			}

		}
		for (Unit unit : newUnits) {
			unitsNew.add(unit);
			unit.getTile().addUnit(unit);
			
		}
		newUnits.clear();
		units = unitsNew;
	}
	
	public void addGroundModifier(GroundModifier gm) {

		synchronized (newGroundModifiers) {

			newGroundModifiers.add(gm);
		}
		
	}
	public void clearDeadAndAddNewThings() {
		// UNITS
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();
		for (Unit unit : units) {
			if (unit.isDead() == true) {
				unit.getTile().removeUnit(unit);
			} else {
				unitsNew.add(unit);
			}
		}
		unitsNew.addAll(newUnits);
		newUnits.clear();
		units = unitsNew;
		
		// GROUND MODIFIERS
		LinkedList<GroundModifier> groundModifiersNew = new LinkedList<GroundModifier>();
		for(GroundModifier modifier : groundModifiers) {
			Tile tile = modifier.getTile();
			if(modifier.isDead() == false) {
				groundModifiersNew.add(modifier);
			} else {
				tile.setModifier(null);
			}
		}
		synchronized (newGroundModifiers) {
			groundModifiersNew.addAll(newGroundModifiers);
			newGroundModifiers.clear();
		}
		
		groundModifiers = groundModifiersNew;
		
		// BUILDINGS
		LinkedList<Building> buildingsNew = new LinkedList<Building>();
		LinkedList<Building> plannedBuildingsNew = new LinkedList<Building>();
		
		for (Building building : buildings) {
			if (building.isDead() == true) {
				building.getTile().setBuilding(null);
			} else {
				buildingsNew.add(building);
			}
			
		}
		for(Building plannedBuilding : plannedBuildings) {
			if(plannedBuilding.getRemainingEffort() < plannedBuilding.getType().getBuildingEffort()) {
				buildingsNew.add(plannedBuilding);
			}else {
				plannedBuildingsNew.add(plannedBuilding);
			}
		}
		plannedBuildings = plannedBuildingsNew;
		buildings = buildingsNew;
	
		// PLANTS
		LinkedList<Plant> plantsCopy = new LinkedList<Plant>();
		for(Plant plant : plants) {
			if(plant.isDead() == true) {
				plant.getTile().setHasPlant(null);
			} else {
				plantsCopy.add(plant);
			}
		}
		plantsCopy.addAll(newPlants);
		newPlants.clear();
		plants = plantsCopy;

		// PROJECTILES
		LinkedList<Projectile> projectilesNew = new LinkedList<Projectile>();
		for(Projectile projectile : projectiles) {
			if(projectile.reachedTarget()) {
				projectile.getTile().removeProjectile(projectile);
			} else {
				projectilesNew.add(projectile);
			}
		}
		projectiles = projectilesNew;
		
		if(Game.ticks % 50 == 0) {
			System.out.println(
					"units: " 				+ units.size() + 
					" \tbuildings: " 		+ buildings.size() + 
					" \tplannedBuildings: " + plannedBuildings.size() + 
					" \tplants: " 		+ plants.size() + 
					" \tgroundModifiers: " 	+ groundModifiers.size() + 
					" \tprojectiles: " 		+ projectiles.size());
		}
	}
	
	public void updatePlantDamage() {
		for(Plant plant : plants) {
			Tile tile = plant.getTile();
			
			if(plant.isAquatic()) {
				if (tile.liquidAmount < tile.liquidType.getMinimumDamageAmount()) {
					if (plant.isAquatic() || tile.liquidType != LiquidType.WATER) {
						
						double difInLiquids = tile.liquidType.getMinimumDamageAmount() - tile.liquidAmount;
						double damageTaken = difInLiquids * tile.liquidType.getDamage();
						int roundedDamage = (int) (damageTaken+1);
						if(roundedDamage >= 1) {
							plant.takeDamage(roundedDamage);
						}
					}
				}
			}
			else {
			
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
			if(tile.checkTerrain(Terrain.GRASS) && tile.getRoad() == null && tile.liquidAmount < tile.liquidType.getMinimumDamageAmount() / 2 && Math.random() < bushRarity) {
				double o = Math.random();
				if(o < PlantType.BERRY.getRarity()) {
					Plant p = new Plant(PlantType.BERRY, tile);
					tile.setHasPlant(p);
					newPlants.add(tile.getPlant());
				}
			}
			//tile.liquidType.WATER &&
			//generates water plants
			if( Math.random() < waterPlantRarity) {
				double o = Math.random();
				if(tile.liquidType == LiquidType.WATER && tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()  && o < PlantType.CATTAIL.getRarity()) {
					Plant p = new Plant(PlantType.CATTAIL, tile);
					tile.setHasPlant(p);
					newPlants.add(tile.getPlant());
				}
			}
		}
	}


	public void makeForest() {
		
		for(Tile t : tileListRandom) {
			double tempDensity = forestDensity;
			if(t.getTerrain() == Terrain.DIRT) {
				tempDensity /= 2;
			}
			if (t.canPlant() && t.getRoad() == null && t.liquidAmount < t.liquidType.getMinimumDamageAmount() / 2)
				if (Math.random() < tempDensity) {
					Plant plant = new Plant(PlantType.FOREST1, t);
					t.setHasPlant(plant);
					newPlants.add(plant);
				}
		}
		
	}
	
	public List<Tile> getNeighbors(Tile tile) {
		int x = tile.getLocation().x;
		int y = tile.getLocation().y;
		int minX = Math.max(0, tile.getLocation().x - 1);
		int maxX = Math.min(this.getWidth()-1, tile.getLocation().x + 1);
		int minY = Math.max(0, tile.getLocation().y-1);
		int maxY = Math.min(this.getHeight()-1, tile.getLocation().y + 1);

		LinkedList<Tile> tiles = new LinkedList<>();
		for(int i = minX; i <= maxX; i++) {
			for(int j = minY; j <= maxY; j++) {
				if(i == x || j == y) {
					if(i != x || j != y) {
						if(this.get(new TileLoc(i, j)) != null) {
							tiles.add(this.get(new TileLoc(i, j)));
						}
					}
				}
			}
		}
		Collections.shuffle(tiles); 
		return tiles;
	}
	
	public void generateWorld(MapType mapType, int size) {
		liquidSimulationPhases.clear();
		for(int i = 0; i < NUM_LIQUID_SIMULATION_PHASES; i++) {
			liquidSimulationPhases.add(new ArrayList<>());
		}
		width = size;
		height = size;
		tiles = new Tile[width][height];
		int smoothingRadius = (int) (Math.sqrt((width + height)/2)/2);
		
		double[][] heightMap = Generation.generateHeightMap(smoothingRadius, width, height);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tiles[i][j] = Tile.makeTile(new TileLoc(i, j), Terrain.DIRT);
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

		volcano = Generation.makeVolcano(this, heightMap);
		heightMap = Utils.smoothingFilter(heightMap, 3, 3);

		for(Tile tile : getTiles()) {
			tile.setHeight(heightMap[tile.getLocation().x][tile.getLocation().y]);
		}

		for(Tile tile : getTiles()) {
			if(tile.getTerrain() == Terrain.DIRT) {
				Terrain t;
				if (tile.getHeight() > TERRAIN_SNOW_LEVEL) {
					t = Terrain.ROCK;
				}
				else if (tile.getHeight() > 0.6) {
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

		Season.makeSeasonArrays(getHeight());
		int numTiles = width*height;
		Generation.makeLake(numTiles * 1.0/100, this);
		Generation.makeLake(numTiles * 1.0/200, this);
		Generation.makeLake(numTiles * 1.0/400, this);
		Generation.makeLake(numTiles * 1.0/800, this);
		System.out.println("Simulating water for 100 iterations");
		for(int i = 0; i < 100; i++) {
			Liquid.propogate(this);
		}

		Generation.genResources(this);
		this.genPlants();
		this.makeForest();
		Generation.generateWildLife(this);
		System.out.println("Finished generating " + width + "x" + height + " world with " + tileList.size() + " tiles.");
	}
	

	public BufferedImage[] createTerrainImage() {
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
		BufferedImage terrainImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage minimapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);

		Graphics minimapGraphics = minimapImage.getGraphics();
		Graphics terrainGraphics = terrainImage.getGraphics();
		for(Tile tile : this.getTiles()) {
			Color minimapColor = terrainColors.get(tile.getTerrain());
			Color terrainColor = terrainColors.get(tile.getTerrain());
			if(tile.getResource() != null) {
				terrainColor = tile.getResource().getType().getColor(0);
				minimapColor = tile.getResource().getType().getColor(0);
			}
			if(tile.getRoad() != null) {
				terrainColor = Utils.roadColor;
				minimapColor = Utils.roadColor;
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
			if(tile.getHasBuilding()) {
				terrainColor = tile.getBuilding().getColor(0);
				minimapColor = tile.getBuilding().getColor(0);
			}
			if(tile.getModifier() != null) {
				minimapColor = tile.getModifier().getType().getColor(0);
				terrainColor = Utils.blendColors(tile.getModifier().getType().getColor(0), terrainColor, 0.9);
			}
			if(tile.getIsTerritory()) {
				minimapColor = Utils.blendColors(Game.playerColor, minimapColor, 0.3);
				terrainColor = Utils.blendColors(Game.playerColor, terrainColor, 0.3);
			}
			double tilebrightness = tile.getBrightness();
			minimapColor = Utils.blendColors(minimapColor, Color.black, brighnessModifier + tilebrightness);
			terrainColor = Utils.blendColors(terrainColor, Color.black, brighnessModifier + tilebrightness);
			minimapImage.setRGB(tile.getLocation().x, tile.getLocation().y, minimapColor.getRGB());
			terrainImage.setRGB(tile.getLocation().x, tile.getLocation().y, terrainColor.getRGB());
		}
		minimapGraphics.dispose();
		terrainGraphics.dispose();
		
		BufferedImage heightMapImage = new BufferedImage(tiles.length, tiles[0].length, BufferedImage.TYPE_3BYTE_BGR);
		for(Tile tile : getTiles() ) {
			int r = Math.max(Math.min((int)(255*tile.getHeight()), 255), 0);
			Color c = new Color(r, 0, 255-r);
			heightMapImage.setRGB(tile.getLocation().x, tile.getLocation().y, c.getRGB());
		}
		return new BufferedImage[] { terrainImage, minimapImage, heightMapImage};
	}

	public int ticksUntilDay() {
		int currentDayOffset = Game.ticks%(DAY_DURATION + NIGHT_DURATION);
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
		return (Game.ticks + TRANSITION_PERIOD)%(DAY_DURATION + NIGHT_DURATION);
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
