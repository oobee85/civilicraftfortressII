package wildlife;

import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import world.*;

public class Animal extends Unit {
	
	public static final int MAX_ENERGY = 100;
	
	
	private double energy;
	private double drive;
	
	public Animal(UnitType type, Tile tile, boolean isPlayerControlled) {
		super(type, tile, isPlayerControlled);
		energy = MAX_ENERGY;
		drive = 0;
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("EN=%." + Game.NUM_DEBUG_DIGITS + "f", getEnergy()));
		return strings;
	}
	
	public void climb(double height) {
		if(height > 0) {
			energy -= height;
		}
	}
	
	public double computeTileDamage(Tile tile, double height) {
		double damage = 0;
		if(getType().isFlying()) {
			
		}
		else {
			if(getType().isAquatic()) {
				if(tile.liquidAmount < LiquidType.DRY.getMinimumDamageAmount()) {
					damage += (LiquidType.DRY.getMinimumDamageAmount() - tile.liquidAmount) * LiquidType.DRY.getDamage();
				}
			}
			else {
				if(getTile().liquidAmount > getTile().liquidType.getMinimumDamageAmount()) {
					damage += tile.liquidAmount * tile.liquidType.getDamage();
				}
			}
		}
		if(tile.checkTerrain(Terrain.SNOW)) {
			if(height > World.SNOW_LEVEL) {
				damage += 0.1 *(height - World.SNOW_LEVEL) / (1 - World.SNOW_LEVEL);
			}
			else {
				damage += 0.01;
			}
		}
		
		return damage;
	}
	
	public double computeDanger(Tile tile, double height) {
		double danger = 0;
		danger += computeTileDamage(tile, height);
		return danger;
	}
	
	public boolean wantsToEat() {
		return Math.random()*100 > energy + 10;
	}
	public void eat() {
		energy += 1;
		drive += 0.01;
	}
	public void loseEnergy() {
		energy -= 0.02;
		if(getHealth() < super.getType().getCombatStats().getHealth()) {
			energy -= 0.04;
			heal(1);
		}
		if(energy < MAX_ENERGY/20) {
			takeDamage(0.05);
		}
	}
	
	public void reproduced() {
		drive = 0;
	}
	
	public boolean wantsToReproduce() {
		return Math.random() < drive - 0.2;
	}
	public double getDrive() {
		return drive;
	}
	
	public boolean isDead() {
		return super.isDead() || energy <= 0;
	}
	
	
	
	
	/**
	 * Moves toward the target and tries to eat it.
	 */
	public void imOnTheHunt(World world) {
		if(getTarget() != null) {
			Tile currentTile = getTile();
			double bestDistance = Integer.MAX_VALUE;
			Tile bestTile = currentTile;
			for (Tile tile : Utils.getNeighbors(currentTile, world)) {
				double distance = tile.getLocation().distanceTo(getTarget().getTile().getLocation());
				if (distance < bestDistance) {
					bestDistance = distance;
					bestTile = tile;
				}
			}
			if(this.readyToMove()) {
				this.moveTo(bestTile);
			}
			this.damageTarget();
//				prey.takeDamage(this.getType().getCombatStats().getAttack());
//				for(int i = 0; i < prey.getType().getCombatStats().getHealth()/2; i++) {
//					eat();
//				}
//				
//				if(prey.isDead()) {
//					prey = null;
//				}
		}
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getSpeed()*0.002 
				+ 0.2*(1 - energy/MAX_ENERGY) 
				+ 0.8*(1 - getHealth()/super.getType().getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
}
