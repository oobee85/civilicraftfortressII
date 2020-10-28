package world;

import java.io.*;

public class Resource implements Serializable {

	private ResourceType resourceType;
	private double yieldLeft;
	private double maxYield;
	
	public Resource(ResourceType resourceType) {
		this.resourceType = resourceType;
		this.maxYield = resourceType.getRemainingEffort();
		this.yieldLeft = maxYield;
	}
	public double getYield() {
		return yieldLeft;
	}
	public void harvest(int harvestAmount) {
		yieldLeft -= harvestAmount;
	}
	public boolean isHarvested() {
		return yieldLeft <= 0;
	}
	public ResourceType getType() {
		return resourceType;
	}
}
