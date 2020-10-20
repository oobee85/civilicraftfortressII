package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Ent extends Animal {

	public Ent(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("ENT"), tile, faction);
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
	public boolean moveTo(Tile t) {
		boolean moved = super.moveTo(t);
		if(moved && t.canPlant() == true) {
			t.setTerrain(Terrain.GRASS);
		}
		return moved;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		for(Building b : buildings) {
			if(b.getType() == Game.buildingTypeMap.get("SAWMILL")) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(b));
				return;
			}
		}
		for(Unit u : units) {
			if(u.getFaction() == World.PLAYER_FACTION) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(u));
				return;
			}
		}
		if(buildings.size() > 0) {
			Building b = buildings.get((int)(Math.random()*buildings.size()));
			clearPlannedActions();
			queuePlannedAction(new PlannedAction(b));
			return;
		}
		return;
	}
	
	
}
