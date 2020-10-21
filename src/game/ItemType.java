package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.*;

public enum ItemType implements HasImage {
	
	COPPER_ORE ( "resources/Images/itemicons/copper_ore.png", null),
	SILVER_ORE ( "resources/Images/itemicons/silver_ore.png", null),
	IRON_ORE ( "resources/Images/itemicons/iron_ore.png", null),
	MITHRIL_ORE ( "resources/Images/itemicons/mithril_ore.png", null),
	GOLD_ORE ( "resources/Images/itemicons/gold_ore.png", null),
	ADAMANTITE_ORE ( "resources/Images/itemicons/adamantite_ore.png", null),
	RUNITE_ORE ( "resources/Images/itemicons/runite_ore.png", null),
	TITANIUM_ORE ( "resources/Images/itemicons/titanium_ore.png", null),
	
	FOOD ( "resources/Images/itemicons/wheat.png", null),
	HORSE ( "resources/Images/units/horse.png", null),
	WOOD ( "resources/Images/itemicons/wood.png", null),
	STONE ( "resources/Images/itemicons/rock.png", null),
	COAL ( "resources/Images/itemicons/coal.png", null),
	
	COPPER_BAR ( "resources/Images/itemicons/copper_bar.png", 			new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,10);	put(ItemType.COAL,10); }}, "BLACKSMITH"),
	SILVER_BAR ( "resources/Images/itemicons/silver_bar.png",  			new HashMap<ItemType, Integer>() { {put(ItemType.SILVER_ORE,10); 	put(ItemType.COAL,10);}}, "BLACKSMITH"),
	BRONZE_BAR ( "resources/Images/itemicons/bronze_bar.png", 			new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,5); 	put(ItemType.SILVER_ORE, 5); }}, "BLACKSMITH"),
	IRON_BAR ( "resources/Images/itemicons/iron_bar.png", 				new HashMap<ItemType, Integer>() { {put(ItemType.IRON_ORE,10); 		put(ItemType.COAL,15);  }}, "BLACKSMITH"),
	MITHRIL_BAR ( "resources/Images/itemicons/mithril_bar.png",			new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_ORE,10); 	put(ItemType.COAL,20); }}, "BLACKSMITH"),
	GOLD_BAR ( "resources/Images/itemicons/gold_bar.png", 				new HashMap<ItemType, Integer>() { {put(ItemType.GOLD_ORE,10); 		put(ItemType.COAL,20); }}, "BLACKSMITH"),
	
	ADAMANTITE_BAR ( "resources/Images/itemicons/adamantite_bar.png", 	new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_ORE,10);put(ItemType.COAL,30); }}, "HELLFORGE"),
	RUNITE_BAR ( "resources/Images/itemicons/runite_bar.png", 			new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_ORE,10);  	put(ItemType.COAL,50);}}, "HELLFORGE"),
	TITANIUM_BAR ( "resources/Images/itemicons/titanium_bar.png", 		new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_ORE,10); 	put(ItemType.COAL,80); }}, "HELLFORGE"),
	
	BRONZE_SWORD ( "resources/Images/itemicons/bronze_sword.png", 		new HashMap<ItemType, Integer>() { {put(ItemType.BRONZE_BAR,5); put(ItemType.WOOD, 50); }}, "BLACKSMITH"),
	IRON_SWORD ( "resources/Images/itemicons/iron_sword.png", 			new HashMap<ItemType, Integer>() { {put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 50); }}, "BLACKSMITH"),
	MITHRIL_SWORD ( "resources/Images/itemicons/mithril_sword.png",		new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,5); put(ItemType.WOOD, 50); }}, "BLACKSMITH"),
	
	ADAMANT_SWORD ( "resources/Images/itemicons/adamant_sword.png", 	new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_BAR,5); put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 50); }}, "HELLFORGE"),
	RUNE_SWORD ( "resources/Images/itemicons/rune_sword.png", 			new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_BAR,5); put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 50); }}, "HELLFORGE"),
	TITANIUM_SWORD ( "resources/Images/itemicons/titanium_sword.png", 	new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_BAR,5); put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 50); }}, "HELLFORGE"),
	;
	
	private MipMap mipmap;
	private HashMap <ItemType, Integer> cost;
	private String building;

	ItemType(String s, HashMap <ItemType, Integer> resourcesNeeded) {
		 this.mipmap = new MipMap(s);
//		 this.researchRequirement = researchNeeded;
		 this.cost = resourcesNeeded;
	}
	
	ItemType(String s, HashMap <ItemType, Integer> resourcesNeeded, String building) {
		 this(s, resourcesNeeded);
		 this.building = building;
	}

	
	
//	public ResearchType getResearchRequirement() {
//		return researchRequirement;
//	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public String getBuilding() {
		return building;
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

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
