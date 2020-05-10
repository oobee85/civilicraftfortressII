package game;

import utils.Quantity;

public class Resource extends Quantity{

	private ResourceType resourceType;
	
	public Resource(int amount, ResourceType resourceType) {
		super(amount);
		this.resourceType = resourceType;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
}
