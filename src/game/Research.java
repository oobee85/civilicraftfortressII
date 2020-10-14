package game;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import utils.*;

public class Research implements HasImage {
	
	public static final String DEFAULT_RESEARCH_IMAGE_PATH = "resources/Images/interfaces/tech.png";
	
	private final int requiredResearchPoints;
	private final int tier;
	private final String name;
	
	private int researchPointsSpent;
	private boolean isUnlocked = false;
	private MipMap mipmap;
	
	private HashMap<ItemType, Integer> cost = new HashMap<>();
	
	private ResearchRequirement req = new ResearchRequirement();
	
	public Research(String researchName, String imagePath, int points, int tier) {
		this.name = researchName;
		mipmap = new MipMap(imagePath);
		requiredResearchPoints = points;
		this.tier = tier;
	}

	public int getTier() {
		return tier;
	}

	public int getRequiredPoints() {
		return requiredResearchPoints;
	}
	
	public String getName() {
		return name;
	}
	
	public ResearchRequirement getRequirement() {
		return req;
	}
	
	public int getPointsSpent() {
		return researchPointsSpent;
	}
	
	public boolean isUnlocked() {
		return isUnlocked;
	}
	
	public void spendResearch(int points) {
		if(!isUnlocked()) {
			researchPointsSpent += points;
			if(researchPointsSpent >= requiredResearchPoints) {
				isUnlocked = true;
				researchPointsSpent = requiredResearchPoints;
			}
		}
	}

	public HashMap<ItemType, Integer> getCost(){
		return cost;
	}
	
	public void addCost(ItemType type, int quanity) {
		if(!cost.containsKey(type)) {
			cost.put(type, 0);
		}
		cost.put(type, cost.get(type) + quanity);
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
	
}
