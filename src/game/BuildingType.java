package game;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import utils.*;

public class BuildingType implements HasImage, Serializable {

	private final String name;
	private transient final double health;
	private transient final MipMap mipmap;
	private transient final double moveSpeedEnhancement;
	private transient final int visionRadius;
	private transient final String researchRequirement;
	private transient final double cultureRate;
	private transient final double buildingEffort;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final HashSet<String> attributes;
	private transient final String[] canBuild;
	private transient final HashSet<UnitType> canBuildSet = new HashSet<>();
	
	private transient HashMap<String, Image> roadImages;
	
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
	public double getCultureRate() {
		return cultureRate;
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
