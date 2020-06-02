package game;

import java.util.*;

public class ResearchRequirement {

	private LinkedList<Research> requirements;
	public ResearchRequirement() {
		requirements = new LinkedList<>();
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
}
