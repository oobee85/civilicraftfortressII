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
				if(world[x][y].checkTerrain(Terrain.GRASS) && Math.random() < 0.1) {
					Animal animal = new Animal(AnimalType.DEER);
					animal.setTile(world[x][y]);
					animals.add(animal);
				}
			}
		}
	}
	
	public static void tick(Tile[][] world) {
		ConcurrentLinkedQueue<Animal> newAnimals = new ConcurrentLinkedQueue<>();
		for(Animal animal : animals) {
			if(animal.getTile().liquidAmount > Liquid.MINIMUM_LIQUID_THRESHOLD) {
				animal.takeDamage(animal.getTile().liquidAmount * animal.getTile().liquidType.getDamage());
			}
			if(animal.isDead()) {
				dead.add(animal);
				continue;
			}
			if(Math.random() < 0.01) {
				if(animal.getTile().getPlant() != null) {
					animal.getTile().getPlant().takeDamage(1);
				}
				else if(animal.getTile().checkTerrain(Terrain.GRASS)) {
					animal.getTile().setTerrain(Terrain.DIRT);
				}
				else {
					animal.takeDamage(1);
				}
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
