package utils;
import java.util.List;
import java.util.*;

import ui.*;
import world.*;

public class Thing {
	
	private double maxHealth;
	private double health;
	private int timeLastDamageTaken = Integer.MIN_VALUE;
	private Tile tile;
	
	public Thing(double maxHealth) {
		health = maxHealth;
		this.maxHealth = maxHealth;
	}
	public Thing(double maxHealth, Tile tile) {
		this(maxHealth);
		this.tile = tile;
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
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(String.format("HP=%." + Game.NUM_DEBUG_DIGITS + "f", getHealth())));
	}

}
