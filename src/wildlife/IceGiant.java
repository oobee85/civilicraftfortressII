package wildlife;

import java.util.*;

import game.*;
import liquid.LiquidType;
import ui.*;
import utils.Thing;
import world.*;

public class IceGiant extends Animal {
	
	public IceGiant(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("ICE_GIANT"), tile, faction);
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
			if(u.getFaction() == World.PLAYER_FACTION) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(u));
				return;
			}
		}
		return;
	}

	@Override
	public void doPassiveThings(World world) {
		super.doPassiveThings(world);
		makeIce(world);
	}
	
	private void makeIce(World world) {
		if(getTile().liquidType != LiquidType.LAVA && getTile().liquidType != LiquidType.WATER) {
			getTile().liquidType = LiquidType.SNOW;
		}else if(getTile().liquidType == LiquidType.WATER) {
			getTile().liquidType = LiquidType.ICE;
		}
		
//		getTile().setModifier(new GroundModifier(GroundModifierType.SNOW, getTile(), 500));
//		world.addGroundModifier(getTile().getModifier());
	}
}
