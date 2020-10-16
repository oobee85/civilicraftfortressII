package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.Tile;

public class Cyclops extends Animal {

	public Cyclops(Tile tile, int faction) {
		super(Game.unitTypeMap.get("CYCLOPS"), tile, faction);
	}

	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToAttack() {
		return false;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
//		for(Building b : buildings) {
//			if(b.getBuildingType() == BuildingType.SAWMILL) {
//				setTarget(b);
//				return;
//			}
//		}
//		for(Unit u : units) {
//			if(u.isPlayerControlled()) {
//				setTarget(u);
//				return;
//			}
//		}
//		if(buildings.size() > 0) {
//			setTarget(buildings.get((int)(Math.random()*buildings.size())));
//			return;
//		}
//		return;
	}
	
	
}
