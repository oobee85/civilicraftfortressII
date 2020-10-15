package game;

import java.util.*;

public class CombatStats {

	private int health;
	private int attack;
	private int moveSpeed;
	private int attackRadius;
	private int attackSpeed;
	private int ticksToBuild;
	private int ticksToHeal;
	private ArrayList<Integer> stats = new ArrayList<Integer>();
	private LinkedList<String> strings = new LinkedList<String>();
	
	/**
	 * @param health
	 * @param attack
	 * @param moveSpeed
	 * @param attackRadius
	 * @param attackSpeed
	 * @param ticksToBuild
	 * @param ticksToHeal
	**/
	public CombatStats(int health, int attack, int moveSpeed, int attackRadius, int attackSpeed, int ticksToBuild, int ticksToHeal) {
		this.health = health;
		this.attack = attack;
		this.moveSpeed = moveSpeed;
		this.attackRadius = attackRadius;
		this.attackSpeed = attackSpeed;
		this.ticksToBuild = ticksToBuild;
		this.ticksToHeal = ticksToHeal;	
		strings.add("health");
		strings.add("attack");
		strings.add("moveSpeed");
		strings.add("attackRadius");
		strings.add("attackSpeed");
		strings.add("ticksToBuild");
		strings.add("ticksToHeal");
		stats.add(health);
		stats.add(attack);
		stats.add(moveSpeed);
		stats.add(attackRadius);
		stats.add(attackSpeed);
		stats.add(ticksToBuild);
		stats.add(ticksToHeal);
	}
	public LinkedList<String> getStrings() {
		return strings;
	}
	public ArrayList<Integer> getStats() {
		return stats;
	}
	public int getHealth() {
		return health;
	}
	public int getAttack() {
		return attack;
	}
	public int getMoveSpeed() {
		return moveSpeed;
	}
	public int getAttackRadius() {
		return attackRadius;
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
	public int getStat(String s) {
		if(s.equals("health")) {
			return health;
		}
		if(s.equals("attack")) {
			return attack;
		}
		if(s.equals("moveSpeed")) {
			return moveSpeed;
		}
		if(s.equals("attackRadius")) {
			return attackRadius;
		}
		if(s.equals("attackSpeed")) {
			return attackSpeed;
		}
		if(s.equals("ticksToBuild")) {
			return ticksToBuild;
		}
		if(s.equals("ticksToHeal")) {
			return ticksToHeal;
		}
		return 0;
	}
	public void add(String s, int i) {
		if(s.equals("health")) {
			health =+ i;
		}
		if(s.equals("attack")) {
			attack =+ i;
		}
		if(s.equals("moveSpeed")) {
			moveSpeed =- i;
		}
		if(s.equals("attackRadius")) {
			attackRadius =+ i;
		}
		if(s.equals("attackSpeed")) {
			attackSpeed =- i;
		}
		if(s.equals("ticksToBuild")) {
			ticksToBuild =- i;
		}
		if(s.equals("ticksToHeal")) {
			ticksToHeal =- i;
		}
		
	}
	public void set(CombatStats cs) {
		this.health = cs.getHealth();
		this.attack = cs.getAttack();
		this.moveSpeed = cs.getMoveSpeed();
		this.attackRadius = cs.getAttackRadius();
		this.attackSpeed = cs.getAttackSpeed();
		this.ticksToBuild = cs.getTicksToBuild();
		this.ticksToHeal = cs.getHealSpeed();
	}
	public void combine(CombatStats cs) {
		this.health += cs.getHealth();
		this.attack += cs.getAttack();
		this.moveSpeed -= cs.getMoveSpeed();
		this.attackRadius += cs.getAttackRadius();
		this.attackSpeed -= cs.getAttackSpeed();
		this.ticksToBuild -= cs.getTicksToBuild();
		this.ticksToHeal -= cs.getHealSpeed();
	}
	public void subtract(CombatStats cs) {
		this.health -= cs.getHealth();
		this.attack -= cs.getAttack();
		this.moveSpeed += cs.getMoveSpeed();
		this.attackRadius -= cs.getAttackRadius();
		this.attackSpeed += cs.getAttackSpeed();
		this.ticksToBuild += cs.getTicksToBuild();
		this.ticksToHeal += cs.getHealSpeed();
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, attack:%d, speed:%d, vision:%d, attack speed:%d, heal speed:%d", health, attack, moveSpeed, attackRadius, attackSpeed, ticksToHeal);
	}
	
}
