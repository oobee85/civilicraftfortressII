package game;

import java.io.*;

import utils.*;
import world.*;

public class Hitsplat implements Externalizable {

	private int maxDuration;
	private	int damage;
	private int square;
	private int thingID;
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		damage = in.readInt();
		thingID = in.readInt();
		square = in.readByte();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(damage);
		out.writeInt(thingID);
		out.writeByte(square);
	}
	/** Used by Externalizable interface */
	public Hitsplat() { 
		maxDuration = World.ticks + 10;
	}
	
	public Hitsplat(int damage, int square, Thing thing) {
		maxDuration = World.ticks + 8;
		this.damage = damage;
		this.square = square;
		this.thingID = thing.id();
	}
	public int getThingID() {
		return thingID;
	}
	public int getMaxDuration() {
		return maxDuration;
	}
	public boolean isDead() {
		if(World.ticks >= maxDuration) {
			return true;
		}
		return false;
	}
	public double getDamage() {
		return damage;
	}
	public int getSquare() {
		return square;
	}
}
