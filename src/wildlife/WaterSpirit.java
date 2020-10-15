package wildlife;

import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import world.*;

public class WaterSpirit extends Animal {

	public WaterSpirit(Tile tile, boolean isPlayerControlled) {
		super(Game.unitTypeMap.get("WATER_SPIRIT"), tile, isPlayerControlled);
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}
	
	@Override
	public boolean moveTo(Tile t) {
		return super.moveTo(t);
	}
	
	@Override
	public void updateState() {
		super.updateState();
		if(Game.ticks % 20 == 0) {
			if(getTile().liquidType == LiquidType.DRY) {
				getTile().liquidType = LiquidType.WATER;
			}
			if(getTile().liquidType == LiquidType.WATER) {
				getTile().liquidAmount += 0.06;
			}
		}
	}

	@Override
	public boolean wantsToAttack() {
		return false;
	}
	
	@Override
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		return;
	}

}
