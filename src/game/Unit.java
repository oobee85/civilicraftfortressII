package game;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import game.components.*;
import game.pathfinding.*;
import utils.*;
import world.*;

public class Unit extends Thing implements Serializable {
	private static final long serialVersionUID = 1L;
	private UnitType unitType;
	private transient double timeToMove;
	private transient double timeToAttack;
	private transient double timeToHeal;
	private transient boolean isIdle;
	private transient int starving;
	private CombatStats combatStats;
	private transient LinkedList<Tile> currentPath;
	
	public transient ConcurrentLinkedQueue<PlannedAction> actionQueue = new ConcurrentLinkedQueue<>();
	private transient PlannedAction passiveAction = PlannedAction.NOTHING;
	
	private transient boolean isHarvesting;
	private transient double timeToHarvest;
	private transient int maxItemAmount;
	private transient double baseTimeToHarvest = 10;
	private transient int ticksForFoodCost = 50;
	
	private Inventory inventory;
	
	public Unit(UnitType unitType, Tile tile, Faction faction) {
		super(unitType.getCombatStats().getHealth(), unitType.getMipMap(), unitType.getMesh(), faction, tile);
		this.unitType = unitType;
		this.combatStats = unitType.getCombatStats();
		this.timeToHeal = unitType.getCombatStats().getHealSpeed();
		this.isIdle = false;
		this.inventory = new Inventory();
		this.maxItemAmount = 20;
		if(this.getType().isCaravan()) {
			this.maxItemAmount = 1;
		}
		this.addComponent(DamageResistance.class, unitType.getDamageResistance());
	}
	
	public boolean readyToHarvest() {
		return timeToHarvest <= 0;
	}
	public void resetTimeToHarvest(double multiplier) {
		timeToHarvest = baseTimeToHarvest*multiplier;
		
	}
	public void setAutoBuild(boolean auto) {
		passiveAction = auto ? PlannedAction.BUILD : PlannedAction.NOTHING;
	}
	public boolean isAutoBuilding() {
		return passiveAction == PlannedAction.BUILD;
	}
	
	public void setGuarding(boolean guarding) {
		passiveAction = guarding ? PlannedAction.GUARD : PlannedAction.NOTHING;
	}
	public boolean isGuarding() {
		return passiveAction == PlannedAction.GUARD;
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public boolean addItem(ItemType type, int amount) {
		if(this.inventory.getItemAmount(type) >= this.maxItemAmount) {
			return true;
		}
		this.inventory.addItem(type, amount);
		return false;
	}
	public void setType(UnitType type) {
		this.unitType = type;
		this.addComponent(DamageResistance.class, type.getDamageResistance());
		this.setMipMap(this.getType().getMipMap());
	}


	public CombatStats getCombatStats() {
		return combatStats;
	}

	public void setCombatStats(CombatStats cm) {
		combatStats = cm;
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
		if(this.isSelected()) {
			System.out.println("queued action: " + plan.type);
		}
		
		actionQueue.add(plan);
	}
	public void clearCompletedPlannedActions() {
		
	}

	public UnitType getUnitType() {
		return unitType;
	}

	public double computeDanger(Tile tile) {
		return this.applyResistance(tile.computeTileDanger());
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
			if(readyToInvade() || (targetTile.getFaction().id() == World.NO_FACTION_ID || targetTile.getFaction().id() == getFactionID())) {
				boolean success = this.moveTo(targetTile);
				if (success) {
					currentPath.removeFirst();
				}
				return success;
			}
		}
		return false;
	}
	
	public boolean readyToInvade() {
		return true;
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
		isIdle = readyToMove() && readyToAttack() && actionQueue.isEmpty() && isSelected() == false;
		if (getHealth() < getMaxHealth() && readyToHeal()) {
			heal(1, false);
			resetTimeToHeal();
		}
		// Take environment damage every 5 ticks
		if (World.ticks % World.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
			int[] tileDamage = getTile().computeTileDamage();
			for(int i = 0; i < tileDamage.length; i++) {
				this.takeDamage(tileDamage[i], DamageType.values()[i]);
			}
		}
		int ticksToCost =  ticksForFoodCost;
		if(isHarvesting == true) {
			ticksToCost = (int) (ticksForFoodCost/2);
		}
		
		if (World.ticks % ticksToCost == 0 && getFaction().usesItems()) {
			if (getFaction().canAfford(ItemType.FOOD, 2)) {
				getFaction().payCost(ItemType.FOOD, 2);
				starving = 0;
			} else {
				starving++;
				int[] damage = DamageType.getZeroDamageArray();
				takeDamage(starving, DamageType.HUNGER);
			}
		}

		// If on tile with an item, take the item
		if (getFaction().usesItems() && this.getType().isBuilder()) {
			this.getInventory().takeAll(getTile().getInventory());
		}
	}
	public PlannedAction getNextPlannedAction() {
		while(!actionQueue.isEmpty()) {
			PlannedAction plan = actionQueue.peek();
			if(plan.isDone(getTile())) {
				actionQueue.poll();
				onFinishedAction(plan);
			}
			else {
				return plan;
			}
		}
		return null;
	}

	@Override
	public boolean takeDamage(int damage, DamageType type) {
		boolean lethal = super.takeDamage(damage, type);
		if (lethal) {
			for (Item item : getType().getDeadItem()) {
				getTile().getInventory().addItem(item);
			}
		}
		return lethal;
	}
	
	public int getMaxRange() {
		int maxRange = 1;
		for(AttackStyle style : getType().getAttackStyles()) {
			maxRange = Math.max(maxRange, style.getRange());
		}
		return maxRange;
	}

	public boolean inRange(Thing other) {
		if (other == null) {
			return false;
		}
		return inRange(other.getTile());
	}
	public boolean inRange(Tile tile) {
		return this.getTile().getLocation().distanceTo(tile.getLocation()) <= getMaxRange();
	}

	/**
	 * this function does not check if unit is ready to attack
	 * does check attack range via chooseAttack()
	 * @return true if attacked, false otherwise
	 */
	public boolean attack(Thing target) {
		AttackStyle style = chooseAttack(target);
		if(style != null) {
			// actually do the attack
			if(style.getProjectile() == null) {
				double initialHP = target.getHealth();
				//does cleave damage
				if(this.getType().hasCleave()) {
					for(Unit unit : target.getTile().getUnits()) {
						unit.takeDamage(style.getDamage(), DamageType.PHYSICAL);
					}
				} 
				else {
					target.takeDamage(style.getDamage(), DamageType.PHYSICAL);
				}
				
				double damageDealt = initialHP - (target.getHealth() < 0 ? 0 : target.getHealth());
				if (style.isLifesteal() && !(target instanceof Building)) {
					this.heal(damageDealt, true);
				}
				if (target instanceof Unit) {
//					((Unit) target).aggro(this);
					//does cleave retaliation
					if(this.getType().hasCleave()) {
						for(Unit unit : target.getTile().getUnits()) {
							if(unit.getFaction() != this.getFaction()) {
								unit.aggro(this);
							}
						}
					}
//					for(Unit u: target.getTile().getUnits()) {
//						if(u.getFaction() == target.getFaction()){
//							u.aggro(this);
//						}
//					}
						
				}
			}
			else {
				Attack.shoot(this, target, style);
			}
			resetTimeToAttack(style.getCooldown());
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

	public Building getNearestBuildingToDeliver() {
		Building bestBuilding = null;
		// TODO fix concurrent modification exception on this for loop
		for (Building building : this.getFaction().getBuildings()) {
			if(!building.isBuilt()) {
				continue;
			}
			if ((building.getType().isColony() || building.getType().isCastle())) {
				if(bestBuilding == null) {
					bestBuilding = building;
				}
				if (this.getTile().getLocation().distanceTo(building.getTile().getLocation()) < 
						this.getTile().getLocation().distanceTo(bestBuilding.getTile().getLocation())) {
					bestBuilding = building;
				}
			}
		}
		return bestBuilding;
	}
	public Building getNearestCastleToDeliver() {
		Building bestBuilding = null;
		for(Building building: this.getFaction().getBuildings()) {
			
			if((building.getType().isCastle())) {
				if(bestBuilding == null) {
					bestBuilding = building;
				}
				if(this.getTile().getLocation().distanceTo(building.getTile().getLocation()) < 
						this.getTile().getLocation().distanceTo(bestBuilding.getTile().getLocation())) {
					bestBuilding = building;
				}
				
			}
				
		}
		return bestBuilding;
	}
	public Plant getNearestPlantToHarvest(Tile oldTile, PlantType type) {
		Plant target = null;
		for(Tile tile: oldTile.getNeighbors()) {
			if(tile.getPlant() != null && tile.getPlant().getType() == type) {
				target = tile.getPlant();
				if(Math.random() < 0.3) {
					break;
				}
			}
		}
		return target;
	}
	
	public void doHarvestBuilding(Building building, PlannedAction action) {
		if(readyToHarvest()) {
			boolean isFull = false;
			if(building.getType().name().equals("IRRIGATION")) {
				building.takeDamage(3, DamageType.PHYSICAL);
				isFull = this.addItem(ItemType.FOOD, 3);
				this.resetTimeToHarvest(1);
			}else if(building.getType().name().equals("MINE")) {
				building.takeDamage(1, DamageType.PHYSICAL);
				if(building.getTile().getResource() != null) {
					isFull = this.addItem(building.getTile().getResource().getType().getItemType(), 1);
					this.resetTimeToHarvest(building.getTile().getResource().getType().getTimeToHarvest());
				}else {
					isFull = this.addItem(ItemType.STONE, 1);
					this.resetTimeToHarvest(1);
				}
			}
			if(isFull) {
				action.setDone(true);
			}
		}
	}
	
	public void doHarvest(Plant plant, PlannedAction action) {
		if(readyToHarvest()) {
			plant.takeDamage(2, DamageType.PHYSICAL);
			boolean isFull = this.addItem(plant.getItem(), 1);
			this.resetTimeToHarvest(2);
			if(isFull) {
				action.setDone(true);
			}
		}
		
	}
	public void doTake(PlannedAction action, Thing target) {
		if(target instanceof Building) {
			Building building = (Building) target;
			this.inventory.takeAll(building.getInventory());
		}
		action.setDone(true);
	}
	public void doDelivery(PlannedAction action, Thing target) {
		if(target instanceof Building) {
			Building building = (Building) target;
			//stores inventory in colony
			if(building.getType().isColony()) {
				building.getInventory().takeAll(this.inventory);
				
				//stores inventory in faction inventory
			}else if(building.getType().isCastle()) {
				this.getFaction().getInventory().takeAll(this.inventory);
			}
		}
		
		action.setDone(true);
	}
	
	/**
	 * selects first attack style that satisfies minimum and maximum range requirements
	 * @param target
	 * @return
	 */
	public AttackStyle chooseAttack(Thing target) {
		int distance = this.getTile().getLocation().distanceTo(target.getTile().getLocation());
		for(AttackStyle style : getType().getAttackStyles()) {
			if (distance <= style.getRange() && distance >= style.getMinRange()) {
				return style;
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
				queuePlannedAction(new PlannedAction(building.getTile(), building.getType().isRoad()));
			}
		}
	}

	public void doMovement() {
		if(!readyToMove()) {
			return;
		}
		PlannedAction plan = getNextPlannedAction();
		if(plan == null) {
			return;
		}
		boolean alreadyInRangeToAttack = plan.target != null && this.inRange(plan.target);
		boolean alreadyInRangeToBuild = plan.isBuildAction() && this.inRange(plan.getTile());
		if(plan.getTile() != null && !alreadyInRangeToAttack && !alreadyInRangeToBuild) {
			moveTowards(plan.getTile());
			// can't reach target so mark the plan as finished.
			if(currentPath == null) {
				plan.setDone(true);
			}
		}
	}

	public final boolean doAttacks(World world) {
		PlannedAction plan = getNextPlannedAction();
		if(plan == null) {
			return false;
		}
		boolean didSomething = false;
		if(!readyToAttack()) {
			return false;
		}
		if(plan.isBuildAction() && unitType.isBuilder() && inRange(plan.targetTile)) {
			Building tobuild = null;
			if(plan.isBuildRoadAction()) {
				tobuild = plan.getTile().getRoad();
			}
			else if(plan.isBuildBuildingAction()) {
				tobuild = plan.getTile().getBuilding();
			}
			if(tobuild != null) {
				buildBuilding(tobuild);
				didSomething = true;
			}
		}
		else if(plan.isHarvestAction() && unitType.isBuilder() && inRange(plan.target) && plan.target instanceof Building) {
			this.doHarvestBuilding((Building)plan.target, plan);
			didSomething = true;
		}
		else if(plan.isHarvestAction() && unitType.isBuilder() && inRange(plan.target) && plan.target instanceof Plant) {
			this.doHarvest((Plant)plan.target, plan);
			didSomething = true;
		}
		else if(plan.isDeliverAction() && inRange(plan.target)) {
			this.doDelivery(plan, plan.target);
			didSomething = true;
		}
		else if(plan.isTakeAction() && unitType.isCaravan() && inRange(plan.target) && plan.target instanceof Building) {
			this.doTake(plan, plan.target);
			didSomething = true;
		}
		else if(plan.target != null) {
			didSomething = attack(plan.target);	
		}
		
//		if(plan.target != null) {
//			if(plan.isBuildAction() && unitType.isBuilder() && inRange(plan.target)) {
//				boolean finished = buildBuilding((Building)plan.target);
//				attacked = true;
//			}
//			else {
//				attacked = attack(plan.target);
//			}
//		}
		if (!didSomething && isGuarding()) {
			HashSet<Tile> inrange = world.getNeighborsInRadius(getTile(), getMaxRange());
			for (Tile tile : inrange) {
				if (tile.getFaction() == getFaction()) {
					for (Unit unit : tile.getUnits()) {
						if (unit.getFaction() != this.getFaction() && unit.getType().isHostile() && unit != this) {
							didSomething = didSomething || attack(unit);
							if (didSomething) {
								break;
							}
						}
					}
				}
			}
		}
		return didSomething;
	}
	
	private void onFinishedAction(PlannedAction finished) {
		//harvesting
		if(finished.isHarvestAction()) {
			Building building = getNearestBuildingToDeliver();
			this.queuePlannedAction(new PlannedAction(building, PlannedAction.DELIVER, new PlannedAction(finished.target, PlannedAction.HARVEST)));
		}
		else if(finished.isTakeAction()) {
			Building castle = getNearestCastleToDeliver();
			this.queuePlannedAction(new PlannedAction(castle, PlannedAction.DELIVER, new PlannedAction(finished.target, PlannedAction.TAKE)));
		}else if(finished.isDeliverAction()) {
			PlannedAction followup = finished.getFollowUp();
			if(followup == null) {
				return;
			}
			//delivery
			if(followup.target instanceof Plant) {
				Plant oldTarget = (Plant)followup.target;
				if(oldTarget.isDead()) {
					Plant newTarget = getNearestPlantToHarvest(followup.getTile(), oldTarget.getType());
					if(newTarget != null) {
						followup = new PlannedAction(newTarget, PlannedAction.HARVEST);
					}
				}
			}else if(followup.target instanceof Building) {
//				if followup building dead, doesnt find new target
			}
			
			if(followup != null && !followup.target.isDead()) {
				this.queuePlannedAction(followup);
			}
		}
	}
	
	/**
	 * @return true if finished building false otherwise
	 */
	private void buildBuilding(Building building) {
		building.expendEffort(1);
		if (building.getRemainingEffort() > 0) {
			building.heal(building.getMaxHealth() / building.getType().getBuildingEffort(), false);
		}
		if (building.getRemainingEffort() < building.getType().getBuildingEffort()) {
			building.setPlanned(false);
		}
	}
	

	public void doPassiveThings(World world) {
		// Workers building stuff
		if (getType().isBuilder()) {
			//worker harvesting
			if(readyToHarvest() && isHarvesting == true && this.getTile().getPlant() != null) {
				ItemType itemType = this.getTile().getPlant().getItem();
				if(itemType != null) {
					getFaction().getInventory().addItem(itemType, 1);
					this.getTile().getPlant().takeDamage(1, DamageType.PHYSICAL);
					resetTimeToHarvest(1);
				}
			}
			Building building = this.getTile().getBuilding();
			if(building != null && building.getType().isColony()) {
				this.getFaction().getInventory().takeAll(this.inventory);
				
			}
		}
		if (getType().isHealer()) {
//			if(!readyToHarvest()) {
//				return;
//			}
			//priest healing
			for(Tile t: this.getTile().getNeighbors()) {
				for(Unit u: t.getUnits()) {
					if(u.getFaction() == this.getFaction()) {
						u.heal(1, true);
						
					}
				}
			}
			resetTimeToHarvest(1);
		}
	}
	public void resetTimeToAttack(int cooldown) {
		timeToAttack = cooldown;
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
