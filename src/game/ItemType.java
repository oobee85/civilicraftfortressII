package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.ImageIcon;

import utils.*;

public enum ItemType implements HasImage {
	
	COPPER_ORE ( "resources/Images/itemicons/copper_ore.png", null, null),
	IRON_ORE ( "resources/Images/itemicons/iron_ore.png", ResearchType.IRON_WORKING, null),
	SILVER_ORE ( "resources/Images/itemicons/silver_ore.png", null, null),
	MITHRIL_ORE ( "resources/Images/itemicons/mithril_ore.png", null, null),
	GOLD_ORE ( "resources/Images/itemicons/gold_ore.png", null, null),
	ADAMANTITE_ORE ( "resources/Images/itemicons/adamantite_ore.png", null, null),
	RUNITE_ORE ( "resources/Images/itemicons/runite_ore.png", null, null),
	TITANIUM_ORE ( "resources/Images/itemicons/titanium_ore.png", null, null),
	FOOD ( "resources/Images/itemicons/wheat.png", null, null),
	HORSE ( "resources/Images/units/horse.png", ResearchType.HORSEBACK_RIDING, null),
	WOOD ( "resources/Images/itemicons/wood.png", null, null),
	ROCK ( "resources/Images/itemicons/rock.png", null, null),
	
	COPPER_BAR ( "resources/Images/itemicons/copper_ore.png", null, new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,10); }}),
	BRONZE_BAR ( "resources/Images/itemicons/copper_ore.png", null, new HashMap<ItemType, Integer>() { {put(ItemType.COPPER_ORE,5); put(ItemType.SILVER_ORE, 5); }}),
	IRON_BAR ( "resources/Images/itemicons/copper_ore.png", null, new HashMap<ItemType, Integer>() { {put(ItemType.IRON_ORE,10);  }}),
	MITHRIL_BAR ( "resources/Images/itemicons/copper_ore.png", null, new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_ORE,10);  }}),
	;
	
	private MipMap mipmap;
	private ResearchType researchRequirement;
	private HashMap <ItemType, Integer> cost;
	
	ItemType(String s, ResearchType researchNeeded, HashMap <ItemType, Integer> resourcesNeeded) {
		 this.mipmap = new MipMap(s);
		 researchRequirement = researchNeeded;
		 cost = resourcesNeeded;
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
