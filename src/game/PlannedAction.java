package game;

import utils.*;
import world.*;

public class PlannedAction {
	public static final PlannedAction NOTHING = new PlannedAction(null, false);
	public static final PlannedAction GUARD = new PlannedAction(null, false);
	public static final PlannedAction BUILD = new PlannedAction(null, false);
	
	private static final int NOT_BUILD = 0;
	private static final int BUILDING = 1;
	private static final int ROAD = 2;
	
	public final Tile targetTile;
	public final Thing target;
	public final int build;
	public PlannedAction(Tile targetTile) {
		this.targetTile = targetTile;
		this.target = null;
		this.build = NOT_BUILD;
	}
	public PlannedAction(Thing target) {
		this.targetTile = null;
		this.target = target;
		this.build = NOT_BUILD;
	}
	public PlannedAction(Tile targetTile, boolean isRoad) {
		this.targetTile = targetTile;
		this.target = null;
		this.build = isRoad ? ROAD : BUILDING;
	}
	
	public Tile getTile() {
		return target == null ? targetTile : target.getTile();
	}
	
	public boolean isBuildAction() {
		return targetTile != null && build != NOT_BUILD;
	}
	public boolean isBuildRoadAction() {
		return build == ROAD;
	}
	public boolean isBuildBuildingAction() {
		return build == BUILDING;
	}
	
	public boolean isDone(Tile currentPosition) {
		if(build == BUILDING) {
			if(targetTile.getBuilding() == null) {
				return true;
			}
			return (targetTile.getBuilding().isDead() || targetTile.getBuilding().isBuilt());
		}
		if(build == ROAD) {
			if(targetTile.getRoad() == null) {
				return true;
			}
			return (targetTile.getRoad().isDead() || targetTile.getRoad().isBuilt());
		}
		if(target != null) {
			return target.isDead();
		}
		return currentPosition == targetTile;
	}
}
