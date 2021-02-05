package world;

import java.io.*;

public class Resource implements Externalizable {

	private ResourceType resourceType;
	private int yieldLeft;
	private int tickNextRegen;
	private Tile tile;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		resourceType = ResourceType.values()[in.readByte()];
		yieldLeft = in.readInt();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(resourceType.ordinal());
		out.writeInt(yieldLeft);
	}
	
	/** Used by Externalizable interface */
	public Resource() {}
	
	public void tick(int ticks) {
		if(ticks >= tickNextRegen) {
			yieldLeft ++;
			resetTimeToRegen(ticks);
		}
	}
	public boolean hasYield() {
		return yieldLeft > 0;
	}
	public void resetTimeToRegen(int tick) {
		tickNextRegen = (int) (tick + resourceType.getTimeToHarvest()*100);
	}
	public Resource(ResourceType resourceType, Tile t) {
		this.resourceType = resourceType;
		this.yieldLeft = resourceType.getRemainingEffort();
		this.tile = t;
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
	public ResourceType getType() {
		return resourceType;
	}
}
