package wildlife;

import java.util.*;

import game.*;
import world.Tile;

public class Cyclops extends Animal {

	public Cyclops(Tile tile, boolean isPlayerControlled) {
		super(UnitType.CYCLOPS, tile, isPlayerControlled);
	}

	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToReproduce() {
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
		setTarget(null);
	}
	
	
}
