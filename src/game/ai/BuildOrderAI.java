package game.ai;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

import game.*;
import game.actions.PlannedAction;
import ui.CommandInterface;
import utils.Utils;
import world.*;

public class BuildOrderAI extends AIInterface {

	public static final BuildingType FARM = Game.buildingTypeMap.get("FARM");
	private static final int MAX_BUILD_RADIUS = 10;
	private static final int MAX_SEARCH_RADIUS = 40;

	public enum WorkerTask {
		FORAGE, FARM, CHOP, GATHERSTONE, GATHERSILVER, GATHERCOPPER, GATHERIRON, GATHERCOAL, GATHERMITHRIL
	}
	
	private HashMap<Integer, Integer> assignedWorkerRoles = new HashMap<>();
	private HashSet<Integer> workersStillAlive = new HashSet<>();
	private LinkedList<Integer> deadWorkersToRemove = new LinkedList<>();

	public BuildOrderAI(CommandInterface commands, Faction faction, World world) {
		super(commands, faction, world);
	}
	
	class QuantityReq {
		int min;
		int enough;
		int max;
		public QuantityReq(int min, int enough, int max) {
			this.min = min;
			this.enough = enough;
			this.max = max;
		}
	}
	
	class Phase {
		HashMap<UnitType, QuantityReq> unitReqs = new HashMap<UnitType, QuantityReq>();
		HashMap<BuildingType, QuantityReq> buildingReqs = new HashMap<BuildingType, QuantityReq>();
		LinkedList<ResearchType> requiredResearch = new LinkedList<ResearchType>();
		int[] workerAssignments = new int[WorkerTask.values().length];
	}
	
	int currentPhase = 0;
	Phase[] phases;
	
	{
		Phase phase0 = new Phase();
		phase0.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(0, 4, 10));
		phase0.requiredResearch.add(Game.researchTypeMap.get("WARRIOR_CODE"));
		phase0.workerAssignments = new int[] {3, 3, 4, 0, 0, 0, 0, 0, 0, 0};

		Phase phase1 = new Phase();
		phase1.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(4, 8, 12));
		phase1.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(0, 1, 1));
		phase1.workerAssignments = new int[] {3, 4, 5, 0, 0, 0, 0, 0, 0, 0};

		Phase phase2 = new Phase();
		phase2.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(8, 15, 30));
		phase2.unitReqs.put(Game.unitTypeMap.get("WARRIOR"), new QuantityReq(0, 5, 8));
		phase2.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 1));
		phase2.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
		phase2.requiredResearch.add(Game.researchTypeMap.get("BRONZE_WORKING"));
		phase2.workerAssignments = new int[] {2, 16, 8, 4, 0, 0, 0, 0, 0, 0};

		Phase phase3 = new Phase();
		phase3.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(8, 20, 34));
		phase3.unitReqs.put(Game.unitTypeMap.get("WARRIOR"), new QuantityReq(0, 5, 5));
		phase3.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 1));
		phase3.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 4));
		phase3.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 1, 4));
		phase3.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 0, 5));
		phase3.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
		phase3.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
		phase3.requiredResearch.add(Game.researchTypeMap.get("IRON_WORKING"));
		phase3.requiredResearch.add(Game.researchTypeMap.get("WHEEL"));
		phase3.requiredResearch.add(Game.researchTypeMap.get("MASONRY"));
		phase3.workerAssignments = new int[] {1, 16, 7, 2, 2, 2, 0, 0, 0, 0};

		Phase phase4 = new Phase();
		phase4.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 22, 36));
		phase4.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 8, 16));
		phase4.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 2));
		phase4.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 10));
		phase4.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 6));
		phase4.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 1, 5));
		phase4.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
		phase4.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
//		phase4.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
		phase4.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
		phase4.workerAssignments = new int[] {1, 19, 6, 2, 2, 2, 3, 1, 0};

		Phase phase5 = new Phase();
		phase5.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 24, 38));
		phase5.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 12, 22));
		phase5.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 2));
		phase5.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 10));
		phase5.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 6));
		phase5.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 1, 5));
		phase5.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
		phase5.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
//		phase5.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
		phase5.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
		phase5.requiredResearch.add(Game.researchTypeMap.get("MATHEMATICS"));
		phase5.workerAssignments = new int[] {1, 22, 7, 1, 1, 3, 7, 3, 0};

		Phase phase6 = new Phase();
		phase6.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 26, 40));
		phase6.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 16, 30));
		phase6.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 3));
		phase6.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 0, 8));
		phase6.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 5));
		phase6.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 0, 12));
		phase6.unitReqs.put(Game.unitTypeMap.get("LONGBOWMAN"), new QuantityReq(0, 1, 10));
		phase6.unitReqs.put(Game.unitTypeMap.get("CATAPULT"), new QuantityReq(0, 1, 6));
		phase4.unitReqs.put(Game.unitTypeMap.get("GRIFFIN"), new QuantityReq(0, 0, 4));
		phase6.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
		phase6.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
		phase6.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
		phase6.buildingReqs.put(Game.buildingTypeMap.get("WORKSHOP"), new QuantityReq(1, 1, 1));
		phase6.buildingReqs.put(Game.buildingTypeMap.get("STABLES"), new QuantityReq(1, 1, 1));
		phase6.requiredResearch.add(Game.researchTypeMap.get("MYSTICISM"));
		phase6.requiredResearch.add(Game.researchTypeMap.get("ENGINEERING"));
		phase6.workerAssignments = new int[] {1, 28, 6, 1, 1, 3, 6, 4, 4};

		phases = new Phase[] {phase0, phase1, phase2, phase3, phase4, phase5, phase6};
	}

	@Override
	public void aiTickLogic() {
		boolean completedUnits = replentishUnits();
		boolean completedResearch = research();
		boolean completedBuilding = build();
		unitActions();
		craftItems();
		
		
		if (completedUnits && completedResearch && completedBuilding) {
			if (currentPhase + 1 < phases.length) {
				currentPhase++;
				phaseTransition(currentPhase);
			}
		}
	}
	
	private void craftItems() {
		if (faction.getInventory().getItemAmount(ItemType.BRONZE_SWORD) < 2) {
			commands.craftItem(faction, ItemType.BRONZE_SWORD, 1);
		}
		if (faction.getInventory().getItemAmount(ItemType.IRON_SWORD) < 1) {
			commands.craftItem(faction, ItemType.IRON_SWORD, 1);
		}

		commands.craftItem(faction, ItemType.BRONZE_BAR, 1);
		commands.craftItem(faction, ItemType.IRON_BAR, 1);
	}
	
	List<Tile> silverTiles = new LinkedList<Tile>();
	List<Tile> copperTiles = new LinkedList<Tile>();
	List<Tile> ironTiles = new LinkedList<Tile>();
	List<Tile> coalTiles = new LinkedList<Tile>();
	List<Tile> mithrilTiles = new LinkedList<Tile>();
	
	private void phaseTransition(int newPhase) {
		if (newPhase == 3) {
			for (Tile t : world.getTiles()) {
				if (t.getResource() == null) {
					continue;
				}
				if (t.getResource().getType() == ResourceType.SILVER) {
					silverTiles.add(t);
				}
				else if (t.getResource().getType() == ResourceType.COPPER) {
					copperTiles.add(t);
				}
				else if (t.getResource().getType() == ResourceType.IRON) {
					ironTiles.add(t);
				}
				else if (t.getResource().getType() == ResourceType.COAL) {
					coalTiles.add(t);
				}
				else if (t.getResource().getType() == ResourceType.MITHRIL) {
					mithrilTiles.add(t);
				}
			}
		}
	}
	
	private void unitActions() {
		int[] workersAssigned = new int[WorkerTask.values().length];
		for (Entry<Integer, Integer> preassigned : assignedWorkerRoles.entrySet()) {
			workersAssigned[preassigned.getValue()]++;
		}
		workersStillAlive.clear();
		Iterator<Building> farmIterator = faction.getBuildings().iterator();
		for(Unit unit : faction.getUnits()) {
			if(unit.isBuilder()) {
				workersStillAlive.add(unit.id());
				if(!unit.isAutoBuilding()) {
					unit.setAutoBuild(true);
				}
				PlannedAction p = unit.getNextPlannedAction();
				if (p != null && p.isBuildAction()) {
					continue;
				}
				int taskIndex = 0;
				if (assignedWorkerRoles.containsKey(unit.id())) {
					taskIndex = assignedWorkerRoles.get(unit.id());
				}
				else {
					while(workersAssigned[taskIndex] >= phases[currentPhase].workerAssignments[taskIndex]) {
						taskIndex = taskIndex + 1;
						if (taskIndex >= workersAssigned.length) {
							taskIndex = 0;
							break;
						}
					}
					assignedWorkerRoles.put(unit.id(), taskIndex);
					workersAssigned[taskIndex]++;
				}
				switch (WorkerTask.values()[taskIndex]) {
				case FARM:
					handleFarmingWorker(unit, farmIterator);
					break;
				case CHOP:
					handleChoppingWorker(unit);
					break;
				case FORAGE:
					handleForagingWorker(unit);
					break;
				case GATHERSTONE:
					handleGatherStoneWorker(unit);
					break;
				case GATHERSILVER:
					handleGatherMetalWorker(unit, silverTiles);
					break;
				case GATHERCOPPER:
					handleGatherMetalWorker(unit, copperTiles);
					break;
				case GATHERIRON:
					handleGatherMetalWorker(unit, ironTiles);
					break;
				case GATHERCOAL:
					handleGatherMetalWorker(unit, coalTiles);
					break;
				case GATHERMITHRIL:
					handleGatherMetalWorker(unit, mithrilTiles);
					break;
				}
			}
			else if(unit.getType().isCaravan()) {
				if (unit.isIdle()) {
					pickupResources(unit);
				}
			}
			else {
				if (unit.isIdle()) {
					attackTowardsNearestEnemy(unit);
				}
			}
		}
		for (Integer id : assignedWorkerRoles.keySet()) {
			if (!workersStillAlive.contains(id)) {
				deadWorkersToRemove.add(id);
			}
		}
		for (Integer id : deadWorkersToRemove) {
			assignedWorkerRoles.remove(id);
		}
		deadWorkersToRemove.clear();
		System.out.print("Worker assignments: ");
		for (int i = 0; i < workersAssigned.length; i++) {
			System.out.print(WorkerTask.values()[i].name() + ": " + workersAssigned[i] + ", ");
		}
	}

	private void handleGatherMetalWorker(Unit worker, List<Tile> tiles) {
		Tile tile = tiles.get(worker.id() % tiles.size());
		if(tile == null) {
			return;
		}
		commands.planAction(worker, PlannedAction.harvestTile(tile), true);
	}
	
	private void handleGatherStoneWorker(Unit worker) {
		Tile tile = getTargetTile(worker.getTile(), 0, MAX_SEARCH_RADIUS, e -> {
			return e.getTerrain() == Terrain.ROCK;
		});
		if(tile == null) {
			return;
		}
		commands.planAction(worker, PlannedAction.harvestTile(tile), true);
	}
	
	private boolean pickupResources(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), 0, MAX_SEARCH_RADIUS, e -> {
			return !e.getInventory().isEmpty() && !e.isBlocked(unit);
		});
		if(tile == null) {
			return false;
		}
		commands.planAction(unit, PlannedAction.moveTo(tile), true);
		commands.planAction(unit, PlannedAction.deliver(castle), false);
		return true;
	}
	
	private void attackTowardsNearestEnemy(Unit unit) {
		if(!unit.isGuarding()) {
			commands.setGuarding(unit, true);
		}
		Tile homeTile = (castle != null) ? castle.getTile() : unit.getTile();

		List<Tile> biggestRange = null;
		for (int range = 0; range < 20; range++) {
			boolean noneInFaction = true;
			biggestRange = Utils.getRingOfTiles(homeTile, world, range);
			for (Tile tile : biggestRange) {
				if (tile.getFaction() != unit.getFaction()) {
					continue;
				}
				noneInFaction = false;
				for (Unit other : tile.getUnits()) {
					if (other.getFaction() != unit.getFaction() &&
							other != unit) {

						commands.planAction(unit, PlannedAction.attackMoveTo(tile), true);
						return;
					}
				}
				if (tile.getBuilding() != null && tile.getBuilding().getFaction() != unit.getFaction()) {
					commands.planAction(unit, PlannedAction.attack(tile.getBuilding()), true);
					return;
				}
			}
			if (noneInFaction) {
				break;
			}
		}
		
		Tile randomTile = biggestRange.get((int)(Math.random()*biggestRange.size()));
		commands.planAction(unit, PlannedAction.attackMoveTo(randomTile), true);
	}

	private boolean handleForagingWorker(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), 0, MAX_SEARCH_RADIUS, e -> {
			
			return (e.getPlant() != null && e.getPlant().getType() != Game.plantTypeMap.get("TREE"))
					|| e.getInventory().getItemAmount(ItemType.FOOD) > 0;
		});
		if(tile == null) {
			return false;
		}
		if(tile.getPlant() != null && tile.getPlant().getType() != Game.plantTypeMap.get("TREE")) {
			commands.planAction(unit, PlannedAction.harvest(tile.getPlant()), true);
		}
		else {
			commands.planAction(unit, PlannedAction.moveTo(tile), true);
			commands.planAction(unit, PlannedAction.deliver(castle), false);
		}
		return true;
	}
	
	private void handleChoppingWorker(Unit worker) {
		Tile tile = getTargetTile(worker.getTile(), 0, MAX_SEARCH_RADIUS, e -> {
			return e.getPlant() != null && e.getPlant().getType() == Game.plantTypeMap.get("TREE");
		});
		if(tile == null) {
			return;
		}
		commands.planAction(worker, PlannedAction.harvest(tile.getPlant()), true);
	}
	
	private void handleFarmingWorker(Unit worker, Iterator<Building> farmIterator) {
		Building farm = null;
		while (farmIterator.hasNext()) {
			Building building = farmIterator.next();
			if (building.getType() == FARM) {
				farm = building;
				break;
			}
		}

		if(farm != null) {
			finishBuildAndHarvest(worker, farm);
			return;
		}
		Tile homeTile = (castle != null) ? castle.getTile() : worker.getTile();
		Tile chosenTile = getTargetTile(homeTile, 1, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild() && e.canPlant();
		});
		if(chosenTile == null) {
			return;
		}
		buildAndHarvest(worker, chosenTile, FARM);
	}
	private void buildAndHarvest(Unit unit, Tile tile, BuildingType type) {
		Building building = checkCostAndBuild(type, unit, tile);
		if(building == null) {
			return;
		}
		commands.planAction(unit, PlannedAction.harvest(building), false);
		return;
	}

	private void finishBuildAndHarvest(Unit unit, Building harvestable) {
		boolean clearQueue = true;
		if(!harvestable.isBuilt()) {
			commands.planAction(unit, PlannedAction.buildOnTile(harvestable.getTile(), false), true);
			clearQueue = false;
		}
		commands.planAction(unit, PlannedAction.harvest(harvestable), clearQueue);
	}
	
	private boolean build() {
		boolean completed = true;
		for (Entry<BuildingType, QuantityReq> buildingReq : phases[currentPhase].buildingReqs.entrySet()) {

			if (buildingQuantities[buildingReq.getKey().id()] < buildingReq.getValue().enough) {
				completed = false;
			}
			if (buildingQuantities[buildingReq.getKey().id()] < buildingReq.getValue().max) {
				tryToBuild(buildingReq.getKey());
			}
		}	
		return completed;
	}
	
	private void tryToBuild(BuildingType type) {
		Unit chosenBuilder = null;
		for (Unit unit : faction.getUnits()) {
			if (unit.isBuilder() && unit.getBuildableBuildingTypes().contains(type)) {
				chosenBuilder = unit;
				break;
			}
		}
		if (chosenBuilder == null) {
			return;
		}

		Tile homeTile = (castle != null) ? castle.getTile() : chosenBuilder.getTile();
		Tile chosenTile = getTargetTile(homeTile, 1, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild();
		});
		if (chosenTile == null) {
			return;
		}
		checkCostAndBuild(type, chosenBuilder, chosenTile);
	}
	
	private Building checkCostAndBuild(BuildingType type, Unit builder, Tile tile) {
		if (!faction.canAfford(type.getCost())) {
			return null;
		}
		Building b = commands.planBuilding(builder, tile, true, type);
		buildingQuantities[type.id()]++;
		return b;
	}
	
	private boolean research() {
		boolean researchFinished = true;
		ListIterator<ResearchType> iter = phases[currentPhase].requiredResearch.listIterator();
		while (iter.hasNext()) {
			ResearchType type = iter.next();
			if (faction.getResearch(type).isCompleted()) {
				iter.remove();
				continue;
			}
			researchFinished = false;
			if(faction.setResearchTarget(type)) {
				return researchFinished;
			}
		}
		return researchFinished;
	}
	
	private boolean replentishUnits() {
		boolean completed = true;
		for (Entry<UnitType, QuantityReq> unitReq : phases[currentPhase].unitReqs.entrySet()) {
			if (unitQuantities[unitReq.getKey().id()] < unitReq.getValue().enough) {
				completed = false;
			}
			if (unitQuantities[unitReq.getKey().id()] < unitReq.getValue().max) {
				attemptToQueueUnit(unitReq.getKey());
			}
		}
		return completed;
	}
	
	private void attemptToQueueUnit(UnitType type) {
		for (Building building : faction.getBuildings()) {
			if (!building.getType().unitsCanProduceSet().contains(type)) {
				continue;
			}
			if (!building.getProducingUnit().isEmpty()) {
				continue;
			}
			commands.produceUnit(building, type);
		}
	}

	private Tile getTargetTile(Tile source, int minRadius, int maxRadius, Predicate<Tile> requirement) {
		for(int radius = minRadius; radius <= maxRadius; radius++) {
			List<Tile> candidates = Utils.getRingOfTiles(source, world, radius);
			while (!candidates.isEmpty()) {
				Tile t = candidates.remove((int)(Math.random() * candidates.size()));
				if(requirement.test(t)) {
					return t;
				}
			}
		}
		return null;
	}
}
