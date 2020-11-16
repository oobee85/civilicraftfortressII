package game;

import java.io.*;

import world.*;

public class GroundModifier implements Externalizable {

	private GroundModifierType type;
	private int aliveUntil;
	private int duration;
	private Tile tile;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = GroundModifierType.values()[in.readByte()];
		aliveUntil = in.readInt();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(type.ordinal());
		out.writeInt(aliveUntil);
	}
	
	/** Used by Externalizable interface */
	public GroundModifier() {
		
	}
	
	public GroundModifier(GroundModifierType type, Tile tile, int duration) {
		this.type = type;
		this.tile = tile;
		this.aliveUntil = World.ticks + duration;
		this.duration = duration;
	}
	public void refreshDuration() {
		this.aliveUntil = World.ticks + duration;
	}
	
	public GroundModifierType getType() {
		return type;
	}
	public boolean isDead() {
		return World.ticks >= aliveUntil;
	}
	public int timeLeft() {
		return aliveUntil - World.ticks;
	}
	public void setDuration(int duration) {
		aliveUntil = World.ticks + duration;
	}
	
	public void finish() {
		aliveUntil = World.ticks;
	}
	public Tile getTile() {
		return tile;
	}
	public boolean isCold() {
//		if(gmt == GroundModifierType.SNOW) {
//			return true;
//		}
		return false;
	}
	public boolean isHot() {
		if(type == GroundModifierType.FIRE) {
			return true;
		}
		return false;
	}
}
