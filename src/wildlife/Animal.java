package wildlife;

import java.util.*;

import game.*;
import game.actions.*;
import utils.*;
import world.*;

public class Animal extends Unit {
	private static final int TARGETING_COOLDOWN = 10;
	
	private int dontMigrateUntil;
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
		if (!isIdle()) {
			return;
		}
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
		double currentDanger = this.applyResistance(getTile().computeTileDanger());
		if(currentDanger >= 0.9) {
			Tile best = getTile();
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
			if(bestDanger < currentDanger) {
				if(best != getTile()) {
					queuePlannedAction(PlannedAction.moveTo(best));
					return;
				}
			}
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
					queuePlannedAction(PlannedAction.moveTo(bestHerd));
					return;
				}
			}
		}
		// Go to a random tile
		if(World.ticks > dontMigrateUntil && Math.random() < 0.1) {
			Tile target;
			if(getType().isMigratory())
				target = world.getRandomTile();
			else 
				target = getTile().getNeighbors().get((int)(Math.random()*getTile().getNeighbors().size()));
			if(this.applyResistance(target.computeTileDanger()) <= 0.9) {
				dontMigrateUntil = World.ticks + (int)((2 + Math.random()*4) * Constants.DAY_DURATION);
				queuePlannedAction(PlannedAction.moveTo(target));
				return;
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
				queuePlannedAction(PlannedAction.attack(iveGotYouInMySights));
			}
		}
		else {
//			if(getTile().getPlant() != null) {
//				queuePlannedAction(PlannedAction.eatPlant(getTile().getPlant()));
//			}
//			else {
//				for(Tile neighbor : getTile().getNeighbors()) {
//					if(neighbor.getPlant() != null) {
//						queuePlannedAction(PlannedAction.eatPlant(neighbor.getPlant()));
//					}
//				}
//			}
		}
	}
	
	@Override
	public boolean readyToMove() {
		// trap doesnt block even when its not completed
		// if animal is on a trap, it cannot move
		if(getTile().getBuilding() != null && getTile().getBuilding().getType().isTrap()) {
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
				queuePlannedAction(PlannedAction.attack(target));
				return;
			}
		}
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getMoveSpeed()*0.02 
				+ 0.8*(1 - getHealth()/getMaxHealth());
	}
}
