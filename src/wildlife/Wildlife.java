package wildlife;

import java.util.*;
import java.util.concurrent.*;

import game.*;
import liquid.*;
import utils.*;
import world.*;

public class Wildlife {
	
	private static ConcurrentLinkedQueue<Animal> animals = new ConcurrentLinkedQueue<>();

	private static LinkedList<Animal> dead = new LinkedList<>();
	
	public static void generateWildLife(World world) {
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				TileLoc loc = new TileLoc(x, y);
				if(Math.random() < 0.04) {
					if(world[loc].checkTerrain(Terrain.GRASS) || world[loc].checkTerrain(Terrain.DIRT) || world[loc].checkTerrain(Terrain.SNOW)) {
						Animal animal = new Animal(AnimalType.DEER);
						animal.setTile(world[loc]);
						animals.add(animal);
					}
					if(world[loc].liquidAmount > world[loc].liquidType.getMinimumDamageAmount()) {
						Animal animal = new Animal(AnimalType.FISH);
						animal.setTile(world[loc]);
						animals.add(animal);
					}
				}
				
				if(world[loc].getTerrain() == Terrain.VOLCANO && Math.random() < 0.01) {
					Animal animal = new Animal(AnimalType.DRAGON);
					animal.setTile(world[loc]);
					animals.add(animal);
				}
			}
		}
	}
	
	public static void tick(World world) {
		ConcurrentLinkedQueue<Animal> newAnimals = new ConcurrentLinkedQueue<>();
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
			animal.loseEnergy();
			animal.loseEnergy();
			animal.loseEnergy();
			animal.loseEnergy();
			animal.loseEnergy();
			if(animal.wantsToEat()) {
				if(!animal.getType().isHostile() && animal.getTile().getPlant() != null) {
					animal.getTile().getPlant().takeDamage(0.1);
					animal.eat();
				}else if(animal.getTile().checkTerrain(Terrain.GRASS)) {
					animal.getTile().setTerrain(Terrain.DIRT);
					animal.eat();
				}
				
				if(animal.getType().isHostile() == true) {
					int pickAnimal = (int) (animals.size()*Math.random());
					Animal iveGotYouInMySights = animals.peek();
					
//					System.out.println(pickAnimal + ", " +animals.size());
					
				}
			}
			
			
			if(Math.random() < animal.getMoveChance()) {
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
	
	public static ConcurrentLinkedQueue<Animal> getAnimals() {
		return animals;
	}
}
