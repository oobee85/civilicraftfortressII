package game;


import java.util.*;

import liquid.*;
import pathfinding.*;
import ui.*;
import utils.*;
import wildlife.Animal;
import wildlife.Dragon;
import world.*;

public class Unit extends Thing {
	
	
	private UnitType unitType;
	private double timeToMove;
	private double timeToAttack;
	private double timeToHeal;
	private Thing target;
	private int remainingEffort;
	private boolean isIdle;
	
	private LinkedList<Tile> currentPath;
	
	
	public Unit(UnitType unitType, Tile tile, boolean isPlayerControlled) {
		super(unitType.getCombatStats().getHealth(), unitType, isPlayerControlled, tile);
		this.unitType = unitType;
		this.timeToAttack = unitType.getCombatStats().getAttackSpeed();
		this.remainingEffort = unitType.getCombatStats().getTicksToBuild();
		this.timeToHeal = unitType.getCombatStats().getHealSpeed();
		this.isIdle = false;
		
	}
	public void expendEffort(int effort) {
		remainingEffort -= effort;
		if(remainingEffort < 0) {
			remainingEffort = 0;
		}
	}
	
	public int getRemainingEffort() {
		return remainingEffort;
	}
	public void setRemainingEffort(int effort) {
		remainingEffort = effort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
	}
	
	public void setTarget(Thing t) {
		target = t;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
	public int computeDanger(Tile tile) {
		// currently only tile damage but at some point might check if enemies there
		return tile.computeTileDamage(this);
	}
	
	public double movePenaltyTo(Tile from, Tile to) {
		double penalty = to.getTerrain().moveSpeed();
		
		if(from.getRoad() != null && to.getRoad() != null) {
			penalty = penalty/from.getRoad().getRoadType().getSpeed()/2;
		}
		if(this.getUnitType().isFlying()) {
			penalty = 0;
		}
		penalty += unitType.getCombatStats().getMoveSpeed();
		return penalty;
	}
	
	public boolean moveTo(Tile t) {
		if(!readyToMove()) {
			return false;
		}
		if(t.canMove(this) == false) {
			return false;
		}
		if(this.getTargetTile() == null) {
			this.setTargetTile(this.getTile());
		}
		double penalty = movePenaltyTo(this.getTile(), t);
		timeToMove += penalty;
		
		getTile().removeUnit(this);
		t.addUnit(this);
		this.setTile(t);
		
		if(this.getUnitType() == UnitType.DRAGON && t.canPlant() == true) {
//			t.setTerrain(Terrain.BURNED_GROUND);
		}
		if(this.getUnitType() == UnitType.ENT && t.canPlant() == true) {
			t.setTerrain(Terrain.GRASS);
		}
		return true;
	}
	
	public void moveTowards(Tile tile) {
		if(((currentPath == null || currentPath.isEmpty() || currentPath.getLast() != tile) && tile != this.getTile())
				|| (currentPath != null && !currentPath.isEmpty() && !currentPath.getFirst().canMove(this))) {
			currentPath = Pathfinding.getBestPath(this, this.getTile(), tile);
		}
		if(currentPath != null && !currentPath.isEmpty()) {
			Tile targetTile = currentPath.getFirst();
			boolean success = this.moveTo(targetTile);
			if(success) {
				currentPath.removeFirst();
			}
		}
	}

	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
		if(timeToAttack > 0) {
			timeToAttack -= 1;
		}
		if(timeToHeal > 0) {
			timeToHeal -= 1;
		}
		if(readyToMove() && readyToAttack() && target == null && isPlayerControlled() && getIsSelected() == false) {
			isIdle = true;
		}else {
			isIdle = false;
		}
		
		if(getHealth() < unitType.getCombatStats().getHealth() && readyToHeal()) {
			heal(1);
			resetTimeToHeal();
		}
		if(unitType == UnitType.WORKER) {
			Building tobuild = this.getTile().getBuilding();
			if(tobuild != null && tobuild.isBuilt()) {
				tobuild = null;
			}
			if(tobuild == null) {
				for(Tile tile : this.getTile().getNeighbors()) {
					tobuild = tile.getBuilding();
					if(tobuild != null && tobuild.isBuilt()) {
						tobuild = null;
					}
					if(tobuild != null) {
						break;
					}
				}
			}
			if(tobuild != null) {
				tobuild.expendEffort(1);
//				tobuild.setHealth(tobuild.getHealth() + 1);
			}
//			if(this.getTile().getResource() != null && this.getTile().getResource().getType() == ResourceType.DEAD_ANIMAL) {
//				this.getTile().getResource().getType().expendEffort(1);
//			}
		}
		
	}
	
	public boolean inRange(Thing other) {
		if(other == null) {
			return false;
		}
		return !(this.getTile().getLocation().distanceTo(other.getTile().getLocation()) > getType().getCombatStats().getAttackRadius() 
				&& this.getTile() != other.getTile());
	}
	
	/**
	 * this function does not check the attack range!
	 * @return amount of damage dealt to target
	 */
	public double attack(Thing other) {
		if(other == null || timeToAttack > 0 || other.isDead()) {
			return 0;
		}
		double initialHP = other.getHealth();
		other.takeDamage(this.getType().getCombatStats().getAttack());
		double damageDealt = initialHP - (other.getHealth() < 0 ? 0 : other.getHealth());
		if(unitType.hasLifeSteal() && !(other instanceof Building)) {
			this.heal(this.getType().getCombatStats().getAttack());
		}
		resetTimeToAttack();
		if(other instanceof Unit) {
			((Unit)other).setTarget(this);
		}
		return damageDealt;
	}
	
	
	public void resetTimeToAttack() {
		timeToAttack = unitType.getCombatStats().getAttackSpeed();
	}
	public Thing getTarget() {
		return target;
	}
	public UnitType getType() {
		return unitType;
	}
	
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	public double getTimeToMove() {
		return timeToMove;
	}
	public boolean readyToAttack() {
		return timeToAttack <= 0;
	}
	public double getTimeToAttack() {
		return timeToAttack;
	}
	public void setTimeToAttack(int x) {
		timeToAttack = x;
	}
	public double getTimeToHeal() {
		return timeToHeal;
	}
	public boolean readyToHeal() {
		return timeToHeal <= 0;
	}
	public void resetTimeToHeal() {
		timeToHeal = unitType.getCombatStats().getHealSpeed();
	}
	public boolean isIdle() {
		return isIdle;
	}
	public boolean isRanged() {
		return unitType.isRanged();
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("TTM=%.1f", getTimeToMove()));
		strings.add(String.format("TTA=%.1f", getTimeToAttack()));
		strings.add(String.format("TTH=%.1f", getTimeToHeal()));
		return strings;
	}
	@Override
	public String toString() {
		return unitType.toString();
	}
}
