package ui;

import game.*;
import utils.*;
import world.*;

public interface CommandInterface {
	public void setBuildingRallyPoint(Building building, Tile rallyPoint);
	public void setTargetTile(Unit thing, Tile target, boolean clearQueue);
	public void attackThing(Unit thing, Thing target, boolean clearQueue);
}
