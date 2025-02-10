package game;

import java.awt.*;
import java.io.*;
import java.util.*;

import game.components.GameComponent;
import utils.*;

public class BuildingType implements Serializable {

	private transient static int idCounter = 0;
	private transient final int id;

	private final String name;
	private transient final String info;
	private transient final int health;
	private transient final MipMap mipmap;
	private transient final TiledMipmap tiledMipmap;
	private transient final double moveSpeedEnhancement;
	private transient final int visionRadius;
	private transient final String researchRequirement;
	private transient final double cultureRate;
	private transient final double buildingEffort;
	private transient final double effort_to_produce_item;
	private transient final double base_effort_to_produce_item;
	private transient final double effort_to_produce_harvest;
	private transient final double base_effort_to_produce_harvest;
	private transient final HashMap <ItemType, Integer> cost;
	private transient final HashMap <ItemType, Integer> itemProduced;
	private transient final HashSet<String> attributes;
	private transient final int inventoryStackSize;
	private transient final String[] canProduce;
	private transient final HashSet<UnitType> canProduceSet = new HashSet<>();
//	private transient HashMap<String, Image> roadImages;
	private transient final Set<GameComponent> components = new HashSet<>();

	
	public BuildingType(String name, String info, int hp, double buildingEffort,
			String texturePath, String tiledImageFolder, double cultureRate, int visionRadius, 
			String requirement, HashMap <ItemType, Integer> resourcesNeeded, HashMap <ItemType, Integer> itemProduced,
			String[] canProduce, double moveSpeedEnhancement, HashSet<String> attributes,
			int inventoryStackSize, double effort_to_produce_item, double effort_to_produce_harvest) {
		id = idCounter++;
		this.name = name;
		this.info = info;
		mipmap = new MipMap(texturePath);
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		this.moveSpeedEnhancement = moveSpeedEnhancement;
		this.buildingEffort = buildingEffort;
		this.effort_to_produce_item = effort_to_produce_item;
		this.effort_to_produce_harvest = effort_to_produce_harvest;
		this.cost = resourcesNeeded;
		this.itemProduced = itemProduced;
		this.visionRadius = visionRadius;
		this.canProduce = canProduce;
		this.attributes = attributes;
		this.inventoryStackSize = inventoryStackSize;
		this.base_effort_to_produce_item = 100;
		this.base_effort_to_produce_harvest = 20;
		
//		if(isRoad()) {
//			roadImages = ImageCreation.createRoadImages(texturePath);
//		}

		this.tiledMipmap = tiledImageFolder == null ? null : new TiledMipmap(tiledImageFolder);
	}
	public double getEffortToProduceItem() {
		if(effort_to_produce_item <= 0 ) {
			return base_effort_to_produce_item;
		}
		return effort_to_produce_item;
	}
	public double getEffortToProduceHarvest() {
		if(effort_to_produce_harvest <= 0) {
			return base_effort_to_produce_harvest;
		}
		return effort_to_produce_harvest;
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

	public boolean isTiledImage() {
		return tiledMipmap != null;
	}
	
	public MipMap getTiledImage(int tileBitmap) {
		return tiledMipmap.get(tileBitmap);
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
	public boolean isSmithy() {
		return attributes.contains("smithy");
	}
	public boolean isLumber() {
		return attributes.contains("lumber");
	}
	public boolean isCrafting() {
		return attributes.contains("crafting");
	}
	public boolean isUpgrading() {
		return attributes.contains("upgrade");
	}
	public boolean isHarvestable() {
		return attributes.contains("harvestable");
	}
	public boolean isProducing() {
		return attributes.contains("producing");
	}
	public boolean isTrap() {
		return attributes.contains("trap");
	}
	public boolean isStoneConstruction() {
		return attributes.contains("stone");
	}
	public boolean isWoodConstruction() {
		return attributes.contains("wood");
	}
	public boolean isBridge() {
		return attributes.contains("bridge");
	}
	public boolean isDrain() {
		return attributes.contains("drain");
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	public HashMap<ItemType, Integer> getProduced(){
		return itemProduced;
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
