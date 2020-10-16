package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Ogre extends Animal {

	public Ogre(Tile tile, boolean isPlayerControlled) {
		super(Game.unitTypeMap.get("OGRE"), tile, isPlayerControlled);
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
			if(u != this && u.isPlayerControlled()) {
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
