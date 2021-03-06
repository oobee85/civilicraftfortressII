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
			if(!r.isCompleted())
				return false;
		}
		return true;
	}
	public boolean areSecondLayerRequirementsMet() {
		for(Research r : requirements) {
			if(!r.getRequirement().areRequirementsMet()) {
				return false;
			}
		}
		return true;
	}
	
	public LinkedList<Research> getRequirements() {
		return requirements;
	}
}
