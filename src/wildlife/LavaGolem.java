package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class LavaGolem extends Animal {

	public LavaGolem(Tile tile, int faction) {
		super(Game.unitTypeMap.get("LAVAGOLEM"), tile, faction);
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
			for(Building building : buildings) {
				if(building.getType() == BuildingType.MINE) {
					setTarget(building);
					return;
				}
			}
			
		}
		return;
	}
	
	
}
