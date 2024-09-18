package game;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

import javax.swing.*;

import utils.*;

public class ResearchType {

	public static final String DEFAULT_RESEARCH_IMAGE_PATH = "Images/interfaces/tech.png";

	public final String name;
	public final MipMap mipmap;
	public final LinkedList<String> researchRequirements;
	public final int requiredResearchPoints;
	public final int tier;
	public final HashMap<ItemType, Integer> cost;
	public final HashMap<BuildingType, Integer> buildingRequirement;
	public final LinkedList<String> unlocks;
	
	public ResearchType(String name, String image, LinkedList<String> researchRequirements, int requiredResearchPoints, int tier, HashMap<ItemType, Integer> cost, HashMap<BuildingType, Integer> buildingRequirement) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.researchRequirements = researchRequirements;
		this.requiredResearchPoints = requiredResearchPoints;
		this.tier = tier;
		this.cost = cost;
		this.buildingRequirement = buildingRequirement;
		this.unlocks = new LinkedList<>();
	}
	
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}
}
