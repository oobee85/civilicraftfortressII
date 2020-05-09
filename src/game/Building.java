package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), tile);
		this.buildingType = buildingType;
	}
	
	public Image getImage(int size) {
		return buildingType.getImage(size);
	} 
	public BuildingType getBuildingType() {
		return buildingType;
	}
	
}
