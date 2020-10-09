package wildlife;

import java.util.*;

import game.*;
import utils.Thing;
import world.*;

public class IceGiant extends Animal {
	
	public IceGiant(Tile tile, boolean isPlayerControlled) {
		super(UnitType.ICE_GIANT, tile, isPlayerControlled);
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
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		for(Unit u : units) {
			if(u.isPlayerControlled()) {
				setTarget(u);
				return;
			}
		}
		return;
	}

	@Override
	public void doPassiveThings(World world) {
		super.doPassiveThings(world);
		if(getTile().getModifier() != null) {
			if(getTile().getModifier().getType() != GroundModifierType.SNOW) {
				this.getTile().getModifier().finish();
				makeIce(world);
			}
		}
		else {
			makeIce(world);
		}
	}
	
	private void makeIce(World world) {
		getTile().setModifier(new GroundModifier(GroundModifierType.SNOW, getTile(), 500));
		world.newGroundModifiers.add(getTile().getModifier());
	}
}
