package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.Position;

public class Building {
	
	private BuildingType buildingType;
	private Position position;
	private int health;
	
	public Building(BuildingType buildingType, Position pos) {
		this.buildingType = buildingType;
		this.position = pos;
		health = buildingType.getHealth();
	}
	
	public int getHealth() {
    	return health; 
    }
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health < 0;
	}
	public Position getPos() {
		return position;
	}
	public Image getImage(int size) {
		return buildingType.getImage(size);
	} 
	
	
}
