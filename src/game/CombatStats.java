package game;

public class CombatStats {

	private int health;
	private int attack;
	private int speed;
	private int visionRadius;
	private int attackSpeed;
	private int ticksToBuild;
	

	
	/**
	 * @param health
	 * @param attack
	 * @param speed
	 * @param visionRadius
	 * @param attackSpeed
	 * @param ticksToBuild
	**/
	public CombatStats(int health, int attack, int speed, int visionRange, int attackSpeed, int ticksToBuild) {
		this.health = health;
		this.attack = attack;
		this.speed = speed;
		this.visionRadius = visionRange;
		this.attackSpeed = attackSpeed;
		this.ticksToBuild = ticksToBuild;
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
	public int getTicksToBuild() {
		return ticksToBuild;
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, attack:%d, speed:%d, vision:%d, attack speed:%d", health, attack, speed, visionRadius, attackSpeed);
	}
	
}
