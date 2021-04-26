package world;

import java.util.*;

import game.*;
import game.liquid.*;
import utils.*;

public class Generation {
	
	public static final int OREMULTIPLIER = 16384;
	
	// From https://en.wikipedia.org/wiki/Perlin_noise
	
	private static final float noise(double x, double y) {
		return (float) Utils.getRandomNormalF(5);
	}
	
	public static float[][] generateHeightMap(long seed, int smoothingRadius, int width, int height) {
		int power = 1;
		while(power < width || power < height) {
			power *= 2;
		}
		
		float[][] heightMap = PerlinNoise.generateHeightMap(seed, power, power);
		float[][] croppedHeightMap = new float[width][height];
		int croppedWidth = (power - width)/2;
		int croppedHeight = (power - height)/2;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				croppedHeightMap[i][j] = heightMap[i + croppedWidth][j + croppedHeight];
			}
		}
		Utils.normalize(croppedHeightMap, 0, 1000);
		int center = width/2;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double dif = 1 * Math.abs(i-center)/center;
				croppedHeightMap[i][j] +=  (float)1000 * Math.pow(dif +1, -0.1) + (float)(Math.random());
			}
		}
		Utils.normalize(croppedHeightMap, 0, 1000);
		return croppedHeightMap;
	}
	
	
	public static TileLoc makeVolcano(World world, float[][] heightMap) {
		int x = 0;
		int y = 0; 
		float highest = -1000;
//		for(int i = 0; i < world.getWidth(); i++) {
//			for(int j = 0; j < world.getHeight(); j++) {
//				if(heightMap[i][j] > highest) {
//					highest = heightMap[i][j];
//					x = i;
//					y = j;
//				}
//			}
//		}
		x = world.getWidth()/2;
		y = world.getHeight()/2;
		float lavaRadius = 5;
		float volcanoRadius = 10;
		float mountainRadius = 20;
		float mountainEdgeRadius = 23;
		
		for(Tile tile : world.getTiles()) {
			int i =  tile.getLocation().x();
			int j =  tile.getLocation().y();
			int dx = i - x;
			int dy = j - y;
			float distanceFromCenter = (float) Math.sqrt(dx*dx + dy*dy);
			if(distanceFromCenter < mountainEdgeRadius) {
				
				float height = 1 - 0.2f*(lavaRadius - distanceFromCenter)/lavaRadius;
				if(distanceFromCenter > lavaRadius) {
					height = 1 - (distanceFromCenter - lavaRadius)/mountainEdgeRadius;
//					heightMap[i][j] = Math.max(height, heightMap[i][j]);
				}
				else {
//					heightMap[i][j] = height;
				}
				
				if(distanceFromCenter < lavaRadius) {
					tile.setTerrain(Terrain.VOLCANO);
					tile.liquidType = LiquidType.LAVA;
					tile.liquidAmount = 0.2f;
				}else if(distanceFromCenter < volcanoRadius) {
					tile.setTerrain(Terrain.VOLCANO);
				}
			}
		}
		return new TileLoc(x, y);
	}

	public static void generateResources(World world) {
		for(ResourceType resource : ResourceType.values()) {
			int numVeins = (int)(world.getWidth() * world.getHeight() * resource.getNumVeins() / OREMULTIPLIER);
			
			System.out.println("Tiles of " + resource.name() + ": " + numVeins);
			
			for(Tile tile : world.getTilesRandomly()) {
				if(tile.getResource() != null) {
					continue;
				}
				if(numVeins <= 0) {
					break;
				}
				if(resource.isOre() && tile.canOre() ) {
					// if ore is rare the tile must be able to support rare ore
					
					if(!resource.isRare() || tile.canSupportRareOre()) {
						makeOreVein(tile, resource, resource.getVeinSize());
						numVeins --;
					}
				}
				
				
			}
		}
	}

	
	public static void makeOreVein(Tile t, ResourceType resource, int veinSize) {
		HashMap<Tile, Double> visited = new HashMap<>();
		
		PriorityQueue<Tile> search = new PriorityQueue<>((x, y) ->  { 
			double distancex = visited.get(x);
			double distancey = visited.get(y);
			if(distancey < distancex) {
				return 1;
			}
			else if(distancey > distancex) {
				return -1;
			}
			else {
				return 0;
			}
		});
		visited.put(t, 0.0);
		search.add(t);
		
		while(veinSize > 0 && !search.isEmpty()) {
			Tile potential = search.poll();
			
			for(Tile ti : potential.getNeighbors()) {
				if(visited.containsKey(ti)) {
					continue;
				}
				visited.put(ti, ti.getLocation().distanceTo(t.getLocation()) + Math.random()*10);
				search.add(ti);
			}
			
			if(resource.isOre() && potential.canOre()  && potential.getResource() == null) {
				// if ore is rare the tile must be able to support rare ore
				
				if(!resource.isRare() || potential.canSupportRareOre()) {
					potential.setResource(new Resource(resource, potential));
					veinSize--;
				}
			}
		}
		
		
		
	}

	private void oldMakeLake(int volume, Tile[][] world, double[][] heightMap) {
		
		// Fill tiles until volume reached
		PriorityQueue<TileLoc> queue = new PriorityQueue<TileLoc>((p1, p2) -> {
			return heightMap[p1.x()][p1.y()] - heightMap[p2.x()][p2.y()] > 0 ? 1 : -1;
		});
		boolean[][] visited = new boolean[world.length][world[0].length];
		queue.add(new TileLoc((int) (Math.random() * world.length), (int) (Math.random() * world[0].length)));
		while(!queue.isEmpty() && volume > 0) {
			TileLoc next = queue.poll();
			int i = next.x();
			int j = next.y();
//			world[i][j].liquidAmount += volume/5;
//			if(!world[i][j].checkTerrain(Terrain.WATER)) {
//				world[i][j].setTerrain(Terrain.WATER);
//				volume--;
//			}
			// Add adjacent tiles to the queue
			if(i > 0 && !visited[i-1][j]) {
				queue.add(new TileLoc(i-1, j));
				visited[i-1][j] = true;
			}
			if(j > 0 && !visited[i][j-1]) {
				queue.add(new TileLoc(i, j-1));
				visited[i][j-1] = true;
			}
			if(i + 1 < world.length && !visited[i+1][j]) {
				queue.add(new TileLoc(i+1, j));
				visited[i+1][j] = true;
			}
			if(j + 1 < world[0].length && !visited[i][j+1]) {
				queue.add(new TileLoc(i, j+1));
				visited[i][j+1] = true;
			}
		}
	}

	public static void makeLake(double volume, World world) {
		// Fill tiles until volume reached
		PriorityQueue<TileLoc> queue = new PriorityQueue<TileLoc>((p1, p2) -> {
			return (world.get(p1).getHeight() + world.get(p1).liquidAmount) - (world.get(p2).getHeight() + world.get(p2).liquidAmount) > 0 ? 1 : -1;
		});
		boolean[][] visited = new boolean[world.getWidth()][world.getHeight()];
		queue.add(new TileLoc((int) (Math.random() * world.getWidth()), (int) (Math.random() * world.getHeight())));
		while(!queue.isEmpty() && volume > 0) {
			TileLoc next = queue.poll();
			int i = next.x();
			int j = next.y();
			world.get(next).liquidAmount += 2;
			volume -= 2;
			// Add adjacent tiles to the queue
			for(Tile t : world.get(next).getNeighbors()) {
				if(!visited[t.getLocation().x()][t.getLocation().y()]) {
					queue.add(t.getLocation());
					visited[t.getLocation().x()][t.getLocation().y()] = true;
				}
			}
//			if(i > 0 && !visited[i-1][j]) {
//				queue.add(new TileLoc(i-1, j));
//				visited[i-1][j] = true;
//			}
//			if(j > 0 && !visited[i][j-1]) {
//				queue.add(new TileLoc(i, j-1));
//				visited[i][j-1] = true;
//			}
//			if(i + 1 < world.getWidth() && !visited[i+1][j]) {
//				queue.add(new TileLoc(i+1, j));
//				visited[i+1][j] = true;
//			}
//			if(j + 1 < world.getHeight() && !visited[i][j+1]) {
//				queue.add(new TileLoc(i, j+1));
//				visited[i][j+1] = true;
//			}
		}
	}
	public static void generateWildLife(World world) {
		for(Tile tile : world.getTilesRandomly()) {
			TileLoc loc = tile.getLocation();
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS) || tile.checkTerrain(Terrain.DIRT)) {
					world.makeAnimal(Game.unitTypeMap.get("DEER"), world, loc);
				}
				
				if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()) {
					if(tile.liquidType != LiquidType.LAVA) {
						world.makeAnimal(Game.unitTypeMap.get("FISH"), world, loc);
					}
					
				}
			}
			
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.DIRT)) {
					world.makeAnimal(Game.unitTypeMap.get("HORSE"), world, loc);
				}
			}
			
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					world.makeAnimal(Game.unitTypeMap.get("PIG"), world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					world.makeAnimal(Game.unitTypeMap.get("SHEEP"), world, loc);
				}
			}
			if(Math.random() < 0.01) {
				if(tile.checkTerrain(Terrain.GRASS)) {
					world.makeAnimal(Game.unitTypeMap.get("COW"), world, loc);
				}
			}
			
			
			if(tile.getTerrain() == Terrain.ROCK && Math.random() < 0.001) {
				world.makeAnimal(Game.unitTypeMap.get("WOLF"), world, loc);
			}
		}
		world.spawnDragon(null);
	}
	
}
