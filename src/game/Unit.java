package game;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import pathfinding.*;
import utils.*;
import world.*;

public class Unit extends Thing implements Serializable {
	
	private UnitType unitType;
	private transient double timeToMove;
	private transient double timeToAttack;
	private transient double timeToHeal;
	private int remainingEffort;
	private transient boolean isIdle;
	private transient int starving;
	private CombatStats combatStats;
	private transient LinkedList<Tile> currentPath;
	
	public transient ConcurrentLinkedQueue<PlannedAction> actionQueue = new ConcurrentLinkedQueue<>();
	private transient PlannedAction passiveAction = PlannedAction.NOTHING;
	
	private transient LinkedList<Attack> attacks;
	private transient boolean isHarvesting;
	private transient double timeToHarvest;
	private transient double baseTimeToHarvest = 10;
	private transient int ticksForFoodCost = 60;

	public Unit(UnitType unitType, Tile tile, Faction faction) {
		super(unitType.getCombatStats().getHealth(), unitType, faction, tile);
		this.unitType = unitType;
		this.combatStats = unitType.getCombatStats();
		this.timeToAttack = unitType.getCombatStats().getAttackSpeed();
		this.remainingEffort = unitType.getCombatStats().getTicksToBuild();
		this.timeToHeal = unitType.getCombatStats().getHealSpeed();
		this.isIdle = false;

		attacks = new LinkedList<>();
		// projectile attacks
		if (unitType.getProjectileType() != null) {
			if(unitType.getProjectileType().getSpeed() != 0) {
				addAttackType(new Attack(unitType.getCombatStats().getAttackRadius(), unitType.getProjectileType(),
						unitType.getCombatStats().getAttackSpeed()));
			}
			else {
				addAttackType(new Attack(unitType.getCombatStats().getAttackRadius(), unitType.getProjectileType().getDamage(), unitType.getCombatStats().getAttackSpeed()));
			}
		}
		// melee attacks
		if (unitType.getCombatStats().getAttack() > 0) {
			addAttackType(new Attack(1, unitType.getCombatStats().getAttack(), unitType.getCombatStats().getAttackSpeed()));
		}
	}
	
	public boolean readyToHarvest() {
		return timeToHarvest <= 0;
	}
	public void resetTimeToHarvest() {
		timeToHarvest = baseTimeToHarvest;
		
	}
	public void setAutoBuild(boolean auto) {
		passiveAction = auto ? PlannedAction.BUILD : PlannedAction.NOTHING;
	}
	public boolean getAutoBuild() {
		return passiveAction == PlannedAction.BUILD;
	}
	
	public void setGuarding(boolean guarding) {
		passiveAction = guarding ? PlannedAction.GUARD : PlannedAction.NOTHING;
	}
	public boolean isGuarding() {
		return passiveAction == PlannedAction.GUARD;
	}
	
	public void setHarvesting(boolean harvesting) {
		isHarvesting = harvesting;
	}
	public boolean getIsHarvesting() {
		return isHarvesting;
	}

	public void setType(UnitType type) {
		this.unitType = type;
		this.setImage(this.getType());
	}

	public void addAttackType(Attack a) {
		attacks.add(a);
	}

	public void expendEffort(int effort) {
		remainingEffort -= effort;
		if (remainingEffort < 0) {
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

	public void setPassiveAction(PlannedAction action) {
		this.passiveAction = action;
	}
	public void clearPlannedActions() {
		actionQueue.clear();
	}
	public Tile getTargetTile() {
		if(!actionQueue.isEmpty()) {
			return actionQueue.peek().targetTile;
		}
		return null;
	}
	public void queuePlannedAction(PlannedAction plan) {
		actionQueue.add(plan);
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

		if (from.getRoad() != null && to.getRoad() != null && from.getRoad().isBuilt() && to.getRoad().isBuilt()) {
			penalty = penalty / from.getRoad().getType().getSpeed();
		}
		if (this.getUnitType().isFlying()) {
			penalty = 0;
		}
		penalty += combatStats.getMoveSpeed();
		return penalty;
	}

	public boolean moveTo(Tile t) {
		if (!readyToMove()) {
			return false;
		}
		if (t.isBlocked(this) == true) {
			return false;
		}
		double penalty = movePenaltyTo(this.getTile(), t);
		timeToMove += penalty;

		getTile().removeUnit(this);
		t.addUnit(this);
		this.setTile(t);
		return true;
	}

	public boolean moveTowards(Tile tile) {
		if (((currentPath == null || currentPath.isEmpty() || currentPath.getLast() != tile) && tile != this.getTile())
				|| (currentPath != null && !currentPath.isEmpty() && currentPath.getFirst().isBlocked(this))) {
			currentPath = Pathfinding.getBestPath(this, this.getTile(), tile);
		}
		if (currentPath != null && !currentPath.isEmpty()) {
			Tile targetTile = currentPath.getFirst();
			boolean success = this.moveTo(targetTile);
			if (success) {
				currentPath.removeFirst();
			}
			return success;
		}
		return false;
	}

	public LinkedList<Tile> getCurrentPath() {
		return currentPath;
	}

	public void updateState() {
		if (timeToMove > 0) {
			timeToMove -= 1;
		}
		if (timeToAttack > 0) {
			timeToAttack -= 1;
		}
		if (timeToHeal > 0) {
			timeToHeal -= 1;
		}
		if (timeToHarvest > 0) {
			timeToHarvest -= 1;
		}
		isIdle = readyToMove() && readyToAttack() && actionQueue.isEmpty() && getIsSelected() == false;
		if (getHealth() < combatStats.getHealth() && readyToHeal()) {
			heal(1, false);
			resetTimeToHeal();
		}
		// Take environment damage every 5 ticks
		if (World.ticks % World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			int tileDamage = (int) getTile().computeTileDamage(this);
			if (tileDamage != 0) {
				this.takeDamage(tileDamage);
			}
		}
		int ticksToCost =  ticksForFoodCost;
		if(isHarvesting == true) {
			ticksToCost = (int) (ticksForFoodCost/2);
		}
		
		if (World.ticks % ticksToCost == 0 && getFaction().usesItems()) {
			if (getFaction().canAfford(ItemType.FOOD, 1)) {
				getFaction().payCost(ItemType.FOOD, 1);
				starving = 0;
			} else {
				starving++;
				takeDamage(starving);
			}
		}

		// If on tile with an item, take the item
		if (getFaction().usesItems()) {
			for (Item item : getTile().getItems()) {
				getFaction().addItem(item.getType(), item.getAmount());
			}
			getTile().clearItems();
		}
	}

	@Override
	public boolean takeDamage(double damage) {
		boolean lethal = super.takeDamage(damage);
		if (lethal) {
			for (Item item : getType().getDeadItem()) {
				getTile().addItem(item);
			}
		}
		return lethal;
	}

	public boolean inRange(Thing other) {
		if (other == null) {
			return false;
		}
		return !(this.getTile().getLocation().distanceTo(other.getTile().getLocation()) > combatStats.getAttackRadius()
				&& this.getTile() != other.getTile());
	}

	/**
	 * this function does not check the attack range or if unit is ready to attack
	 * 
	 * @return true if attacked, false otherwise
	 */
	public boolean attack(Thing target) {
		Attack attack = chooseAttack(target);
		if(attack != null) {
			// actually do the attack
			if(attack.projectileType == null) {
				double initialHP = target.getHealth();
				target.takeDamage(attack.damage);
				double damageDealt = initialHP - (target.getHealth() < 0 ? 0 : target.getHealth());
				if (unitType.hasLifeSteal() && !(target instanceof Building)) {
					this.heal(damageDealt, true);
				}
				resetTimeToAttack();
				if (target instanceof Unit) {
					((Unit) target).aggro(this);
				}
			}
			else {
				Attack.shoot(this, target, attack);
			}
			return true;
		}
		return false;
	}

	public void aggro(Unit attacker) {
		if(attacker == null) {
			return;
		}
		if (this.getFaction() != attacker.getFaction() && getTarget() != attacker && isIdle()) {
			this.queuePlannedAction(new PlannedAction(attacker));
		}
	}

	public Attack chooseAttack(Thing target) {
		int distance = this.getTile().getLocation().distanceTo(target.getTile().getLocation());
		for (Attack a : attacks) {
			if (distance <= a.range && (a.projectileType == null || distance >= a.projectileType.getMinimumRange())) {
				return a;
			}
		}
		return null;
	}

	private Building getBuildingToBuild(LinkedList<Building> buildings) {
		if (buildings.isEmpty()) {
			return null;
		}
		Building firstNotStartedBuilding = null;
		for (Building building : buildings) {
			if (building.isBuilt() == false && building.isStarted()) {
				return building;
			}
			else if(!building.isStarted()) {
				firstNotStartedBuilding = building;
			}
		}
		return firstNotStartedBuilding;
	}

	public void planActions(World world) {
		// Workers deciding whether to move toward something to build
		if (unitType.isBuilder() && isIdle() && passiveAction == PlannedAction.BUILD && getTile().getFaction() == getFaction()) {
			Building building = getBuildingToBuild(world.getBuildings());
			if (building != null && building.getTile().getFaction() == getFaction()) {
				queuePlannedAction(new PlannedAction(building, true));
			}
		}
	}

	public void doMovement() {
		if(readyToMove()) {
			Tile targetTile = null; 
			Thing targetThing = null;
			while(!actionQueue.isEmpty()) {
				PlannedAction plan = actionQueue.peek();
				if(plan.isDone(getTile())) {
					actionQueue.poll();
					continue;
				}
				targetThing = plan.target;
				targetTile = plan.getTile();
				break;
			}
			boolean alreadyInRange = targetThing != null && this.inRange(targetThing);
			if(targetTile != null && !alreadyInRange) {
				moveTowards(targetTile);
				// can't reach target
				if(currentPath == null) {
					actionQueue.poll();
				}
			}
		}
	}

	public final boolean doAttacks(World world) {
		boolean attacked = false;
		// remove already finished planned actions
		if(!actionQueue.isEmpty()) {
			PlannedAction plan = actionQueue.peek();
			if(plan.isDone(getTile())) {
				actionQueue.poll();
			}
		}
		if(!readyToAttack()) {
			return false;
		}
		if(!actionQueue.isEmpty()) {
			PlannedAction plan = actionQueue.peek();
			if(plan.target != null) {
				if(plan.isBuildAction() && unitType.isBuilder() && inRange(plan.target)) {
					boolean finished = buildBuilding((Building)plan.target);
					attacked = true;
				}
				else {
					attacked = attack(plan.target);
				}
			}
		}
		if (!attacked && isGuarding()) {
			HashSet<Tile> inrange = world.getNeighborsInRadius(getTile(), getType().getCombatStats().getAttackRadius());
			for (Tile tile : inrange) {
				if (tile.getFaction() == getFaction()) {
					for (Unit unit : tile.getUnits()) {
						if (unit.getFaction() != this.getFaction() && unit.getType().isHostile() && unit != this) {
							attacked = attacked || attack(unit);
							if (attacked) {
								break;
							}
						}
					}
				}
			}
		}
		return attacked;
	}
	
	/**
	 * @return true if finished building false otherwise
	 */
	private boolean buildBuilding(Building building) {
		building.expendEffort(1);
		if (building.getRemainingEffort() > 0) {
			building.heal(building.getMaxHealth() / building.getType().getBuildingEffort(), false);
		}
		if (building.getRemainingEffort() < building.getType().getBuildingEffort()) {
			building.setPlanned(false);
		}
		return building.isBuilt();
	}

	public void doPassiveThings(World world) {
		// Workers building stuff
		if (getType().isBuilder()) {
			//worker harvesting
			if(readyToHarvest() && isHarvesting == true && this.getTile().getPlant() != null) {
				ItemType itemType = this.getTile().getPlant().getItem();
				if(itemType != null) {
					getFaction().addItem(itemType, 1);
					this.getTile().getPlant().takeDamage(1);
					resetTimeToHarvest();
				}
			}
		}
	}
	public void resetTimeToAttack() {
		timeToAttack = combatStats.getAttackSpeed();
	}

	public Thing getTarget() {
		if(!actionQueue.isEmpty()) {
			return actionQueue.peek().target;
		}
		return null;
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
		if(isGuarding()) {
			strings.add("GUARD");
		}
		return strings;
	}

	@Override
	public String toString() {
		return unitType.toString();
	}
}
