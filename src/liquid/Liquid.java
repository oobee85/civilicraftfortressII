package liquid;

import java.util.*;

import utils.*;
import world.*;

public class Liquid {
	
	public static final double MINIMUM_LIQUID_THRESHOLD = 0.001;
	
	private static final double FRICTION_RATIO = 0.99;
	
	private static double[][] liquidAmountsTemp;
	private static LiquidType[][] liquidTypesTemp;
	private static LinkedList<TileLoc> tiles;

	
	
	// idea: create constant arraylist of positions, initialize it once, then every time simply use a random permutation to access all elements randomly.
	
	public static void propogate(World world) {
		if(liquidAmountsTemp == null || liquidAmountsTemp.length != world.getWidth() || liquidAmountsTemp[0].length != world.getHeight()) {
			liquidAmountsTemp = new double[world.getWidth()][world.getHeight()];
		}
		if(liquidTypesTemp == null || liquidTypesTemp.length != world.getWidth() || liquidTypesTemp[0].length != world.getHeight()) {
			liquidTypesTemp = new LiquidType[world.getWidth()][world.getHeight()];
		}
		if(tiles == null) {
			tiles = new LinkedList<>();
			for(int x = 0; x < world.getWidth(); x++) {
				for(int y = 0; y < world.getHeight(); y++) {
					tiles.add(new TileLoc(x, y));
				}
			}
		}
		
//		double[] totals = new double[LiquidType.values().length];
//		for(int x = 0; x < world.length; x++) {
//			for(int y = 0; y < world.length; y++) {
//				for(int i = 0; i < totals.length; i++) {
//					if(world[x][y].liquidType == LiquidType.values()[i]) {
//						totals[i] += world[x][y].liquidAmount;
//					}
//				}
//			}
//		}
//		for(int i = 0; i < totals.length; i++) {
//			System.out.println("Total " + LiquidType.values()[i].name() + ": " + totals[i]);
//		}
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				liquidAmountsTemp[x][y] = world[new TileLoc(x, y)].liquidAmount;
				liquidTypesTemp[x][y] = world[new TileLoc(x, y)].liquidType;
			}
		}
		
		Collections.shuffle(tiles); 
		for(TileLoc pos : tiles) {
			propogate(pos.x, pos.y, world);
		}
		
		for(int x = 0; x < world.getWidth(); x++) {
			for(int y = 0; y < world.getHeight(); y++) {
				Tile tile = world[new TileLoc(x, y)];
				tile.liquidAmount = Math.max(liquidAmountsTemp[x][y] * 0.9999 - 0.00001, 0);
				if(tile.liquidAmount == 0) {
					tile.liquidType = LiquidType.DRY;
				}
				else {
					tile.liquidType = liquidTypesTemp[x][y];
				}
				
				if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount > tile.liquidType.surfaceTension*2) {
					if(tile.checkTerrain(Terrain.GRASS) ) {
						tile.setTerrain(Terrain.DIRT);
					}
					if(tile.checkTerrain(Terrain.SNOW)) {
						tile.setTerrain(Terrain.ROCK);
					}
				}
				if(tile.liquidType == LiquidType.WATER) {
					if(tile.checkTerrain(Terrain.DIRT)) {
						if(Math.random() < tile.liquidAmount*0.04) {
							tile.setTerrain(Terrain.GRASS);
						}
					}
				}
			}
		}
		//Utils.normalize(heightMap);
	}
	private static void propogate(int x, int y, World world) {
		int minX = Math.max(0, x - 1);
		int maxX = Math.min(world.getWidth()-1, x + 1);
		int minY = Math.max(0, y-1);
		int maxY = Math.min(world.getHeight()-1, y + 1);

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
			
			double myh = world.heightMap[x][y];
			double myv = liquidAmountsTemp[x][y];
			LiquidType mytype = liquidTypesTemp[x][y];
			
			double oh = world.heightMap[other.x][other.y];
			double ov = world[other].liquidAmount;
			LiquidType otype = liquidTypesTemp[other.x][other.y];
			
			if(myh + myv < oh + ov) {
				double delta = (oh + ov) - (myh + myv);
				double change = delta/2 * otype.viscosity;
				
				if(ov - change < 0) {
					change = ov;
				}
				
				if(otype == mytype || mytype == LiquidType.DRY) {
					if(myv < MINIMUM_LIQUID_THRESHOLD && change < otype.surfaceTension) { 
						change = 0;
					}
					if(myh < oh) {
						double deltah = oh - myh;
						double changeh = deltah/2 * Math.min(change* FRICTION_RATIO, 1);
						world.heightMap[x][y] += changeh;
						world.heightMap[other.x][other.y] -= changeh;
					}
					liquidAmountsTemp[x][y] += change;
					liquidTypesTemp[x][y] = otype;
					
					liquidAmountsTemp[other.x][other.y] -= change;
					world[other].liquidAmount -= change;
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
						
//						if(heightMap[x][y] + combined > 1) {
//							combined = 1 - heightMap[x][y];
//							extra = change - combined;
//						}
//						heightMap[x][y] += combined;
						
						if(world.heightMap[x][y] + combined > 1) {
							world.heightMap[x][y] = 1;
						}
						
						if(extra == 0) {
							liquidAmountsTemp[x][y] -= combined;
							world[new TileLoc(x, y)].liquidAmount -= combined;
						}
						else {
							liquidAmountsTemp[x][y] = extra;
							liquidTypesTemp[x][y] = otype;
						}
						
						liquidAmountsTemp[other.x][other.y] -= change;
						world[other].liquidAmount -= change;
					}
				}
			}
		}
	}
}