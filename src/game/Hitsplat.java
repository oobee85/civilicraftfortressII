package game;

import world.*;

public class Hitsplat {

	private int maxDuration;
	private	double damage;
	private int square;
	
	public Hitsplat(double damage, int square) {
		maxDuration = World.ticks + 8;
		this.damage = damage;
		this.square = square;
		
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
