package world;

import game.ItemType;

public class Resource {

	private ResourceType resourceType;
	private double yieldLeft;
	private double maxYield;
	private Tile tile;
	private ItemType itemType;
	
	public Resource(ResourceType resourceType) {
		this.resourceType = resourceType;
		this.maxYield = resourceType.getRemainingEffort();
		this.yieldLeft = maxYield;
	}
	public Tile getTile() {
		return tile;
	}
	public double getYield() {
		return yieldLeft;
	}
	public void harvest(int harvestAmount) {
		yieldLeft -= harvestAmount;
	}
	public boolean isHarvested() {
		if(yieldLeft <= 0) {
			return true;
		}
		return false;
	}
	public ResourceType getType() {
		return resourceType;
	}
	
	public ItemType getResourceType() {
		return itemType;
	}
	
	
}
