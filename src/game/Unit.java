package game;
import java.awt.Image;

import game.*;
import utils.*;
import world.Tile;

public class Unit {
	
	
	Tile tile;
	UnitType unitType;
	
	public Unit(UnitType ut, Tile tile) {
		this.unitType = ut;
		this.tile = tile;
	}
	
	
	public void changePosion(Tile t) {
		tile = t;
	}
	public Tile getTile() {
		return tile;
	}
	public Image getImage() {
		return unitType.getImage();
	}
}
