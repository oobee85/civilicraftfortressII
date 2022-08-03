package game.actions;

import utils.*;
import world.*;
import static game.actions.ActionType.*;

import java.util.*;

import game.*;

public class PlannedAction {
	
	public static final PlannedAction NOTHING = new PlannedAction(null, false);
	public static final PlannedAction GUARD = new PlannedAction(null, false);
	public static final PlannedAction BUILD = new PlannedAction(null, false);
	
	public final Tile targetTile;
	public final Thing target;
	public final ActionType type;
	
	private boolean forceDone;
	private PlannedAction followup;
	
	private PlannedAction(PlannedAction copy) {
		this.targetTile = copy.targetTile;
		this.target = copy.target;
		this.type = copy.type;
	}
	private PlannedAction(Tile targetTile, ActionType type) {
		this.targetTile = targetTile;
		this.target = null;
		this.type = type;
	}
	private PlannedAction(Tile targetTile, boolean isRoad) {
		this(targetTile, isRoad, null);
	}
	private PlannedAction(Tile targetTile, boolean isRoad, PlannedAction followup) {
		this.targetTile = targetTile;
		this.target = null;
		this.type = isRoad ? BUILD_ROAD : BUILD_BUILDING;
		this.followup = followup;
	}
	
	private PlannedAction(Thing target) {
		this(target, NO_TYPE);
	}
	private PlannedAction(Thing target, ActionType type) {
		this(target, type, null);
	}
	private PlannedAction(Thing target, ActionType type, PlannedAction followup) {
		this.targetTile = null;
		this.target = target;
		this.type = type;
		this.followup = followup;
	}

	public static PlannedAction makeCopy(PlannedAction original) {
		return new PlannedAction(original);
	}
	public static PlannedAction eatPlant(Plant plant) {
		return new PlannedAction(plant);
	}
	public static PlannedAction buildOnTile(Tile tile, boolean road) {
		return new PlannedAction(tile, road);
	}
	public static PlannedAction buildOnTile(Tile tile, boolean road, PlannedAction followup) {
		return new PlannedAction(tile, road, followup);
	}
	public static PlannedAction attack(Thing target) {
		return new PlannedAction(target, ActionType.ATTACK);
	}
	public static PlannedAction harvest(Thing target) {
		return new PlannedAction(target, ActionType.HARVEST);
	}
	public static PlannedAction takeItemsFrom(Thing target) {
		System.out.println("aaAAAAAAAAAAAAA");
		return new PlannedAction(target, ActionType.TAKE_ITEMS);
	}
	public static PlannedAction deliver(Thing target) {
		System.out.println("FOLLOWUP");
		return new PlannedAction(target, ActionType.DELIVER);
	}
	public static PlannedAction deliver(Thing target, PlannedAction followup) {
		System.out.println("FOLLOWUP2222222");
		return new PlannedAction(target, ActionType.DELIVER, followup);
	}
	public static PlannedAction moveTo(Tile targetTile) {
		return new PlannedAction(targetTile, ActionType.MOVE);
	}
	public static PlannedAction attackMoveTo(Tile targetTile) {
		return new PlannedAction(targetTile, ActionType.ATTACK_MOVE);
	}
	public static PlannedAction harvestTile(Tile targetTile) {
		return new PlannedAction(targetTile, ActionType.HARVEST);
	}
	public static PlannedAction wanderAroundTile(Tile targetTile) {
		return new PlannedAction(targetTile, ActionType.WANDER_AROUND);
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
		if(type == BUILD_BUILDING || type == BUILD_ROAD) {
			range = 1;
		}
		else if(type == HARVEST || type == DELIVER || type == TAKE_ITEMS) {
			range = 0;
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
		else if(type == ATTACK_MOVE) {
			return actor.getTile() == targetTile;
		}
		else if(type == WANDER_AROUND) {
			return false;
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
