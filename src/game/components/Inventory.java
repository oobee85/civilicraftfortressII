package game.components;

import game.*;

public class Inventory extends GameComponent {
	
	private int maxStack;
	private Item[] items = new Item[ItemType.values().length];
	
	public Inventory() {
		this(Integer.MAX_VALUE);
	}
	
	public Inventory(int maxStack) {
		this.maxStack = maxStack;
	}
	
	public GameComponent instance() {
		return new Inventory(maxStack);
	}
	
	public void setMaxStack(int maxStack) {
		this.maxStack = maxStack;
	}
	
	public int getMaxStack() {
		return maxStack;
	}

	public boolean isFull() {
		for(Item item : items) {
			if(item != null && item.getAmount() >= this.maxStack) {
				return true;
			}
		}
		return false;
	}
	
	public void clear() {
		for(Item item : items) {
			if(item != null) {
				item.addAmount(-item.getAmount());
			}
		}
	}
	
	public int numUnique() {
		int unique = 0;
		for(Item item : items) {
			if(item != null && item.getAmount() > 0) {
				unique++;
			}
		}
		return unique;
	}
	
	public Item[] getItems() {
		return items;
	}
	public int getItemAmount(ItemType type) {
		return items[type.ordinal()] != null ? items[type.ordinal()].getAmount() : 0;
	}
	public void takeAll(Inventory from) {
		if(from == null) {
			return;
		}
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
	public int addItem(ItemType type, int amount) {
		if(items[type.ordinal()] == null) {
			this.setAmount(type, 0);
		}
		Item item = items[type.ordinal()];
		int amountToAdd = amount;
		if(item.getAmount() + amount > maxStack) {
			amountToAdd = maxStack - item.getAmount();
		}
		else if(item.getAmount() + amount < 0) {
			amountToAdd = -item.getAmount();
		}
		item.addAmount(amountToAdd);
		return amountToAdd;
	}
	public void addItem(Item item) {
		addItem(item.getType(), item.getAmount());
	}
	
	public void setAmount(ItemType type, int amount) {
		if(items[type.ordinal()] == null) {
			items[type.ordinal()] = new Item(0, type);
		}
		items[type.ordinal()].addAmount(amount - items[type.ordinal()].getAmount());
	}
}
