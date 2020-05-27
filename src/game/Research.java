package game;

import java.util.ArrayList;

public class Research {

	private int researchPointsSpent;
	private boolean isUnlocked = false;
	private ResearchType type;
	
	public Research(ResearchType researchType) {
		this.type = researchType;
	}
	
	private boolean getIsUnlocked() {
		return isUnlocked;
	}
	private void spendResearch(int points) {
		researchPointsSpent += points;
		if(researchPointsSpent >= type.getRequirdPoints()) {
			isUnlocked = true;
		}
	}
	
}
