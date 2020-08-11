package wildlife;

import java.util.*;

import game.*;
import utils.*;
import world.*;

public class Wildlife {
	
	private static LinkedList<Animal> animals = new LinkedList<>();
	private static LinkedList<Animal> dead = new LinkedList<>();

	public static void generateWildLife(World world) {
		for(Tile tile : world.getTilesRandomly()) {
			TileLoc loc = tile.getLocation();
			if(Math.random() < 0.01) {
				if(world[loc].checkTerrain(Terrain.GRASS) || world[loc].checkTerrain(Terrain.DIRT)) {
					makeAnimal(UnitType.DEER, world, loc);
				}
				
				if(world[loc].liquidAmount > world[loc].liquidType.getMinimumDamageAmount()) {
					makeAnimal(UnitType.FISH, world, loc);
				}
			}
			
			if(Math.random() < 0.01) {
				if(world[loc].checkTerrain(Terrain.DIRT)) {
					makeAnimal(UnitType.HORSE, world, loc);
				}
			}
			
			if(Math.random() < 0.01) {
				if(world[loc].checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.PIG, world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(world[loc].checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.SHEEP, world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(world[loc].checkTerrain(Terrain.GRASS)) {
					makeAnimal(UnitType.COW, world, loc);
				}
			}
			
			if(world[loc].getTerrain() == Terrain.VOLCANO && Math.random() < 0.01) {
				makeAnimal(UnitType.DRAGON, world, loc);
			}
			if(world[loc].getTerrain() == Terrain.SNOW && Math.random() < 0.005) {
				makeAnimal(UnitType.WOLF, world, loc);
			}
		}
	}
	private static void makeAnimal(UnitType animalType, World world, TileLoc loc) {
		
		if(animalType.isAquatic() == false && world[loc].liquidAmount > world[loc].liquidType.getMinimumDamageAmount()/2 ) {
			return;
		}
		Animal animal = new Animal(animalType, world[loc], false);
		animal.setTile(world[loc]);
		animals.add(animal);
		world[loc].addUnit(animal);
		
	}
	
	public static void addAnimal(Animal a) {
		animals.add(a);
	}
	
	public static void tick(World world) {
		LinkedList<Animal> newAnimals = new LinkedList<>();
		HashMap<Tile, Animal> trying = new HashMap<>();
		for(Animal animal : animals) {
			animal.tick();
			double liquidDamage = animal.computeTileDamage(animal.getTile(), animal.getTile().getHeight());
			if(animal.getType().isFlying() != true) {
				if(liquidDamage >= 1) {
					animal.takeDamage(liquidDamage);
				}
				
			}
			if(animal.isDead()) {
				animal.getTile().setResource(ResourceType.DEAD_ANIMAL);
				dead.add(animal);
				continue;
			}
			animal.loseEnergy();
			if(animal.wantsToEat()) {
				if(animal.getType().isHostile() == true && animal.getTarget() == null) {
					
					Unit iveGotYouInMySights;
					if(Math.random() < 0.01 && world.units.isEmpty() == false) {
						int pickUnit = (int) (world.units.size()*Math.random());
						iveGotYouInMySights = world.units.get(pickUnit);
					}else {
						int pickAnimal = (int) (animals.size()*Math.random());
						iveGotYouInMySights = animals.get(pickAnimal);
					}
					
					if(iveGotYouInMySights != animal) {
						animal.setTarget(iveGotYouInMySights);
					}
				}
				else {
					if(!animal.getType().isHostile() && animal.getTile().getPlant() != null) {
						animal.getTile().getPlant().takeDamage(animal.getType().getCombatStats().getAttack());
						animal.eat(animal.getType().getCombatStats().getAttack());
					}else if(!animal.getType().isHostile() && animal.getTile().checkTerrain(Terrain.GRASS)) {
						animal.getTile().setTerrain(Terrain.DIRT);
						animal.eat(animal.getType().getCombatStats().getAttack());
					}
				}
			}
			if(animal instanceof Ogre) {
				if(animal.getTarget() == null) {
					for(Unit u : world.units) {
						if(u.isPlayerControlled()) {
							animal.setTarget(u);
							break;
						}
					}
				}
			}
			

			if(animal.getTarget() != null) {
				animal.imOnTheHunt(world);
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
						double danger = animal.computeDanger(t, t.getHeight());
						if(danger < bestDanger) {
							best = t;
							bestDanger = danger;
						}
					}
					if(best != null) {
						animal.moveTo(best);
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
					newAnimals.add(newanimal);
				}
				else {
					trying.put(animal.getTile(), animal);
				}
			}
			newAnimals.add(animal);
		}

		for(Animal a : dead) {
			a.getTile().removeUnit(a);
		}
		animals = newAnimals;
	}
	
	public static LinkedList<Animal> getAnimals() {
		return animals;
	}
}
