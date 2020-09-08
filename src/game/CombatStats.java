package game;

public class CombatStats {

	private int health;
	private int attack;
	private int speed;
	private int visionRadius;
	private int attackSpeed;
	private int ticksToBuild;
	private int ticksToHeal;

	
	/**
	 * @param health
	 * @param attack
	 * @param speed
	 * @param visionRadius
	 * @param attackSpeed
	 * @param ticksToBuild
	 * @param ticksToHeal
	**/
	public CombatStats(int health, int attack, int speed, int visionRange, int attackSpeed, int ticksToBuild, int ticksToHeal) {
		this.health = health;
		this.attack = attack;
		this.speed = speed;
		this.visionRadius = visionRange;
		this.attackSpeed = attackSpeed;
		this.ticksToBuild = ticksToBuild;
		this.ticksToHeal = ticksToHeal;	
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
	public int getHealSpeed() {
		return ticksToHeal;
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, attack:%d, speed:%d, vision:%d, attack speed:%d, heal speed:%d", health, attack, speed, visionRadius, attackSpeed, ticksToHeal);
	}
	
}
