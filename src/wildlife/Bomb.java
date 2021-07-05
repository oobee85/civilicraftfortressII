package wildlife;

import game.*;
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
	public boolean takeDamage(int[] damage) {
		if(!isDead()) {
			super.takeDamage(damage);
			world.spawnExplosion(getTile(), 5, DamageType.makeDamageArray(500, DamageType.FIRE));
			this.setDead(true);
		}
		return true;
	}
	
	@Override
	public boolean attack(Thing target) {
		if(target.getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
			takeDamage(DamageType.makeDamageArray(1, DamageType.PHYSICAL));
			return true;
		}
		return false;
	
	}

}
