package wildlife;

import java.util.*;

import game.*;
import world.*;

public class Flamelet extends Animal {

	public Flamelet(Tile tile, boolean isPlayerControlled) {
		super(UnitType.FLAMELET, tile, isPlayerControlled);
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
	public void updateState() {
		super.updateState();
		if(getTile().getModifier() != null) {
			if(getTile().getModifier().getType() != GroundModifierType.FIRE) {
				this.getTile().getModifier().finish();
				makeFlame();
			}
		}
		else {
			makeFlame();
		}
	}
	
	private void makeFlame() {
		getTile().setModifier(new GroundModifier(GroundModifierType.FIRE, this.getTile(), 15));
		synchronized(World.groundModifiers) {
			World.groundModifiers.add(getTile().getModifier());
		}
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Animal> animals, LinkedList<Building> buildings) {
		if(buildings.size() > 0) {
			setTarget(buildings.get((int)(Math.random()*buildings.size())));
			return;
		}
		return;
	}
	
	

}
