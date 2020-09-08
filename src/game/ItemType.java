package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.*;

public enum ItemType implements HasImage {
	
	COPPER_ORE ( "resources/Images/itemicons/copper_ore.png", null, null, null),
	IRON_ORE ( "resources/Images/itemicons/iron_ore.png", ResearchType.IRON_WORKING, null, null),
	SILVER_ORE ( "resources/Images/itemicons/silver_ore.png", null, null, null),
	MITHRIL_ORE ( "resources/Images/itemicons/mithril_ore.png", ResearchType.ARMORING, null, null),
	GOLD_ORE ( "resources/Images/itemicons/gold_ore.png", ResearchType.CURRENCY, null, null),
	ADAMANTITE_ORE ( "resources/Images/itemicons/adamantite_ore.png", ResearchType.CHIVALRY, null, null),
	RUNITE_ORE ( "resources/Images/itemicons/runite_ore.png", ResearchType.CHIVALRY, null, null),
	TITANIUM_ORE ( "resources/Images/itemicons/titanium_ore.png", ResearchType.CHIVALRY, null, null),
	
	FOOD ( "resources/Images/itemicons/wheat.png", null, null, null),
	HORSE ( "resources/Images/units/horse.png", ResearchType.HORSEBACK_RIDING, null, null),
	WOOD ( "resources/Images/itemicons/wood.png", null, null, null),
	STONE ( "resources/Images/itemicons/rock.png", null, null, null),
	COAL ( "resources/Images/itemicons/coal.png", null, null, null),
	
	COPPER_BAR ( "resources/Images/itemicons/copper_bar.png", null, 							new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,10);	put(ItemType.COAL,4); }}, BuildingType.BLACKSMITH),
	IRON_BAR ( "resources/Images/itemicons/iron_bar.png", ResearchType.IRON_WORKING, 			new HashMap<ItemType, Integer>() { {put(ItemType.IRON_ORE,10); 		put(ItemType.COAL,8);  }}, BuildingType.BLACKSMITH),
	BRONZE_BAR ( "resources/Images/itemicons/bronze_bar.png", ResearchType.BRONZE_WORKING, 		new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,5); 	put(ItemType.SILVER_ORE, 5); put(ItemType.COAL,8); }}, BuildingType.BLACKSMITH),
	SILVER_BAR ( "resources/Images/itemicons/silver_bar.png", null, 							new HashMap<ItemType, Integer>() { {put(ItemType.SILVER_ORE,10); 	put(ItemType.COAL,8);}}, BuildingType.BLACKSMITH),
	MITHRIL_BAR ( "resources/Images/itemicons/mithril_bar.png", ResearchType.ARMORING, 			new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_ORE,10); 	put(ItemType.COAL,12); }}, BuildingType.BLACKSMITH),
	GOLD_BAR ( "resources/Images/itemicons/gold_bar.png", ResearchType.CURRENCY, 				new HashMap<ItemType, Integer>() { {put(ItemType.GOLD_ORE,10); 		put(ItemType.COAL,12); }}, BuildingType.BLACKSMITH),
	
	ADAMANTITE_BAR ( "resources/Images/itemicons/adamantite_bar.png", ResearchType.CHIVALRY, 	new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_ORE,10);put(ItemType.COAL,16); }}, BuildingType.HELLFORGE),
	RUNITE_BAR ( "resources/Images/itemicons/runite_bar.png", ResearchType.CHIVALRY, 			new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_ORE,10);  	put(ItemType.COAL,32);}}, BuildingType.HELLFORGE),
	TITANIUM_BAR ( "resources/Images/itemicons/copper_bar.png", ResearchType.CHIVALRY, 			new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_ORE,10); 	put(ItemType.COAL,64); }}, BuildingType.HELLFORGE),
	
	BRONZE_SWORD ( "resources/Images/itemicons/bronze_sword.png", ResearchType.BRONZE_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.BRONZE_BAR,5); put(ItemType.WOOD, 10); }}, BuildingType.BLACKSMITH),
	IRON_SWORD ( "resources/Images/itemicons/iron_sword.png", ResearchType.IRON_WORKING, new HashMap<ItemType, Integer>() { {put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 10); }}, BuildingType.BLACKSMITH),
	MITHRIL_SWORD ( "resources/Images/itemicons/mithril_sword.png", ResearchType.ARMORING, new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,5); put(ItemType.WOOD, 10); }}, BuildingType.BLACKSMITH),
	
	ADAMANT_SWORD ( "resources/Images/itemicons/adamant_sword.png", ResearchType.CHIVALRY, new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_BAR,5); put(ItemType.WOOD, 10); }}, BuildingType.HELLFORGE),
	RUNE_SWORD ( "resources/Images/itemicons/rune_sword.png", ResearchType.CHIVALRY, new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_BAR,5); put(ItemType.WOOD, 10); }}, 		BuildingType.HELLFORGE),
	TITANIUM_SWORD ( "resources/Images/itemicons/titanium_sword.png", ResearchType.CHIVALRY, new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_BAR,5); put(ItemType.WOOD, 10); }}, BuildingType.HELLFORGE),
	;
	
	private MipMap mipmap;
	private ResearchType researchRequirement;
	private HashMap <ItemType, Integer> cost;
	private BuildingType buildingNeeded;
	
	ItemType(String s, ResearchType researchNeeded, HashMap <ItemType, Integer> resourcesNeeded, BuildingType buildingNeeded) {
		 this.mipmap = new MipMap(s);
		 this.researchRequirement = researchNeeded;
		 this.buildingNeeded = buildingNeeded;
		 System.out.println(this.toString() + ", " + buildingNeeded);
		 this.cost = resourcesNeeded;
	}
	
	public BuildingType getBuildingNeeded() {
		System.out.println(this.toString() + ",get " + buildingNeeded);
		return buildingNeeded;
	}
	
	public ResearchType getResearchRequirement() {
		return researchRequirement;
	}
	
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
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
