package game;

import sounds.Sound;
import sounds.SoundEffect;
import sounds.SoundManager;
import utils.*;
import world.*;

public class AttackUtils {
	public static World world;

	public static void shoot(Unit unit, Thing target, AttackStyle style) {
		for(Unit u : target.getTile().getUnits()) {
//			if(target.getTile().getThingOfFaction(unit.getFaction()) != null && u.getFaction() != unit.getFaction()) {
//				return;
//			}
		}
		if(style.getProjectile() == ProjectileType.FIRE_WAVE) {
			fireWave(unit, target, style);
			Sound sound = new Sound(SoundEffect.PROJECTILE_LAUNCH_FIRE, null, unit.getTile(), 1f);
			SoundManager.theSoundQueue.add(sound);
			return;
		}
		Projectile p = new Projectile(style.getProjectile(), unit.getTile(), target.getTile(), unit, style.getDamage());
		unit.getTile().addProjectile(p);
		world.getData().addProjectile(p);
		if(p.isHeavyProjectile()) {
			Sound sound = new Sound(SoundEffect.PROJECTILE_LAUNCH_HEAVY, null, unit.getTile(), 1f);
			SoundManager.theSoundQueue.add(sound);
		}else
		if(p.isLightProjectile() && p.getType() != ProjectileType.BULLET) {
			Sound sound = new Sound(SoundEffect.PROJECTILE_LAUNCH_LIGHT, null, unit.getTile(), 1f);
			SoundManager.theSoundQueue.add(sound);
		}else {
			Sound sound = new Sound(SoundEffect.PROJECTILE_IMPACT_GENERIC, null, unit.getTile(), 1f);
			SoundManager.theSoundQueue.add(sound);
		}
		
	}
	public static void fireWave(Unit unit, Thing target, AttackStyle style) {
		Tile targetTile = target.getTile();
		if(targetTile.getLocation().x() == unit.getTile().getLocation().x()) {
			for(int x = -1; x < 2; x++) {
				TileLoc tileLoc = new TileLoc(targetTile.getLocation().x() + x, targetTile.getLocation().y());
				TileLoc tl = new TileLoc(unit.getTile().getLocation().x() + x, unit.getTile().getLocation().y());
				
				Projectile p = new Projectile(style.getProjectile(), world.get(tl), world.get(tileLoc), unit, style.getDamage());
				unit.getTile().addProjectile(p);
				world.getData().addProjectile(p);
			}
		}
		if(targetTile.getLocation().y() == unit.getTile().getLocation().y()) {
			for(int y = -1; y < 2; y++) {
				TileLoc tileLoc = new TileLoc(targetTile.getLocation().x(), targetTile.getLocation().y() + y);
				TileLoc tl = new TileLoc(unit.getTile().getLocation().x(), unit.getTile().getLocation().y() + y);
				
				Projectile p = new Projectile(style.getProjectile(), world.get(tl), world.get(tileLoc), unit, style.getDamage());
				unit.getTile().addProjectile(p);
				world.getData().addProjectile(p);
			}
		}
	}
}
