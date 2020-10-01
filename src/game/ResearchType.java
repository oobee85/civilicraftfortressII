package game;

import java.awt.*;
import java.util.HashMap;

import javax.swing.*;

import utils.*;

public enum ResearchType implements HasImage {
	
	WARRIOR_CODE (100, "resources/Images/research/warrior_code.png", new ResearchType[] {}, 1, 			null),
	BRONZE_WORKING (100, "resources/Images/research/bronze_working.png", new ResearchType[] {}, 1,	 	null),
	WRITING (100, "resources/Images/research/writing.png", new ResearchType[] {}, 1, 					null), 
	WHEEL (100, "resources/Images/research/wheel.png", new ResearchType[] {}, 1, 						null),
	
	IRON_WORKING (200, "resources/Images/research/iron_working.png", new ResearchType[] {ResearchType.WARRIOR_CODE, ResearchType.BRONZE_WORKING}, 2, 	new HashMap<ItemType, Integer>() { {put(ItemType.BRONZE_SWORD,2); }}),
	FARMING (200, "resources/Images/research/farming.png", new ResearchType[] {ResearchType.BRONZE_WORKING}, 2, 										new HashMap<ItemType, Integer>() { {put(ItemType.BRONZE_BAR,10); }}),
	MATHEMATICS (200, "resources/Images/research/mathematics.png", new ResearchType[] {ResearchType.WRITING, ResearchType.WHEEL}, 2,					new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); }}),
	MASONRY (200, "resources/Images/research/masonry.png", new ResearchType[] {ResearchType.WHEEL}, 2,													new HashMap<ItemType, Integer>() { {put(ItemType.STONE,500); }}),
	
	ARMORING (300, "resources/Images/research/armoring.png", new ResearchType[] {ResearchType.IRON_WORKING}, 3, 										new HashMap<ItemType, Integer>() { {put(ItemType.IRON_SWORD,2); }}),
	HORSEBACK_RIDING (300, "resources/Images/research/horseback_riding.png", new ResearchType[] {ResearchType.FARMING, ResearchType.IRON_WORKING}, 3, 	new HashMap<ItemType, Integer>() { {put(ItemType.HORSE,50); }}),
	CURRENCY (300, "resources/Images/itemicons/gold.png", new ResearchType[] {ResearchType.FARMING, ResearchType.MATHEMATICS}, 3, 						new HashMap<ItemType, Integer>() { {put(ItemType.FOOD,100); }}),
	ENGINEERING (300, "resources/Images/research/engineering.png", new ResearchType[] {ResearchType.MASONRY, ResearchType.MATHEMATICS}, 3, 				new HashMap<ItemType, Integer>() { {put(ItemType.MITHRIL_BAR,10); }}),
	MYSTICISM (300, "resources/Images/research/mysticism.png",  new ResearchType[] {ResearchType.MASONRY}, 3, 											new HashMap<ItemType, Integer>() { {put(ItemType.STONE,1000); }}),
	
	CHIVALRY (400, "resources/Images/research/chivalry.png", new ResearchType[] {ResearchType.ARMORING, ResearchType.HORSEBACK_RIDING}, 3, 						new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_BAR,20); }}),
	MONARCHY (400, "resources/Images/research/monarchy.png", new ResearchType[] {ResearchType.ENGINEERING, ResearchType.CURRENCY, ResearchType.MYSTICISM}, 3, 	new HashMap<ItemType, Integer>() { {put(ItemType.ADAMANTITE_BAR,20); }}),
	;
	
	private ResearchType[] requiredResearch;
	private MipMap mipmap;
	private int requiredRP;
	private int tier;
	private HashMap<ItemType, Integer> cost;
	
	ResearchType(int researchPoints, String s, ResearchType[] rt, int tier, HashMap<ItemType, Integer> resourcesNeeded){
		requiredResearch = rt;
		mipmap = new MipMap(s);
		this.requiredRP = researchPoints;
		this.tier = tier;
		this.cost = resourcesNeeded;
	}
	public int getRequiredPoints() {
		return requiredRP;
	}
	public int getTier() {
		return tier;
	}
	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	public ResearchType[] getRequiredResearch() {
		return requiredResearch;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
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
	
	
}
