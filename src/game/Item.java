package game;

import utils.*;

public class Item extends Quantity {

	private ItemType itemType;
	private boolean isUnlocked = false;
	
	public Item(int amount, ItemType itemType) {
		super(amount);
		this.itemType = itemType;
	}
	
	public boolean isUnlocked() {
		return isUnlocked;
	}
	public void setUnlocked(boolean unlock) {
		isUnlocked = unlock;
	}

	public ItemType getResourceType() {
		return itemType;
	}

	@Override
	public String toString() {
		return itemType;
	}

}
