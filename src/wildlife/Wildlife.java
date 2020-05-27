package wildlife;

import java.util.*;

import game.*;
import utils.*;
import world.*;

public class Wildlife {
	
	private static LinkedList<Animal> animals = new LinkedList<>();

	public static void generateWildLife(World world) {
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				TileLoc loc = new TileLoc(x, y);
				
				if(Math.random() < 0.01) {
					if(world[loc].checkTerrain(Terrain.GRASS) || world[loc].checkTerrain(Terrain.DIRT)) {
						makeAnimal(UnitType.DEER, world, loc);
					}
					
					if(world[loc].liquidAmount > world[loc].liquidType.getMinimumDamageAmount()) {
						makeAnimal(UnitType.FISH, world, loc);
					}
				}
				
				if(Math.random() < 0.01) {
					if(world[loc].checkTerrain(Terrain.GRASS)) {
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
				
				
				if(world[loc].getTerrain() == Terrain.VOLCANO && Math.random() < 0.01) {
					makeAnimal(UnitType.DRAGON, world, loc);
				}
				if(world[loc].getTerrain() == Terrain.SNOW && Math.random() < 0.01) {
					makeAnimal(UnitType.WOLF, world, loc);
				}
			}
		}
	}
	private static void makeAnimal(UnitType animalType, World world, TileLoc loc) {
		Animal animal = new Animal(animalType, world[loc], false);
		animal.setTile(world[loc]);
		animals.add(animal);
		world[loc].addUnit(animal);
	}
	
	
	public static void tick(World world) {
		LinkedList<Animal> newAnimals = new LinkedList<>();
		HashMap<Tile, Animal> trying = new HashMap<>();
		for(Animal animal : animals) {
			animal.tick();
			double liquidDamage = animal.computeTileDamage(animal.getTile(), animal.getTile().getHeight());
			if(animal.getType().isFlying() != true) {
				animal.takeDamage(liquidDamage);
			}
			if(animal.isDead()) {
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
			else if(Math.random() < animal.getMoveChance() && animal.readyToMove()) {
				if(animal.getTile().getStructure() != null && animal.getTile().getStructure().getStructureType() == StructureType.FARM) {
					System.out.println("stuck inside farm");
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
		
		animals = newAnimals;
	}
	
	public static LinkedList<Animal> getAnimals() {
		return animals;
	}
}
