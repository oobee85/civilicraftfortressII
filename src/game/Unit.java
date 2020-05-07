package game;
import java.awt.Image;

import game.*;
import utils.*;

public class Unit {
	
	
	Position pos;
	UnitType unitType;
	
	public Unit(UnitType ut, Position p) {
		this.unitType = ut;
		pos = p;
	}
	
	
	public void changePosion(Position p) {
		pos = p;
	}
	public Position getPosition() {
		return pos;
	}
	public Image getImage() {
		return unitType.getImage();
	}
}
