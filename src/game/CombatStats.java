package game;

import java.io.*;

public class CombatStats implements Serializable {

	private int health;
	private int moveSpeed;
	private int ticksToBuild;
	private int ticksToHeal;
	
	public CombatStats(int health, int moveSpeed, int ticksToBuild, int ticksToHeal) {
		this.health = health;
		this.moveSpeed = moveSpeed;
		this.ticksToBuild = ticksToBuild;
		this.ticksToHeal = ticksToHeal;	
	}
	/** DO NOT USE THIS to get max health of an individual unit. 
	 *  Use Thing::getMaxHealth() instead since it might 
	 *  get increased or decreased during the unit's lifetime.
	 **/
	public int getHealth() {
		return health;
	}
	public int getMoveSpeed() {
		return moveSpeed;
	}
	public int getTicksToBuild() {
		return ticksToBuild;
	}
	public int getTicksToHeal() {
		return ticksToHeal;
	}
	
	public void addHealth(int health) {
		this.health += health;
		if(this.health < 1) {
			this.health = 1;
		}
	}
	public void addMoveSpeed(int speed) {
		this.moveSpeed += speed;
		if(this.moveSpeed < 1) {
			this.moveSpeed = 1;
		}
	}
	public void addTicksToBuild(int ticks) {
		this.ticksToBuild += ticks;
		if(this.ticksToBuild < 1) {
			this.ticksToBuild = 1;
		}
	}
	public void addTicksToHeal(int ticks) {
		this.ticksToHeal += ticks;
		if(this.ticksToHeal < 1) {
			this.ticksToHeal = 1;
		}
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
	public void setMoveSpeed(int speed) {
		this.moveSpeed = speed;
	}
	public void setTicksToBuild(int ticks) {
		this.ticksToBuild = ticks;
	}
	public void setTicksToHeal(int ticks) {
		this.ticksToHeal = ticks;
	}
	
	public void mergeCombatStats(CombatStats other) {
		this.health += other.getHealth();
		this.moveSpeed += other.getMoveSpeed();
		this.ticksToBuild += other.getTicksToBuild();
		this.ticksToHeal += other.getTicksToHeal();
		if(this.health < 1) {
			this.health = 1;
		}
		if(this.moveSpeed < 1) {
			this.moveSpeed = 1;
		}
		if(this.ticksToBuild < 1) {
			this.ticksToBuild = 1;
		}
		if(this.ticksToHeal < 1) {
			this.ticksToHeal = 1;
		}
	}
	
	@Override
	public String toString() {
		return String.format("health:%d, speed:%d, heal speed:%d", health, moveSpeed, ticksToHeal);
	}
	
}
