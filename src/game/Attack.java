package game;

import ui.*;
import utils.*;
import world.*;

public class Attack {
	public static World world;

	public static void shoot(Unit unit, Thing target) {
		if(unit.readyToAttack() && !target.isDead()) {
			Projectile p = new Projectile(unit.getType().getProjectileType(), unit.getTile(), target.getTile());
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
