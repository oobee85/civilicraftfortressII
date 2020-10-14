package game;

import java.awt.*;
import java.awt.Window.Type;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.*;
import utils.*;
import world.Tile;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (100, 100, "resources/Images/buildings/wall_wood.png", 0.25, false, 0,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }},
	 			null),
	 	
	 	WALL_STONE (1000, 200, "resources/Images/buildings/wall_stone.png", 0.25, false, 1,
	 			"ENGINEERING",  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,200);	put(ItemType.WOOD,200);  }},
	 			null),
	 	
	 	WALL_BRICK (5000, 500, "resources/Images/buildings/wall_brick.png", 0.25, false, 1,
	 			"MONARCHY",  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,400); put(ItemType.ADAMANTITE_BAR,1);  }},
	 			null),
	 	
	 	GATE_WOOD (100, 200, "resources/Images/buildings/gate_wood.png", 0.25, false, 1,
	 			"MASONRY",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200); put(ItemType.IRON_BAR,5); }},
	 			null),
	 	
	 	GATE_STONE (1000, 400, "resources/Images/buildings/gate_stone.png", 0.25, false, 1,
	 			"ENGINEERING",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,400); put(ItemType.STONE,400); put(ItemType.MITHRIL_BAR,5); }},
	 			null),
	 	
	 	GATE_BRICK (5000, 1000, "resources/Images/buildings/gate_brick.png", 0.25, false, 1,
	 			"MONARCHY",  new HashMap<ItemType, Integer>() { {put(ItemType.STONE,1000); put(ItemType.WOOD,1000); put(ItemType.ADAMANTITE_BAR,5); }},
	 			null),
	 	
	 	BRIDGE (500, 100, "resources/Images/buildings/bridge.png", 0.25, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,100);  }},
	 			null),
	 	
	 	MINE (500, 100, "resources/Images/buildings/mine256.png", 0.25, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,75); }},
	 			null),
	 	
	 	IRRIGATION (100, 100, "resources/Images/buildings/irrigation.png", 0.25, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50);  }},
	 			null),
	 	
	 	WATCHTOWER (500, 250, "resources/Images/buildings/watchtower.png", 0.25, true, 10,
				"MYSTICISM",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,1000); put(ItemType.MITHRIL_BAR,5); }},
				null),
	 	
	 	WINDMILL (1000, 500, "resources/Images/buildings/ancientwindmill.png", 0.5, true, 1,
	 			null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,1000);  }},
	 			null),
	 	
	 	SAWMILL (1000, 250, "resources/Images/buildings/sawmill.png", 0.5, true, 2,
				null,  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,50); put(ItemType.STONE,25);  }},
				null),
	 	
		FARM (500, 250, "resources/Images/buildings/farm.png", 0.5, true, 1,
	 			"FARMING",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,300); put(ItemType.BRONZE_BAR,5);  }},
	 			null),
		
	 	GRANARY (500, 500, "resources/Images/buildings/granary.png", 0.5, true, 1,
	 			"FARMING",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200);  }},
	 			null),
	 	
	 	CASTLE (2000, 1000, "resources/Images/buildings/castle256.png", 1, true, 5,
	 			"MONARCHY",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,1000); put(ItemType.STONE,1000); put(ItemType.ADAMANTITE_BAR,20); }},
	 			new LinkedList<UnitType>() {{add(UnitType.WORKER);}}),
	 	
	 	BARRACKS (1000, 250, "resources/Images/buildings/barracks256.png", 0.5, true, 1,
				"WARRIOR_CODE",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,300); put(ItemType.STONE,150);  }},
				new LinkedList<UnitType>() {{add(UnitType.WARRIOR);add(UnitType.ARCHER);add(UnitType.HORSE);add(UnitType.HORSEARCHER);add(UnitType.KNIGHT);add(UnitType.SPEARMAN);add(UnitType.SWORDSMAN);add(UnitType.CHARIOT);add(UnitType.ARCHER);}}),	
	 	
		WORKSHOP (1000, 500, "resources/Images/buildings/workshop.png", 0.5, true, 1,
				"MATHEMATICS",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,500); put(ItemType.STONE,500); put(ItemType.IRON_BAR,10); }},
				new LinkedList<UnitType>() {{add(UnitType.LONGBOWMAN); add(UnitType.CATAPULT); add(UnitType.TREBUCHET);}}),
		
		BLACKSMITH (1000, 250, "resources/Images/buildings/blacksmith.png", 0.5, true, 1,
				"BRONZE_WORKING",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200);  }},
				null),
		
		RESEARCHLAB (1000, 250, "resources/Images/buildings/research.png", 0.5, true, 1,
				"IRON_WORKING",  new HashMap<ItemType, Integer>() { {put(ItemType.WOOD,200); put(ItemType.STONE,200); put(ItemType.IRON_BAR,5); }},
				null),
		
		HELLFORGE (1000, 500, "resources/Images/buildings/hellforge.png", 0.5, true, 1,
				"ARMORING",  new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,20); put(ItemType.STONE,1000);  }},
				null),
		;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private int visionRadius;
	private String researchRequirement;
	public double cultureRate;
	private double buildingEffort;
	private HashMap <ItemType, Integer> cost;
	private LinkedList<UnitType> canBuild;
	
	BuildingType(double hp, double buildingEffort, String s, double cultureRate, boolean canMoveThrough, int visionRadius, String requirement, HashMap <ItemType, Integer> resourcesNeeded, LinkedList<UnitType> canBuild) {
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
		mipmap = new MipMap(s);
		this.canMoveThrough = canMoveThrough;
		this.buildingEffort = buildingEffort;
		this.cost = resourcesNeeded;
		this.visionRadius = visionRadius;
		this.canBuild = canBuild;
		
	}
	public LinkedList<UnitType> unitsCanBuild(){
		return canBuild;
	}
	public String getResearchRequirement() {
		return researchRequirement;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
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
