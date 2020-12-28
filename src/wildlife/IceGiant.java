package wildlife;

import game.*;
import game.liquid.*;
import ui.*;
import world.*;

public class IceGiant extends Animal {
	
	public IceGiant(Tile tile, Faction faction) {
		super(Game.unitTypeMap.get("ICE_GIANT"), tile, faction);
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
