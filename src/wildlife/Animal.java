package wildlife;

import world.*;

public class Animal {
	private Tile tile;
	private AnimalType type;
	
	private double health;
	
	public Animal(AnimalType type) {
		this.type = type;
		health = type.getCombatStats().getHealth();
	}
	
	public void takeDamage(double damage) {
		health -= damage;
	}
	public boolean isDead() {
		return health <= 0;
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
