package game;

import java.util.List;

import ui.Game;
import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	private double remainingEffort;
	
	private double culture;
	public static double CULTURE_AREA_MULTIPLIER = 0.1;

	private ResearchRequirement req = new ResearchRequirement();
	
	public Building(BuildingType buildingType, Tile tile) {
		super(buildingType.getHealth(), buildingType, tile);
		this.remainingEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
	}
	
	public void updateCulture() {
		if(isBuilt()) {
			culture += buildingType.cultureRate;
		}
		
	}
	public double getCulture() {
		return culture;
	}
	public void expendEffort(double effort) {
		remainingEffort -= effort;
		if(remainingEffort < 0) {
			remainingEffort = 0;
		}
	}
	public double getRemainingEffort() {
		return remainingEffort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
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
		strings.add(String.format("CT=%." + Game.NUM_DEBUG_DIGITS + "f", getCulture() ));
		if(!isBuilt()) {
			strings.add(String.format("work^2=%." + Game.NUM_DEBUG_DIGITS + "f", getRemainingEffort() ));
		}
		return strings;
	}
	
	@Override
	public String toString() {
		return buildingType;
	}
	
}
