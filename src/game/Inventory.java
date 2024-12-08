package game;

import java.io.*;

public class Inventory implements Externalizable {
	
	private int maxStack;
	private Item[] items = new Item[ItemType.values().length];

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		boolean emptyInventory = isEmpty();
		out.writeBoolean(emptyInventory);
		if (!emptyInventory) {
			out.writeInt(maxStack);
			out.writeObject(items);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		boolean emptyInventory = in.readBoolean();
		if (!emptyInventory) {
			maxStack = in.readInt();
			items = (Item[])in.readObject();
		}
	}
	
	public Inventory() {
		this(Integer.MAX_VALUE);
	}
	
	public Inventory(int maxStack) {
		this.maxStack = maxStack;
	}
	
	public void copyFrom(Inventory other) {
		this.maxStack = other.maxStack;

		for (int index = 0; index < items.length; index++) {
			if (other.items[index] == null) {
				items[index] = null;
			}
			else {
				setAmount(other.items[index].getType(), other.items[index].getAmount());
			}
		}
	}
	
	public boolean isDifferent(Inventory other) {
		if (this.maxStack != other.maxStack) {
			return true;
		}
		for (int index = 0; index < items.length; index++) {
			ItemType type = ItemType.values()[index];
			if (getItemAmount(type) != other.getItemAmount(type)) {
				return true;
			}
		}
		return false;
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
				int amountToTake = this.maxStack - this.getItemAmount(item.getType());
				this.addItem(item.getType(), amountToTake);
				from.addItem(item.getType(), -amountToTake);
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
