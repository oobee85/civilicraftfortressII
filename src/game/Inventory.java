package game;

public class Inventory {
	
	private Item[] items = new Item[ItemType.values().length];
	
	public Inventory() {
		
	}
	
	public Item[] getItems() {
		return items;
	}
	public int getItemAmount(ItemType type) {
		return items[type.ordinal()] != null ? items[type.ordinal()].getAmount() : 0;
	}
	public void takeAll(Inventory from) {
		for(Item item: from.getItems()) {
			if(item != null) {
				this.addItem(item.getType(), item.getAmount());
				from.setAmount(item.getType(), 0);
			}
		}
	}
	public boolean isEmpty() {
		for(Item item: items) {
			if(item != null && item.getAmount() != 0) {
				return false;
			}
		}
		return true;
	}
	public void addItem(ItemType type, int amount) {
		if(items[type.ordinal()] == null) {
			this.setAmount(type, 0);
		}
		items[type.ordinal()].addAmount(amount);
	
	}
	
	public void setAmount(ItemType type, int amount) {
		if(items[type.ordinal()] == null) {
			items[type.ordinal()] = new Item(0, type);
		}
		items[type.ordinal()].addAmount(amount - items[type.ordinal()].getAmount());
	}
}
