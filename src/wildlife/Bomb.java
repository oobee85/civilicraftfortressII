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
		if(!buildings.isEmpty()) {
			Building target = buildings.get((int)(Math.random()*buildings.size()));
			clearPlannedActions();
			queuePlannedAction(new PlannedAction(target));
		}
	}
	
	@Override
	public boolean doAttacks(World world) {
		if(getTarget() != null && getTarget().getTile().getLocation().distanceTo(getTile().getLocation()) == 0) {
			world.spawnExplosion(getTile(), 5, 500);
			this.setDead(true);
			return true;
		}
		return false;
	}

}
