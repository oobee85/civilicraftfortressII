package wildlife;

import world.*;

public class Animal {
	public static final int MAX_ENERGY = 100;
	private Tile tile;
	private AnimalType type;
	
	private double health;
	private double energy;
	private double drive;
	
	public Animal(AnimalType type) {
		this.type = type;
		health = type.getCombatStats().getHealth();
		energy = MAX_ENERGY;
		drive = 0;
	}
	
	public double computeDanger(Tile tile) {
		double danger = 0;
		// 3/4 of liquid damage amount starts being considered dangerous
		if(tile.liquidAmount > tile.liquidType.getMinimumDamageAmount()*0.75) {
			danger += tile.liquidAmount * tile.liquidType.getDamage();
		}
		return danger;
	}
	
	public boolean wantsToEat() {
		return Math.random()*100 > energy + 10;
	}
	public void eat() {
		energy += 1;
		drive += 0.02;
	}
	public void loseEnergy() {
		energy -= 0.01;
		if(health < type.getCombatStats().getHealth()) {
			energy -= 0.02;
			health += 0.1;
		}
		if(energy < MAX_ENERGY/10) {
			health -= 0.01;
		}
	}
	
	public void reproduced() {
		drive = 0;
	}
	
	public double getDrive() {
		return drive;
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
		return 0.02 + 0.1*(1 - energy/MAX_ENERGY) + 0.1*(1 - health/type.getCombatStats().getHealth());
	}
	
	public double getEnergy() {
		return energy;
	}
	public double getHealth() {
		return health;
	}
}
