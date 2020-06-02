package game;

import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private boolean isUnlocked = false;
	private BuildingType buildingType;
	private ResearchType requirement;
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), buildingType, tile);
		this.buildingType = buildingType;
		this.requirement = buildingType.getResearchRequirement();
	}
	
	public BuildingType getBuildingType() {
		return buildingType;
	}
	public boolean isUnlocked() {
		return isUnlocked;
	}
	
	@Override
	public String toString() {
		return buildingType;
	}
	
}
