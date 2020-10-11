package wildlife;

import java.util.*;

import game.*;
import liquid.*;
import world.*;

public class WaterSpirit extends Animal {

	public WaterSpirit(Tile tile, boolean isPlayerControlled) {
		super(UnitType.WATER_SPIRIT, tile, isPlayerControlled);
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
		if(getTile().liquidType == LiquidType.DRY) {
			getTile().liquidType = LiquidType.WATER;
		}
		if(getTile().liquidType == LiquidType.WATER) {
			getTile().liquidAmount += 0.003;
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
