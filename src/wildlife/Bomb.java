package wildlife;

import java.util.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class Bomb extends Animal {
	private World world;
	public Bomb(Tile tile, Faction faction, World world) {
		super(Game.unitTypeMap.get("BOMB"), tile, faction);
		this.world = world;
	}
	
	@Override
	public boolean takeDamage(double damage) {
		if(!isDead()) {
			super.takeDamage(damage);
			world.spawnExplosion(getTile(), 5, 500);
			this.setDead(true);
		}
		return true;
	}
	
	@Override
	public boolean attack(Thing target) {
		if(target.getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
			takeDamage(1);
			return true;
		}
		return false;
	
	}

}
