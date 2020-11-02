package game;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import utils.*;

public class Research {
	
	public static final String DEFAULT_RESEARCH_IMAGE_PATH = "resources/Images/interfaces/tech.png";
	
	public final ResearchType type;
	
	private int researchPointsSpent = 0;
	private boolean isUnlocked = false;
	private boolean payedFor = false;
	private ResearchRequirement req = new ResearchRequirement();
	
	public Research(ResearchType type) {
		this.type = type;
	}

	public int getTier() {
		return type.tier;
	}

	public int getRequiredPoints() {
		return type.requiredResearchPoints;
	}
	
	public String getName() {
		return type.toString();
	}
	
	public ResearchRequirement getRequirement() {
		return req;
	}
	
	public int getPointsSpent() {
		return researchPointsSpent;
	}
	
	public boolean isPayedFor() {
		return payedFor;
	}
	public void setPayedFor(boolean payedFor) {
		this.payedFor = payedFor;
	}
	
	public boolean isUnlocked() {
		return isUnlocked;
	}
	
	public void spendResearch(int points) {
		if(!isUnlocked()) {
			researchPointsSpent += points;
			if(researchPointsSpent >= type.requiredResearchPoints) {
				isUnlocked = true;
				researchPointsSpent = type.requiredResearchPoints;
			}
		}
	}

	public HashMap<ItemType, Integer> getCost(){
		return type.cost;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
}
