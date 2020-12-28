package game;

import java.io.*;
import java.util.*;

public class CombatStats implements Serializable {

	private int health;
	private int moveSpeed;
	private int ticksToBuild;
	private int ticksToHeal;
	private transient ArrayList<Integer> stats = new ArrayList<Integer>();
	private transient LinkedList<String> strings = new LinkedList<String>();
	
	/**
	 * @param health
	 * @param attack
	 * @param moveSpeed
	 * @param attackRadius
	 * @param attackSpeed
	 * @param ticksToBuild
	 * @param ticksToHeal
	**/
	public CombatStats(int health, int moveSpeed, int ticksToBuild, int ticksToHeal) {
		this.health = health;
		this.moveSpeed = moveSpeed;
		this.ticksToBuild = ticksToBuild;
		this.ticksToHeal = ticksToHeal;	
		strings.add("health");
		strings.add("moveSpeed");
		strings.add("ticksToBuild");
		strings.add("ticksToHeal");
		stats.add(health);
		stats.add(moveSpeed);
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
	public int getMoveSpeed() {
		return moveSpeed;
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
		if(s.equals("moveSpeed")) {
			return moveSpeed;
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
		if(s.equals("moveSpeed")) {
			moveSpeed =- i;
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
		this.moveSpeed = cs.getMoveSpeed();
		this.ticksToBuild = cs.getTicksToBuild();
		this.ticksToHeal = cs.getHealSpeed();
	}
	public void combine(CombatStats cs) {
		this.health += cs.getHealth();
		this.moveSpeed -= cs.getMoveSpeed();
		this.ticksToBuild -= cs.getTicksToBuild();
		this.ticksToHeal -= cs.getHealSpeed();
	}
	public void subtract(CombatStats cs) {
		this.health -= cs.getHealth();
		this.moveSpeed += cs.getMoveSpeed();
		this.ticksToBuild += cs.getTicksToBuild();
		this.ticksToHeal += cs.getHealSpeed();
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, speed:%d, heal speed:%d", health, moveSpeed, ticksToHeal);
	}
	
}
