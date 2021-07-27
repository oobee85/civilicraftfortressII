package game.actions;

import utils.*;
import world.*;
import static game.actions.ActionType.*;

import game.*;

public class PlannedAction {
	
	public static final PlannedAction NOTHING = new PlannedAction(null, false);
	public static final PlannedAction GUARD = new PlannedAction(null, false);
	public static final PlannedAction BUILD = new PlannedAction(null, false);
	
	public final Tile targetTile;
	public final Thing target;
	public final ActionType type;
	private boolean forceDone;
	
	// for stuff like harvesting
	private PlannedAction followup;
	
	
	public PlannedAction(PlannedAction copy) {
		this.targetTile = copy.targetTile;
		this.target = copy.target;
		this.type = copy.type;
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
	public PlannedAction(Thing target, ActionType type) {
		this.targetTile = null;
		this.target = target;
		this.type = type;
	}
	public PlannedAction(Tile target, ActionType type) {
		this.targetTile = target;
		this.target = null;
		this.type = type;
	}
	public PlannedAction(Thing target, ActionType type, PlannedAction followup) {
		this.targetTile = null;
		this.target = target;
		this.type = type;
		this.followup = followup;
	}
	public PlannedAction getFollowUp() {
		return this.followup;
	}
	public Tile getTile() {
		return (target == null || target.getTile() == null) ? targetTile : target.getTile();
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
	public boolean isTakeItemsAction() {
		return type == TAKE_ITEMS;
	}
	public boolean isBuildRoadAction() {
		return type == BUILD_ROAD;
	}
	public boolean isBuildBuildingAction() {
		return type == BUILD_BUILDING;
	}
	
	public boolean inRange(Unit actor) {
		int range = 0;
		if(type == BUILD_BUILDING || type == BUILD_ROAD ||
				type == HARVEST || type == DELIVER || type == TAKE_ITEMS) {
			range = 1;
		}
		else if(type == ATTACK) {
			range = actor.getMaxAttackRange();
		}
		return actor.getTile().distanceTo(this.getTile()) <= range;
	}
	
	public boolean isDone(Thing actor) {
		if(forceDone == true) {
			return true;
		}
		if(type == MOVE) {
			return actor.getTile() == targetTile;
		}
		else if(type == ATTACK) {
			if(target != null) {
				return target.isDead();
			}
			else {
				return actor.getTile() == targetTile;
			}
		}
		else if(type == BUILD_BUILDING) {
			return targetTile.getBuilding() == null 
					|| targetTile.getBuilding().isDead() 
					|| targetTile.getBuilding().isBuilt();
		}
		else if(type == BUILD_ROAD) {
			return targetTile.getRoad() == null 
					|| targetTile.getRoad().isDead() 
					|| targetTile.getRoad().isBuilt();
		}
		else if(type == HARVEST) {
			return actor.getInventory().isFull() 
					|| (target != null && target.isDead());
		}
		else if(type == DELIVER) {
			return actor.getTile() == targetTile;
		}
		else if(type == TAKE_ITEMS) {
			return actor.getTile() == targetTile;
		}
		return actor.getTile() == targetTile;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{type: ");
		sb.append(type);
		if(targetTile != null ) {
			sb.append(", target: ");
			sb.append(targetTile);
		}
		if(target != null) {
			sb.append(", target: ");
			sb.append(target);
		}
		if(followup != null) {
			sb.append(", followup: ");
			sb.append(followup);
		}
		sb.append("}");
		return sb.toString();
	}
}
