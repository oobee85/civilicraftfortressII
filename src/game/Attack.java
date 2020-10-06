package game;

import ui.*;
import utils.*;
import world.*;

public class Attack {
	
	public int range;
	public int damage;
	public boolean lifesteal;
	public int cooldown;
	public ProjectileType projectileType;
	
	public Attack(int range, int damage, int cooldown) {
		this.range = range;
		this.damage = damage;
		this.cooldown = cooldown;
	}
	
	public Attack(int range, ProjectileType projectileType, int cooldown) {
		this.range = range;
		this.projectileType = projectileType;
		this.cooldown = cooldown;
	}
	
	public static World world;

	public static boolean tryToAttack(Unit unit, Thing target) {
		Attack attack = unit.chooseAttack(target);
		if(attack != null) {
			// actually do the attack
			if(attack.projectileType == null) {
				smack(unit, target);
			}
			else {
				shoot(unit, target, attack);
			}
		}
		return false;
	}

	public static void shoot(Unit unit, Thing target, Attack attack) {
		if(unit.readyToAttack() && !target.isDead()) {
			Projectile p = new Projectile(attack.projectileType, unit.getTile(), target.getTile(), unit);
			p.setDamageBuff(Game.combatBuffs.getAttack());
			world.projectiles.add(p);
			unit.getTile().addProjectile(p);
			unit.resetTimeToAttack();
		}
	}
	
	public static void smack(Unit unit, Thing target) {
		CombatStats combine = new CombatStats(0,0,0,0,0,0,0);
		combine.set(unit.getType().getCombatStats());
		combine.combine(Game.combatBuffs);
		unit.setCombatStats(combine);
		unit.attack(target);
	}
}
