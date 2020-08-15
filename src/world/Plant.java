package world;

import utils.*;

public class Plant extends Thing {

	private PlantType plantType;
	private int currentYield;
	
	public Plant(PlantType pt, Tile t) {
		super(pt.getHealth(), pt, t);
		plantType = pt;
	}
	
	public int getYield() {
		return currentYield;
	}
	public void harvest(int h) {
		currentYield -= h;
	}
	public boolean isAquatic() {
		return plantType.isAquatic();
	}
	public PlantType getPlantType() {
		return plantType;
	}

	@Override
	public String toString() {
		return plantType.toString();
	}
}
