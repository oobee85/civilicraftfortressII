package game;

import java.util.HashMap;

import utils.*;

public enum ItemType {

	FOOD ( "Images/itemicons/wheat.png"),
	WOOD ( "Images/itemicons/wood.png"),
	STONE ( "Images/itemicons/rock.png"),
	MAGIC ( "Images/itemicons/magic.png"),
	
	HORSE ( "Images/units/horse.png"),
	GRIFFIN ( "Images/units/griffin.png"),
	ENT ( "Images/units/ent.png"),
	VAMPIRE ( "Images/units/vampire.png"),
	WEREWOLF ( "Images/units/werewolf.png"),
	WOLF ( "Images/units/wolf.png"),
	
	COAL ( "Images/itemicons/coal.png"),
	COPPER_ORE ( "Images/itemicons/copper_ore.png"),
	SILVER_ORE ( "Images/itemicons/silver_ore.png"),
	GOLD_ORE ( "Images/itemicons/gold_ore.png"),
	IRON_ORE ( "Images/itemicons/iron_ore.png"),
	MITHRIL_ORE ( "Images/itemicons/mithril_ore.png"),
	ADAMANTITE_ORE ( "Images/itemicons/adamantite_ore.png"),
	RUNITE_ORE ( "Images/itemicons/runite_ore.png"),
	TITANIUM_ORE ( "Images/itemicons/titanium_ore.png"),
	
	BRONZE_BAR 	("SMITHY",     makeCosts(COPPER_ORE,5, SILVER_ORE, 5),	"Images/itemicons/bronze_bar.png"),
	GOLD_BAR 	("SMITHY",     makeCosts(GOLD_ORE, 10), 				"Images/itemicons/gold_bar.png"),
	IRON_BAR 	("SMITHY",     makeCosts(COAL,10, 	IRON_ORE,10),		"Images/itemicons/iron_bar.png"),
	MITHRIL_BAR ("SMITHY",     makeCosts(COAL,20, 	MITHRIL_ORE,20),	"Images/itemicons/mithril_bar.png"),
	
	ADAMANTITE_BAR("HELLFORGE",makeCosts(COAL,40, 	ADAMANTITE_ORE,40),	"Images/itemicons/adamantite_bar.png"),
	RUNITE_BAR 	("HELLFORGE",  makeCosts(COAL,80, 	RUNITE_ORE,80),		"Images/itemicons/runite_bar.png"),
	TITANIUM_BAR("HELLFORGE",  makeCosts(COAL,160, 	TITANIUM_ORE,160),	"Images/itemicons/titanium_bar.png"),
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
