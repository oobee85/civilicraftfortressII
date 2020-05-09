package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	private Tile tile;
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth());
		this.buildingType = buildingType;
		this.tile = tile;
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
	
}
