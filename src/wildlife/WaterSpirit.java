package wildlife;

import java.util.*;

import game.*;
import ui.*;
import world.*;
import world.liquid.*;

public class WaterSpirit extends Animal {

	public WaterSpirit(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("WATER_SPIRIT"), tile, faction);
	}
	
	@Override
	public boolean wantsToEat() {
		return false;
	}
	
	@Override
	public void updateState() {
		super.updateState();
		if(World.ticks % 20 == 0) {
			if(getTile().liquidType == LiquidType.DRY) {
				getTile().liquidType = LiquidType.WATER;
			}
			if(getTile().liquidType == LiquidType.WATER) {
				getTile().liquidAmount += 0.06;
			}
		}
	}
}
