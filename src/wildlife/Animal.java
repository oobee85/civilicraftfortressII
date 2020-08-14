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
		if(type.isHostile() == true) {
			energy *= 10;
		}
		drive = 0;
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("EN=%.1f", getEnergy()));
		return strings;
	}
	
	public void climb(double height) {
		if(height > 0) {
			energy -= height;
		}
	}
	
	public boolean wantsToEat() {
		return Math.random()*1000 > energy + 10;
	}
	public void eat(int damage) {

		if(readyToAttack()) {
			energy += damage;
			drive += 0.01;
			super.resetTimeToAttack();
		}
		

	}
	public void loseEnergy() {
		energy -= 0.005;
		if(getHealth() < super.getType().getCombatStats().getHealth() && readyToAttack()) {
			energy -= 1.0;
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
			if(this.getTile().getLocation().distanceTo(getTarget().getTile().getLocation()) > getType().getCombatStats().getVisionRadius()) {
				this.moveTowardsTarget();
			}
			this.damageTarget();
		}
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getSpeed()*0.02 
				+ 0.2*(1 - energy/MAX_ENERGY) 
				+ 0.8*(1 - getHealth()/super.getType().getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
}
