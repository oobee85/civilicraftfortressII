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
			double deltaT = (tile.getTemperature() - World.MINTEMP) - (air.getTemperature() - World.MINTEMP);
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
			if(t.liquidType == LiquidType.WATER || t.liquidType == LiquidType.ICE) {
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
				double addedEnergy = tile.liquidAmount / tile.getEnergy() * 1000;
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
			double addedEnergy = 0;
			//removes energy for hight level
			if(tile.getTemperature() <= 5) {
				double modifier = 1 - (tile.getTemperature()/World.BALANCETEMP);
				double heightRatio = tile.getHeight() / World.MAXHEIGHT;
				addedEnergy -= (Math.sqrt(Math.abs(heightRatio * modifier)));
//				System.out.println(addedEnergy);
			}
			if(tile.getTemperature() <= -5) {
				double modifier = 1 - (tile.getTemperature()/World.BALANCETEMP);
				double heightRatio = tile.getHeight() / World.MAXHEIGHT;
				addedEnergy -= (modifier*Math.sqrt(Math.abs(heightRatio)));
//				System.out.println(addedEnergy);
			}
			
			
			
			//adds energy for ground modifiers
			GroundModifier gm = tile.getModifier();
			if(gm != null && gm.isHot()) {
				double mod = gm.timeLeft() / 500;
				addedEnergy += (mod);
			}
			
			float seasonEnergy = Seasons.getRateEnergy();
			
			
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
				}else if(tile.liquidType != LiquidType.WATER && tile.liquidType != LiquidType.ICE && tile.getTemperature() < World.FREEZETEMP) {
					tile.liquidType = LiquidType.SNOW;
					isSnow = true;
				}
				double totalAmount = tile.liquidAmount + tile.getAir().getVolumeLiquid();
				
				
				double amount = 0.05 * vol / maxVol;
				if(tile.liquidAmount >= 10) {
					amount = 0;
				}
				tile.getAir().addVolumeLiquid(-amount);
				tile.liquidAmount += amount;
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
			tile.getAir().addEnergy(seasonEnergy);
			tile.getAir().addEnergy(addedEnergy);
			
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
				double mycombined = mypress / (myvolume);
				
				double opress = otherAir.getPressure();
				double ovolume = otherAir.getVolumeLiquid();
				double oenergy = otherAir.getEnergy();
				double ocombined = opress / (ovolume);
				
				boolean transferred = false;
				double deltap = 1 - opress / mypress;
				double deltavol = Math.sqrt((myvolume - ovolume)*deltap);
				
				
				// PREVENTS AIRFLOW DIRECTIONS FROM CHANGING RAPIDLY
				Direction attemptFlow = Direction.getDirection(tileLoc, otherLoc);
				Direction oldFlow = tileAir.getFlowDirection();
				double directionValue = Math.abs(oldFlow.deltay() + attemptFlow.deltay());
//				if (directionValue == 0.5 || directionValue == 2) {

					
				// IF CONDITIONS MET FOR TRANSFER
//				if(mycombined > ocombined && Math.abs(deltavol) > 0.002) {
					if (myenergy > oenergy * 1.0010) {
//					if (mypress > opress * 1.001 
////							&& myvolume > ovolume 
//							&& Math.abs(deltavol) > 0.00001 // 0.0015
//							){
						transferred = true;
						tileAir.setFlowDirection(attemptFlow);
//					double deltap = 1 - opress / mypress;
//					double deltavol = Math.sqrt((myvolume - ovolume)*deltap);
//					System.out.println(deltavol);
//						if (volumeTemp[tileLoc.x()][tileLoc.y()] - deltavol > 0) {
//							volumeTemp[otherLoc.x()][otherLoc.y()] += deltavol;
//							volumeTemp[tileLoc.x()][tileLoc.y()] -= deltavol;
//							transferred = true;
//						}

						double deltae = Math.abs(myenergy - oenergy) /50;
						
						double ratio = myenergy / deltavol;
//					double ratio = oenergy / myenergy * Math.sqrt(deltae);
						energyTemp[otherLoc.x()][otherLoc.y()] += deltae;
						energyTemp[tileLoc.x()][tileLoc.y()] -= deltae;
						
						if (myvolume > ovolume * 1.0010) {
							double deltaHum = Math.abs(myvolume - ovolume) /2;
							volumeTemp[otherLoc.x()][otherLoc.y()] += deltaHum;
							volumeTemp[tileLoc.x()][tileLoc.y()] -= deltaHum;
						}
						
//						System.out.println(deltae);

//				}else if (Math.abs(deltavol) <= 0.0015){
//					tileAir.setFlowDirection(Direction.NONE);
//					}
				}
				if(transferred == true) {
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
