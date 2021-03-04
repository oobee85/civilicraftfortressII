package game;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import utils.*;

public class ResearchType implements HasImage {

	public static final String DEFAULT_RESEARCH_IMAGE_PATH = "Images/interfaces/tech.png";

	public final String name;
	public final MipMap mipmap;
	public final LinkedList<String> researchRequirements;
	public final int requiredResearchPoints;
	public final int tier;
	public final HashMap<ItemType, Integer> cost;
	public final LinkedList<String> unlocks;
	
	public ResearchType(String name, String image, LinkedList<String> researchRequirements, int requiredResearchPoints, int tier, HashMap<ItemType, Integer> cost) {
		this.name = name;
		this.mipmap = new MipMap(image);
		this.researchRequirements = researchRequirements;
		this.requiredResearchPoints = requiredResearchPoints;
		this.tier = tier;
		this.cost = cost;
		this.unlocks = new LinkedList<>();
	}
	
	public String name() {
		return name;
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
	public Image getHighlight(int size) {
		return mipmap.getHighlight(size);
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
