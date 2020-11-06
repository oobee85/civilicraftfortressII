package wildlife;

import game.*;
import ui.*;
import world.*;

public class Ent extends Animal {

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
}
