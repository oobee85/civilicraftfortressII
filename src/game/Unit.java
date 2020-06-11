package game;


import utils.*;
import world.*;

public class Unit extends Thing {
	
	
	private UnitType unitType;
	private double timeToMove;
	private boolean isPlayerControlled;
	
	
	public Unit(UnitType unitType, Tile tile, boolean isPlayerControlled) {
		super(unitType.getCombatStats().getHealth(), unitType, tile);
		this.unitType = unitType;
		this.isPlayerControlled = isPlayerControlled;
	}
	public boolean isPlayerControlled() {
		return isPlayerControlled;
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
			penalty = penalty/getTile().getRoadType().getSpeed();
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
		}
	}
	
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	

	@Override
	public String toString() {
		return unitType;
	}
}
