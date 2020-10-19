package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Vampire extends Animal {

	public Vampire(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("VAMPIRE"), tile, faction);
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
		for(Unit u : units) {
			if(u.getFaction() == World.PLAYER_FACTION) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(u));
				return;
			}
		}
		if(buildings.size() > 0) {
			Building building = buildings.get((int)(Math.random()*buildings.size()));
			clearPlannedActions();
			queuePlannedAction(new PlannedAction(building));
			return;
		}
		return;
	}
}
