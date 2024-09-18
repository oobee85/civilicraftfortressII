package game;

import java.io.*;
import java.util.*;

import ui.*;
import utils.*;

public class Research implements Externalizable {
	
	public static final String DEFAULT_RESEARCH_IMAGE_PATH = "Images/interfaces/tech.png";
	
	private ResearchType type;
	
	private int researchPointsSpent = 0;
	private boolean isCompleted = false;
	private boolean isPayedFor = false;
	
	private ResearchRequirement req = new ResearchRequirement();
	
	public Research() {
		
	}
	public Research(ResearchType type) {
		this.type = type;
	}
	
	public ResearchType type() {
		return type;
	}
	public void setType(ResearchType type) {
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
		return isPayedFor;
	}
	public void setPayedFor(boolean payedFor) {
		this.isPayedFor = payedFor;
	}
	
	public boolean isCompleted() {
		return isCompleted;
	}
	
	public void setCompleted(boolean completed) {
		this.isCompleted = completed;
	}
	
	public void setResearchPointsSpend(int point) {
		researchPointsSpent = point;
	}
	
	public void spendResearch(int points) {
		if(!isCompleted()) {
			researchPointsSpent += points;
			if(researchPointsSpent >= type.requiredResearchPoints) {
				isCompleted = true;
				researchPointsSpent = type.requiredResearchPoints;
			}
		}
	}

	public HashMap<ItemType, Integer> getCost(){
		return type.cost;
	}
	
	public HashMap<BuildingType, Integer> getBuildingRequirement(){
		return type.buildingRequirement;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}	

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = Game.researchTypeMap.get(in.readUTF());
		researchPointsSpent = in.readInt();
		isCompleted = in.readBoolean();
		isPayedFor = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(type.name());
		out.writeInt(researchPointsSpent);
		out.writeBoolean(isCompleted);
		out.writeBoolean(isPayedFor);
	}
	
}
