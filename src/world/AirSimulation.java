package world;

import java.util.*;

import game.*;
import game.liquid.*;
import utils.*;

public class AirSimulation {
	
	public static void updateAirStuff(LinkedList<Tile> tiles) {
		for(Tile tile : tiles) {
//			Air air = tile.getAir();
//			air.updateHeight(tile.getHeight());
//			air.updateMaxVolume();
//			air.updateHumidity();
//			air.updatePressure();

			tile.updateAir();
//			tile.updateAtmosphere();
		}
	}
	
	public static void blackBodyRadiation(LinkedList<Tile> tiles) {
		for(Tile tile : tiles) {
			Air air = tile.getAir();
			// Q = o(T1 - T2) * A
			// o = 5.670374419 × 10^-8 W*m-2*K-4
			double end = 0;
			double deltaT = (tile.getTemperature() - World.KELVINOFFSET) - (air.getTemperature() - World.KELVINOFFSET);
			end = World.BOLTZMANNMODIFIED * World.VOLUMEPERTILE * deltaT * 750;
			air.addEnergy(end);
			tile.addEnergy(-1*end);
		}
	}

	public static void updateEnergyToTemperature(LinkedList<Tile> tiles) {
		for(Tile tile : tiles) {
			tile.updateEnergyToTemperature();
		}
	}
	
	public static void updateEnergy(LinkedList<Tile> tiles) {
//		if(World.ticks % TICKSTOUPDATEAIR == 0) {
//			return;
//		}
	
		double averageWater = 0;
		double averageTemp = 0;
		for(Tile t : tiles) {
			averageTemp += t.getTemperature();
			if(t.liquidType == LiquidType.WATER || t.liquidType == LiquidType.ICE || t.liquidType == LiquidType.SNOW) {
				averageWater += t.liquidAmount;
			}
			averageWater += t.getAir().getVolumeLiquid();
			
		}
		averageTemp /= tiles.size();
		averageWater /= tiles.size();;
		for(Tile tile : tiles) {
			if(tile == null) {
				System.out.println("null tile when updating energy");
				continue;
			}
//			blackBodyRadiation();
//			updateEnergyToTemperature(tile);
			//adds energy for lava
			if(tile.liquidType == LiquidType.LAVA && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()) {
//				double modifier = 1 - (tile.getTemperature()/MAXTEMP);
				double addedEnergy = tile.liquidAmount / tile.getEnergy() * 10000;
				tile.addEnergy(addedEnergy);
			}
			
			
			//adds energy for water
//			if(tile.liquidType == LiquidType.WATER && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()) {
//				double modifier = 1 - (tile.getTemperature()/50);
//				tile.addEnergy(Math.log(Math.sqrt(Math.sqrt(tile.liquidAmount * modifier))));
//			}
			
			//removes energy for ice
//			if(tile.liquidType == LiquidType.ICE && tile.liquidAmount >= tile.liquidType.getMinimumDamageAmount()) {
//				double modifier = 1 - (tile.getTemperature()/100);
//				tile.addEnergy(-Math.log(Math.sqrt(Math.sqrt(tile.liquidAmount * modifier))));
//			}
			float seasonEnergy = Seasons.getRateEnergy();
			double addedEnergy = 0;
			
			
			if(tile.getTemperature() <= (World.BALANCETEMP + 5)) {
				double modifier = -1 * (tile.getTemperature()/World.BALANCETEMP);
				double heightRatio = tile.getHeight() / World.MAXHEIGHT;
				addedEnergy -= (Math.sqrt(Math.abs(heightRatio * modifier)));
//				System.out.println(addedEnergy);
			}
			if(tile.getTemperature() > (World.BALANCETEMP + 25) || tile.getAir().getTemperature() > (World.BALANCETEMP + 25)) {
				seasonEnergy -= 1;
			}
			if(tile.getTemperature() <= (World.BALANCETEMP - 5)) {
				double modifier = 1 - (tile.getTemperature()/World.BALANCETEMP);
				double heightRatio = tile.getHeight() / World.MAXHEIGHT;
				addedEnergy -= (modifier*Math.sqrt(Math.abs(heightRatio)));
//				System.out.println(addedEnergy);
			}
			if(tile.getTemperature() < (World.BALANCETEMP - 20) || tile.getAir().getTemperature() < (World.BALANCETEMP - 20)) {
				seasonEnergy += 1;
			}
			
			
//			int groundModEnergy = 0;
			//adds energy for ground modifiers
			GroundModifier gm = tile.getModifier();
			if(gm != null && gm.isHot()) {
				double mod = gm.timeLeft() / 500;
				addedEnergy += (mod);
			}
			
			
			
			
			double heightMod = 1 - tile.getHeight() / World.MAXHEIGHT;
//			seasonEnergy *= heightMod;
			
			
			//evaporative cooling
			double evaporation = tile.getEvaporation();
			seasonEnergy -= evaporation;
			
			
			double humidity = tile.getAir().getHumidity();
			double airloss = humidity * (-2*seasonEnergy);
//			seasonEnergy += airloss;
			
			
			double vol = tile.getAir().getVolumeLiquid();
			double maxVol = tile.getAir().getMaxVolumeLiquid();
			boolean isSnow = false;
			
			//does raining
			if(tile.getAir().canRain() && tile.liquidType != LiquidType.LAVA) {
				if(tile.liquidType != LiquidType.ICE && tile.liquidType != LiquidType.SNOW && tile.getTemperature() >= World.FREEZETEMP) {
					tile.liquidType = LiquidType.WATER;
				}else if(tile.liquidType != LiquidType.WATER && tile.liquidType != LiquidType.ICE && tile.getTemperature() < World.FREEZETEMP && tile.getAir().getTemperature() < World.FREEZETEMP) {
					tile.liquidType = LiquidType.SNOW;
					isSnow = true;
				}
				double totalAmount = tile.liquidAmount + tile.getAir().getVolumeLiquid();
				
				
				double amount = 0.05 * vol / maxVol;
				// if too much liquid on the ground, cant rain
				if(tile.liquidAmount >= 7.5) {
					amount = 0;
				}
				tile.getAir().addVolumeLiquid(-amount);
				tile.liquidAmount += amount;
				// snow takes up twice the volume of other liquids
				if(isSnow == true) {
					tile.liquidAmount += amount;
				}
//				seasonEnergy += 0.01;
			}
			if(tile.getHeight() > 800 && averageWater < World.BALANCEWATER) {
				double addedMod = World.BALANCEWATER / averageWater - 1;
				tile.liquidAmount += addedMod;
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
			tile.getAir().addEnergy(seasonEnergy/2);
			tile.getAir().addEnergy(addedEnergy/2);
			tile.addEnergy(seasonEnergy/2);
			tile.addEnergy(addedEnergy/2);
			
			tile.updateEnergyToTemperature();
			
			if(tile.getLocation().x() == 5 && tile.getLocation().y() == 5 && World.ticks % 50 == 1) {
//				tile.setEnergy(21000);
//				System.out.println(tile.getTemperature());
				System.out.println("Energy: " + tile.getEnergy() + ", T: " + tile.getTemperature() + ", " + World.ticks + ", uT: " + averageTemp + ", uW: " + averageWater);
			}
//			tile.setEnergy(energy);
//			tile.addEnergy(joules);
		}
		
	}
	
	public static void updateAirMovement(LinkedList<Tile> tiles, int worldWidth, int worldHeight) {
		if(World.ticks % World.TICKSTOUPDATEAIR == 0) {
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
			for(Tile otherTile : tile.getNeighbors()) {
				TileLoc otherLoc = otherTile.getLocation();
				Air otherAir = otherTile.getAir();
				double mypress = tileAir.getPressure();
				double myvolume = tileAir.getVolumeLiquid();
				double myenergy = tileAir.getEnergy();
				
				double opress = otherAir.getPressure();
				double ovolume = otherAir.getVolumeLiquid();
				double omaxvolume = otherAir.getMaxVolumeLiquid();
				double oenergy = otherAir.getEnergy();

				boolean transferred = false;
				
				// PREVENTS AIRFLOW DIRECTIONS FROM CHANGING RAPIDLY
				Direction attemptFlow = Direction.getDirection(tileLoc, otherLoc);
				Direction oldFlow = tileAir.getFlowDirection();
				double directionValue = Math.abs(oldFlow.deltay() + attemptFlow.deltay());
				
				// on: set flow to only move in 4 direction. off: flow move in any direction
//				if (directionValue == 0.5 || directionValue == 2) {

				// IF CONDITIONS MET FOR TRANSFER
				if (mypress > (opress * 1.001)) {
					double deltaE = Math.abs(myenergy - oenergy) / 25;
					// if energy can handle transfer
					if (myenergy - deltaE > 0) {
						transferred = true;
						tileAir.setFlowDirection(attemptFlow);
						energyTemp[otherLoc.x()][otherLoc.y()] += deltaE;
						energyTemp[tileLoc.x()][tileLoc.y()] -= deltaE;
						// if transfer energy, transfer some humidity too
						double deltaVol = Math.abs(myvolume - ovolume) / 2;
						if (myvolume - deltaVol > 0 && ovolume + deltaVol < omaxvolume) {
							volumeTemp[otherLoc.x()][otherLoc.y()] += deltaVol;
							volumeTemp[tileLoc.x()][tileLoc.y()] -= deltaVol;
							break;
						}
					}
				}
				
				// if volume can handle transfer
				if (myvolume > (ovolume * 1.15)) {
					double deltaVol = Math.abs(myvolume - ovolume) / 2;
					if (myvolume - deltaVol > 0) {
						transferred = true;
						tileAir.setFlowDirection(attemptFlow);
						volumeTemp[otherLoc.x()][otherLoc.y()] += deltaVol;
						volumeTemp[tileLoc.x()][tileLoc.y()] -= deltaVol;
					}

				}
//				} // on: set flow to only move in 4 direction. off: flow move in any direction
				
				if (transferred == true) { // stops air from being transferred to multiple tiles
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
