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
	public boolean takeDamage(int damage, DamageType type) {
		if(!isDead()) {
			super.takeDamage(damage, type);
			world.spawnExplosionCircle(getTile(), 5, 500);
			this.setDead(true);
		}
		return true;
	}
	
	@Override
	public boolean attack(Thing target) {
		if(target.getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
			takeDamage(1, DamageType.PHYSICAL);
			return true;
		}
		return false;
	
	}

}
