package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (100, 100, "resources/Images/buildings/wall_wood.png", 0, false, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }}),
	 	WALL_STONE (1000, 200, "resources/Images/buildings/wall_stone.png", 0, false, 1,
	 			ResearchType.MASONRY,  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,200);	put(ItemType.WOOD,50);  }}),
	 	WALL_BRICK (5000, 500, "resources/Images/buildings/wall_brick.png", 0, false, 1,
	 			ResearchType.ENGINEERING,  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,100); put(ItemType.ADAMANTITE_BAR,1);  }}),
	 	BRIDGE (500, 100, "resources/Images/buildings/bridge.png", 0, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }}),
	 	MINE (500, 100, "resources/Images/buildings/mine256.png", 0, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,10); put(ItemType.STONE,10);  }}),
	 	IRRIGATION (100, 100, "resources/Images/buildings/irrigation.png", 0, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }}),
	 	WATCHTOWER (500, 250, "resources/Images/buildings/watchtower.png", 0, true, 10,
				ResearchType.MYSTICISM,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200);  }}),
	 	
	 	
	 	WINDMILL (1000, 500, "resources/Images/buildings/ancientwindmill.png", 0.25, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200);  }}),
		SAWMILL (1000, 250, "resources/Images/buildings/sawmill.png", 0.25, true, 1,
				null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,10); put(ItemType.STONE,10);  }}),
	 	FARM (500, 250, "resources/Images/buildings/farm.png", 0.25, true, 1,
	 			ResearchType.FARMING,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }}),
	 	GRANARY (500, 500, "resources/Images/buildings/granary.png", 0.25, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200);  }}),
	 	CASTLE (4000, 1000, "resources/Images/buildings/castle256.png", 1, true, 100,
	 			ResearchType.MONARCHY,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,1000); put(ItemType.STONE,1000);  }}),
		BARRACKS (1000, 250, "resources/Images/buildings/barracks256.png", 0.25, true, 1,
				ResearchType.WARRIOR_CODE,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,100);  }}),
		WORKSHOP (1000, 250, "resources/Images/buildings/workshop.png", 0.25, true, 1,
				ResearchType.MATHEMATICS,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200);  }}),
		BLACKSMITH (1000, 250, "resources/Images/buildings/blacksmith.png", 0.25, true, 1,
				ResearchType.BRONZE_WORKING,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200);  }}),
		RESEARCHLAB (1000, 250, "resources/Images/buildings/research.png", 0.25, true, 1,
				null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200);  }}),
		HELLFORGE (1000, 500, "resources/Images/buildings/hellforge.png", 0.25, true, 1,
				ResearchType.ARMORING,  new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,20); put(ItemType.STONE,500);  }}),
		
	 	
		;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private int visionRadius;
	private ResearchType researchRequirement;
	public double cultureRate;
	private double buildingEffort;
	private HashMap <ItemType, Integer> cost;
	
	BuildingType(double hp, double buildingEffort, String s, double cultureRate, boolean canMoveThrough, int visionRadius, ResearchType requirement, HashMap <ItemType, Integer> resourcesNeeded) {
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		mipmap = new MipMap(s);
		this.canMoveThrough = canMoveThrough;
		this.buildingEffort = buildingEffort;
		this.cost = resourcesNeeded;
		this.visionRadius = visionRadius;
		
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
	public int getVisionRadius() {
		return visionRadius;
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
