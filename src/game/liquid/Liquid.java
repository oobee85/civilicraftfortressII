package game.liquid;

import java.util.*;
import java.util.concurrent.*;

import game.*;
import utils.*;
import world.*;

public class Liquid {
	
	public static final float MINIMUM_LIQUID_THRESHOLD = 0.001f;
	
	private static final float FRICTION_RATIO = 0.99f;
	
	private static float[][] liquidAmountsTemp;
	private static LiquidType[][] liquidTypesTemp;
	
	
	private static final boolean MULTITHREADED = true;
	
	// idea: create constant arraylist of positions, initialize it once, then every time simply use a random permutation to access all elements randomly.
	
	public static void propogate(World world) {
		if(liquidAmountsTemp == null || liquidAmountsTemp.length != world.getWidth() || liquidAmountsTemp[0].length != world.getHeight()) {
			liquidAmountsTemp = new float[world.getWidth()][world.getHeight()];
		}
		if(liquidTypesTemp == null || liquidTypesTemp.length != world.getWidth() || liquidTypesTemp[0].length != world.getHeight()) {
			liquidTypesTemp = new LiquidType[world.getWidth()][world.getHeight()];
		}
		
//		float[] totals = new float[LiquidType.values().length];
//		for(int x = 0; x < world.length; x++) {
//			for(int y = 0; y < world.length; y++) {
//				for(int i = 0; i < totals.length; i++) {
//					if(world.get(x][y].liquidType == LiquidType.values()[i]) {
//						totals[i] += world.get(x][y].liquidAmount;
//					}
//				}
//			}
//		}
//		for(int i = 0; i < totals.length; i++) {
//			System.out.println("Total " + LiquidType.values()[i].name() + ": " + totals[i]);
//		}
		for(Tile tile : world.getTiles()) {
			liquidAmountsTemp[tile.getLocation().x()][tile.getLocation().y()] = world.get(tile.getLocation()).liquidAmount;
			liquidTypesTemp[tile.getLocation().x()][tile.getLocation().y()] = world.get(tile.getLocation()).liquidType;
		}
//		for(int x = 0; x < world.getWidth(); x++) {
//			for(int y = 0; y < world.getHeight(); y++) {
//				liquidAmountsTemp[x][y] = world.get(new TileLoc(x, y)].liquidAmount;
//				liquidTypesTemp[x][y] = world.get(new TileLoc(x, y)].liquidType;
//			}
//		}

		if(MULTITHREADED) {
			for(ArrayList<Tile> tiles : world.getLiquidSimulationPhases()) {
				int chunkSize = tiles.size()/world.getWidth();
				chunkSize = chunkSize<1 ? 1 : chunkSize;
				ArrayList<Future<?>> futures = new ArrayList<>();
				for(int chunkIndex = 0; chunkIndex < tiles.size(); chunkIndex+=chunkSize) {
					final int start = chunkIndex;
					final int end = Math.min(chunkIndex + chunkSize, tiles.size());
					Future<?> future = Utils.executorService.submit(() -> {
						for(int i = start; i < end; i++) {
							propogate(tiles.get(i), world);
						}
					});
					futures.add(future);
				}
				try {
					for(Future<?> future : futures) {
						future.get();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			for(Tile tile : world.getTiles()) {
				propogate(tile, world);
			}
		}

		for(Tile tile : world.getTiles()) {
			int x = tile.getLocation().x();
			int y = tile.getLocation().y();
			tile.liquidAmount = Math.max(liquidAmountsTemp[x][y] * 0.9999f - 0.00001f, 0);
			if(tile.liquidAmount == 0) {
				tile.liquidType = LiquidType.DRY;
			}
			else {
				tile.liquidType = liquidTypesTemp[x][y];
			}
			
			if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount > tile.liquidType.surfaceTension*2) {
				if(tile.getTerrain().isPlantable(tile.getTerrain())) {
					tile.setTerrain(Terrain.DIRT);
//					tile.setTerrain(Terrain.BURNED_GROUND);
				}
				if(tile.getModifier() != null && tile.getModifier().isCold()) {
					tile.getModifier().finish();
				}
			}
			double volume = tile.getAir().getVolume();
			if(tile.getWeather() != null) {
				volume += tile.getWeather().getStrength();
			}
//			tile.getAir().setVolume(volume);
			
			double evaporation = tile.getEvaporation();
			if(tile.liquidAmount - evaporation >= 0) {
				tile.getAir().addVolume(evaporation);
				tile.liquidAmount -= evaporation;
//				tile.getAir().addHumidity(evaporation);
			}
			
		}
		//Utils.normalize(heightMap);
	}
	private static void propogate(Tile tile, World world) {
		TileLoc current = tile.getLocation();
		int x = current.x();
		int y = current.y();
		
		float tempurature = tile.getTemperature();
		if(tempurature > Season.MELTING_TEMPURATURE) {
			if(tile.liquidType == LiquidType.ICE) {
				tile.liquidType = LiquidType.WATER;
				liquidTypesTemp[x][y] = LiquidType.WATER;
				liquidAmountsTemp[x][y] /= 4;
			}
			if(tile.liquidType == LiquidType.SNOW) {
				tile.liquidType = LiquidType.WATER;
				liquidTypesTemp[x][y] = LiquidType.WATER;
				liquidAmountsTemp[x][y] /= 4;
			}
		}
		else if(tempurature < Season.FREEZING_TEMPURATURE) {
			if(tile.liquidType == LiquidType.WATER) {
				tile.liquidType = LiquidType.ICE;
				liquidTypesTemp[x][y] = LiquidType.ICE;
			}
		}
		
		for(Tile otherTile : tile.getNeighbors()) {
			TileLoc other = otherTile.getLocation();
			LiquidType otype = liquidTypesTemp[other.x()][other.y()];
			if(otype.viscosity == 0) {
				continue;
			}
			float myh = tile.getHeight();
			if(tile.hasWall() == true) {
				myh += 10;
			}
			float myv = liquidAmountsTemp[x][y];
			LiquidType mytype = liquidTypesTemp[x][y];
			
			float oh = otherTile.getHeight();
			float ov = world.get(other).liquidAmount;
			
			if(myh + myv < oh + ov) {
				float delta = (oh + ov) - (myh + myv);
				float change = delta/2 * otype.viscosity;
				
				if(ov - change < 0) {
					change = ov;
				}
				if(otype == mytype || mytype == LiquidType.DRY || (otype.isWater && mytype.isWater)) {
					if(change < otype.selfSurfaceTension) { 
						change = 0;
					}
					// disabled erosion due to making it hard to parallelize
					// could probably fix by making height variable in Tile volatile
//					if(myh < oh) {
//						float deltah = oh - myh;
//						float changeh = deltah/2 * Math.min(change* FRICTION_RATIO, 1);
//						world.get(current).setHeight(world.get(current).getHeight() + changeh);
//						world.get(other).setHeight(world.get(other).getHeight() - changeh);
//					}
					liquidAmountsTemp[x][y] += change;
					if(mytype == LiquidType.DRY) {
						liquidTypesTemp[x][y] = otype;
					}
					
					liquidAmountsTemp[other.x()][other.y()] -= change;
					world.get(other).liquidAmount -= change;
				}
				else {
					// Subtractive combination
					if(		(otype.isWater && mytype == LiquidType.LAVA) ||
							(otype == LiquidType.LAVA && mytype.isWater)) {
						if(change < otype.surfaceTension) { 
							change = 0;
						}
						float combined = change;
						float extra = 0;
						if(myv - change < 0) {
							combined = myv;
							extra = change - combined;
						}
						world.get(current).setHeight(world.get(current).getHeight() + combined);
						
						if(extra == 0) {
							liquidAmountsTemp[x][y] -= combined;
							world.get(new TileLoc(x, y)).liquidAmount -= combined;
						}
						else {
							liquidAmountsTemp[x][y] = extra;
							liquidTypesTemp[x][y] = otype;
						}
						
						liquidAmountsTemp[other.x()][other.y()] -= change;
						world.get(other).liquidAmount -= change;
					}
				}
			}
		}
	}
}
