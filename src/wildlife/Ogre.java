package wildlife;

import game.*;
import world.*;

public class Ogre extends Animal {

	public Ogre(UnitType type, Tile tile, boolean isPlayerControlled) {
		super(type, tile, isPlayerControlled);
	}

	@Override
	public boolean wantsToEat() {
		return false;
	}

	@Override
	public boolean wantsToReproduce() {
		return false;
	}

	
	
	
}
