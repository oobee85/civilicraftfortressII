package utils;

import java.io.*;

public class TileLoc implements Serializable {
	public final int x, y;
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
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
