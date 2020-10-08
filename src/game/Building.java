package game;

import java.util.LinkedList;
import java.util.List;

import ui.Game;
import utils.*;
import world.Tile;

public class Building extends Thing {
	
	private BuildingType buildingType;
	private double remainingEffort;
	private LinkedList<Unit> buildingUnitList = new LinkedList<Unit>();
	private double culture;
	public static double CULTURE_AREA_MULTIPLIER = 0.1;
	private Tile spawnLocation;
	private double timeToHarvest;
	private double baseTimeToHarvest = 20;
	private boolean isPlayerControlled;
	private boolean isPlanned;
	
	private ResearchRequirement req = new ResearchRequirement();
	
	public Building(BuildingType buildingType, Tile tile, boolean isPlayerControlled) {
		super(buildingType.getHealth(), buildingType, true, tile);
		this.remainingEffort = buildingType.getBuildingEffort();
		this.buildingType = buildingType;
		this.spawnLocation = tile;
		this.timeToHarvest = baseTimeToHarvest;
		this.isPlayerControlled = isPlayerControlled;
		this.isPlanned = false;
		
		
	}
	public void setPlanned(boolean planned) {
		isPlanned = planned;
	}
	public boolean isPlanned() {
		return isPlanned;
	}
	public void tick() {
		updateInProgressUnit();
		timeToHarvest --;
//		System.out.println(timeToHarvest);
	}
	public boolean readyToHarvest() {
//		System.out.println("ready");
		return timeToHarvest <= 0;
	}
	public void resetTimeToHarvest() {
//		System.out.println("in reset");
		if(this.getTile().getResource() != null) {
//			System.out.println("reset ore");
			timeToHarvest = this.getTile().getResource().getType().getTimeToHarvest();
		}else {
//			System.out.println("reset normal");
			timeToHarvest = baseTimeToHarvest;
		}
		
	}
	public Tile getSpawnLocation() {
		return spawnLocation;
	}
	public void setSpawnLocation(Tile tile) {
		spawnLocation = tile;
	}
	public void setBuildingUnit(Unit buildingUnit) {
		this.buildingUnitList.add(buildingUnit);
	}
	private void updateInProgressUnit() {
		if (buildingUnitList.peek() != null) {
			buildingUnitList.peek().expendEffort(1);
		}
	}
	public void updateCulture() {
		if(isBuilt()) {
			culture += buildingType.cultureRate;
		}
		
	}
	
	public boolean getIsPlayerControlled(){
		return isPlayerControlled;
	}
	public LinkedList<Unit> getBuildingUnit() {
		return buildingUnitList;
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
	public void setRemainingEffort(double effort) {
		remainingEffort = effort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
	}
	public BuildingType getType() {
		return buildingType;
	}
	
	public ResearchRequirement getRequirement() {
		return req;
	}
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("CT=%.1f", getCulture() ));
		if(!isBuilt()) {
			strings.add(String.format("work^2=%.0f", getRemainingEffort() ));
		}
		return strings;
	}
	
	@Override
	public String toString() {
		return buildingType.toString();
	}
	
}
