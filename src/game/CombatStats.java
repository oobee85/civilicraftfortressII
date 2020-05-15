package game;

public class CombatStats {

	private int health;
	private int attack;
	private int speed;
	
	

	
	/**
	 * @param health
	 * @param attack
	 * @param speed
	**/
	public CombatStats(int health, int attack, int speed) {
		this.health = health;
		this.attack = attack;
		this.speed = speed;
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
	
}
