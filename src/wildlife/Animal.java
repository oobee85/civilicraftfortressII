package wildlife;

import world.*;

public class Animal {
	private Tile tile;
	private AnimalType type;
	
	private double health;
	private double energy;
	
	public Animal(AnimalType type) {
		this.type = type;
		health = type.getCombatStats().getHealth();
		energy = 1;
	}
	
	public boolean wantsToEat() {
		return Math.random() > energy + 0.1;
	}
	public void eat() {
		energy += 0.01;
	}
	public void loseEnergy() {
		energy -= 0.001;
	}
	
	
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health <= 0 || energy <= 0;
	}
	
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	
	public Tile getTile() {
		return tile;
	}
	public AnimalType getType() {
		return type;
	}
	
	public double getMoveChance() {
		return 0.01;
	}
}
