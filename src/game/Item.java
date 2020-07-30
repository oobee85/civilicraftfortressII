package game;

import utils.*;

public class Item extends Quantity {

	private ItemType itemType;

	public Item(int amount, ItemType itemType) {
		super(amount);
		this.itemType = itemType;
	}

	public ItemType getResourceType() {
		return itemType;
	}

	@Override
	public String toString() {
		return itemType;
	}

}
