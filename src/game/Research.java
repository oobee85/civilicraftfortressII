package game;

public class Research {

	private int researchPointsSpent;
	private boolean isUnlocked = false;
	private ResearchType type;
	
	private ResearchRequirement req = new ResearchRequirement();
	
	public Research(ResearchType researchType) {
		this.type = researchType;
	}
	
	public ResearchType getType() {
		return type;
	}
	
	public ResearchRequirement getRequirement() {
		return req;
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
