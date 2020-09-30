package wildlife;

import java.util.*;

import game.*;
import liquid.LiquidType;
import utils.*;
import world.*;

public class Wildlife {
	
	private static LinkedList<Animal> animals = new LinkedList<>();
	private static LinkedList<Animal> dead = new LinkedList<>();
	private static LinkedList<Animal> newAnimals = new LinkedList<>();

	public static void generateWildLife(World world) {
		for(Tile tile : world.getTilesRandomly()) {
			TileLoc loc = tile.getLocation();
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS) || tile.checkTerrain(Terrain.DIRT)) {
					makeAnimal(UnitType.DEER, world, loc);
				}
				
				if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
					if(tile.liquidType != LiquidType.LAVA) {
						makeAnimal(UnitType.FISH, world, loc);
					}
					
				}
			}
			
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.DIRT)) {
					makeAnimal(UnitType.HORSE, world, loc);
				}
			}
			
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.PIG, world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.SHEEP, world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.COW, world, loc);
				}
			}
			
			if(tile.getTerrain() == Terrain.VOLCANO && Math.random() < 0.01) {
				world.spawnDragon();
			}
			if(tile.getTerrain() == Terrain.SNOW && Math.random() < 0.005) {
				makeAnimal(UnitType.WOLF, world, loc);
			}
		}
	}
	private static void makeAnimal(UnitType animalType, World world, TileLoc loc) {
		
		if(animalType.isAquatic() == false && world.get(loc).liquidAmount > world.get(loc).liquidType.getMinimumDamageAmount()/2 ) {
			return;
		}
		Animal animal = new Animal(animalType, world.get(loc), false);
		animal.setTile(world.get(loc));
		newAnimals.add(animal);
		world.get(loc).addUnit(animal);
		
	}
	
	public static void addAnimal(Animal a) {
		newAnimals.add(a);
	}
	
	public static void tick(World world) {
		LinkedList<Animal> animalsCopy = new LinkedList<>();
		HashMap<Tile, Animal> trying = new HashMap<>();
		for(Animal animal : animals) {
			animal.tick();
			double liquidDamage = animal.getTile().computeTileDamage(animal);
			if(animal.getType().isFlying() != true) {
				if(liquidDamage >= 1) {
					animal.takeDamage(liquidDamage);
				}
				
			}
			if(animal.isDead()) {
				if(animal.getType().getDeadResource() != null) {
					animal.getTile().setResource(animal.getType().getDeadResource());
				}
				dead.add(animal);
				continue;
			}
			animal.loseEnergy();
			animal.tick();
			if(animal.wantsToEat()) {
				animal.chooseWhatToEat(world.units, getAnimals());
			}
			if(animal.wantsToAttack() && animal.getTarget() == null) {
				animal.chooseWhatToAttack(world.units, getAnimals(), world.buildings);
			}

			animal.imOnTheHunt(world);
			if(animal.getTarget() != null) {
				
			}
			else if(Math.random() < animal.getMoveChance() && animal.readyToMove()) {
				
				if(animal.getTile().getBuilding() != null && animal.getTile().getBuilding().getBuildingType() == BuildingType.FARM) {
				//stuck inside farm
				}
				else {
					List<Tile> neighbors = Utils.getNeighborsIncludingCurrent(animal.getTile(), world);
					Tile best = null;
					
					double bestDanger = Double.MAX_VALUE;
					for(Tile t : neighbors) {
						// deer cant move onto walls
						if(t.isBlocked(animal)) {
							continue;
						}
						double danger = animal.computeDanger(t);
						if(danger < bestDanger) {
							best = t;
							bestDanger = danger;
						}
					}
					if(best != null) {
						animal.moveTo(best);
//						animal.moveTowards(best);
					}
				}
			}
			
			else if(animal.wantsToReproduce()) {
				if(trying.containsKey(animal.getTile())) {
					Animal other = trying.remove(animal.getTile());
					animal.reproduced();
					other.reproduced();
					Animal newanimal = new Animal(animal.getType(), animal.getTile(), false);
					newanimal.setTile(animal.getTile());
					animalsCopy.add(newanimal);
				}
				else {
					trying.put(animal.getTile(), animal);
				}
			}
			animalsCopy.add(animal);
			
			if(animal.getType() == UnitType.BOMB) {
				if(animal.getTargetTile() == animal.getTile()) {
					world.spawnExplosion(animal.getTile(), 5, 500);
				}
			}
		}

		for(Animal a : dead) {
			a.getTile().removeUnit(a);
		}
		animalsCopy.addAll(newAnimals);
		newAnimals.clear();
		animals = animalsCopy;
	}
	
	public static LinkedList<Animal> getAnimals() {
		return animals;
	}
}
