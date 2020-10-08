package game;

import java.util.LinkedList;
import java.util.List;

import ui.Game;
import utils.*;
import world.Tile;

public class Road extends Thing {
	
	private RoadType roadType;
	private double remainingEffort;
	private double health;
	private boolean isPlanned;
	private ResearchRequirement req = new ResearchRequirement();
	
	public Road(RoadType roadType, Tile tile) {
		super(roadType.getHealth(), roadType, true, tile);
		this.remainingEffort = roadType.getBuildingEffort();
		this.roadType = roadType;
		this.health = roadType.getHealth();
		this.isPlanned = false;
		
	}
	public boolean getIsPlanned() {
		return isPlanned;
	}
	public void setIsPlanned(boolean planned) {
		isPlanned = planned;
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
	public double getHealth() {
		return health;
	}
	public void setRemainingEffort(double effort) {
		remainingEffort = effort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
	}
	public RoadType getRoadType() {
		return roadType;
	}
	
	
	public ResearchRequirement getRequirement() {
		return req;
	}
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("HP=%.1f", getHealth() ));
		if(!isBuilt()) {
			strings.add(String.format("work^2=%.0f", getRemainingEffort() ));
		}
		return strings;
	}
	
	@Override
	public String toString() {
		return roadType.toString();
	}
	
}
