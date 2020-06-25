package game;


import utils.*;
import world.*;

public class Unit extends Thing {
	
	
	private UnitType unitType;
	private double timeToMove;
	private double timeToAttack;
	private boolean isPlayerControlled;
	private Unit target;
	
	
	public Unit(UnitType unitType, Tile tile, boolean isPlayerControlled) {
		super(unitType.getCombatStats().getHealth(), unitType, tile);
		this.unitType = unitType;
		this.isPlayerControlled = isPlayerControlled;
		this.timeToAttack = unitType.getCombatStats().getAttackSpeed();
	}
	public boolean isPlayerControlled() {
		return isPlayerControlled;
	}
	
	public void setTarget(Unit t) {
		target = t;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
	public void moveTo(Tile t) {
		double penalty = t.getTerrain().moveSpeed();
		if(this.getUnitType().isFlying()) {
			penalty = 0;
		}
		if(getTile().getRoadType() != null && t.getRoadType() != null) {
			penalty = (penalty/getTile().getRoadType().getSpeed())/2;
		}
		timeToMove += penalty;
		getTile().removeUnit(this);
		t.addUnit(this);
		this.setTile(t);

		if(this.getUnitType() == UnitType.DRAGON && t.canPlant() == true) {
			t.setTerrain(Terrain.BURNED_GROUND);
		}
	}
	
	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
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
			}
			if(this.getTile().getResource() != null && this.getTile().getResource().getType() == ResourceType.DEAD_ANIMAL) {
				this.getTile().getResource().getType().expendEffort(1);
			}
		}
		
	}
	public void damageTarget() {
		if(target == null || timeToAttack > 0) {
			return;
		}
		if(this.getTile().getLocation().distanceTo(target.getTile().getLocation()) <= getType().getCombatStats().getVisionRadius() 
				|| this.getTile() == target.getTile()) {
			target.takeDamage(this.getType().getCombatStats().getAttack());
			timeToAttack = unitType.getCombatStats().getAttackSpeed();
			target.setTarget(this);
			
			if (target.isDead()) {
				target = null;
				return;
			}
		}
	}
	public Unit getTarget() {
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
	

	@Override
	public String toString() {
		return unitType;
	}
}
