package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Bomb extends Animal {

	public Bomb(Tile tile, boolean isPlayerControlled) {
		super(Game.unitTypeMap.get("BOMB"), tile, isPlayerControlled);
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
	public void doAttacks(World world) {
		if(getTargetTile() == getTile()) {
			world.spawnExplosion(getTile(), 5, 500);
			this.setDead(true);
		}
	}

}
