package world;

public class Resource {

	private ResourceType resourceType;
	private int yieldLeft;
	private int maxYield;
	private Tile tile;
	
	public Resource(ResourceType resourceType) {
		this.resourceType = resourceType;
		this.maxYield = resourceType.getYield();
		this.yieldLeft = maxYield;
	}
	public Tile getTile() {
		return tile;
	}
	public int getYield() {
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
	
	
}
