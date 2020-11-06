package wildlife;

import game.*;
import ui.*;
import world.*;

public class Ent extends Animal {
	private static int TIME_UNTIL_ATTACK = 500;
	
	public Ent(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("ENT"), tile, faction);
	}
	@Override
	public boolean moveTo(Tile t) {
		boolean moved = super.moveTo(t);
		if(moved && t.canPlant() == true) {
			t.setTerrain(Terrain.GRASS);
		}
		return moved;
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
