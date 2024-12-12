package wildlife;

import game.*;
import ui.*;
import world.*;
import world.liquid.*;

public class Balrog extends Animal {
	
	public Balrog(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("BALROG"), tile, faction);
	}
	
	@Override
	public boolean wantsToAttack() {
		return true;
	}

	@Override
	public void doPassiveThings(World world) {
		super.doPassiveThings(world);
		if(getTile().getModifier() != null) {
			if(getTile().getModifier().getType() != GroundModifierType.FIRE) {
				this.getTile().getModifier().finish();
				makeFlame(world);
			}
			else {
				this.getTile().replaceOrAddDurationModifier(GroundModifierType.FIRE, 100, world.getData());
			}
		}
		else {
			makeFlame(world);
		}
	}
	
	private void makeFlame(World world) {
		getTile().setModifier(new GroundModifier(GroundModifierType.FIRE, this.getTile(), 100));
		world.getData().addGroundModifier(getTile().getModifier());
	}
}
