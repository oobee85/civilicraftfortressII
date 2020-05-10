package game;
import java.awt.Image;

import game.*;
import utils.*;
import world.Tile;

public class Unit extends Thing{
	
	
	private UnitType unitType;
	
	public Unit(UnitType unitType, Tile tile) {
		super(unitType.getHealth(), unitType, tile);
		this.unitType = unitType;
	}
	
	
	public Tile getTile() {
		return super.getTile();
	}
	public Image getImage() {
		return unitType.getImage();
	}
}
