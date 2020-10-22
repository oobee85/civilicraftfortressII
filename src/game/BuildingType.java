package game;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import utils.*;

public class BuildingType implements HasImage {

	private final String name;
	private final double health;
	private final MipMap mipmap;
	private final double moveSpeedEnhancement;
	private final int visionRadius;
	private final String researchRequirement;
	public final double cultureRate;
	private final double buildingEffort;
	private final HashMap <ItemType, Integer> cost;
	private final HashSet<String> attributes;
	private final String[] canBuild;
	private final HashSet<UnitType> canBuildSet = new HashSet<>();
	
	private HashMap<String, Image> roadImages;
	
	public BuildingType(String name, double hp, double buildingEffort, String s, double cultureRate, int visionRadius, 
			String requirement, HashMap <ItemType, Integer> resourcesNeeded, String[] canBuild, double moveSpeedEnhancement, HashSet<String> attributes) {
		this.name = name;
		mipmap = new MipMap(s);
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		this.moveSpeedEnhancement = moveSpeedEnhancement;
		this.buildingEffort = buildingEffort;
		this.cost = resourcesNeeded;
		this.visionRadius = visionRadius;
		this.canBuild = canBuild;
		this.attributes = attributes;
		
		if(isRoad()) {
			roadImages = ImageCreation.createRoadImages(s);
		}
	}
	public HashSet<UnitType> unitsCanBuildSet() {
		return canBuildSet;
	}
	public String[] unitsCanBuild(){
		return canBuild;
	}
	public String getResearchRequirement() {
		return researchRequirement;
	}

	public Image getRoadImage(String roadCorner) {
		return roadImages.get(roadCorner);
	}
	@Override
	public Image getImage(int size) {
		if(isRoad()) {
			return roadImages.get(Utils.ALL_DIRECTIONS);
		}
		else {
			return mipmap.getImage(size);
		}
	}
	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		if(isRoad()) {
			return new ImageIcon(roadImages.get(Utils.ALL_DIRECTIONS));
		}
		else {
			return mipmap.getImageIcon(size);
		}
	}

	public boolean blocksMovement() {
		return attributes.contains("blocksmovement");
	}
	public boolean isRoad() {
		return attributes.contains("road");
	}
	public boolean isGate() {
		return attributes.contains("gate");
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
	public int getVisionRadius() {
		return visionRadius;
	}
	public double getSpeed() {
		return moveSpeedEnhancement;
	}

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
	public String name() {
		return name;
	}
}
