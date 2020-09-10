package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.*;

public enum ItemType implements HasImage {
	
	COPPER_ORE ( "resources/Images/itemicons/copper_ore.png", null, null),
	SILVER_ORE ( "resources/Images/itemicons/silver_ore.png", null, null),
	IRON_ORE ( "resources/Images/itemicons/iron_ore.png", ResearchType.IRON_WORKING, null),
	MITHRIL_ORE ( "resources/Images/itemicons/mithril_ore.png", ResearchType.IRON_WORKING, null),
	GOLD_ORE ( "resources/Images/itemicons/gold_ore.png", ResearchType.CURRENCY, null),
	ADAMANTITE_ORE ( "resources/Images/itemicons/adamantite_ore.png", ResearchType.ARMORING, null),
	RUNITE_ORE ( "resources/Images/itemicons/runite_ore.png", ResearchType.CHIVALRY, null),
	TITANIUM_ORE ( "resources/Images/itemicons/titanium_ore.png", ResearchType.CHIVALRY, null),
	
	FOOD ( "resources/Images/itemicons/wheat.png", null, null),
	HORSE ( "resources/Images/units/horse.png", ResearchType.HORSEBACK_RIDING, null),
	WOOD ( "resources/Images/itemicons/wood.png", null, null),
	STONE ( "resources/Images/itemicons/rock.png", null, null),
	COAL ( "resources/Images/itemicons/coal.png", null, null),
	
	COPPER_BAR ( "resources/Images/itemicons/copper_bar.png", null, 							new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,10);	put(ItemType.COAL,5); }}),
	SILVER_BAR ( "resources/Images/itemicons/silver_bar.png", null, 							new HashMap<ItemType, Integer>() { {put(ItemType.SILVER_ORE,10); 	put(ItemType.COAL,5);}}),
	BRONZE_BAR ( "resources/Images/itemicons/bronze_bar.png", ResearchType.BRONZE_WORKING, 		new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,5); 	put(ItemType.SILVER_ORE, 5); put(ItemType.COAL,10); }}),
	IRON_BAR ( "resources/Images/itemicons/iron_bar.png", ResearchType.IRON_WORKING, 			new HashMap<ItemType, Integer>() { {put(ItemType.IRON_ORE,10); 		put(ItemType.COAL,10);  }}),
	MITHRIL_BAR ( "resources/Images/itemicons/mithril_bar.png", ResearchType.IRON_WORKING, 		new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_ORE,10); 	put(ItemType.COAL,15); }}),
	GOLD_BAR ( "resources/Images/itemicons/gold_bar.png", ResearchType.CURRENCY, 				new HashMap<ItemType, Integer>() { {put(ItemType.GOLD_ORE,10); 		put(ItemType.COAL,15); }}),
	
	ADAMANTITE_BAR ( "resources/Images/itemicons/adamantite_bar.png", ResearchType.ARMORING, 	new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_ORE,10);put(ItemType.COAL,30); }}),
	RUNITE_BAR ( "resources/Images/itemicons/runite_bar.png", ResearchType.CHIVALRY, 			new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_ORE,10);  	put(ItemType.COAL,50);}}),
	TITANIUM_BAR ( "resources/Images/itemicons/titanium_bar.png", ResearchType.CHIVALRY, 		new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_ORE,10); 	put(ItemType.COAL,80); }}),
	
	BRONZE_SWORD ( "resources/Images/itemicons/bronze_sword.png", ResearchType.BRONZE_WORKING,	new HashMap<ItemType, Integer>() { {put(ItemType.BRONZE_BAR,5); put(ItemType.WOOD, 10); }}),
	IRON_SWORD ( "resources/Images/itemicons/iron_sword.png", ResearchType.IRON_WORKING, 		new HashMap<ItemType, Integer>() { {put(ItemType.IRON_BAR,5); put(ItemType.WOOD, 10); }}),
	MITHRIL_SWORD ( "resources/Images/itemicons/mithril_sword.png", ResearchType.IRON_WORKING, 	new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,5); put(ItemType.WOOD, 10); }}),
	
	ADAMANT_SWORD ( "resources/Images/itemicons/adamant_sword.png", ResearchType.ARMORING, 		new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_BAR,5); put(ItemType.WOOD, 10); }}),
	RUNE_SWORD ( "resources/Images/itemicons/rune_sword.png", ResearchType.CHIVALRY, 			new HashMap<ItemType, Integer>() { {put(ItemType.RUNITE_BAR,5); put(ItemType.WOOD, 10); }}),
	TITANIUM_SWORD ( "resources/Images/itemicons/titanium_sword.png", ResearchType.CHIVALRY, 	new HashMap<ItemType, Integer>() { {put(ItemType.TITANIUM_BAR,5); put(ItemType.WOOD, 10); }}),
	;
	
	private MipMap mipmap;
	private ResearchType researchRequirement;
	private HashMap <ItemType, Integer> cost;
	
	ItemType(String s, ResearchType researchNeeded, HashMap <ItemType, Integer> resourcesNeeded) {
		 this.mipmap = new MipMap(s);
		 this.researchRequirement = researchNeeded;
		 this.cost = resourcesNeeded;
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
