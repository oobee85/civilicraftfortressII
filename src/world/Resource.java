package world;

import java.io.*;

public class Resource implements Externalizable {

	private ResourceType resourceType;
	private int yieldLeft;

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
	
	public Resource(ResourceType resourceType) {
		this.resourceType = resourceType;
		this.yieldLeft = resourceType.getRemainingEffort();
	}
	public int getYield() {
		return yieldLeft;
	}
	public void harvest(int harvestAmount) {
		yieldLeft -= harvestAmount;
	}
	public boolean isHarvested() {
		return yieldLeft <= 0;
	}
	public ResourceType getType() {
		return resourceType;
	}
}
