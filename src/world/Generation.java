package world;

import java.util.*;

import liquid.*;
import utils.*;

public class Generation {
	
	private static final double snowEdgeRatio = 0.5;
	private static final double rockEdgeRatio = 0.7;
	
	public static double[][] generateHeightMap(int smoothingRadius, int width, int height) {
		LinkedList<double[][]> noises = new LinkedList<>();

		for (int octave = 2; octave <= width; octave *= 2) {
			double[][] noise1 = new double[octave][octave];
			for (int i = 0; i < noise1.length; i++) {
				for (int j = 0; j < noise1[0].length; j++) {
					noise1[i][j] = Utils.getRandomNormal(5);
				}
			}
			noises.add(noise1);
		}

		double[][] combinedNoise = new double[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double rand = 0;
				int divider = width;
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
		Utils.normalize(heightMap);
		return heightMap;
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
				if(snowMountainEdge < 1 && Math.random()<snowEdgeRatio) {
					world[i][j] = new Tile(p, Terrain.SNOW);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if (snowMountain < 1 ) {
					world[i][j] = new Tile(p, Terrain.SNOW);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if(mountainEdge < 1 && Math.random()<rockEdgeRatio) {
					world[i][j] = new Tile(p, Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}else if(mountain < 1) {
					world[i][j] = new Tile(p, Terrain.ROCK);
					heightMap[i][j] = Math.max(1 - ratio*0.4, heightMap[i][j]);
				}
				
				
				
				
			}
		}
		return new TileLoc(x0, y0);
	}
	
	public static TileLoc makeVolcano(Tile[][] world, double[][] heightMap) {
		int x = (int) (Math.random() * world.length);
		int y = (int) (Math.random() * world.length);
		
		double lavaRadius = 5;
		double volcanoRadius = 10;
		double mountainRadius = 20;
		double mountainEdgeRadius = 23;
		
		for(int i = 0; i < world.length; i++) {
			for(int j = 0; j < world[i].length; j++) {
				int dx = i - x;
				int dy = j - y;
				double distanceFromCenter = Math.sqrt(dx*dx + dy*dy);
				TileLoc p = new TileLoc(i, j);
				if(distanceFromCenter < mountainEdgeRadius) {
					
					double height = 1 - (lavaRadius - distanceFromCenter)/lavaRadius/2;
					if(distanceFromCenter > lavaRadius) {
						height = 1 - (distanceFromCenter - lavaRadius)/mountainEdgeRadius*2;
					}
					heightMap[i][j] = Math.max(height, heightMap[i][j]);
					
					if(distanceFromCenter < lavaRadius) {
						world[i][j] = new Tile(p, Terrain.LAVA);
						world[i][j].liquidType = LiquidType.LAVA;
						world[i][j].liquidAmount = 0.2;
					}else if(distanceFromCenter < volcanoRadius) {
						world[i][j] = new Tile(p, Terrain.VOLCANO);
					}else if(distanceFromCenter < mountainRadius && world[i][j].checkTerrain(Terrain.SNOW) == false) {
						world[i][j] = new Tile(p, Terrain.ROCK);
					}else if(distanceFromCenter < mountainEdgeRadius && Math.random()<rockEdgeRatio) {
						world[i][j] = new Tile(p, Terrain.ROCK);
					}
				}
				
			}
		}
		return new TileLoc(x, y);
	}
}
