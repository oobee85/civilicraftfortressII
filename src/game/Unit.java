package game;

import utils.*;
import world.*;

public class Unit extends Thing {
	
	
	private UnitType unitType;
	private boolean isSelected;
	private Tile targetTile;
	
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
	
	public void setIsSelected(boolean select) {
		isSelected = select;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public UnitType getUnitType() {
		return unitType;
	}
	public Tile getTargetTile() {
		return targetTile;
	}
	public void setTargetTile(Tile t) {
		if(!t.equals(getTile()) ) {
			targetTile = t;
		}
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
			t.setTerrain(Terrain.BURNEDGROUND);
		}
	}
	
	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
	}
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	
	@Override
	public void setTile(Tile tile) {
		super.setTile(tile);
		if(targetTile == getTile() ) {
			targetTile = null;
		}
		
	}
	
}
