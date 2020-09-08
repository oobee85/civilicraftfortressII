package wildlife;

import java.util.*;

import game.*;
import world.*;

public class LavaGolem extends Animal {

	public LavaGolem(Tile tile, boolean isPlayerControlled) {
		super(UnitType.LAVAGOLEM, tile, isPlayerControlled);
	}
	
	@Override
	public boolean isFireResistant() {
		return true;
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
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		if(buildings.size() > 0) {
			for(Building building : buildings) {
				if(building.getBuildingType() == BuildingType.MINE) {
					setTarget(building);
					return;
				}
			}
			
		}
		return;
	}
	
	
}
