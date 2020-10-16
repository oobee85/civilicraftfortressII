package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Ogre extends Animal {

	public Ogre(Tile tile, int faction) {
		super(Game.unitTypeMap.get("OGRE"), tile, faction);
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
			if(u != this && u.getFaction() == World.PLAYER_FACTION) {
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
