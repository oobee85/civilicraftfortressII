package game;

import java.util.HashMap;

import utils.*;

public enum ItemType {

	FOOD ( "Images/itemicons/wheat.png"),
	WOOD ( "Images/itemicons/wood.png"),
	STONE ( "Images/itemicons/rock.png"),
	COAL ( "Images/itemicons/coal.png"),
	HORSE ( "Images/units/horse.png"),
	
	COPPER_ORE ( "Images/itemicons/copper_ore.png"),
	SILVER_ORE ( "Images/itemicons/silver_ore.png"),
	IRON_ORE ( "Images/itemicons/iron_ore.png"),
	MITHRIL_ORE ( "Images/itemicons/mithril_ore.png"),
	GOLD_ORE ( "Images/itemicons/gold_ore.png"),
	ADAMANTITE_ORE ( "Images/itemicons/adamantite_ore.png"),
	RUNITE_ORE ( "Images/itemicons/runite_ore.png"),
	TITANIUM_ORE ( "Images/itemicons/titanium_ore.png"),
	
	BRONZE_BAR 	("SMITHY",     makeCosts(COPPER_ORE,5,SILVER_ORE, 5),	"Images/itemicons/bronze_bar.png"),
	IRON_BAR 	("SMITHY",     makeCosts(COAL,15, 	IRON_ORE,10),		"Images/itemicons/iron_bar.png"),
	MITHRIL_BAR ("SMITHY",     makeCosts(COAL,20, 	MITHRIL_ORE,10),	"Images/itemicons/mithril_bar.png"),
	GOLD_BAR 	("SMITHY",     makeCosts(COAL,20, 	GOLD_ORE,10), 		"Images/itemicons/gold_bar.png"),
	
	ADAMANTITE_BAR("HELLFORGE",makeCosts(COAL,30, 	ADAMANTITE_ORE,10),	"Images/itemicons/adamantite_bar.png"),
	RUNITE_BAR 	("HELLFORGE",  makeCosts(COAL,50, 	RUNITE_ORE,10),		"Images/itemicons/runite_bar.png"),
	TITANIUM_BAR("HELLFORGE",  makeCosts(COAL,80, 	TITANIUM_ORE,10),	"Images/itemicons/titanium_bar.png"),
	
	BRONZE_SWORD("SMITHY",     makeCosts(WOOD,50, 	BRONZE_BAR,5),	"Images/itemicons/bronze_sword.png"),
	IRON_SWORD 	("SMITHY",     makeCosts(WOOD,50, 	IRON_BAR,5),	"Images/itemicons/iron_sword.png"),
	MITHRIL_SWORD("SMITHY",    makeCosts(WOOD,50, 	MITHRIL_BAR,5),	"Images/itemicons/mithril_sword.png"),
	
	ADAMANT_SWORD("HELLFORGE", makeCosts(WOOD,50, 	IRON_BAR,5,	ADAMANTITE_BAR,5),	"Images/itemicons/adamant_sword.png"),
	RUNE_SWORD 	("HELLFORGE",  makeCosts(WOOD,50, 	IRON_BAR,5,	RUNITE_BAR,5),		"Images/itemicons/rune_sword.png"),
	TITANIUM_SWORD("HELLFORGE",makeCosts(WOOD,50, 	IRON_BAR,5,	TITANIUM_BAR,5),	"Images/itemicons/titanium_sword.png"),
	;
	
	private MipMap mipmap;
	private HashMap <ItemType, Integer> cost;
	private String building;

	ItemType(String s) {
		 this.mipmap = new MipMap(s);
	}
	
	ItemType(String building, HashMap <ItemType, Integer> resourcesNeeded, String s) {
		this(s);
		this.cost = resourcesNeeded;
		this.building = building;
	}
	
	/**
	 * Creates HashMap of argument pairs
	 * @param costs must be an even sized array of <ItemType, Integer> pairs.
	 */
	private static HashMap<ItemType, Integer> makeCosts(Object ...costs) {
		HashMap<ItemType, Integer> map = new HashMap<>();
		for(int i = 0; i < costs.length; i+=2) {
			ItemType type = (ItemType)costs[i];
			int amount = (int)costs[i+1];
			map.put(type, amount);
		}
		return map;
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public String getBuilding() {
		return building;
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
