package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (250, 100, "resources/Images/buildings/wall_wood.png", 0, false, 
	 			ResearchType.WOODCUTTING,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
	 	WALL_STONE (1000, 200, "resources/Images/buildings/wall_stone.png", 0, false, 
	 			ResearchType.MASONRY,  new HashMap<ItemType, Integer>() { {put(ItemType.ROCK,100);  }}),
	 	WALL_BRICK (5000, 500, "resources/Images/buildings/wall_brick.png", 0, false, 
	 			ResearchType.MASONRY,  new HashMap<ItemType, Integer>() { {put(ItemType.ROCK,100);  }}),
	 	BRIDGE (500, 100, "resources/Images/buildings/bridge.png", 0, true, 
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }}),
	 	MINE (500, 100, "resources/Images/buildings/mine256.png", 0, true, 
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100); put(ItemType.ROCK,100);  }}),
	 	IRRIGATION (100, 100, "resources/Images/buildings/irrigation.png", 0, true, 
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }}),
	 	
	 	WINDMILL (1000, 500, "resources/Images/buildings/ancientwindmill.png", 0.05, true, 
	 			ResearchType.CONSTRUCTION,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
		SAWMILL (1000, 500, "resources/Images/buildings/sawmill.png", 0.05, true, 
				null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
	 	FARM (500, 250, "resources/Images/buildings/farm.png", 0.05, true, 
	 			ResearchType.FARMING,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
	 	GRANARY (500, 500, "resources/Images/buildings/granary.png", 0.05, true, 
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
	 	CASTLE (5000, 1000, "resources/Images/buildings/castle256.png", 1, true, 
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,1000); put(ItemType.ROCK,1000);  }}),
		BARRACKS (1000, 250, "resources/Images/buildings/barracks256.png", 0.1, true, 
				ResearchType.WARRIOR_CODE,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.ROCK,100);  }}),
		WORKSHOP (1000, 250, "resources/Images/buildings/workshop.png", 0.1, true, 
				ResearchType.WARRIOR_CODE,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.ROCK,500);  }}),
	 	
		;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private ResearchType researchRequirement;
	public double cultureRate;
	private double buildingEffort;
	private HashMap <ItemType, Integer> cost;
	
	BuildingType(double hp, double buildingEffort, String s, double cultureRate, boolean canMoveThrough, ResearchType requirement, HashMap <ItemType, Integer> resourcesNeeded) {
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		mipmap = new MipMap(s);
		this.canMoveThrough = canMoveThrough;
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
	public boolean canMoveThrough() {
		return canMoveThrough;
	}

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
