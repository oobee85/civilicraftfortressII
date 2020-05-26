package game;

public class CombatStats {

	private int health;
	private int attack;
	private int speed;
	private int visionRadius;
	

	
	/**
	 * @param health
	 * @param attack
	 * @param speed
	 * @param visionRadius
	**/
	public CombatStats(int health, int attack, int speed, int visionRange) {
		this.health = health;
		this.attack = attack;
		this.speed = speed;
		this.visionRadius = visionRange;
	}
	public int getHealth() {
		return health;
	}
	public int getAttack() {
		return attack;
	}
	public int getSpeed() {
		return speed;
	}
	public int getVisionRadius() {
		return visionRadius;
	}
	
}
