package game;

import utils.*;
import world.*;

public class PlannedAction {
	public static final PlannedAction NOTHING = new PlannedAction(null, false);
	public static final PlannedAction GUARD = new PlannedAction(null, false);
	public static final PlannedAction BUILD = new PlannedAction(null, false);
	public static final PlannedAction WAITTOATTACK = new PlannedAction(null, false);
	
	
	public final Tile targetTile;
	public final Thing target;
	public final boolean build;
	public final int whenToAtack;
	public PlannedAction(Tile targetTile) {
		this.targetTile = targetTile;
		this.target = null;
		this.build = false;
		this.whenToAtack = 0;
	}
	public PlannedAction(Thing target) {
		this.targetTile = null;
		this.target = target;
		this.build = false;
		this.whenToAtack = 0;
	}
	public PlannedAction(Thing target, boolean build) {
		this.targetTile = null;
		this.target = target;
		this.build = build;
		this.whenToAtack = 0;
	}
	public PlannedAction(Tile target, int time) {
		this.targetTile = target;
		this.target = null;
		this.build = false;
		this.whenToAtack = time;
	}
	
	public Tile getTile() {
		return target == null ? targetTile : target.getTile();
	}
	
	public boolean isBuildAction() {
		return target != null && target instanceof Building && build;
	}
	
	
	public boolean isDone(Tile currentPosition) {
		if(target == null) {
			return currentPosition == targetTile;
		}
		if(!build && target.isDead()) {
			return true;
		}
		if(build) {
			if(target instanceof Building) {
				if(((Building)target).isBuilt()) {
					return true;
				}
			}
		}
		return false;
	}
}
