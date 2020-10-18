package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Bomb extends Animal {
	
	public Bomb(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("BOMB"), tile, faction);
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(buildings.size() > 0) {
			setTarget(buildings.get((int)(Math.random()*buildings.size())));
			return;
		}
		return;
	}
	
	@Override
	public boolean doAttacks(World world) {
		if(getTarget().getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
			world.spawnExplosion(getTile(), 5, 500);
			this.setDead(true);
			return true;
		}
		return false;
	}

}
