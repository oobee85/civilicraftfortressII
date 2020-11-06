package wildlife;

import game.*;
import ui.*;
import world.*;

public class Flamelet extends Animal {

	private static int TIME_UNTIL_ATTACK = 0;
	public Flamelet(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("FLAMELET"), tile, faction);
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
		world.getData().addGroundModifier(getTile().getModifier());
	}
	
	@Override
	public void updateState() {
		super.updateState();
		TIME_UNTIL_ATTACK --;
	}
	@Override
	public boolean wantsToAttack() {
		if(TIME_UNTIL_ATTACK <= 0) {
			return true;
		}
		return false;
	}
}
