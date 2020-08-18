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
	private Thing target;
	private int remainingEffort;
	
	
	public Unit(UnitType unitType, Tile tile, boolean isPlayerControlled) {
		super(unitType.getCombatStats().getHealth(), unitType, isPlayerControlled, tile);
		this.unitType = unitType;
		this.timeToAttack = unitType.getCombatStats().getAttackSpeed();
		this.remainingEffort = unitType.getCombatStats().getTicksToBuild();
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
		if(from.getRoadType() != null && to.getRoadType() != null) {
			penalty = penalty/from.getRoadType().getSpeed()/2;
		}
		if(this.getUnitType().isFlying()) {
			penalty = 0;
		}
		penalty += unitType.getCombatStats().getSpeed();
		return penalty;
	}
	
	public void moveTo(Tile t) {
		if(!readyToMove()) {
			return;
		}
		if(t.canMove(this) == false) {
			return;
		}
		double penalty = movePenaltyTo(this.getTile(), t);
		timeToMove += penalty;
		
		getTile().removeUnit(this);
		t.addUnit(this);
		this.setTile(t);
		
		if(this.getUnitType() == UnitType.DRAGON && t.canPlant() == true) {
			t.setTerrain(Terrain.BURNED_GROUND);
		}
		if(this.getUnitType() == UnitType.ENT && t.canPlant() == true) {
			t.setTerrain(Terrain.GRASS);
		}
	}
	
	public void moveTowards(Tile tile) {
		this.moveTo(Pathfinding.chooseBestTile(this, this.getTile(), tile));
	}

	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 2;
		}
		if(timeToAttack > 0) {
			timeToAttack -= 1;
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
			if(this.getTile().getResource() != null && this.getTile().getResource().getType() == ResourceType.DEAD_ANIMAL) {
				this.getTile().getResource().getType().expendEffort(1);
			}
		}
		
	}
	
	public boolean inRange(Thing other) {
		if(other == null) {
			return false;
		}
		return !(this.getTile().getLocation().distanceTo(other.getTile().getLocation()) > getType().getCombatStats().getVisionRadius() 
				&& this.getTile() != other.getTile());
	}
	
	/**
	 * this function does not check the attack range!
	 * @return amount of damage dealt to target
	 */
	public double attack(Thing other) {
		if(other == null || timeToAttack > 0) {
			return 0;
		}
		double initialHP = other.getHealth();
		other.takeDamage(this.getType().getCombatStats().getAttack());
		double damageDealt = initialHP - (other.getHealth() < 0 ? 0 : other.getHealth());
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
	public boolean readyToAttack() {
		return timeToAttack <= 0;
	}
	
	public double getTimeToAttack() {
		return timeToAttack;
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("TTA=%.1f", getTimeToAttack()));
		return strings;
	}
	@Override
	public String toString() {
		return unitType.toString();
	}
}
