package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;

public class Flamelet extends Animal {

	public Flamelet(Tile tile, int faction) {
		super(Game.unitTypeMap.get("FLAMELET"), tile, faction);
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}
	
	@Override
	public void updateState() {
		super.updateState();
	}
	
	@Override
	public void doPassiveThings(World world) {
		super.doPassiveThings(world);
		if(getTile().getModifier() != null) {
			if(getTile().getModifier().getType() != GroundModifierType.FIRE) {
				this.getTile().getModifier().finish();
				makeFlame(world);
			}
		}
		else {
			makeFlame(world);
		}
	}
	
	private void makeFlame(World world) {
		getTile().setModifier(new GroundModifier(GroundModifierType.FIRE, this.getTile(), 30));
		world.addGroundModifier(getTile().getModifier());
	}

	@Override
	public boolean wantsToAttack() {
		return true;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		if(buildings.size() > 0) {
			setTarget(buildings.get((int)(Math.random()*buildings.size())));
			return;
		}
		return;
	}
	
	

}
