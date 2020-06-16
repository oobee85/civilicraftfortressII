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
	
	public static void propogate(World world) {
		if(liquidAmountsTemp == null || liquidAmountsTemp.length != world.getWidth() || liquidAmountsTemp[0].length != world.getHeight()) {
			liquidAmountsTemp = new double[world.getWidth()][world.getHeight()];
		}
		if(liquidTypesTemp == null || liquidTypesTemp.length != world.getWidth() || liquidTypesTemp[0].length != world.getHeight()) {
			liquidTypesTemp = new LiquidType[world.getWidth()][world.getHeight()];
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
		for(Tile tile : world.getTilesRandomly()) {
			liquidAmountsTemp[tile.getLocation().x][tile.getLocation().y] = world[tile.getLocation()].liquidAmount;
			liquidTypesTemp[tile.getLocation().x][tile.getLocation().y] = world[tile.getLocation()].liquidType;
		}
//		for(int x = 0; x < world.getWidth(); x++) {
//			for(int y = 0; y < world.getHeight(); y++) {
//				liquidAmountsTemp[x][y] = world[new TileLoc(x, y)].liquidAmount;
//				liquidTypesTemp[x][y] = world[new TileLoc(x, y)].liquidType;
//			}
//		}
		
		for(Tile tile : world.getTiles()) {
			propogate(tile, world);
		}
		
		for(Tile tile : world.getTiles()) {
			int x = tile.getLocation().x;
			int y = tile.getLocation().y;
			tile.liquidAmount = Math.max(liquidAmountsTemp[x][y] * 0.9999 - 0.00001, 0);
			if(tile.liquidAmount == 0) {
				tile.liquidType = LiquidType.DRY;
			}
			else {
				tile.liquidType = liquidTypesTemp[x][y];
			}
			
			if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount > tile.liquidType.surfaceTension*2) {
				if(tile.checkTerrain(Terrain.GRASS) || tile.checkTerrain(Terrain.DIRT)) {
					tile.setTerrain(Terrain.BURNED_GROUND);
				}
				if(tile.checkTerrain(Terrain.SNOW)) {
					tile.setTerrain(Terrain.ROCK);
				}
			}
			if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount >= 0.0001) {
				tile.liquidAmount -= 0.0001;
			}
		}
		//Utils.normalize(heightMap);
	}
	private static void propogate(Tile tile, World world) {
		TileLoc current = tile.getLocation();
		int x = current.x;
		int y = current.y;
		int minX = Math.max(0, x - 1);
		int maxX = Math.min(world.getWidth()-1, x + 1);
		int minY = Math.max(0, y-1);
		int maxY = Math.min(world.getHeight()-1, y + 1);
		
		List<Tile> neighbors = Utils.getNeighbors(tile, world);
		for(Tile otherTile : neighbors) {
			TileLoc other = otherTile.getLocation();
			double myh = tile.getHeight();
			double myv = liquidAmountsTemp[x][y];
			LiquidType mytype = liquidTypesTemp[x][y];
			
			double oh = otherTile.getHeight();
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
						world[current].setHeight(world[current].getHeight() + changeh);
						world[other].setHeight(world[other].getHeight() - changeh);
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
						world[current].setHeight(world[current].getHeight() + combined);
						
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
