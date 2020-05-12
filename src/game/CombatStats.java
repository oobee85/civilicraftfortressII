package game;

public class CombatStats {

	private int health;
	private int attack;
	private int defence;
	private int speed;
	
	

	
	/**
	 * @param health
	 * @param attack
	 * @param defence
	 * @param speed
	**/
	public CombatStats(int health, int attack, int defence, int speed) {
		this.health = health;
		this.attack = attack;
		this.defence = defence;
		this.speed = speed;
	}
	public int getHealth() {
		return health;
	}
	public int getAttack() {
		return attack;
	}
	public int getDefence() {
		return defence;
	}
	public int getSpeed() {
		return speed;
	}
	
}
