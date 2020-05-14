package game;
import java.awt.Image;

import game.*;
import utils.*;
import world.Tile;

public class Unit extends Thing{
	
	
	private UnitType unitType;
	private boolean isSelected;
	private Tile targetTile;
	
	private double timeToMove;
	
	public Unit(UnitType unitType, Tile tile) {
		super(unitType.getHealth(), unitType, tile);
		this.unitType = unitType;
	}
	
	
	public void selectUnit(boolean select) {
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
		if(getTile().getHasRoad() && t.getHasRoad()) {
			penalty = penalty/2;
		}
		timeToMove += penalty;
		getTile().setUnit(null);
		t.setUnit(this);
		this.setTile(t);
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
