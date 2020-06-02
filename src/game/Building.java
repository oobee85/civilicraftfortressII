package game;

import java.util.List;

import ui.Game;
import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	private int culture;
	public static double CULTURE_AREA_MULTIPLIER = 0.1;

	private ResearchRequirement req = new ResearchRequirement();
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), buildingType, tile);
		this.buildingType = buildingType;
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
	
	public ResearchRequirement getRequirement() {
		return req;
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
