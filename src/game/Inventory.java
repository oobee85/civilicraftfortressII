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
	
	public void addItem(Item item) {
		if(items[item.getType().ordinal()] == null) {
			items[item.getType().ordinal()] = item;
		}
		items[item.getType().ordinal()].addAmount(item.getAmount());
	
	}
	
	public void setAmount(ItemType type, int amount) {
		if(items[type.ordinal()] == null) {
			items[type.ordinal()] = new Item(0, type);
		}
		items[type.ordinal()].addAmount(amount - items[type.ordinal()].getAmount());
	}
}
