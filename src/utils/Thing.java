package utils;

import java.awt.*;

import ui.*;

public class Thing {
	
	private double maxHealth;
	private double health;
	private int timeLastDamageTaken = 0;
	
	public Thing(double maxHealth) {
		health = maxHealth;
		this.maxHealth = maxHealth;
	}

	public boolean isDead() {
		return health < 0;
	}
	public void takeDamage(double damage) {
		health -= damage;
		timeLastDamageTaken = Game.ticks;
	}
	public double getHealth() {
		return health;
	}
	public double getMaxHealth() {
		return maxHealth;
	}
	public int getTimeLastDamageTaken() {
		return timeLastDamageTaken;
	}

}
