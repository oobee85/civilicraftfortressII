package game;

import java.util.LinkedList;

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

	public static void shoot(Unit unit, Thing target, Attack attack) {
		for(Unit u : target.getTile().getUnits()) {
			if(target.getTile().getThingOfFaction(unit.getFaction()) != null && u.getFaction() != unit.getFaction()) {
				return;
			}
		}
		
		if(attack.projectileType == ProjectileType.FIREWAVE && unit.readyToAttack() && !target.isDead()) {
			fireWave(unit, target, attack);
		}
		if(unit.readyToAttack() && !target.isDead()) {
			Projectile p = new Projectile(attack.projectileType, unit.getTile(), target.getTile(), unit);
			world.getData().addProjectile(p);
			unit.getTile().addProjectile(p);
			unit.resetTimeToAttack();
		}
	}
	public static void fireWave(Unit unit, Thing target, Attack attack) {
		Tile targetTile = target.getTile();
		
		if(unit.readyToAttack()) {
			
			if(targetTile.getLocation().x() == unit.getTile().getLocation().x()) {
				
				for(int x = -1; x < 2; x++) {
					TileLoc tileLoc = new TileLoc(targetTile.getLocation().x() + x, targetTile.getLocation().y());
					TileLoc tl = new TileLoc(unit.getTile().getLocation().x() + x, unit.getTile().getLocation().y());
					
					Projectile p = new Projectile(attack.projectileType, world.get(tl), world.get(tileLoc), unit);
					world.getData().addProjectile(p);
					unit.getTile().addProjectile(p);
				}
				
				
			}
			if(targetTile.getLocation().y() == unit.getTile().getLocation().y()) {
				for(int y = -1; y < 2; y++) {
					TileLoc tileLoc = new TileLoc(targetTile.getLocation().x(), targetTile.getLocation().y() + y);
					TileLoc tl = new TileLoc(unit.getTile().getLocation().x(), unit.getTile().getLocation().y() + y);
					
					Projectile p = new Projectile(attack.projectileType, world.get(tl), world.get(tileLoc), unit);
					world.getData().addProjectile(p);
					unit.getTile().addProjectile(p);
				}
			}
			
			
		}
		unit.resetTimeToAttack();
	}
}
