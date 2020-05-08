package world;

import java.awt.Image;

import utils.Position;

public class Plant {

	private Position position;
	private PlantType plantType;
	private int currentYield;
	private int health;
	
	public Plant(PlantType pt, Position pos) {
		position = pos;
		plantType = pt;
		health = pt.getHealth();
		
	}
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health < 0;
	}
	
	public int getHealth() {
		return health;
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
