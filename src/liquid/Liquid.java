package liquid;

import java.util.*;

import utils.*;
import world.*;

public class Liquid {
	
	public static final double MINIMUM_LIQUID_THRESHOLD = 0.001;
	
	private static final double FRICTION_RATIO = 0.99;
	
	private static double[][] liquidAmountsTemp;
	private static LiquidType[][] liquidTypesTemp;

	
	
	// idea: create constant arraylist of positions, initialize it once, then every time simply use a random permutation to access all elements randomly.
	
	public static void propogate(Tile[][] world, double[][] heightMap) {
		if(liquidAmountsTemp == null || liquidAmountsTemp.length != world.length || liquidAmountsTemp[0].length != world[0].length) {
			liquidAmountsTemp = new double[world.length][world[0].length];
		}
		if(liquidTypesTemp == null || liquidTypesTemp.length != world.length || liquidTypesTemp[0].length != world[0].length) {
			liquidTypesTemp = new LiquidType[world.length][world[0].length];
		}
		LinkedList<TileLoc> tiles = new LinkedList<>();
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world.length; y++) {
				tiles.add(new TileLoc(x, y));
			}
		}
		
//		double[] totals = new double[LiquidType.values().length];
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world.length; y++) {
//				for(int i = 0; i < totals.length; i++) {
//					if(world[x][y].liquidType == LiquidType.values()[i]) {
//						totals[i] += world[x][y].liquidAmount;
//					}
//				}
				
				liquidAmountsTemp[x][y] = world[x][y].liquidAmount;
				liquidTypesTemp[x][y] = world[x][y].liquidType;
			}
		}
//		for(int i = 0; i < totals.length; i++) {
//			System.out.println("Total " + LiquidType.values()[i].name() + ": " + totals[i]);
//		}
		
		Collections.shuffle(tiles); 
		while(!tiles.isEmpty() ) {
			TileLoc pos = tiles.remove();
			int x = pos.x;
			int y = pos.y;
			propogate(x, y, world, heightMap);
		}
		
		for(int x = 0; x < world.length; x++) {
			for(int y = 0; y < world.length; y++) {
				world[x][y].liquidAmount = Math.max(liquidAmountsTemp[x][y] * 0.9999 - 0.00001, 0);
				
				world[x][y].liquidType = liquidTypesTemp[x][y];
				if(world[x][y].liquidType == LiquidType.LAVA && world[x][y].liquidAmount > world[x][y].liquidType.surfaceTension*2) {
					if(world[x][y].checkTerrain(Terrain.GRASS) ) {
						world[x][y].setTerrain(Terrain.DIRT);
					}
					if(world[x][y].checkTerrain(Terrain.SNOW)) {
						world[x][y].setTerrain(Terrain.ROCK);
					}
				}
				if(world[x][y].liquidType == LiquidType.WATER) {
					if(world[x][y].checkTerrain(Terrain.DIRT)) {
						if(Math.random() < world[x][y].liquidAmount*0.04) {
							world[x][y].setTerrain(Terrain.GRASS);
						}
					}
				}
			}
		}
		//Utils.normalize(heightMap);
	}
	private static void propogate(int x, int y, Tile[][] world, double[][] heightMap) {
		int minX = Math.max(0, x - 1);
		int maxX = Math.min(world.length-1, x + 1);
		int minY = Math.max(0, y-1);
		int maxY = Math.min(world[0].length-1, y + 1);

		LinkedList<TileLoc> tiles = new LinkedList<>();
		for(int i = minX; i <= maxX; i++) {
			for(int j = minY; j <= maxY; j++) {
				if(i == x || j == y) {
					if(i != x || j != y) {
						tiles.add(new TileLoc(i, j));
					}
				}
			}
		}
		Collections.shuffle(tiles); 
		
		while(!tiles.isEmpty()) {
			TileLoc other = tiles.remove();
			// Interaction between liquids happens here
			
			double myh = heightMap[x][y];
			double myv = liquidAmountsTemp[x][y];
			LiquidType mytype = liquidTypesTemp[x][y];
			
			double oh = heightMap[other.x][other.y];
			double ov = world[other.x][other.y].liquidAmount;
			LiquidType otype = liquidTypesTemp[other.x][other.y];
			
			if(myh + myv < oh + ov) {
				double delta = (oh + ov) - (myh + myv);
				double change = delta/2 * otype.viscosity;
				
				if(ov - change < 0) {
					change = ov;
				}
				
				if(otype == mytype) {
					if(myv < MINIMUM_LIQUID_THRESHOLD && change < otype.surfaceTension) { 
						change = 0;
					}
					if(myh < oh) {
						double deltah = oh - myh;
						double changeh = deltah/2 * Math.min(change* FRICTION_RATIO, 1);
						heightMap[x][y] += changeh;
						heightMap[other.x][other.y] -= changeh;
					}
					liquidAmountsTemp[x][y] += change;
					liquidTypesTemp[x][y] = otype;
					
					liquidAmountsTemp[other.x][other.y] -= change;
					world[other.x][other.y].liquidAmount -= change;
				}
				else {
					if(otype == LiquidType.WATER && mytype == LiquidType.LAVA ||
							otype == LiquidType.LAVA && mytype == LiquidType.WATER) {
						if(change < otype.surfaceTension) { 
							change = 0;
						}
						double combined = change;
						double extra = 0;
						if(myv - change < 0) {
							combined = myv;
							extra = change - combined;
						}
						if(heightMap[x][y] + combined > 1) {
							combined = 1 - heightMap[x][y];
							extra = change - combined;
						}
						heightMap[x][y] += combined;
						
						if(extra == 0) {
							liquidAmountsTemp[x][y] -= combined;
							world[x][y].liquidAmount -= combined;
						}
						else {
							liquidAmountsTemp[x][y] = extra;
							liquidTypesTemp[x][y] = otype;
						}
						
						liquidAmountsTemp[other.x][other.y] -= change;
						world[other.x][other.y].liquidAmount -= change;
					}
				}
			}
		}
	}
}
