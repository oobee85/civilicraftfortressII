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
	
	@Override
	public void updateState() {
		super.updateState();
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
	@Override
	public boolean takeDamage(double damage) {
		boolean lethal = super.takeDamage(damage);
		if(lethal) {
			if(getType().getDeadItem() != null) {
				getTile().addItem(getType().getDeadItem());
			}
		}
		return lethal;
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
	
	@Override
	public void planActions(LinkedList<Unit> units, LinkedList<Building> buildings, LinkedList<Building> plannedBuildings) {
		chooseWhatToEat(units);
		if(wantsToAttack() && getTarget() == null) {
			chooseWhatToAttack(units, buildings);
		}
		chooseWhereToMove();
	}
	
	@Override
	public void doPassiveThings(World world) {
	}
	
	public void chooseWhereToMove() {
		if(resourceTarget != null) {
			if(getTile() != resourceTarget) {
				setTargetTile(resourceTarget);
				return;
			}
		}
		if(foodTarget != null) {
			if(this.getTile().getLocation().distanceTo(foodTarget.getTile().getLocation()) > getType().getCombatStats().getAttackRadius()) {
				setTargetTile(foodTarget.getTile());
				return;
			}
		}
		if(getTarget() != null) {
			if(this.getTile().getLocation().distanceTo(getTarget().getTile().getLocation()) > getType().getCombatStats().getAttackRadius()) {
				setTargetTile(getTarget().getTile());
				return;
			}
		}
		if(Math.random() < getMoveChance() && readyToMove()) {
			if(getTile().getBuilding() != null && getTile().getBuilding().getType() == BuildingType.FARM) {
				//stuck inside farm
			}
			else {
				Tile best = getTile();
				double bestDanger = computeDanger(best);
				for(Tile t : getTile().getNeighbors()) {
					if(t.isBlocked(this)) {
						continue;
					}
					double danger = computeDanger(t);
					if(danger < bestDanger) {
						best = t;
						bestDanger = danger;
					}
				}
				if(best != getTile()) {
					setTargetTile(best);
				}
			}
		}
	}
	
	private void chooseWhatToEat(LinkedList<Unit> units) {
		if(foodTarget != null || !wantsToEat()) {
			return;
		}
		if(getType().isHostile() == true) {
			Unit iveGotYouInMySights = null;
			if(!units.isEmpty()) {
				int pickUnit = (int) (units.size()*Math.random());
				iveGotYouInMySights = units.get(pickUnit);
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
	
	@Override
	public void doAttacks(World world) {
		super.doAttacks(world);
		if(foodTarget != null) {
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
	}
	
	public boolean wantsToAttack() {
		return false;
	}
	
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
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
