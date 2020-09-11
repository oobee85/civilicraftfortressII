package game;

import java.awt.Image;
import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import world.Tile;

public class CombatStats {

	private int health;
	private int attack;
	private int moveSpeed;
	private int attackRadius;
	private int attackSpeed;
	private int ticksToBuild;
	private int ticksToHeal;
	private ArrayList<Integer> stats = new ArrayList<Integer>();
	private List strings = new List();
	
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
		strings.add("Health");
		strings.add("Attack");
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
	public List getStrings() {
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
	public int getVisionRadius() {
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
	public void add(CombatStats cs) {
		this.health += cs.getHealth();
		this.attack += cs.getAttack();
		this.moveSpeed += cs.getMoveSpeed();
		this.attackRadius += cs.getVisionRadius();
		this.attackSpeed += cs.getAttackSpeed();
		this.ticksToBuild += cs.getTicksToBuild();
		this.ticksToHeal += cs.getHealSpeed();
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, attack:%d, speed:%d, vision:%d, attack speed:%d, heal speed:%d", health, attack, moveSpeed, attackRadius, attackSpeed, ticksToHeal);
	}
	
}
