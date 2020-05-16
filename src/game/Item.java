package game;

import utils.Quantity;

public class Item extends Quantity{

	private ItemType resourceType;
	
	public Item(int amount, ItemType resourceType) {
		super(amount);
		this.resourceType = resourceType;
	}
	
	public ItemType getResourceType() {
		return resourceType;
	}
	
}
