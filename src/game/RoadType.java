package game;

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.*;

public enum RoadType implements HasImage {
	STONE_ROAD (50, 10, "resources/Images/buildings/research.png", 4, 
			null,  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,10);  }}),
	;
	
	private final double health;
	private MipMap mipmap;
	private ResearchType researchRequirement;
	private double buildingEffort;
	private HashMap <ItemType, Integer> cost;
	private double speed;
	
	RoadType(double hp, double buildingEffort, String s, double speed,  ResearchType requirement, HashMap <ItemType, Integer> resourcesNeeded) {
		this.researchRequirement = requirement;
		this.health = hp;
		mipmap = new MipMap(s);
		this.buildingEffort = buildingEffort;
		this.cost = resourcesNeeded;
		
	}
	
	public ResearchType getResearchRequirement() {
		return researchRequirement;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public double getBuildingEffort() {
		return buildingEffort;
	}
	public double getHealth() {
		return health;
	}

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}

	public double getSpeed() {
		return speed;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}

}
