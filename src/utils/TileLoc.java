package utils;

import java.io.*;

public class TileLoc implements Externalizable {
	private int x, y;
	/** Used only by serialization */
	public TileLoc() {
		
	}
	public TileLoc(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
	public TileLoc add(TileLoc other) {
		return new TileLoc(x + other.x, y + other.y);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof TileLoc) {
			return x == ((TileLoc)other).x && y == ((TileLoc)other).y;
		}
		return false;
	}
	
	public int distanceTo(TileLoc other) {
		return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
//		return Math.sqrt((this.x - other.x)*(this.x - other.x) + (this.y - other.y)*(this.y - other.y));
	}
	public double euclideanDistance(TileLoc other) {
		return Math.sqrt((this.x - other.x)*(this.x - other.x) + (this.y - other.y)*(this.y - other.y));
	}
	public int x() {
		return x;
	}
	public int y() {
		return y;
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readShort();
		y = in.readShort();
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(x);
		out.writeShort(y);
	}
	
	public static TileLoc readFromExternal(ObjectInput in) throws IOException {
		return new TileLoc(in.readShort(), in.readShort());
	}
}
