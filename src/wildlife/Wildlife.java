package wildlife;

import java.util.*;
import java.util.concurrent.*;

import game.*;
import liquid.*;
import utils.*;
import world.*;

public class Wildlife {
	
//	private static ConcurrentLinkedQueue<Animal> animals = new ConcurrentLinkedQueue<>();
	private static LinkedList<Animal> animals = new LinkedList<>();

	private static LinkedList<Animal> dead = new LinkedList<>();
	
	public static void generateWildLife(World world) {
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				TileLoc loc = new TileLoc(x, y);
				
				if(Math.random() < 0.01) {
					if(world[loc].checkTerrain(Terrain.GRASS) || world[loc].checkTerrain(Terrain.DIRT)) {
						makeAnimal(AnimalType.DEER, world, loc);
					}
					
					if(world[loc].liquidAmount > world[loc].liquidType.getMinimumDamageAmount()) {
						makeAnimal(AnimalType.FISH, world, loc);
					}
				}
				
				if(Math.random() < 0.01) {
					if(world[loc].checkTerrain(Terrain.GRASS)) {
						makeAnimal(AnimalType.HORSE, world, loc);
					}
				}
				
				
				if(world[loc].getTerrain() == Terrain.VOLCANO && Math.random() < 0.01) {
					makeAnimal(AnimalType.DRAGON, world, loc);
				}
			}
		}
	}
	private static void makeAnimal(AnimalType animalType, World world, TileLoc loc) {
		Animal animal = new Animal(animalType);
		animal.setTile(world[loc]);
		animals.add(animal);
	}
	
	
	public static void tick(World world) {
		LinkedList<Animal> newAnimals = new LinkedList<>();
		HashMap<Tile, Animal> trying = new HashMap<>();
		for(Animal animal : animals) {
			double liquidDamage = animal.computeTileDamage(animal.getTile(), animal.getTile().getHeight());
			if(animal.getType().isFlying() != true) {
				animal.takeDamage(liquidDamage);
			}
			if(animal.isDead()) {
				dead.add(animal);
				continue;
			}
			animal.loseEnergy();
			if(animal.wantsToEat()) {
				if(animal.getType().isHostile() == true && animal.getPrey() == null) {
					int pickAnimal = (int) (animals.size()*Math.random());
					Animal iveGotYouInMySights = animals.get(pickAnimal);
					if(iveGotYouInMySights != animal) {
						animal.setPrey(iveGotYouInMySights);
					}
//					animal.setTargetTile(iveGotYouInMySights.getTile());
//					System.out.println(pickAnimal + ", " +animals.size());
				}
				else {
					if(!animal.getType().isHostile() && animal.getTile().getPlant() != null) {
						animal.getTile().getPlant().takeDamage(0.1);
						animal.eat();
					}else if(animal.getTile().checkTerrain(Terrain.GRASS)) {
						animal.getTile().setTerrain(Terrain.DIRT);
						animal.eat();
					}
				}
			}
			

			if(animal.getPrey() != null) {
				animal.imOnTheHunt(world);
			}
			else if(Math.random() < animal.getMoveChance()) {
				List<Tile> neighbors = Utils.getNeighborsIncludingCurrent(animal.getTile(), world);
				Tile best = null;
				double bestDanger = Double.MAX_VALUE;
				for(Tile t : neighbors) {
					// deer cant move onto walls
					if(!animal.getType().isFlying() && t.getHasBuilding() && t.getBuilding().getBuildingType() == BuildingType.WALL_STONE) {
						continue;
					}
					double danger = animal.computeDanger(t, t.getHeight());
					if(danger < bestDanger) {
						best = t;
						bestDanger = danger;
					}
				}
				if(best != null) {
					double heightIncrease = best.getHeight() - animal.getTile().getHeight();
					animal.climb(heightIncrease);
					animal.setTile(best);
				}
			}
			
			else if(animal.wantsToReproduce()) {
				if(trying.containsKey(animal.getTile())) {
					Animal other = trying.remove(animal.getTile());
					animal.reproduced();
					other.reproduced();
					Animal newanimal = new Animal(animal.getType());
					newanimal.setTile(animal.getTile());
					newAnimals.add(newanimal);
				}
				else {
					trying.put(animal.getTile(), animal);
				}
			}
			newAnimals.add(animal);
		}
		
		animals = newAnimals;
	}
//	
//	private static void moveToLocation(Animal animal, World world) {
//		if(animal.getTargetTile() != null) {
//			Tile currentTile = animal.getTile();
//			double bestDistance = Integer.MAX_VALUE;
//			Tile bestTile = currentTile;
//	
//			for (Tile tile : Utils.getNeighbors(currentTile, world)) {
////				if (tile.getHasUnit()) {
////					tile.getUnit().takeDamage(animal.getType().getCombatStats().getAttack());
////					break;
////				}
////				if (tile.getHasAnimal()) {
////					tile.getAnimal().takeDamage(animal.getType().getCombatStats().getAttack());
////					break;
////				}
//				double distance = tile.getLocation().distanceTo(animal.getTargetTile().getLocation());
//				if (distance < bestDistance) {
//					bestDistance = distance;
//					bestTile = tile;
//				}
//			}
//			animal.moveTo(bestTile);
//		}
//	}
	
	public static LinkedList<Animal> getAnimals() {
		return animals;
	}
}
