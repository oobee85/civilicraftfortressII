package game;

import utils.*;
import world.*;

public class PlannedAction {
	
	public static final PlannedAction NOTHING = new PlannedAction(null, false);
	public static final PlannedAction GUARD = new PlannedAction(null, false);
	public static final PlannedAction BUILD = new PlannedAction(null, false);
	
	public static final int NO_TYPE = 0;
	public static final int BUILD_BUILDING = 1;
	public static final int BUILD_ROAD = 2;
	public static final int HARVEST = 3;
	public static final int DELIVER = 4;
	public static final int TAKE = 5;
	
	public final Tile targetTile;
	public final Thing target;
	public final int type;
	private boolean forceDone;
	
	// for stuff like harvesting
	private PlannedAction followup;
	
	public PlannedAction(Tile targetTile) {
		this.targetTile = targetTile;
		this.target = null;
		this.type = NO_TYPE;
	}
	public PlannedAction(Thing target) {
		this.targetTile = null;
		this.target = target;
		this.type = NO_TYPE;
	}
	public PlannedAction(Tile targetTile, boolean isRoad) {
		this.targetTile = targetTile;
		this.target = null;
		this.type = isRoad ? BUILD_ROAD : BUILD_BUILDING;
	}
	public PlannedAction(Thing target, int type) {
		this.targetTile = null;
		this.target = target;
		this.type = type;
	}
	public PlannedAction(Tile target, int type) {
		this.targetTile = target;
		this.target = null;
		this.type = type;
	}
	public PlannedAction(Thing target, int type, PlannedAction followup) {
		this.targetTile = null;
		this.target = target;
		this.type = type;
		this.followup = followup;
	}
	public PlannedAction getFollowUp() {
		return this.followup;
	}
	public Tile getTile() {
		return target == null ? targetTile : target.getTile();
	}
	public void setDone(boolean done) {
		forceDone = done;
	}
	public boolean isBuildAction() {
		return targetTile != null && (type == BUILD_ROAD || type == BUILD_BUILDING);
	}
	public boolean isHarvestAction() {
		return type == HARVEST;
	}
	public boolean isDeliverAction() {
		return type == DELIVER;
	}
	public boolean isTakeAction() {
		return type == TAKE;
	}
	public boolean isBuildRoadAction() {
		return type == BUILD_ROAD;
	}
	public boolean isBuildBuildingAction() {
		return type == BUILD_BUILDING;
	}
	
	public boolean isDone(Tile currentPosition) {
		if(forceDone == true) {
			return true;
		}
		if(type == BUILD_BUILDING) {
			if(targetTile.getBuilding() == null) {
				return true;
			}
			return (targetTile.getBuilding().isDead() || targetTile.getBuilding().isBuilt());
		}
		if(type == BUILD_ROAD) {
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
	
	@Override
	public String toString() {
		String str = "{type: " + type;
		
		if(targetTile != null ) {
			str += ", target: " + targetTile;
		}
		if(target != null) {
			str += ", target: " + target;
		}
		if(followup != null) {
			str += ", followup: " + followup;
		}
		return str + "}";
	}
}
