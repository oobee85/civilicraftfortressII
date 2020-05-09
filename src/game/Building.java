package game;

import java.awt.Image;

import javax.swing.ImageIcon;

import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), buildingType, tile);
		this.buildingType = buildingType;
	}
	
	public BuildingType getBuildingType() {
		return buildingType;
	}
	
}
