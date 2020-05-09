package world;

import java.awt.Image;

import utils.Position;

public class Plant {

	private Tile tile;
	private PlantType plantType;
	private int currentYield;
	private double health;
	
	public Plant(PlantType pt, Tile t) {
		tile = t;
		plantType = pt;
		health = pt.getHealth();
		
	}
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health < 0;
	}
	
	public double getHealth() {
		return health;
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
