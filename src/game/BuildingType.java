package game;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import game.components.GameComponent;
import ui.graphics.*;
import utils.*;

public class BuildingType implements Serializable {

	private transient static int idCounter = 0;
	private transient final int id;

	private final String name;
	private transient final String info;
	private transient final int health;
	private transient final MipMap mipmap;
	private transient final double moveSpeedEnhancement;
	private transient final int visionRadius;
	private transient final String researchRequirement;
	private transient final double cultureRate;
	private transient final double buildingEffort;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final HashSet<String> attributes;
	private transient final int inventoryStackSize;
	private transient final String[] canProduce;
	private transient final HashSet<UnitType> canProduceSet = new HashSet<>();
	private transient HashMap<String, Image> roadImages;
	private transient final Set<GameComponent> components = new HashSet<>();

	
	public BuildingType(String name, String info, int hp, double buildingEffort, String texturePath, double cultureRate, int visionRadius, 
			String requirement, HashMap <ItemType, Integer> resourcesNeeded, String[] canProduce, double moveSpeedEnhancement, HashSet<String> attributes, int inventoryStackSize) {
		id = idCounter++;
		this.name = name;
		this.info = info;
		mipmap = new MipMap(texturePath);
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		this.moveSpeedEnhancement = moveSpeedEnhancement;
		this.buildingEffort = buildingEffort;
		this.cost = resourcesNeeded;
		this.visionRadius = visionRadius;
		this.canProduce = canProduce;
		this.attributes = attributes;
		this.inventoryStackSize = inventoryStackSize;
		
		if(isRoad()) {
			roadImages = ImageCreation.createRoadImages(texturePath);
		}
	}
	
	public int getInventoryStackSize() {
		return inventoryStackSize;
	}
	
	public Set<GameComponent> getComponents() {
		return components;
	}
	public HashSet<UnitType> unitsCanProduceSet() {
		return canProduceSet;
	}
	public String[] unitsCanProduce(){
		return canProduce;
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
	
	public MipMap getMipMap() {
		return mipmap;
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
	public boolean isColony() {
		return attributes.contains("colony");
	}
	public boolean isCastle() {
		return attributes.contains("castle");
	}
	public boolean isHarvestable() {
		return attributes.contains("harvestable");
	}
	public boolean isTrap() {
		return attributes.contains("trap");
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public double getBuildingEffort() {
		return buildingEffort;
	}
	public int getHealth() {
		return health;
	}
	public int getVisionRadius() {
		return visionRadius;
	}
	public double getSpeed() {
		return moveSpeedEnhancement;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
	public String name() {
		return name;
	}
	public String info() {
		return info;
	}
	public int id() {
		return id;
	}
}
