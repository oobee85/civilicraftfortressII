package world;

import java.awt.Image;

import utils.Position;

public class Plant {

	private Position position;
	private PlantType plantType;
	private int currentYield;
	
	public Plant(PlantType pt, Position pos) {
		position = pos;
		plantType = pt;
	}
	
	
	public Position getPos() {
		return position;
	}
	public int getYield() {
		return currentYield;
	}
	public Image getImage(int size) {
		return plantType.getImage(size);
	}
	
	
}
