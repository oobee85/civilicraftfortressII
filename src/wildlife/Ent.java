package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Ent extends Animal {

	public Ent(Tile tile, int faction) {
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
			if(b.getType() == BuildingType.SAWMILL) {
				setTarget(b);
				return;
			}
		}
		for(Unit u : units) {
			if(u.getFaction() == World.PLAYER_FACTION) {
				setTarget(u);
				return;
			}
		}
		if(buildings.size() > 0) {
			setTarget(buildings.get((int)(Math.random()*buildings.size())));
			return;
		}
		return;
	}
	
	
}
