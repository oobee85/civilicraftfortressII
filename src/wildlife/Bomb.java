package wildlife;

import game.*;
import sounds.SoundEffect;
import sounds.SoundManager;
import ui.*;
import utils.*;
import world.*;

public class Bomb extends Animal {
	private transient World world;
	public Bomb(Tile tile, Faction faction, World world) {
		super(Game.unitTypeMap.get("BOMB"), tile, faction);
		this.world = world;
	}
	
	@Override
	public boolean takeDamage(int damage, DamageType type) {
		if(!isDead()) {
			super.takeDamage(damage, type);
		}
		if(isDead()) {
			world.spawnExplosionCircle(getTile(), 5, 500);
			SoundManager.queueSoundEffect(SoundEffect.EXPLOSION, getTileLocation());
		}
		return true;
	}
	
	@Override
	public boolean attack(Thing target) {
		this.takeDamage(1000, DamageType.PHYSICAL);
//		if(target.getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
//			takeDamage(100, DamageType.PHYSICAL);
//			return true;
//		}
		return false;
	
	}

}
