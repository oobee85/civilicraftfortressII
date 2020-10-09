package game;


import java.util.*;

import liquid.*;
import pathfinding.*;
import ui.*;
import utils.*;
import wildlife.Animal;
import wildlife.Dragon;
import world.*;

public class Unit extends Thing  {
	
	private UnitType unitType;
	private double timeToMove;
	private double timeToAttack;
	private double timeToHeal;
	private Thing target;
	private int remainingEffort;
	private boolean isIdle;
	private CombatStats combatStats;
	private LinkedList<Tile> currentPath;
	private LinkedList<Tile> queuedPath;
	private LinkedList<Attack> attacks;
	
	
	public Unit(UnitType unitType, Tile tile, boolean isPlayerControlled) {
		super(unitType.getCombatStats().getHealth(), unitType, isPlayerControlled, tile);
		this.unitType = unitType;
		this.combatStats = unitType.getCombatStats();
		this.timeToAttack = unitType.getCombatStats().getAttackSpeed();
		this.remainingEffort = unitType.getCombatStats().getTicksToBuild();
		this.timeToHeal = unitType.getCombatStats().getHealSpeed();
		this.isIdle = false;

		attacks = new LinkedList<>();
		// projectile attacks
		if(unitType.getProjectileType() != null) {
			addAttackType(new Attack(unitType.getCombatStats().getAttackRadius(), unitType.getProjectileType(), unitType.getCombatStats().getAttackSpeed()));
		}
		// melee attacks
		if(unitType.getCombatStats().getAttack() > 0) {
			addAttackType(new Attack(1, unitType.getCombatStats().getAttack(), unitType.getCombatStats().getAttackSpeed()));
		}
	}
	
	public void setType(UnitType type) {
		this.unitType = type;
		this.setImage(this.getType());
	}
	
	public void addToPath(Tile t) {
		if(queuedPath == null) {
			queuedPath = new LinkedList<Tile>();
		}
		queuedPath.add(t);

	}
	public void addAttackType(Attack a) {
		attacks.add(a);
	}
	
	public void expendEffort(int effort) {
		remainingEffort -= effort;
		if(remainingEffort < 0) {
			remainingEffort = 0;
		}
	}
	public CombatStats getCombatStats() {
		return combatStats;
	}
	public void setCombatStats(CombatStats cm) {
		combatStats = cm;
	}
	
	
	public int getRemainingEffort() {
		return remainingEffort;
	}
	public void setRemainingEffort(int effort) {
		remainingEffort = effort;
	}
	public boolean isBuilt() {
		return remainingEffort <= 0;
	}
	
	public void setTarget(Thing t) {
		target = t;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
	public double computeDanger(Tile tile) {
		// currently only tile damage but at some point might check if enemies there
		return tile.computeTileDamage(this);
	}
	
	public double movePenaltyTo(Tile from, Tile to) {
		double penalty = to.getTerrain().moveSpeed();
		
		if(from.getRoad() != null && to.getRoad() != null && from.getRoad().isBuilt() && to.getRoad().isBuilt()) {
			penalty = penalty/from.getRoad().getRoadType().getSpeed()/2;
		}
		if(this.getUnitType().isFlying()) {
			penalty = 0;
		}
		penalty += combatStats.getMoveSpeed();
		return penalty;
	}
	
	public boolean moveTo(Tile t) {
		if(!readyToMove()) {
			return false;
		}
		if(t.canMove(this) == false) {
			return false;
		}
		if(this.getTargetTile() == null) {
			this.setTargetTile(this.getTile());
		}
		double penalty = movePenaltyTo(this.getTile(), t);
		timeToMove += penalty;
		
		getTile().removeUnit(this);
		t.addUnit(this);
		this.setTile(t);
		
		if(this.getUnitType() == UnitType.ENT && t.canPlant() == true) {
			t.setTerrain(Terrain.GRASS);
		}
		return true;
	}
	
	public void moveTowards(Tile tile) {
		if(((currentPath == null || currentPath.isEmpty() || currentPath.getLast() != tile) && tile != this.getTile())
				|| (currentPath != null && !currentPath.isEmpty() && !currentPath.getFirst().canMove(this))) {
			currentPath = Pathfinding.getBestPath(this, this.getTile(), tile);
		}
		if(currentPath != null && !currentPath.isEmpty()) {
			Tile targetTile = currentPath.getFirst();
			boolean success = this.moveTo(targetTile);
			if(success) {
				currentPath.removeFirst();
			}
		}
	}

	public void updateState() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
		if(timeToAttack > 0) {
			timeToAttack -= 1;
		}
		if(timeToHeal > 0) {
			timeToHeal -= 1;
		}
		isIdle = readyToMove() && readyToAttack() && target == null && getTargetTile() == null && isPlayerControlled() && getIsSelected() == false;
		
		if(getHealth() < combatStats.getHealth() && readyToHeal()) {
			heal(1, false);
			resetTimeToHeal();
		}
		// Take environment damage every 5 ticks
		if(Game.ticks % World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			int tileDamage = (int)getTile().computeTileDamage(this);
			if (tileDamage != 0) {
				this.takeDamage(tileDamage);
			}
		}
	}
	
	public boolean inRange(Thing other) {
		if(other == null) {
			return false;
		}
		return !(this.getTile().getLocation().distanceTo(other.getTile().getLocation()) > combatStats.getAttackRadius() 
				&& this.getTile() != other.getTile());
	}
	
	
	/**
	 * this function does not check the attack range!
	 * @return amount of damage dealt to target
	 */
	public double attack(Thing other) {
		if(other == null || timeToAttack > 0 || other.isDead()) {
			return 0;
		}
		double initialHP = other.getHealth();
		
		other.takeDamage(combatStats.getAttack());
		double damageDealt = initialHP - (other.getHealth() < 0 ? 0 : other.getHealth());
		if(unitType.hasLifeSteal() && !(other instanceof Building)) {
			this.heal(combatStats.getAttack(), true);
		}
		resetTimeToAttack();
		if(other instanceof Unit) {
			((Unit)other).aggro(this);
		}
		return damageDealt;
	}
	
	public void aggro(Unit attacker) {
		if(this.isPlayerControlled() && attacker.isPlayerControlled()) {
			return;
		}
		this.setTarget(attacker);
	}
	
	public Attack chooseAttack(Thing target) {
		for(Attack a : attacks) {
			int distance = this.getTile().getLocation().distanceTo(target.getTile().getLocation());
			if(distance <= a.range && (a.projectileType == null || distance >= a.projectileType.getMinimumRange())) {
				return a;
			}
		}
		return null;
	}
	

	private Building getBuildingToBuild(LinkedList<Building> buildings, LinkedList<Building> plannedBuildings) {
		if(buildings.isEmpty()) {
			return null;
		}
		for(Building building : buildings) {
			if(building.isBuilt() == false) {
				return building;
			}
		}
		for(Building pBuilding : plannedBuildings) {
			return pBuilding;
		}
		return null;
	}
	
	public void planActions(LinkedList<Unit> units, LinkedList<Building> buildings, LinkedList<Building> plannedBuildings) {
		// Workers deciding whether to move toward something to build
		if (unitType.isBuilder() && isIdle && getTile().getIsTerritory()) {
			Building building = getBuildingToBuild(buildings, plannedBuildings);
			if(building != null && building.getTile().getIsTerritory() == true) {
				setTargetTile(building.getTile());
			}
		}
	}
	
	public void doMovement(HashMap<ItemType, Item> items) {
		if(getTargetTile() == null && queuedPath != null && !queuedPath.isEmpty()) {
			setTargetTile(queuedPath.getFirst());
			queuedPath.remove();
		}
		if (readyToMove() && getTargetTile() != null) {
			moveTowards(getTargetTile());
		}
		// If on tile with an item, take the item
		if(this.isPlayerControlled()) {
			for(Item item : getTile().getItems()) {
				items.get(item.getType()).addAmount(item.getAmount());
			}
			getTile().clearItems();
		}
	}
	
	public void doAttacks(World world) {
		boolean attacked = false;
		if(target != null) {
			attacked = Attack.tryToAttack(this, target);
			if(target.isDead()) {
				target = null;
			}
		}
		if(!attacked && isPlayerControlled()) {
			for(Unit enemyUnit : world.getHostileUnitsInTerritory()){
				if(!inRange(enemyUnit)) {
					continue;
				}
				Attack.tryToAttack(this, enemyUnit);
			}
		}
	}

	private Building getAdjacentUnfinishedBuilding(Tile tile) {
		Building tobuild = tile.getBuilding();
		if(tobuild != null && !tobuild.isBuilt()) {
			return tobuild;
		}
		for(Tile neighbor : tile.getNeighbors()) {
			tobuild = neighbor.getBuilding();
			if(tobuild != null && !tobuild.isBuilt()) {
				return tobuild;
			}
		}
		return null;
	}
	
	public void doPassiveThings(World world) {
		// Workers building stuff
		if(getType().isBuilder()) {
			Building tobuild = getAdjacentUnfinishedBuilding(getTile());
			if(tobuild != null) {
				tobuild.expendEffort(1);
				if(tobuild.getRemainingEffort() > 0) {
					tobuild.heal(tobuild.getMaxHealth()/tobuild.getType().getBuildingEffort(), false);
				}
				if(tobuild.getRemainingEffort() < tobuild.getType().getBuildingEffort()) {
					tobuild.setPlanned(false);
				}
			}
			for(Tile tile : this.getTile().getNeighbors()) {
				if(tile.getRoad() != null && tile.getRoad().getRemainingEffort() > 0) {
					tile.getRoad().expendEffort(1);
				}
			}
			
		}
	}
	
	public void resetTimeToAttack() {
		timeToAttack = combatStats.getAttackSpeed();
	}
	public Thing getTarget() {
		return target;
	}
	public UnitType getType() {
		return unitType;
	}
	
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	public double getTimeToMove() {
		return timeToMove;
	}
	public boolean readyToAttack() {
		return timeToAttack <= 0;
	}
	public double getTimeToAttack() {
		return timeToAttack;
	}
	public void setTimeToAttack(int x) {
		timeToAttack = x;
	}
	public double getTimeToHeal() {
		return timeToHeal;
	}
	public boolean readyToHeal() {
		return timeToHeal <= 0;
	}
	public void resetTimeToHeal() {
		timeToHeal = combatStats.getHealSpeed();
	}
	public boolean isIdle() {
		return isIdle;
	}
	public boolean isRanged() {
		return unitType.isRanged();
	}
	
	@Override
	public List<String> getDebugStrings() {
		List<String> strings = super.getDebugStrings();
		strings.add(String.format("TTM=%.1f", getTimeToMove()));
		strings.add(String.format("TTA=%.1f", getTimeToAttack()));
		strings.add(String.format("TTH=%.1f", getTimeToHeal()));
		return strings;
	}
	@Override
	public String toString() {
		return unitType.toString();
	}
}
