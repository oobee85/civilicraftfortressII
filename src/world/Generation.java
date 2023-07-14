package world;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import game.*;
import utils.*;
import world.liquid.*;

public class Generation {
	
	public static final long DEFAULT_SEED = 131313131313131313L;
	public static final int OREMULTIPLIER = 16384;

	public static void addCliff(World world, float[][] heightmap) {
		int x = (int)(Math.random()*heightmap.length);
		int y = (int)(Math.random()*heightmap[x].length);
		
		for(int i = 0; i < 20 && x + i < heightmap.length; i++) {
			int yy = y + i/3;
			if(yy - 1 >= 0 && yy < heightmap[x+i].length) {
				heightmap[x + i][yy] /= 2;
				world.get(new TileLoc(x + i, yy)).setTerrain(Terrain.ROCK);
			}
		}
		
	}

	public static double[][] generateMap2(
			long seed,
			int width,
			int height,
			int scale,
			int minValue,
			int maxValue) {
		int numoctaves = 4;
		double amplitude = 1;
		int frequency = 1;
		double[][] map = new double[height][width];
		double scaleMult = 1.0/scale;
		for(int octave = 0; octave < numoctaves; octave++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					map[y][x] += amplitude * OpenSimplex2S.noise2(seed*octave, x*scaleMult*frequency, y*scaleMult*frequency);
				}
			}
			frequency *= 2;
			amplitude *= 0.5;
		}
		Utils.normalize(map, maxValue, minValue);
		return map;
	}

	public static float[][] generateHeightMap(long seed, int width, int height) {
		
//		int numoctaves = 6;
//		double amplitude = 1;
//		int frequency = 1;
//		float[][] heightmap = new float[height][width];
//		for(int octave = 0; octave < numoctaves; octave++) {
//			for (int y = 0; y < height; y++) {
//				for (int x = 0; x < width; x++) {
//					double nx = 1.0*x / width + 1;
//					double ny = 1.0*y / height;
//					float value = (float)(amplitude * OpenSimplex2S.noise2( seed + octave, frequency*nx, frequency*ny));
//					heightmap[y][x] += value;
//				}
//			}
//			frequency *= 2;
//			amplitude *= 0.5;
//		}
		double[][] basic = generateMap2(
				seed, width, height, 50,
				0, 1);

		double[][] erosionMap = generateMap2(
				seed + 12345678,width, height, 200,
				0, 1);
		double[][] continentalMap = generateMap2(
				seed + 9876123, width, height, 100,
				0, 1);

		saveMap(basic, "basic.png");
		saveMap(erosionMap, "erosionMap.png");
		saveMap(continentalMap, "continentalMap.png");

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (continentalMap[y][x] < .25) {
					continentalMap[y][x] = .25;
				}
				else if (continentalMap[y][x] < .5) {
					continentalMap[y][x] = .5;
				}
				else if (continentalMap[y][x] < .75) {
					continentalMap[y][x] = .75;
				}
				else {
					continentalMap[y][x] = 1;
				}
				
//				erosionMap[y][x] = erosionMap[y][x] * erosionMap[y][x];
//				erosionMap[y][x] = erosionMap[y][x] > 0.5 ? 1 : 0;
			}
		}
		saveMap(erosionMap, "erosionMap2.png");
		saveMap(continentalMap, "continentalMap2.png");

		float[][] heightmap = new float[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double h = basic[y][x] * continentalMap[y][x];
				h = (h*h) * (erosionMap[y][x]) + (h) * (1 - erosionMap[y][x]);
//				if (erosionMap[y][x] > 0.5) {
//					h = h*h;
//				}
				heightmap[y][x] = (float) h;
			}
		}
		
		
		saveMap(heightmap, "heightmap.png");
//		saveImage(heightmap, "map.png");
//		maps.add(heightmap);
//		saveImageChain(maps, "octaves.png");
		return heightmap;
	}

	public static void saveMap(float[][] map, String filename) {
		double[][] map2 = new double[map.length][map[0].length];
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				map2[y][x] = map[y][x];
			}
		}
		saveMap(map2, filename);
	}

	public static void saveMap(double[][] map, String filename) {
		double low = map[0][0];
		double high = map[0][0];
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				low = Math.min(low, map[y][x]);
				high = Math.max(high, map[y][x]);
			}
		}
		Color[] colors = new Color[] {
				new Color(255 * 0/5, 255 * 0/5, 255 * 0/5),
				new Color(255 * 1/5, 255 * 1/5, 255 * 1/5),
				new Color(255 * 2/5, 255 * 2/5, 255 * 2/5),
				new Color(255 * 3/5, 255 * 3/5, 255 * 3/5),
				new Color(255 * 4/5, 255 * 4/5, 255 * 4/5),
				new Color(255 * 5/5, 255 * 5/5, 255 * 5/5),
				new Color(255 * 5/5, 255 * 5/5, 255 * 5/5),
		};
		BufferedImage image = new BufferedImage(map[0].length, map.length, BufferedImage.TYPE_BYTE_GRAY);

		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				double ratio = (double) (map[y][x] - low) / (high - low);
				Color c = colors[(int)(ratio * (colors.length - 1))];
				image.setRGB(x, y, c.getRGB());
			}
		}
		try {
			ImageIO.write(image, "png", new File("maps/" + filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static float[][] generateHeightMap(long seed, int smoothingRadius, int width, int height) {
		int power = 1;
		while(power < width || power < height) {
			power *= 2;
		}
		
		float[][] heightMap = generateHeightMap(seed, power, power);
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
		float highest = -1000;
		for(int i = 0; i < world.getWidth(); i++) {
			for(int j = 0; j < world.getHeight(); j++) {
				if(heightMap[i][j] > highest) {
					highest = heightMap[i][j];
				}
			}
		}
		highest = highest*0.6f;
		int x = world.getWidth()/2;
		int y = world.getHeight()/2;
		float lavaRadius = 5;
		float volcanoRadius = 10;
		float mountainRadius = 20;
		float mountainEdgeRadius = 30;
		
		for(Tile tile : world.getTiles()) {
			int i =  tile.getLocation().x();
			int j =  (int) (tile.getLocation().y() + (i%2)*0.5f);
			int dx = i - x;
			int dy = j - y;
			float distanceFromCenter = (float) Math.sqrt(dx*dx + dy*dy);
			if(distanceFromCenter < mountainEdgeRadius) {
				
				if(distanceFromCenter > lavaRadius) {
					float height = highest - highest*(distanceFromCenter - lavaRadius)/(mountainEdgeRadius-lavaRadius);
					heightMap[i][j] = Math.max(height, heightMap[i][j]);
				}
				else {
					float height = highest - highest*(lavaRadius - distanceFromCenter)/lavaRadius;
					heightMap[i][j] = Math.max(height, heightMap[i][j]);
				}
				
				if(distanceFromCenter < lavaRadius) {
					tile.setTerrain(Terrain.VOLCANO);
					tile.liquidType = LiquidType.LAVA;
					tile.liquidAmount = 0.2f;
				}else if(distanceFromCenter < volcanoRadius * .9) {
					tile.setTerrain(Terrain.VOLCANO);
				}
			}
		}
		return new TileLoc(x, y);
	}

	public static void generateResources(World world, Random rand) {
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
						makeOreVein(tile, resource, resource.getVeinSize(), rand);
						numVeins --;
					}
				}
				
				
			}
		}
		// scatter some random rocks around
		int numRegions = 5;
		int regionWidth = world.getWidth()/numRegions;
		int regionHeight = world.getHeight()/numRegions;
		int maxRadius = 2;
		for(int xdiv = 0; xdiv < numRegions; xdiv++) {
			for(int ydiv = 0; ydiv < numRegions; ydiv++) {
				List<Tile> regionTiles = Utils.getTilesBetween(
						world, 
						new Position(xdiv*regionWidth, ydiv*regionHeight), 
						new Position(xdiv*regionWidth + regionWidth, ydiv*regionHeight + regionHeight));
				Collections.shuffle(regionTiles, rand);
				Tile targetTile = regionTiles.get(0);
				int radius = (int) (rand.nextDouble()*(maxRadius + 1));
				List<Tile> targetTiles = Utils.getTilesInRadius(targetTile, world, radius);
				for(Tile tile : targetTiles) {
					if(tile.getTerrain() == Terrain.DIRT || tile.getTerrain() == Terrain.GRASS) {
						if(rand.nextDouble() < 0.75) {
							tile.setTerrain(Terrain.ROCK);
						}
					}
				}
			}
		}
	}
	
	public static void makeBiome(Tile tile, Terrain newTerrain, int size, int maxHeightDifference, Terrain toReplace[], Random rand) {
		HashMap<Tile, Double> visited = new HashMap<>();
		List<Terrain> replaceable = new LinkedList<Terrain>();
		for(Terrain terr: toReplace) {
			replaceable.add(terr);
		}
		
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
		visited.put(tile, 0.0);
		search.add(tile);
		
		while(size > 0 && !search.isEmpty()) {
			Tile potential = search.poll();
			
			for(Tile ti : potential.getNeighbors()) {
				if(visited.containsKey(ti)) {
					continue;
				}
				visited.put(ti, ti.getLocation().distanceTo(tile.getLocation()) + rand.nextDouble()*10);
				search.add(ti);
			}
			if(Math.abs(tile.getHeight() - potential.getHeight()) < maxHeightDifference) {
				if(replaceable.contains(potential.getTerrain())) {
					potential.setTerrain(newTerrain);
					size--;
				}
			}
			
			
		}
		
		
		
	}

	
	public static void makeOreVein(Tile t, ResourceType resource, int veinSize, Random rand) {
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
				visited.put(ti, ti.getLocation().distanceTo(t.getLocation()) + rand.nextDouble()*10);
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

	public static void makeLake(double volume, World world, Random rand) {
		// Fill tiles until volume reached
		PriorityQueue<TileLoc> queue = new PriorityQueue<TileLoc>((p1, p2) -> {
			return (world.get(p1).getHeight() + world.get(p1).liquidAmount) - (world.get(p2).getHeight() + world.get(p2).liquidAmount) > 0 ? 1 : -1;
		});
		TileLoc startingLoc = new TileLoc((int) (rand.nextDouble() * world.getWidth()), (int) (rand.nextDouble() * world.getHeight()));
		queue.add(startingLoc);
		while(!queue.isEmpty() && volume > 0) {
			TileLoc next = queue.poll();
			Tile tile = world.get(next);
			tile.liquidAmount += 100;
			volume -= 100;
			queue.add(next);
			for(Tile neighbor : tile.getNeighbors()) {
				queue.add(neighbor.getLocation());
			}
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
