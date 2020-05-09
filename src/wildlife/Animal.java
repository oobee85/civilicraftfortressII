package wildlife;

import java.util.*;

import ui.*;
import utils.*;
import world.*;

public class Animal extends Thing {
	public static final int MAX_ENERGY = 100;
	private AnimalType type;
	
	private double energy;
	private double drive;
	
	public Animal(AnimalType type) {
		super(type.getCombatStats().getHealth());
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
	
	public double computeDanger(Tile tile) {
		double danger = 0;
		// 3/4 of liquid damage amount starts being considered dangerous
		if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()*0.75) {
			danger += tile.liquidAmount * tile.liquidType.getDamage();
		}
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
		return 0.02 + 0.1*(1 - energy/MAX_ENERGY) + 0.1*(1 - getHealth()/type.getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
}
