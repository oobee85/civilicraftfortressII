package game;
import java.awt.Image;

import game.*;
import utils.*;
import world.Tile;

public class Unit extends Thing{
	
	
	private UnitType unitType;
	private boolean isSelected;
	private Tile targetTile;
	
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
		targetTile = t;
	}
	
}
