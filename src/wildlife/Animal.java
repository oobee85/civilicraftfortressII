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
	private Thing foodTarget;
	private Tile resourceTarget;
	
	public Animal(UnitType type, Tile tile, boolean isPlayerControlled) {
		super(type, tile, isPlayerControlled);
		energy = MAX_ENERGY;
		drive = 0;
	}
	public boolean getHasHome() {
		return false;
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
	
	public void loseEnergy() {
		energy -= 0.005;
		if(getHealth() < super.getType().getCombatStats().getHealth() && readyToHeal()) {
			energy -= 1.0;
			heal(1, false);
			resetTimeToHeal();
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

	public boolean wantsToEat() {
		return Math.random()*1000 > energy + 10;
	}
	public void eat(double damage) {
		energy += damage;
		drive += 0.01;
	}
	
	public void chooseWhatToEat(LinkedList<Unit> units, LinkedList<Animal> animals) {
		if(foodTarget != null) {
			return;
		}
		if(getType().isHostile() == true) {
			Unit iveGotYouInMySights;
			if(Math.random() < 0.01 && units.isEmpty() == false) {
				int pickUnit = (int) (units.size()*Math.random());
				iveGotYouInMySights = units.get(pickUnit);
			}else {
				int pickAnimal = (int) (animals.size()*Math.random());
				iveGotYouInMySights = animals.get(pickAnimal);
			}
			
			if(iveGotYouInMySights != this) {
				foodTarget = iveGotYouInMySights;
			}
		}
		else {
			if(!getType().isHostile() && getTile().getPlant() != null) {
				if(readyToAttack()) {
					foodTarget = getTile().getPlant();
				}
			}
		}
	}
	
	/**
	 * Moves toward the target and tries to eat it.
	 */
	public void imOnTheHunt(World world) {
		if(resourceTarget != null) {
			if(getTile() != resourceTarget) {
				moveTowards(resourceTarget);
			}
//			if(getTile() == resourceTarget) {
//				if(resourceTarget.getResource() == null || resourceTarget.getResource().getYield() <= 0 || resourceTarget.getResource().getType() != ResourceType.DEAD_ANIMAL) {
//					resourceTarget = null;
//				}
//				else {
//					resourceTarget.getResource().harvest(1);
//					eat(1);
//					if(resourceTarget.getResource().getYield() <= 0) {
//						resourceTarget.setResource(null);
//						resourceTarget = null;
//					}
//				}
//			}
			return;
		}
		if(foodTarget != null) {
			if(this.getTile().getLocation().distanceTo(foodTarget.getTile().getLocation()) > getType().getCombatStats().getAttackRadius()) {
				moveTowards(foodTarget.getTile());
			}
			if(inRange(foodTarget)) {
				double damageDealt = attack(foodTarget);
				if(damageDealt > 0) {
					eat(damageDealt);
				}
				if(foodTarget.isDead()) {
					resourceTarget = foodTarget.getTile();
					foodTarget = null;
				}
			}
			return;
		}
		if(getTarget() != null) {
			if(this.getTile().getLocation().distanceTo(getTarget().getTile().getLocation()) > getType().getCombatStats().getAttackRadius()) {
				this.moveTowards(getTarget().getTile());
			}
			Attack.tryToAttack(this, getTarget());
			if(getTarget().isDead()) {
				setTarget(null);
			}
			return;
		}
	}
	
	public boolean wantsToAttack() {
		return false;
	}
	
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		return;
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getMoveSpeed()*0.02 
				+ 0.2*(1 - energy/MAX_ENERGY) 
				+ 0.8*(1 - getHealth()/super.getType().getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
}
