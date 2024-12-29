package game;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import game.actions.*;
import game.ai.*;
import game.components.*;
import game.pathfinding.*;
import sounds.Sound;
import sounds.SoundEffect;
import sounds.SoundManager;
import utils.*;
import world.*;

public class Unit extends Thing implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Unit cannot get further than this distance from guarded tile
	 */
	private static final int TETHERED_ATTACK_RANGE = 8; 
	
	private UnitType unitType;
	private transient double timeToMove;
	private transient double cooldownToDoAction;
	private transient double timeToHeal;
	private transient boolean isIdle;
	private CombatStats combatStats;
	
	private transient LinkedList<Tile> currentPath;

	public transient ConcurrentLinkedDeque<PlannedAction> actionQueue = new ConcurrentLinkedDeque<>();
	private transient PlannedAction passiveAction = PlannedAction.NOTHING;
	
	private transient boolean isHarvesting;
	private transient double timeToHarvest;
	private transient double baseTimeToHarvest = 10;
	
	private transient int ticksForFoodCost = 50;
	private transient int starving;
	
	public Unit(UnitType unitType, Tile tile, Faction faction) {
		super(unitType.getCombatStats().getHealth(), unitType.getMipMap(), faction, tile, unitType.getInventoryStackSize());
		this.unitType = unitType;
		this.combatStats = unitType.getCombatStats();
		this.timeToHeal = unitType.getCombatStats().getHealSpeed();
		this.isIdle = false;
		for(GameComponent c : unitType.getComponents()) {
			this.addComponent(c.getClass(), c);
		}
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
	
	public boolean isGuarding() {
		PlannedAction plan = actionQueue.peek();
		return plan != null && plan.type == ActionType.GUARD;
	}
	
	public void setType(UnitType type) {
		this.unitType = type;
		for(GameComponent c : type.getComponents()) {
			this.addComponent(c.getClass(), c);
		}
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
			System.out.println("queued action: " + plan);
		}
		
		actionQueue.add(plan);
	}
	public void prequeuePlannedAction(PlannedAction plan) {
		if(this.isSelected()) {
			System.out.println("queued action: " + plan);
		}
		actionQueue.addFirst(plan);
	}

	public UnitType getUnitType() {
		return unitType;
	}

	public double computeDanger(Tile tile) {
		return this.applyResistance(tile.computeTileDanger());
	}

	public double movePenaltyTo(Tile from, Tile to) {
		double unitPenalty = combatStats.getMoveSpeed();
		if (getUnitType().isFlying()) {
			return unitPenalty;
		}
		double terrainPenalty = to.getTerrain().moveSpeed();
		double elevationPenalty = Math.abs(to.getHeight() - from.getHeight());
		elevationPenalty = elevationPenalty * elevationPenalty / 100;
		if (from.getRoad() != null && from.getRoad().isBuilt()) {
			terrainPenalty = terrainPenalty / from.getRoad().getType().getSpeed();
			unitPenalty = unitPenalty / from.getRoad().getType().getSpeed();
			
			if (to.getRoad() != null && to.getRoad().isBuilt()) {
				elevationPenalty /= (from.getRoad().getType().getSpeed() / 2); 
			}
		}
		return terrainPenalty + unitPenalty + elevationPenalty;
	}

	public boolean moveTo(Tile t) {
		if (!readyToInvade() 
				&& !t.getFaction().isNeutral()
				&& t.getFaction().id() != getFactionID()) {
			return false;
		}
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
	
	private boolean getPathTo(Tile tile) {
		if (currentPath == null
				|| currentPath.isEmpty()
				|| currentPath.getLast() != tile
				|| currentPath.getFirst().isBlocked(this)) {
			currentPath = Pathfinding.getBestPath(this, this.getTile(), tile);
		}
		if (currentPath == null || currentPath.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean moveTowards(Tile tile) {
		if (tile == this.getTile()) {
			return true;
		}
		if (!getPathTo(tile)) {
			return false;
		}
		Tile targetTile = currentPath.getFirst();
		boolean success = this.moveTo(targetTile);
		if (success) {
			currentPath.removeFirst();
		}
		return success;
	}
	
	public boolean readyToInvade() {
		return true;
	}

	public LinkedList<Tile> getCurrentPath() {
		return currentPath;
	}
	
	public void updateSimulatedCurrentPath() {
		PlannedAction p = getNextPlannedAction();
		if (p == null) {
			currentPath = null;
			return;
		}
		if (currentPath != null && currentPath.contains(getTile())) {
			while (!currentPath.isEmpty()) {
				if (currentPath.removeFirst() == getTile()) {
					break;
				}
			}
		}
		else {
			getPathTo(p.getTile());
		}
	}
	
	public void updateState() {
		if (timeToMove > 0) {
			timeToMove -= 1;
		}
		if (cooldownToDoAction > 0) {
			cooldownToDoAction -= 1;
		}
		if (timeToHeal > 0) {
			timeToHeal -= 1;
		}
		if (timeToHarvest > 0) {
			timeToHarvest -= 1;
		}
		isIdle = readyToMove() && readyToDoAction() && actionQueue.isEmpty() && isSelected() == false;
		if (getHealth() < getMaxHealth() && readyToHeal()) {
			heal(1, false);
			resetTimeToHeal();
		}
		// Take environment damage every 5 ticks
		if (World.ticks % Constants.TICKS_PER_ENVIRONMENTAL_DAMAGE == 0) {
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
			if (getFaction().canAfford(ItemType.FOOD, 1)) {
				getFaction().payCost(ItemType.FOOD, 1);
				starving = 0;
			} else {
				starving++;
//				int[] damage = DamageType.getZeroDamageArray();
				takeDamage(starving, DamageType.HUNGER);
			}
		}

		// If on tile with an item, take the item
		if (this.hasInventory()) {
			this.getInventory().takeAll(getTile().getInventory());
		}
	}
	public PlannedAction getNextPlannedAction() {
		while(!actionQueue.isEmpty()) {
			PlannedAction plan = actionQueue.peek();
			if(plan != null && plan.isDone(this)) {
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
			// drop death items on death
//			for (Item item : getType().getDeadItem()) {
//				getTile().getInventory().addItem(item);
//			}
			
			
		}
		return lethal;
	}
	
	public int getMaxAttackRange() {
		int maxRange = 1;
		for(AttackStyle style : getType().getAttackStyles()) {
			maxRange = Math.max(maxRange, style.getRange());
		}
		return maxRange;
	}

	public boolean inRangeToAttack(Thing other) {
		if(other == null || other.getTile() == null) {
			return false;
		}
		return this.getTile().distanceTo(other.getTile()) <= getMaxAttackRange();
	}

	/**
	 * this function does not check if unit is ready to attack
	 * does check attack range via chooseAttack()
	 * @return true if attacked, false otherwise
	 */
	public boolean attack(Thing target) {
		AttackStyle style = chooseAttack(target);
		if(style == null) {
			return false;
		}
		// actually do the attack
		if(style.getProjectile() == null) {
			double initialHP = target.getHealth();
			//does cleave damage
			if(this.getType().hasCleave()) {
				for(Unit unit : target.getTile().getUnits()) {
					unit.takeDamage(style.getDamage(), DamageType.PHYSICAL);
					Sound sound = new Sound(SoundEffect.CLEAVEMELEEATTACK, null, this.getTile());
					SoundManager.theSoundQueue.add(sound);
				}
			} 
			else {
				target.takeDamage(style.getDamage(), DamageType.PHYSICAL);
				Sound sound = new Sound(SoundEffect.MELEEATTACK, null, this.getTile());
				SoundManager.theSoundQueue.add(sound);
			}
			
			
			double damageDealt = initialHP - (target.getHealth() < 0 ? 0 : target.getHealth());
			if (style.isLifesteal() && !(target instanceof Building)) {
				this.heal(damageDealt, true);
			}
			if (target instanceof Unit) {
				((Unit) target).aggro(this);
			}
			//does cleave retaliation
			if(this.getType().hasCleave()) {
				for(Unit unit : target.getTile().getUnits()) {
					if(unit.getFaction() != this.getFaction()) {
						unit.aggro(this);
					}
				}
			}
		}
		else {
			AttackUtils.shoot(this, target, style);
		}
		resetTimeToAttack(style.getCooldown());
		return true;
	}

	public void aggro(Unit attacker) {
		if(attacker == null) {
			return;
		}
		if (this.getFaction() != attacker.getFaction() && getTarget() != attacker && isIdle()) {
			this.queuePlannedAction(PlannedAction.attack(attacker));
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
	public Plant getNeighborPlantToHarvest(Tile oldTile, PlantType type) {
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
		if (building.isDead()) {
			action.setDone(true);
			return;
		}
		
		// If try to harvest from unfinished building, build it instead
		if (!building.isBuilt()) {
			action.setFollowUp(PlannedAction.buildOnTile(
					building.getTile(), 
					false, 
					PlannedAction.harvest(building)));
			action.setDone(true);
			return;
		}
		
		if (!readyToHarvest() || !hasInventory() || !building.readyToHarvest()) {
			return;
		}
		
		// harvest from farm
		if (building.getType() == BasicAI.FARM) {
			int modifier = 1;
//			if(building.getTile().canGrow() == false) {
//				modifier /= 2;
//			}
			building.takeDamage(5 * modifier, DamageType.PHYSICAL);
			getInventory().addItem(ItemType.FOOD, 5 * modifier);
			this.resetTimeToHarvest(4);
			building.resetTimeToHarvest(this.timeToHarvest);
			// set up followup action
			if (building.isDead()) {
				building.setPlanned(true);
				building.setHealth(1);
				building.setRemainingEffort(building.getType().getBuildingEffort());
			}
		}
		// harvest from mine
		else if (building.getType() == BasicAI.MINE) {
			building.takeDamage(5, DamageType.PHYSICAL);
			getInventory().addItem(ItemType.STONE, 5);
			this.resetTimeToHarvest(5);
			building.resetTimeToHarvest(this.timeToHarvest);
			
			// set up followup action
			if (building.isDead()) {
				building.setPlanned(true);
				building.setHealth(1);
				building.setRemainingEffort(building.getType().getBuildingEffort());
			}
			
		}
		// harvest from research lab
		else if (building.getType() == BasicAI.LAB) {
			building.takeDamage(5, DamageType.PHYSICAL);
			getInventory().addItem(ItemType.MAGIC, 5);
			this.resetTimeToHarvest(5);
			building.resetTimeToHarvest(this.timeToHarvest);
			
			// set up followup action
			if (building.isDead()) {
				building.setPlanned(true);
				building.setHealth(1);
				building.setRemainingEffort(building.getType().getBuildingEffort());
			}
		}
		
		if (getInventory().isFull()) {
			action.setDone(true);
		}
	}
	
	public void doHarvest(Plant plant, PlannedAction action) {
		if(readyToHarvest() && hasInventory()) {
			plant.takeDamage(2, DamageType.PHYSICAL);
			for(Item item: plant.getItem()) {
				getInventory().addItem(item.getType(), item.getAmount());
			}
			
			this.resetTimeToHarvest(2);
			if(getInventory().isFull()) {
				action.setDone(true);
			}
		}
	}
	public void doHarvest(Tile tile, PlannedAction action) {
		if(readyToHarvest() && hasInventory()) {
			//  figure out if harvest stone or ore
			ItemType itemType = null;
			if(tile.getResource() != null && getFaction().areRequirementsMet(tile.getResource())) {
				itemType = tile.getResource().getItemType();
			}
			else if(tile.getTerrain() == Terrain.ROCK) {
				itemType = ItemType.STONE;
			}
			if(itemType != null) {
				getInventory().addItem(itemType, 1);
				this.resetTimeToHarvest(2.5);
				if(getInventory().isFull()) {
					action.setDone(true);
				}
			}
		}
	}
	public void doTake(PlannedAction action, Thing target) {
		if(this.hasInventory()) {
			this.getInventory().takeAll(target.getInventory());
		}
		action.setDone(true);
	}
	public void doDelivery(PlannedAction action, Thing target) {
		if(target instanceof Building) {
			Building building = (Building) target;
			if(building.hasInventory()) {
				building.getInventory().takeAll(this.getInventory());
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
		if (isBuilder() && isIdle() && passiveAction == PlannedAction.BUILD && getTile().getFaction() == getFaction()) {
			
			Building building = getBuildingToBuild(world.getBuildings());
			// if the building is on friendly faction
			if (building != null && building.getTile().getFaction() == getFaction()) {
				// checks if building requirements are met before planning building
				
				queuePlannedAction(PlannedAction.buildOnTile(building.getTile(), building.getType().isRoad()));
				
			}
		}
	}

	public void doMovement() {
		if(!readyToMove()) {
			return;
		}
		PlannedAction plan = getNextPlannedAction();
		if(plan == null) {
			currentPath = null;
			return;
		}
		Tile targetTile = plan.getTile();
		if(targetTile != null && !plan.inRange(this)) {
			if (plan.type == ActionType.TETHERED_ATTACK
					&& targetTile.distanceTo(plan.targetTile) > TETHERED_ATTACK_RANGE + this.getMaxAttackRange()) {
				// lose aggro if enemy is outside of tether range 
				plan.setDone(true);
				return;
			}
			moveTowards(targetTile);
			Sound sound = new Sound(SoundEffect.MOVEDIRT, this.getFaction(), this.getTile());
			SoundManager.theSoundQueue.add(sound);
			// can't reach target so mark the plan as finished.
			if(currentPath == null) {
				plan.setDone(true);
			}
		}
	}

	public final boolean doActions(World world) {
		if(!readyToDoAction()) {
			return false;
		}
		PlannedAction plan = getNextPlannedAction();
		if(plan == null) {
			return false;
		}

		if(plan.type == ActionType.WANDER_AROUND) {
			Tile targetTile = plan.targetTile;
			if(Math.random() < 0.9) {
				List<Tile> nearby = Utils.getTilesInRadius(getTile(), world, 5);
				targetTile = nearby.get((int)(Math.random()*nearby.size()));
			}
			if(getTile().distanceTo(plan.targetTile) > 5) {
				prequeuePlannedAction(PlannedAction.moveTo(targetTile));
			}
			else {
				prequeuePlannedAction(PlannedAction.attackMoveTo(targetTile));
			}
		}
		else if(plan.type == ActionType.ATTACK_MOVE) {
			Thing closestEnemy = getClosestEnemyInRange(world, getTile(), getMaxAttackRange() + 1);
			if(closestEnemy != null) {
				prequeuePlannedAction(PlannedAction.attack(closestEnemy));
			}
		}
		else if (plan.type == ActionType.GUARD) {
			Thing closestEnemy = getClosestEnemyInRange(world, plan.targetTile, TETHERED_ATTACK_RANGE + getMaxAttackRange());
			if(closestEnemy != null) {
				prequeuePlannedAction(PlannedAction.tetheredAttack(plan.targetTile, closestEnemy));
			}
		}
		
		if(!plan.inRange(this)) {
			return false;
		}
		boolean didSomething = false;
		if(plan.isBuildRoadAction() && isBuilder()) {
			Building tobuild = plan.getTile().getRoad();
			if(tobuild != null) {
				didSomething = true;
				buildBuilding(tobuild);
			}
		}
		else if(plan.isBuildBuildingAction() && isBuilder()) {
			Building tobuild = plan.getTile().getBuilding();
			if(tobuild != null) {
				buildBuilding(tobuild);
				didSomething = true;
			}
		}
		else if(plan.isHarvestAction() && isBuilder() && plan.targetTile != null) {
			this.doHarvest(plan.targetTile, plan);
			didSomething = true;
		}
		else if(plan.isHarvestAction() && 
				isBuilder() && 
				plan.target != null && 
				plan.target instanceof Building && 
				((Building)plan.target).getType().isHarvestable()) {
			this.doHarvestBuilding((Building)plan.target, plan);
			didSomething = true;
		}
		else if(plan.isTakeItemsAction() && (unitType.isCaravan() || isBuilder()) && plan.target instanceof Building
				&& ((Building)plan.target).hasInventory() == true) {
//				&& ((Building)plan.target).getType().isColony() == true) {
			this.doTake(plan, plan.target);
			didSomething = true;
		}
		else if(plan.isHarvestAction() && 
				isBuilder() && 
				plan.target != null && 
				plan.target instanceof Plant) {
			this.doHarvest((Plant)plan.target, plan);
			didSomething = true;
		}
		
		else if(plan.isDeliverAction()) {
			this.doDelivery(plan, plan.target);
			didSomething = true;
		}
		
		else if(plan.type == ActionType.MOVE) {
			
		}
		else if(plan.type == ActionType.ATTACK && plan.target != null) {
			didSomething = attack(plan.target);
		}
		else if (plan.type == ActionType.TETHERED_ATTACK) {
			if (plan.targetTile.distanceTo(plan.target.getTile()) > TETHERED_ATTACK_RANGE + this.getMaxAttackRange()) {
				plan.setDone(true);
			}
			didSomething = attack(plan.target);
		}
		return didSomething;
	}
	
	private Thing getClosestEnemyInRange(World world, Tile fromTile, int range) {
		Thing closest = null;
		int closestDistance = -1;
		Thing closestBuilding = null;
		int closestBuildingDistance = -1;
		for (Tile tile : Utils.getTilesInRadius(fromTile, world, range)) {
			int dist = getTile().distanceTo(tile);
			for (Unit unit : tile.getUnits()) {
				if(!shouldAggroOn(unit) || !unit.getType().isHostile()) {
					continue;
				}
				if(closest == null || dist < closestDistance) {
					closest = unit;
					closestDistance = dist;
				}
			}
			Building building = tile.getBuilding();
			if (building != null && shouldAggroOn(building) 
					&& (closestBuilding == null || dist < closestBuildingDistance)) {
				closestBuilding = building;
				closestBuildingDistance = dist;
			}
		}
		if (closest == null) {
			closest = closestBuilding;
		}
		return closest;
	}
	
	private boolean shouldAggroOn(Thing potential) {
		// TODO replace anywhere faction comparison is used to determine enemies
		if (potential == this) {
			return false;
		}
		if (potential.getFactionID() != this.getFactionID()) {
			return true;
		}
		if (potential.getFaction().isNeutral()) {
			if (!(potential instanceof Unit)) {
				// neutrals shouldnt attack other neutral buildings or plants
				return false;
			}
			Unit u = (Unit) potential;
			// dont attack neutrals of the same type
			if (u.getType() != getType()) {
				return true;
			}
		}
		return false;
	}
	
	private void onFinishedAction(PlannedAction finished) {
		// Special logic for delivery actions because the followup might be gone
		if (finished.isDeliverAction() && finished.getFollowUp() != null) {
			PlannedAction followup = finished.getFollowUp();
			// if the current target plant has died, find a neighbor plant to harvest
			if (followup.target != null && followup.target.isDead()) {
				if (followup.target instanceof Plant) {
					// if followup plan is dead, find a nearby similar plant
					Plant newTarget = getNeighborPlantToHarvest(followup.getTile(), ((Plant)followup.target).getType());
					
					
					
					if (newTarget != null) {
						followup = PlannedAction.harvest(newTarget);
					}
					else {
						// failed to find similar plant
						return;
					}
				}
				else if (followup.target instanceof Building) {
					// if followup building dead, doesnt find new target
					return;
				}
			}
			this.queuePlannedAction(followup);
		}
		else if(finished.getFollowUp() != null) {
			this.queuePlannedAction(finished.getFollowUp());
		}
		else if(finished.isHarvestAction()) {
			Building building = getNearestBuildingToDeliver();
			this.queuePlannedAction(PlannedAction.deliver(building, PlannedAction.makeCopy(finished)));
		}
		else if(finished.isTakeItemsAction()) {
			Building castle = getNearestCastleToDeliver();
			this.queuePlannedAction(PlannedAction.deliver(castle, PlannedAction.takeItemsFrom(finished.target)));
		}
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
		if (isBuilder()) {
			//worker harvesting
			if(readyToHarvest() && isHarvesting == true && this.getTile().getPlant() != null) {
				for(Item item: this.getTile().getPlant().getItem()) {
					ItemType itemType = item.getType();
					if(itemType != null) {
						getFaction().getInventory().addItem(itemType, 1);
						this.getTile().getPlant().takeDamage(1, DamageType.PHYSICAL);
						resetTimeToHarvest(1);
					}
				}
				
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
		cooldownToDoAction = cooldown;
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

	public boolean readyToDoAction() {
		return cooldownToDoAction <= 0;
	}

	public double getCooldownToDoAction() {
		return cooldownToDoAction;
	}

	public void setCooldownToDoAction(int x) {
		cooldownToDoAction = x;
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
		strings.add(String.format("TTA=%.1f", getCooldownToDoAction()));
		strings.add(String.format("TTH=%.1f", getTimeToHeal()));
		if(isGuarding()) {
			strings.add("GUARD");
		}
		return strings;
	}

	@Override
	public String toString() {
		return unitType.toString() + this.id();
	}
}
