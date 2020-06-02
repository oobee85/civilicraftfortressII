package game;

import java.util.List;

import ui.Game;
import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private boolean isUnlocked = false;
	private BuildingType buildingType;
	private ResearchType requirement;
	private int culture;
	public static double CULTURE_AREA_MULTIPLIER = 0.1;
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), buildingType, tile);
		this.buildingType = buildingType;
		this.requirement = buildingType.getResearchRequirement();
	}
	
	public void updateCulture() {
		culture += buildingType.cultureRate;
	}
	public int getCulture() {
		return culture;
	}
	public BuildingType getBuildingType() {
		return buildingType;
	}
	public boolean isUnlocked() {
		return isUnlocked;
	}
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("CT=%." + Game.NUM_DEBUG_DIGITS + "f", getCulture() * 1.0));
		return strings;
	}
	
	@Override
	public String toString() {
		return buildingType;
	}
	
}
