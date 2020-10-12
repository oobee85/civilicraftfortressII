package wildlife;

import java.util.*;

import game.*;
import liquid.*;
import ui.*;
import utils.*;
import world.*;

public class Animal extends Unit {
	
	private Thing foodTarget;
	private Tile resourceTarget;
	
	private int migratingUntil;
	
	public Animal(UnitType type, Tile tile, boolean isPlayerControlled) {
		super(type, tile, isPlayerControlled);
	}
	
	public boolean getHasHome() {
		return false;
	}
	
	@Override
	public void updateState() {
		super.updateState();
		if(getHealth() < super.getType().getCombatStats().getHealth() && readyToHeal()) {
			heal(1, false);
			resetTimeToHeal();
		}
	}
	@Override
	public boolean takeDamage(double damage) {
		boolean lethal = super.takeDamage(damage);
		if(lethal) {
			if(getType().getDeadItem() != null) {
				getTile().addItem(getType().getDeadItem());
			}
		}
		return lethal;
	}
	
	public boolean isDead() {
		return super.isDead();
	}

	public boolean wantsToEat() {
		return Math.random() < 0.005;
	}
	
	@Override
	public void planActions(World world) {
		chooseWhatToEat(world.units);
		if(wantsToAttack() && getTarget() == null) {
			chooseWhatToAttack(world.units, world.buildings);
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
		if(resourceTarget != null) {
			if(getTile() != resourceTarget) {
				setTargetTile(resourceTarget);
				return;
			}
		}
		if(getTarget() != null) {
			if(this.getTile().getLocation().distanceTo(getTarget().getTile().getLocation()) > getType().getCombatStats().getAttackRadius()) {
				setTargetTile(getTarget().getTile());
				return;
			}
		}
		// Try to avoid danger
		Tile best = getTile();
		double currentDanger = computeDanger(best);
		double bestDanger = currentDanger;
		for(Tile t : getTile().getNeighbors()) {
			if(t.isBlocked(this)) {
				continue;
			}
			double danger = computeDanger(t);
			if(danger < bestDanger) {
				best = t;
				bestDanger = danger;
			}
		}
		if(bestDanger < currentDanger && currentDanger >= 0.9) {
			if(best != getTile()) {
				setTargetTile(best);
			}
			return;
		}
		// everything below only if idle
		if(!isIdle()) {
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
			if(bestHerdAmount > currentHerdAmount && computeDanger(bestHerd) < 1) {
				if(bestHerd != getTile()) {
					setTargetTile(bestHerd);
					return;
				}
			}
		}
		// Migrate according to the season
		if(Game.ticks > migratingUntil && Math.random() < 0.1) {
			double season = World.getSeason4();
			if(season > 0.4 && season < 0.8) {
				// heading into winter
				if(getTile().getLocation().y < world.getHeight()/2) { 
					TileLoc migrationTarget = new TileLoc((int)(Math.random() * world.getWidth()), getTile().getLocation().y + (int)(Math.random()*world.getHeight()/4 + world.getHeight()/4));
					setTargetTile(world.get(migrationTarget));
					System.out.println(this.getType() + " at " + this.getTile() + " migrating to " + this.getTargetTile());
					migratingUntil = Game.ticks + World.SEASON_DURATION/2;
				}
			}
			if(season > 1.4 && season < 1.8) {
				// heading into summer
				if(getTile().getLocation().y > world.getHeight()/2) { 
					TileLoc migrationTarget = new TileLoc((int)(Math.random() * world.getWidth()), getTile().getLocation().y - (int)(Math.random()*world.getHeight()/4 + world.getHeight()/4));
					setTargetTile(world.get(migrationTarget));
					System.out.println(this.getType() + " at " + this.getTile() + " migrating to " + this.getTargetTile());
					migratingUntil = Game.ticks + World.SEASON_DURATION/2;
				}
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
				int pickUnit = (int) (units.size()*Math.random());
				iveGotYouInMySights = units.get(pickUnit);
			}
			if(iveGotYouInMySights != this) {
				setTarget(iveGotYouInMySights);
			}
		}
		else {
			if(!getType().isHostile() && getTile().getPlant() != null) {
				if(readyToAttack()) {
					foodTarget = getTile().getPlant();
				}
			}
		}
	}
	
	@Override
	public void doAttacks(World world) {
		super.doAttacks(world);
		if(foodTarget != null) {
			if(inRange(foodTarget)) {
				double damageDealt = attack(foodTarget);
//				if(this.getType() == UnitType.WEREWOLF) {
//					
//				}
				
				if(damageDealt > 0) {
				}
				if(foodTarget.isDead()) {
					resourceTarget = foodTarget.getTile();
					foodTarget = null;
				}
			}
			return;
		}
	}
	
	@Override
	public boolean readyToMove() {
		if(getTile().getBuilding() != null && getTile().getBuilding().getType() == BuildingType.FARM) {
			return false;
		}
		return super.readyToMove();
	}
	
	public boolean wantsToAttack() {
		return false;
	}
	
	public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) {
		return;
	}
	
	public double getMoveChance() {
		return getType().getCombatStats().getMoveSpeed()*0.02 
				+ 0.8*(1 - getHealth()/super.getType().getCombatStats().getHealth());
	}
}
