package wildlife;

import java.util.*;

import liquid.*;
import ui.*;
import utils.*;
import world.*;

public class Animal extends Thing {
	public static final int MAX_ENERGY = 100;
	private AnimalType type;
	
	private double energy;
	private double drive;
	
	public Animal(AnimalType type) {
		super(type.getCombatStats().getHealth(), type);
		this.type = type;
		energy = MAX_ENERGY;
		drive = 0;
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("EN=%." + Game.NUM_DEBUG_DIGITS + "f", getEnergy()));
		return strings;
	}
	
	public double computeTileDamage(Tile tile) {
		double damage = 0;
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
		if(tile.checkTerrain(Terrain.SNOW)) {
			damage += 0.01;
		}
		return damage;
	}
	
	public double computeDanger(Tile tile) {
		double danger = 0;
		danger += computeTileDamage(tile);
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
		energy -= 0.01;
		if(getHealth() < type.getCombatStats().getHealth()) {
			energy -= 0.02;
			takeDamage(-0.1);
		}
		if(energy < MAX_ENERGY/10) {
			takeDamage(0.01);
		}
	}
	
	public void reproduced() {
		drive = 0;
	}
	
	public boolean wantsToReproduce() {
		return Math.random() < drive - 0.1;
	}
	public double getDrive() {
		return drive;
	}
	
	public boolean isDead() {
		return super.isDead() || energy <= 0;
	}
	
	public AnimalType getType() {
		return type;
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getSpeed()*0.001 + 0.1*(1 - energy/MAX_ENERGY) + 0.4*(1 - getHealth()/type.getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
}
