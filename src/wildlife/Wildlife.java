package wildlife;

import java.util.*;
import java.util.concurrent.*;

import liquid.*;
import utils.*;
import world.*;

public class Wildlife {
	
	private static ConcurrentLinkedQueue<Animal> animals = new ConcurrentLinkedQueue<>();

	private static LinkedList<Animal> dead = new LinkedList<>();
	
	public static void generateWildLife(Tile[][] world) {
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world[0].length; y++) {
				if(world[x][y].checkTerrain(Terrain.GRASS) && Math.random() < 0.01) {
					Animal animal = new Animal();
					animal.setTile(world[x][y]);
					animals.add(animal);
				}
			}
		}
	}
	
	/** 
	 * 
	 * @param animal
	 * @return true if survived, false if died
	 */
	private static boolean liquidInteraction(Animal animal) {
		if(animal.getTile().liquidType == LiquidType.LAVA && animal.getTile().liquidAmount > Liquid.MINIMUM_LIQUID_THRESHOLD) {
			return false;
		}
		if(animal.getTile().liquidType == LiquidType.WATER && animal.getTile().liquidAmount > 0.1) {
			return false;
		}
		return true;
	}
	
	public static void tick(Tile[][] world) {
		ConcurrentLinkedQueue<Animal> newAnimals = new ConcurrentLinkedQueue<>();
		for(Animal animal : animals) {
			if(!liquidInteraction(animal)) {
				dead.add(animal);
				continue;
			}
			animal.getTile().setHasPlant(null);
			if(animal.getTile().checkTerrain(Terrain.GRASS)) {
				animal.getTile().setTerrain(Terrain.DIRT);
			}
			if(Math.random() < animal.getMoveChance()) {
				List<Tile> neighbors = Utils.getNeighbors(animal.getTile(), world);
				Tile next = neighbors.get(0);
				animal.setTile(next);
			}
			newAnimals.add(animal);
		}
		animals = newAnimals;
	}
	
	public static ConcurrentLinkedQueue<Animal> getAnimals() {
		return animals;
	}
}
