package game;

import ui.Game;

public class Hitsplat {

	private double maxDuration;
	private double currentDuration = Game.ticks;
	private	double damage;
	private int square;
	
	public Hitsplat(double damage, int square) {
		currentDuration = Game.ticks;
		maxDuration = currentDuration + 4;
		this.damage = damage;
		this.square = square;
		
	}
	public void updateDuration() {
//		System.out.println("maxDur"+ maxDuration);
//		System.out.println("curDur"+ currentDuration);
		currentDuration = Game.ticks;
	}
	public boolean isDead() {
		if(currentDuration >= maxDuration) {
//			System.out.println("hitsplat dead");
			return true;
		}
//		System.out.println("hitsplat not dead");
		return false;
	}
	public double getDamage() {
		return damage;
	}
	public int getSquare() {
		return square;
	}
	
	
	
}
