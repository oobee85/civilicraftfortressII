package game;

import utils.*;
import world.*;

public class PlannedAction {
	public static final PlannedAction NOTHING = new PlannedAction(null, null);
	public static final PlannedAction GUARD = new PlannedAction(null, null);
	public static final PlannedAction BUILD = new PlannedAction(null, null);
	
	
	public final Tile targetTile;
	public final Thing target;
	public PlannedAction(Tile targetTile, Thing target) {
		this.targetTile = targetTile;
		this.target = target;
	}
	
	public Tile getTile() {
		return target == null ? targetTile : target.getTile();
	}
}
