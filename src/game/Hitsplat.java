package game;

import ui.Game;

public class Hitsplat {

	double maxDuration;
	double currentDuration = Game.ticks;
	double damage;
	
	public Hitsplat(double damage) {
		currentDuration = Game.ticks;
		maxDuration = currentDuration + 4;
		this.damage = damage;
		
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
	
	
	
}
