package world;

import game.ItemType;
import utils.*;

public class Plant extends Thing {

	private PlantType plantType;
	private int currentYield;
	
	public Plant(PlantType pt, Tile t) {
		super(pt.getHealth(), pt, World.NO_FACTION, t);
		plantType = pt;
	}
	public ItemType getItem() {
		return plantType.getItem();
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
