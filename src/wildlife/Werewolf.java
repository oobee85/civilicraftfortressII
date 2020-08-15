package wildlife;

import java.util.*;

import game.*;
import world.*;

public class Werewolf extends Animal {

	public Werewolf(Tile tile, boolean isPlayerControlled) {
		super(UnitType.WEREWOLF, tile, isPlayerControlled);
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
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				setTarget(u);
				return;
			}
		}
		return;
	}
}
