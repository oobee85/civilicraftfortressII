package game;

import java.util.*;

public class Research {

	private int researchPointsSpent;
	private boolean isUnlocked = false;
	private ResearchType type;
	
	private LinkedList<Research> requirements;
	
	public Research(ResearchType researchType) {
		this.type = researchType;
		requirements = new LinkedList<>();
	}
	
	public ResearchType getType() {
		return type;
	}
	
	public void addRequirement(Research r) {
		requirements.add(r);
	}
	
	public boolean areRequirementsMet() {
		for(Research r : requirements) {
			if(!r.isUnlocked())
				return false;
		}
		return true;
	}
	
	public boolean isUnlocked() {
		return isUnlocked;
	}
	
	public void spendResearch(int points) {
		if(!isUnlocked()) {
			researchPointsSpent += points;
			System.out.println(researchPointsSpent);
			if(researchPointsSpent >= type.getRequirdPoints()) {
				isUnlocked = true;
			}
		}
	}
	
	@Override
	public String toString() {
		return type;
	}
	
}
