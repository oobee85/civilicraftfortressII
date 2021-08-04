package wildlife;

import java.util.*;

import game.*;
import game.actions.*;
import utils.*;
import world.*;

public class Animal extends Unit {
	private static final int TARGETING_COOLDOWN = 10;
	
	private int migratingUntil;
	private int nextTimeToChooseTarget;
	private int whenToInvade;
	
	public Animal(UnitType type, Tile tile, Faction faction) {
		super(type, tile, faction);
		
		if(type.isDelayedInvasion()) {
			whenToInvade = World.ticks + 2000;
		}
	}
	
	public boolean getHasHome() {
		return false;
	}

	@Override
	public boolean readyToInvade() {
		return World.ticks > whenToInvade;
	}

	public boolean wantsToEat() {
		return getType().getTargetingInfo().isEmpty() && Math.random() < 0.005;
	}
	
	@Override
	public void planActions(World world) {
		chooseWhatToEat(world.getUnits());
		if(wantsToAttack() && getTarget() == null && World.ticks >= nextTimeToChooseTarget) {
			chooseWhatToAttack(world.getUnits(), world.getBuildings());
			nextTimeToChooseTarget = World.ticks + TARGETING_COOLDOWN;
		}
		chooseWhereToMove(world);
	}
	
	@Override
	public void doPassiveThings(World world) {
	}
	
	private int computeHerd(Tile tile) {
		int herd = 0;
		for(Tile t : tile.getNeighbors()) {
			for(Unit u : t.getUnits()) {
				if(u != this && u.getUnitType() == this.getUnitType()) {
					herd+=10;
				}
			}
		}
		for(Unit u : tile.getUnits()) {
			if(u == this) {
				continue;
			}
			if(u.getUnitType() == this.getUnitType()) {
				herd-=5;
			}
			else {
				herd -= 1;
			}
		}
		return herd;
	}
	
	public void chooseWhereToMove(World world) {
		// everything below only if idle
		if(!isIdle()) {
			return;
		}
		// Try to avoid danger
		Tile best = getTile();
		double currentDanger = this.applyResistance(best.computeTileDanger());
		double bestDanger = currentDanger;
		for(Tile t : getTile().getNeighbors()) {
			if(t.isBlocked(this)) {
				continue;
			}
			double danger = this.applyResistance(t.computeTileDanger());
			if(danger < bestDanger) {
				best = t;
				bestDanger = danger;
			}
		}
		if(bestDanger < currentDanger && currentDanger >= 0.9) {
			if(best != getTile()) {
				queuePlannedAction(new PlannedAction(best, ActionType.MOVE));
			}
			return;
		}
		// Try to stay next to same species
		if(Math.random() < 0.1) {
			Tile bestHerd = getTile();
			int currentHerdAmount = computeHerd(bestHerd); 
			int bestHerdAmount = currentHerdAmount; 
			for(Tile t : getTile().getNeighbors()) {
				if(t.isBlocked(this)) {
					continue;
				}
				int herdAmount = computeHerd(t); 
				if(herdAmount > bestHerdAmount) {
					bestHerd = t;
					bestHerdAmount = herdAmount;
				}
			}
			if(bestHerdAmount > currentHerdAmount && this.applyResistance(bestHerd.computeTileDanger()) < 1) {
				if(bestHerd != getTile()) {
					queuePlannedAction(new PlannedAction(bestHerd, ActionType.MOVE));
					return;
				}
			}
		}
		// Migrate according to the season
		if(getType().isMigratory() && World.ticks > migratingUntil && Math.random() < 0.1) {
			double season = Seasons.getSeason4migration();
			Tile migrationTarget = null;
			// heading into winter
			if(season > 0.4 && season < 0.8 && getTile().getLocation().y() < world.getHeight()/2) {
				migrationTarget = world.get(new TileLoc((int)(Math.random() * world.getWidth()), getTile().getLocation().y() + (int)(Math.random()*world.getHeight()/4 + world.getHeight()/4)));
			}
			// heading into summer
			else if(season > 1.4 && season < 1.8 && getTile().getLocation().y() > world.getHeight()/2) {
				migrationTarget = world.get(new TileLoc((int)(Math.random() * world.getWidth()), getTile().getLocation().y() - (int)(Math.random()*world.getHeight()/4 + world.getHeight()/4)));
			}
			if(migrationTarget != null) {
//				System.out.println(this.getType() + " at " + this.getTile() + " migrating to " + migrationTarget);
				migratingUntil = World.ticks + World.SEASON_DURATION/2;
				queuePlannedAction(new PlannedAction(migrationTarget, ActionType.MOVE));
			}
		}
	}

	private void chooseWhatToEat(LinkedList<Unit> units) {
		if(!wantsToEat()) {
			return;
		}
		if(getType().isHostile() == true) {
			Unit iveGotYouInMySights = null;
			if(!units.isEmpty()) {
				iveGotYouInMySights = units.get((int) (units.size()*Math.random()));
			}
			if(iveGotYouInMySights != this) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(iveGotYouInMySights, ActionType.ATTACK));
			}
		}
		else {
			if(getTile().getPlant() != null) {
				queuePlannedAction(new PlannedAction(getTile().getPlant()));
			}
			else {
				for(Tile neighbor : getTile().getNeighbors()) {
					if(neighbor.getPlant() != null) {
						queuePlannedAction(new PlannedAction(neighbor.getPlant()));
					}
				}
			}
		}
	}
	
	@Override
	public boolean readyToMove() {
		if(getTile().getBuilding() != null && getTile().getBuilding().getType() == Game.buildingTypeMap.get("FARM")) {
			return false;
		}
		return super.readyToMove();
	}
	
	public boolean wantsToAttack() {
		return !getType().getTargetingInfo().isEmpty();
	}
	
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		for(TargetingInfo targetType : getType().getTargetingInfo()) {
			Thing target = targetType.getValidTargetFor(this, units, buildings);
			if(target != null) {
				clearPlannedActions();
				queuePlannedAction(new PlannedAction(target, ActionType.ATTACK));
				return;
			}
		}
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getMoveSpeed()*0.02 
				+ 0.8*(1 - getHealth()/getMaxHealth());
	}
}
