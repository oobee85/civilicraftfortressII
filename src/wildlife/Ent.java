package wildlife;

import game.*;
import world.Tile;

public class Ent extends Animal {

	public Ent(UnitType type, Tile tile, boolean isPlayerControlled) {
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
