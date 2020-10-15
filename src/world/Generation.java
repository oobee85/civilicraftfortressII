package world;

import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;

public class Generation {
	
	private static final double snowEdgeRatio = 0.5;
	private static final double rockEdgeRatio = 0.7;
	
	public static final int OREMULTIPLIER = 16384;
	
	public static double[][] generateHeightMap(int smoothingRadius, int width, int height) {
		LinkedList<double[][]> noises = new LinkedList<>();
		
		int power = 1;
		while(power < width && power < height) {
			power *= 2;
		}
		

		for (int octave = 2; octave <= power; octave *= 2) {
			double[][] noise1 = new double[octave][octave];
			for (int i = 0; i < noise1.length; i++) {
				for (int j = 0; j < noise1[0].length; j++) {
					noise1[i][j] = Utils.getRandomNormal(5);
				}
			}
			noises.add(noise1);
		}

		double[][] combinedNoise = new double[power][power];
		for (int i = 0; i < power; i++) {
			for (int j = 0; j < power; j++) {
				double rand = 0;
				int divider = power;
				double multiplier = 1;
				for (double[][] noise : noises) {
					divider /= 2;
					multiplier /= 1.4;
					rand += multiplier * noise[i / divider][j / divider];
				}
				combinedNoise[i][j] = rand;
			}
		}

		double[][] heightMap = Utils.smoothingFilter(combinedNoise, smoothingRadius, 100);
		double[][] croppedHeightMap = new double[width][height];
		int croppedWidth = (power - width)/2;
		int croppedHeight = (power - height)/2;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				croppedHeightMap[i][j] = heightMap[i + croppedWidth][j + croppedHeight];
			}
		}
		Utils.normalize(croppedHeightMap);
//		for (int i = 0; i < width; i++) {
//			for (int j = 0; j < height; j++) {
//				heightMap[i][j] *= heightMap[i][j];
//			}
//		}
		return croppedHeightMap;
	}
	
	public static TileLoc makeMountain(Tile[][] world, double[][] heightMap) {
		
		int x0 = (int) (Math.random() * world.length);
		int y0 = (int) (Math.random() * world.length);
		
		int mountainSize = 80 * world.length / 256;
		
		double mountLength = (0.1 + 0.9 * Math.random() ) *mountainSize;
		double mountHeight = (0.1 + 0.9 * Math.random() )*mountainSize;
		double mountLengthEdge = mountLength+3;
		double mountHeightEdge = mountHeight+3;
		
		double snowMountLength = mountLength/4;
		double snowMountHeight = mountHeight/4;
		double snowMountLengthEdge = mountLength/3;
		double snowMountHeightEdge = mountHeight/3;
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[i].length; j++) {
				int dx = i - x0;
				int dy = j - y0;
				double mountain = (dx*dx)/(mountLength*mountLength) + (dy*dy)/(mountHeight*mountHeight);
				double mountainEdge = (dx*dx)/(mountLengthEdge*mountLengthEdge) + (dy*dy)/(mountHeightEdge*mountHeightEdge);
				
				double snowMountain = (dx*dx)/(snowMountLength*snowMountLength) + (dy*dy)/(snowMountHeight*snowMountHeight);
				double snowMountainEdge = (dx*dx)/(snowMountLengthEdge*snowMountLengthEdge) + (dy*dy)/(snowMountHeightEdge*snowMountHeightEdge);

				double ratio = Math.sqrt(dx*dx/mountLength/mountLength + dy*dy/mountHeight/mountHeight);
				//double ratio = dist / Math.max(mountLength, mountHeight);
				TileLoc p = new TileLoc(i, j);
				if(mountainEdge < 1 && Math.random()<rockEdgeRatio) {
					world[i][j].setTerrain(Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if(mountain < 1) {
					world[i][j].setTerrain(Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}
				
				
				
				
			}
		}
		return new TileLoc(x0, y0);
	}
	
	public static TileLoc makeVolcano(World world, double[][] heightMap) {
		int x = 0;
		int y = 0; 
		
		for(Tile t : world.getTilesRandomly()) {
//			if(t.getHeight() >= snowEdgeRatio) {
				x = t.getLocation().x;
				y = t.getLocation().y;
//			}
		}
		double lavaRadius = 5;
		double volcanoRadius = 10;
		double mountainRadius = 20;
		double mountainEdgeRadius = 23;
		
		for(Tile tile : world.getTiles()) {
			int i =  tile.getLocation().x;
			int j =  tile.getLocation().y;
			int dx = i - x;
			int dy = j - y;
			double distanceFromCenter = Math.sqrt(dx*dx + dy*dy);
			if(distanceFromCenter < mountainEdgeRadius) {
				
				double height = 1 - (lavaRadius - distanceFromCenter)/lavaRadius/2;
				if(distanceFromCenter > lavaRadius) {
					height = 1 - (distanceFromCenter - lavaRadius)/mountainEdgeRadius;
				}
				heightMap[i][j] = Math.max(height, heightMap[i][j]);
				
				if(distanceFromCenter < lavaRadius) {
					tile.setTerrain(Terrain.VOLCANO);
					tile.liquidType = LiquidType.LAVA;
					tile.liquidAmount = 0.2;
				}else if(distanceFromCenter < volcanoRadius) {
					tile.setTerrain(Terrain.VOLCANO);
				}else if(distanceFromCenter < mountainRadius) {
					tile.setTerrain(Terrain.ROCK);
				}else if(distanceFromCenter < mountainEdgeRadius && Math.random()<rockEdgeRatio) {
					tile.setTerrain(Terrain.ROCK);
				}
			}
		}
		return new TileLoc(x, y);
	}

	public static void genResources(World world) {
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
	private static void makeOreVein(Tile t, ResourceType resource, int veinSize) {
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
					potential.setResource(resource);
					veinSize--;
				}
			}
		}
		
		
		
	}

	private void oldMakeLake(int volume, Tile[][] world, double[][] heightMap) {
		
		// Fill tiles until volume reached
		PriorityQueue<TileLoc> queue = new PriorityQueue<TileLoc>((p1, p2) -> {
			return heightMap[p1.x][p1.y] - heightMap[p2.x][p2.y] > 0 ? 1 : -1;
		});
		boolean[][] visited = new boolean[world.length][world[0].length];
		queue.add(new TileLoc((int) (Math.random() * world.length), (int) (Math.random() * world[0].length)));
		while(!queue.isEmpty() && volume > 0) {
			TileLoc next = queue.poll();
			int i = next.x;
			int j = next.y;
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
			int i = next.x;
			int j = next.y;
			world.get(next).liquidAmount += 0.02;
			volume -= 0.02;
			// Add adjacent tiles to the queue
			for(Tile t : world.get(next).getNeighbors()) {
				if(!visited[t.getLocation().x][t.getLocation().y]) {
					queue.add(t.getLocation());
					visited[t.getLocation().x][t.getLocation().y] = true;
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
		world.spawnDragon();
	}
	
}
