package world.air;

import java.util.*;
import java.util.concurrent.*;

import game.*;
import utils.*;
import world.*;
import world.liquid.*;

public class AirSimulation {
	
	private interface SimulationTask {
		void task(Tile t);
	}
	
	private static void simulationWork(World world, List<Tile> tilesRandomOrder, SimulationTask task) {
		if (!Settings.AIR_MULTITHREADED) {
			for(Tile tile : tilesRandomOrder) {
				task.task(tile);
			}
			return;
		}

		for(ArrayList<Tile> tiles : world.getLiquidSimulationPhases()) {
			int chunkSize = Math.max(1, tiles.size()/world.getWidth());
			ArrayList<Future<?>> futures = new ArrayList<>();
			for(int chunkIndex = 0; chunkIndex < tiles.size(); chunkIndex+=chunkSize) {
				final int start = chunkIndex;
				final int end = Math.min(chunkIndex + chunkSize, tiles.size());
				Future<?> future = Utils.executorService.submit(() -> {
					for(int i = start; i < end; i++) {
						task.task(tiles.get(i));
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
	
	private static void averagingHelper(Tile tile, AverageValues avg) {
//		if(Settings.DEBUG_PRINT_AIR_ENERGY && tile.liquidAmount < 0) {
//			System.out.println("AAAAAAAAAAAAAA NEGATIVE LIQUIDS\n");
//		}
//		if(Settings.DEBUG_PRINT_AIR_ENERGY && tile.getAir().getVolumeLiquid() < 0) {
//			System.out.println(" NEGATIVE air LIQUIDS");
//		}
		avg.temp += tile.getTemperature();
		if(tile.liquidType == LiquidType.WATER 
				|| tile.liquidType == LiquidType.ICE ) {
			avg.water += tile.liquidAmount;
		}
		if(tile.liquidType == LiquidType.SNOW) {
			avg.water += tile.liquidAmount/2;
		}
		avg.water += tile.getAir().getVolumeLiquid();
		
		if(tile.getPlant() != null && tile.getPlant().getType() == Game.plantTypeMap.get("BERRY")) {
			avg.berries += 1;
		}
		if(tile.getPlant() != null && tile.getPlant().getType() == Game.plantTypeMap.get("TREE")) {
			avg.tree += 1;
		}
	}

	private static AverageValues computeAverageValues(World world) {
		AverageValues avg = new AverageValues();
		if (!Settings.AIR_MULTITHREADED) {
			for(Tile tile : world.getTiles()) {
				averagingHelper(tile, avg);
			}
		}
		else {
			int NUM_THREADS = 4;
			ArrayList<Tile> tiles = world.getTileArray();
			int chunkSize = tiles.size()/NUM_THREADS;
			ArrayList<Future<?>> futures = new ArrayList<>();
			AverageValues[] avgs = new AverageValues[NUM_THREADS];
			for (int chunk = 0; chunk < NUM_THREADS; chunk++) {
				final int index = chunk;
				avgs[index] = new AverageValues();
				final int start = chunk * chunkSize;
				final int end = Math.min((chunk + 1) * chunkSize, tiles.size());
				Future<?> future = Utils.executorService.submit(() -> {
					for(int i = start; i < end; i++) {
						averagingHelper(tiles.get(i), avgs[index]);
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
			for (int i = 0; i < avgs.length; i++) {
				avg.temp += avgs[i].temp;
				avg.water += avgs[i].water;
				avg.berries += avgs[i].berries;
				avg.tree += avgs[i].tree;
				
			}
		}
		avg.temp /= world.getTiles().size();
		avg.water /= world.getTiles().size();
	
		return avg;
	}
	
	public static void doAirSimulationStuff(World world, List<Tile> tilesRandomOrder, int width, int height) {
		
		simulationWork(world, tilesRandomOrder, (tile) -> {
			tile.updateAir();
			tile.updateEnergyToTemperature();
		});
		
		AverageValues avg = computeAverageValues(world);
		
		simulationWork(world, tilesRandomOrder, (tile) -> {
			AirSimulation.updateEnergy(tile, avg.temp, avg.water, avg.berries, avg.tree);
			tile.updateEnergyToTemperature();
			AirSimulation.blackBodyRadiation(tile);
			tile.updateEnergyToTemperature();
		});

		AirSimulation.updateAirMovement(tilesRandomOrder, width, height);
		simulationWork(world, tilesRandomOrder, (tile) -> {
			tile.updateEnergyToTemperature();
		});
	}
	
	public static void blackBodyRadiation(Tile tile) {
		Air air = tile.getAir();
		// Q = o(T1 - T2) * A
		// o = 5.670374419 � 10^-8 W*m-2*K-4
		double end = 0;
		double deltaT = (tile.getTemperature() - Constants.KELVINOFFSET) - (air.getTemperature() - Constants.KELVINOFFSET);
		end = Constants.BOLTZMANNMODIFIED * Constants.VOLUMEPERTILE * deltaT * 750;
		air.addEnergy(end);
		tile.addEnergy(-1*end);
	}

	public static void updateEnergyToTemperature(LinkedList<Tile> tiles) {
		for(Tile tile : tiles) {
			tile.updateEnergyToTemperature();
		}
	}
	
	public static void updateEnergy(Tile tile, double averageTemp, double averageWater, double avgBerry, double avgTree) {
			
			blackBodyRadiation(tile);
//			updateEnergyToTemperature(tile);
			
			
			//adds energy for water
//			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()) {
//				double modifier = 1 - (tile.getTemperature()/50);
//				tile.addEnergy(Math.log(Math.sqrt(Math.sqrt(tile.liquidAmount * modifier))));
//			}
			
			
			float seasonEnergy = Seasons.getRateEnergy();
			double addedEnergy = 0;
			
			
			// reduce energy if more than balancetemp
			if(tile.getTemperature() >= (Constants.BALANCETEMP + 15)) {
				double modifier = (tile.getTemperature()/Constants.BALANCETEMP);
				double heightRatio = tile.getHeight() / Constants.MAXHEIGHT;
				addedEnergy -= (Math.sqrt(Math.abs(heightRatio * modifier)));
//				System.out.println(addedEnergy);
			}
			if(tile.getTemperature() > (Constants.BALANCETEMP + 20) || tile.getAir().getTemperature() > (Constants.BALANCETEMP + 20)) {
				double modifier = (tile.getTemperature()/Constants.BALANCETEMP) + 1;
				if(modifier > 3) {
					modifier = 3;
				}
				addedEnergy -= modifier;
			}
			
			// add energy if less than balancetemp
			if(tile.getTemperature() <= (Constants.BALANCETEMP - 15)) {
				double modifier = (tile.getTemperature()/Constants.BALANCETEMP);
				double heightRatio = tile.getHeight() / Constants.MAXHEIGHT;
				addedEnergy += (Math.sqrt(Math.abs(heightRatio * modifier)));
//				System.out.println(addedEnergy);
			}
			if(tile.getTemperature() < (Constants.BALANCETEMP - 20) || tile.getAir().getTemperature() < (Constants.BALANCETEMP - 20)) {
				double modifier = (tile.getTemperature()/Constants.BALANCETEMP) + 1;
				if(modifier > 3) {
					modifier = 3;
				}
				addedEnergy += modifier;
			}
			
			
			// height above 700 gets reduced energy
			if(tile.getHeight() >= Constants.MAXHEIGHT /1.43) {
//				double modifier = (tile.getTemperature()/World.BALANCETEMP);
				double heightRatio = tile.getHeight() / Constants.MAXHEIGHT;
				addedEnergy -= heightRatio*2;
			}
			
			// add some energy for sand
			if(tile.getTerrain() == Terrain.SAND) {
				addedEnergy += 0.5;
			}
			
			
//			int groundModEnergy = 0;
			//adds energy for ground modifiers
			GroundModifier gm = tile.getModifier();
			if(gm != null && gm.isHot()) {
				double mod = gm.timeLeft() / 500;
				addedEnergy += (mod);
			}
			
			//adds energy for lava
			if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()) {
//				double modifier = 1 - (tile.getTemperature()/MAXTEMP);
				double modifier = tile.liquidAmount / tile.getEnergy() * 10000;
				addedEnergy += modifier;
			}
			
			// reduces energy gain linearly after 2h/maxHeight - 1
			// i.e. at 500:0, at 1000:1
			double heightMod = 2 * tile.getHeight() / Constants.MAXHEIGHT - 1;
			addedEnergy = addedEnergy - heightMod;
//			seasonEnergy *= heightMod;
			
			//removes energy for ice and snow reflecting sun
			if(tile.liquidType == LiquidType.ICE || tile.liquidType == LiquidType.ICE) {
				double modifier = tile.liquidAmount / 10;
				if(modifier > 2) {
					modifier = 2;
				}
				addedEnergy -= modifier;
//				tile.addEnergy(-Math.log(Math.sqrt(Math.sqrt(tile.liquidAmount * modifier))));
			}
			
			//evaporative cooling
			double evaporation = tile.getEvaporation();
			seasonEnergy -= evaporation;
			
			
			double humidity = tile.getAir().getHumidity();
			double airloss = humidity * (-2*seasonEnergy);
//			seasonEnergy += airloss;
			
			
			double vol = tile.getAir().getVolumeLiquid();
			double maxVol = tile.getAir().getMaxVolumeLiquid();
			boolean isSnow = false;
			
			if(tile.getAir().getVolumeLiquid() < 0) {
				tile.getAir().setVolumeLiquid(0);
			}
			//does raining
			if(tile.getAir().canRain() && tile.liquidType != LiquidType.LAVA) {
				if(tile.liquidType != LiquidType.ICE && tile.liquidType != LiquidType.SNOW && tile.getTemperature() >= Constants.FREEZETEMP) {
					tile.liquidType = LiquidType.WATER;
				}else if(tile.liquidType != LiquidType.WATER && tile.liquidType != LiquidType.ICE && tile.getTemperature() < Constants.FREEZETEMP && tile.getAir().getTemperature() < Constants.FREEZETEMP) {
					tile.liquidType = LiquidType.SNOW;
					isSnow = true;
				}
				
				
				double amount = 0.1 * vol / maxVol;
				// if too much liquid on the ground, cant rain
//				if(tile.liquidAmount >= 5) {
//					amount = 0;
//				}else 
				if(tile.getAir().getVolumeLiquid() - amount >= 0){
					tile.getAir().addVolumeLiquid(-amount);
					tile.liquidAmount += amount;
					// snow takes up twice the volume of other liquids
					if(isSnow == true) {
						tile.liquidAmount += amount;
					}
	//				seasonEnergy += 0.01;
				}
			}
			
			// force add humidity on mountain top
			if(tile.getHeight() > 500 && averageWater < Constants.BALANCEWATER) {
				double addedMod = Constants.BALANCEWATER / averageWater/4;
//				tile.liquidAmount += addedMod/2;
				tile.getAir().addVolumeLiquid(addedMod);
			}
			
//			double tempChange = averageTemp / tile.getTemperature();
//			if(tempChange < 1 && tile.liquidType != LiquidType.LAVA) {
//				if(tempChange < 0.25) {
//					tempChange *= -1;
//				}else {
//					addedEnergy *= tempChange;
//				}
//				
//			}
			
			// adds some randomness into energy distribution
//			if(Math.random() > 0.01) {
//				boolean addEnergy = false;
//				boolean stop = false;
//				double num = Math.random();
//				if(num > 0.5) {
//					addEnergy = true;
//					seasonEnergy *= 2;
//				}else if(num < 0.5) {
//					addEnergy = false;
//					seasonEnergy *= -2;
//				}else {
//					stop = true;
//				}
//				if(stop != true) {
//					for(Tile t: tile.getNeighbors()) {
//						t.addEnergy(seasonEnergy);
//					}
//				}
//				
//			}else {
//				tile.addEnergy(seasonEnergy);
//			}
			
			tile.updateEnergyToTemperature();
//			blackBodyRadiation();
			
//			tile.addEnergy(seasonEnergy);
//			tile.addEnergy(addedEnergy);
//			if(tile.getTemperature() >= 30) {
//				seasonEnergy = 0;
//			}
			tile.getAir().addEnergy(seasonEnergy * 3/4);
			tile.getAir().addEnergy(addedEnergy * 3/4);
			tile.addEnergy(seasonEnergy/4);
			tile.addEnergy(addedEnergy/4);
			
			tile.updateEnergyToTemperature();
			
			if(Settings.DEBUG_PRINT_AIR_ENERGY 
					&& tile.getLocation().x() == 5 
					&& tile.getLocation().y() == 5 
					&& World.ticks % 100 == 1) {
//				tile.setEnergy(21000);
//				System.out.println(tile.getTemperature());
				System.out.println("Ticks: "+ World.ticks + ", uT: " + averageTemp + ", uW: " + averageWater + ", uBerry" + avgBerry + ", uTree" + avgTree);
			}
//			tile.setEnergy(energy);
//			tile.addEnergy(joules);
		
	}
	
	public static void updateAirMovement(List<Tile> tiles, int worldWidth, int worldHeight) {
		if(World.ticks % Constants.TICKSTOUPDATEAIR == 0) {
			return;
		}
		double totalMass = 0;
		double [][] pressureTemp = new double[worldWidth][worldHeight];
		double [][] volumeTemp = new double[worldWidth][worldHeight];
//		double [][] humidityTemp = new double[worldWidth][worldHeight];
		double [][] energyTemp = new double[worldWidth][worldHeight];
		for(Tile t: tiles) {
			pressureTemp[t.getLocation().x()][t.getLocation().y()] = t.getAir().getPressure();
			volumeTemp[t.getLocation().x()][t.getLocation().y()] = t.getAir().getVolumeLiquid();
//			humidityTemp[t.getLocation().x()][t.getLocation().y()] = t.getAir().getVolumeLiquid();
			energyTemp[t.getLocation().x()][t.getLocation().y()] = t.getAir().getEnergy();
		}
		
		for(Tile tile: tiles) {
			TileLoc tileLoc = tile.getLocation();
			Air tileAir = tile.getAir();
			tileAir.setFlowDirection(Direction.NONE);
			
			int transferred = 0;
			
			
			for(Tile otherTile : tile.getNeighbors()) {
				TileLoc otherLoc = otherTile.getLocation();
				Air otherAir = otherTile.getAir();
				
				double mypress = tileAir.getPressure();
				double myvolume = tileAir.getVolumeLiquid();
				double myenergy = tileAir.getEnergy();
				double mymaxvolume = tileAir.getMaxVolumeLiquid();
				
				double opress = otherAir.getPressure();
				double ovolume = otherAir.getVolumeLiquid();
				double oenergy = otherAir.getEnergy();
				double omaxvolume = otherAir.getMaxVolumeLiquid();

				
				
				// PREVENTS AIRFLOW DIRECTIONS FROM CHANGING RAPIDLY
				
				Direction oldFlow = tileAir.getFlowDirection();
				Direction attemptFlow = Direction.getDirection(tileLoc, otherLoc);
//				double directionValue = Math.abs(oldFlow.deltay() + attemptFlow.deltay());
				
				
				// IF CONDITIONS MET FOR TRANSFER TO OTHER TILE
				if(mypress > opress ) {
					double deltaE = (double) ((myenergy - oenergy)) /(tile.getNeighbors().size()/2 +2);
					transferred += 1;

					tileAir.setFlowDirection(attemptFlow);
					energyTemp[otherLoc.x()][otherLoc.y()] += deltaE;
					energyTemp[tileLoc.x()][tileLoc.y()] -= deltaE;
					double deltaVol = (double)Math.abs(myvolume - ovolume) /(tile.getNeighbors().size()/2 +2);
//					if (myvolume - deltaVol >= 0 && ovolume - deltaVol >= 0) {
						
					if (myvolume - deltaVol >= 0 && ovolume + deltaVol < omaxvolume && ovolume - deltaVol >= 0 && myvolume + deltaVol < mymaxvolume) {
						volumeTemp[otherLoc.x()][otherLoc.y()] += deltaVol;
						volumeTemp[tileLoc.x()][tileLoc.y()] -= deltaVol;
//						break;
					}
				}
				
				if (transferred >= 3) { // stops air from being transferred to multiple tiles
//					continue;
					break;
				}

			}
		}

		for(Tile t: tiles) {
			Air air = t.getAir();
			air.setEnergy(energyTemp[t.getLocation().x()][t.getLocation().y()]);
//			t.setHumidity(energyTemp[t.getLocation().x()][t.getLocation().y()]);
			air.setVolumeLiquid(volumeTemp[t.getLocation().x()][t.getLocation().y()]);
//			t.setEnergy(energyTemp[t.getLocation().x()][t.getLocation().y()]);
//			totalMass += t.getAir().getVolumeLiquid();
		}
//		System.out.println(totalMass);
	}
	
	
	
}
