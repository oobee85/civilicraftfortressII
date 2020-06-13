package game;

public class CombatStats {

	private int health;
	private int attack;
	private int speed;
	private int visionRadius;
	private int attackSpeed;
	

	
	/**
	 * @param health
	 * @param attack
	 * @param speed
	 * @param visionRadius
	 * @param attackSpeed
	**/
	public CombatStats(int health, int attack, int speed, int visionRange, int attackSpeed) {
		this.health = health;
		this.attack = attack;
		this.speed = speed;
		this.visionRadius = visionRange;
		this.attackSpeed = attackSpeed;
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
	public int getAttackSpeed() {
		return attackSpeed;
	}
	
}
