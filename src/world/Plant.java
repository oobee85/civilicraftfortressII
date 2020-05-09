package world;

import java.awt.Image;

import utils.*;

public class Plant extends Thing {

	private Tile tile;
	private PlantType plantType;
	private int currentYield;
	
	public Plant(PlantType pt, Tile t) {
		super(pt.getHealth());
		tile = t;
		plantType = pt;
	}
	
	public Tile getTile() {
		return tile;
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
