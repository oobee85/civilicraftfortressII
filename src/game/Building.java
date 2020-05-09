package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.Position;
import world.Tile;

public class Building {
	
	private BuildingType buildingType;
	private Tile tile;
	private double health;
	private int timeLastDamageTaken = 0;
	
	public Building(BuildingType buildingType, Tile tile) {
		this.buildingType = buildingType;
		this.tile = tile;
		health = buildingType.getHealth();
	}
	
	public double getHealth() {
    	return health; 
    }
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health < 0;
	}
	public Tile getTile() {
		return tile;
	}
	public Image getImage(int size) {
		return buildingType.getImage(size);
	} 
	public BuildingType getBuildingType() {
		return buildingType;
	}
	public int getTimeLastDamageTaken() {
		return timeLastDamageTaken;
	}
	
}
