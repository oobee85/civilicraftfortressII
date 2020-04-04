
public class CombatStats {

	private int health;
	private int attack;
	private int defence;
	private int speed;
	
	CombatStats(int hp, int a, int d, int s){
		this.health = hp;
		this.attack = a;
		this.defence = d;
		this.speed = s;
		
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
