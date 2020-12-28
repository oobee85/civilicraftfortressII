package game;

import java.io.*;

public class Item implements Externalizable {

	private volatile int amount;
	private ItemType itemType;
	
	public Item() {
		
	}
	
	public Item(int amount, ItemType itemType) {
		this.amount = amount;
		this.itemType = itemType;
	}

	public ItemType getType() {
		return itemType;
	}

	@Override
	public String toString() {
		return itemType.toString();
	}
	public int getAmount() {
		return amount;
	}
	public void addAmount(int i) {
		amount += i;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		itemType = ItemType.valueOf(in.readUTF());
		amount = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(itemType.name());
		out.writeInt(amount);
	}

}
