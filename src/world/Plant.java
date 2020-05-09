package world;

import java.awt.Image;

import utils.*;

public class Plant extends Thing {

	private PlantType plantType;
	private int currentYield;
	
	public Plant(PlantType pt, Tile t) {
		super(pt.getHealth(), t);
		plantType = pt;
	}
	
	public int getYield() {
		return currentYield;
	}
	public Image getImage(int size) {
		return plantType.getImage(size);
	}
	public boolean isAquatic() {
		return plantType.isAquatic();
	}
	public PlantType getPlantType() {
		return plantType;
	}
}
