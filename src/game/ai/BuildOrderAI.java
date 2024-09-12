package game.ai;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;

import game.*;
import game.actions.PlannedAction;
import game.ai.BuildOrderPhase.WorkerTask;
import ui.CommandInterface;
import utils.Utils;
import world.*;

public class BuildOrderAI extends AIInterface {

	public static final BuildingType FARM = Game.buildingTypeMap.get("FARM");
	private static final int MAX_BUILD_RADIUS = 40;
	private static final int CLOSE_FORAGE_RADIUS = 3;
	private static final int FAR_FORAGE_RADIUS = 40;

	private UnitManager unitManager;
	private HashSet<Integer> workersStillAlive = new HashSet<>(); // value is a worker Unit ID

	public BuildOrderAI(CommandInterface commands, Faction faction, World world) {
		super(commands, faction, world);
		
		phases = new ArrayList<>();
		for (BuildOrderPhase phase : BuildOrderPhase.phases) {
			phases.add(new BuildOrderPhase(phase));
		}

		unitManager = new UnitManager(phases.get(currentPhase).workerAssignments);
	}
	
	int currentPhase = 0;
	public List<BuildOrderPhase> phases;
	
	
//	{
//		Phase phase0 = new Phase();
//		phase0.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(0, 4, 10));
//		phase0.requiredResearch.add(Game.researchTypeMap.get("FARMING"));
//		phase0.workerAssignments = new int[] {3, 3, 4, 0, 0, 0, 0, 0, 0, 0};
//
//		Phase phase1 = new Phase();
//		phase1.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(4, 8, 12));
//		phase1.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(0, 1, 1));
//		phase1.workerAssignments = new int[] {3, 4, 5, 0, 0, 0, 0, 0, 0, 0};
//
//		Phase phase2 = new Phase();
//		phase2.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(8, 15, 30));
//		phase2.unitReqs.put(Game.unitTypeMap.get("WARRIOR"), new QuantityReq(0, 5, 8));
//		phase2.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 1));
//		phase2.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
//		phase2.requiredResearch.add(Game.researchTypeMap.get("BRONZE_WORKING"));
//		phase2.workerAssignments = new int[] {2, 16, 8, 4, 0, 0, 0, 0, 0, 0};
//
//		Phase phase3 = new Phase();
//		phase3.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(8, 20, 34));
//		phase3.unitReqs.put(Game.unitTypeMap.get("WARRIOR"), new QuantityReq(0, 5, 5));
//		phase3.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 1));
//		phase3.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 4));
//		phase3.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 1, 4));
//		phase3.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 0, 5));
//		phase3.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
//		phase3.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
//		phase3.requiredResearch.add(Game.researchTypeMap.get("IRON_WORKING"));
//		phase3.requiredResearch.add(Game.researchTypeMap.get("WHEEL"));
//		phase3.requiredResearch.add(Game.researchTypeMap.get("MASONRY"));
//		phase3.workerAssignments = new int[] {1, 16, 7, 2, 2, 2, 0, 0, 0, 0};
//
//		Phase phase4 = new Phase();
//		phase4.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 22, 36));
//		phase4.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 8, 16));
//		phase4.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 2));
//		phase4.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 10));
//		phase4.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 6));
//		phase4.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 1, 5));
//		phase4.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
//		phase4.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
////		phase4.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
//		phase4.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
//		phase4.workerAssignments = new int[] {1, 19, 6, 2, 2, 2, 3, 1, 0};
//
//		Phase phase5 = new Phase();
//		phase5.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 24, 38));
//		phase5.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 12, 22));
//		phase5.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 2));
//		phase5.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 2, 10));
//		phase5.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 6));
//		phase5.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 1, 5));
//		phase5.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
//		phase5.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
////		phase5.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
//		phase5.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
//		phase5.requiredResearch.add(Game.researchTypeMap.get("MATHEMATICS"));
//		phase5.workerAssignments = new int[] {1, 22, 7, 1, 1, 3, 7, 3, 0};
//
//		Phase phase6 = new Phase();
//		phase6.unitReqs.put(Game.unitTypeMap.get("WORKER"), new QuantityReq(10, 26, 40));
//		phase6.unitReqs.put(Game.unitTypeMap.get("MASON"), new QuantityReq(0, 16, 30));
//		phase6.unitReqs.put(Game.unitTypeMap.get("CARAVAN"), new QuantityReq(0, 1, 3));
//		phase6.unitReqs.put(Game.unitTypeMap.get("ARCHER"), new QuantityReq(0, 0, 8));
//		phase6.unitReqs.put(Game.unitTypeMap.get("SPEARMAN"), new QuantityReq(0, 0, 5));
//		phase6.unitReqs.put(Game.unitTypeMap.get("SWORDSMAN"), new QuantityReq(0, 0, 12));
//		phase6.unitReqs.put(Game.unitTypeMap.get("LONGBOWMAN"), new QuantityReq(0, 1, 10));
//		phase6.unitReqs.put(Game.unitTypeMap.get("CATAPULT"), new QuantityReq(0, 1, 6));
//		phase4.unitReqs.put(Game.unitTypeMap.get("GRIFFIN"), new QuantityReq(0, 0, 4));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("BARRACKS"), new QuantityReq(1, 1, 1));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("SMITHY"), new QuantityReq(1, 1, 1));
////		phase6.buildingReqs.put(Game.buildingTypeMap.get("GRANARY"), new QuantityReq(1, 1, 1));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("RESEARCH_LAB"), new QuantityReq(1, 1, 1));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("WORKSHOP"), new QuantityReq(1, 1, 1));
//		phase6.buildingReqs.put(Game.buildingTypeMap.get("STABLES"), new QuantityReq(1, 1, 1));
//		phase6.requiredResearch.add(Game.researchTypeMap.get("MYSTICISM"));
//		phase6.requiredResearch.add(Game.researchTypeMap.get("ENGINEERING"));
//		phase6.workerAssignments = new int[] {1, 28, 6, 1, 1, 3, 6, 4, 4};
//
//		phases = new Phase[] {phase0, phase1, phase2, phase3, phase4, phase5, phase6};
//	}

	@Override
	public void aiTickLogic() {
		craftItems();
		boolean completedResearch = research();
		boolean completedBuilding = build();
		unitActions();
		boolean completedUnits = replentishUnits();
		
		
		if (completedUnits && completedResearch && completedBuilding) {
			if (currentPhase + 1 < phases.size()) {
				currentPhase++;
				phaseTransition(currentPhase);
			}
		}
//		else {
//			if (currentPhase > 6) {
//				System.out.println(faction + " current phase: " + currentPhase
//						+ ", completedResearch: " + completedResearch 
//						+ ", completedBuilding: " + completedBuilding
//						+ ", completedUnits: " + completedUnits);
//			}
//		}
	}
	
	private void craftItems() {
		if (faction.getInventory().getItemAmount(ItemType.COAL) > 100) {
			commands.craftItem(faction, ItemType.MITHRIL_BAR, 10);
			commands.craftItem(faction, ItemType.IRON_BAR, 10);
		}
		else {
			commands.craftItem(faction, ItemType.MITHRIL_BAR, 1);
			commands.craftItem(faction, ItemType.IRON_BAR, 1);
		}
		commands.craftItem(faction, ItemType.BRONZE_BAR, 5);
	}
	
	LinkedList<Tile> silverTiles = new LinkedList<Tile>();
	LinkedList<Tile> copperTiles = new LinkedList<Tile>();
	LinkedList<Tile> ironTiles = new LinkedList<Tile>();
	LinkedList<Tile> coalTiles = new LinkedList<Tile>();
	LinkedList<Tile> mithrilTiles = new LinkedList<Tile>();
	private void phaseTransition(int newPhase) {
		heatmap.clear();
		unitManager.newPhase(phases.get(newPhase).workerAssignments);
		System.out.println(faction + " transitioning to phase " + newPhase);
		if (newPhase == 1) {
			Unit w = new Unit(Game.unitTypeMap.get("WORKER"), world.getRandomTile(), faction);
			for (Tile t : world.getTiles()) {
				if (t.getResource() == null) {
					continue;
				}
				if (t.isBlocked(w)) {
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
				sortAndRemoveAllBut(silverTiles, 5);
				sortAndRemoveAllBut(copperTiles, 5);
				sortAndRemoveAllBut(ironTiles, 5);
				sortAndRemoveAllBut(coalTiles, 5);
				sortAndRemoveAllBut(mithrilTiles, 5);
			}
		}
	}
	
	private void sortAndRemoveAllBut(LinkedList<Tile> tiles, int numToKeep) {
		Tile homeTile = getHomeTile(null);
		Collections.sort(tiles, (Tile a, Tile b) -> {
			return a.distanceTo(homeTile) - b.distanceTo(homeTile);
		});
		while(tiles.size() > numToKeep) {
			tiles.removeLast();
		}
	}
	
	
	class TileHeatmap {
		private HashMap<Tile, Integer> steps = new HashMap<>();
		
		public void clear() {
			steps.clear();
		}
		public void steppedOn(Tile tile) {
			if (!steps.containsKey(tile)) {
				steps.put(tile, 0);
			}
			steps.put(tile, 1 + steps.get(tile));
		}
		
		public Tile getMostSteppedOnWithoutRoad() {
			Tile most = null;
			int mostSteps = 0;
			for (Entry<Tile, Integer> entry : steps.entrySet()) {
				if (entry.getKey().hasRoad()) {
					continue;
				}
				if (entry.getValue() >= mostSteps) {
					most = entry.getKey();
					mostSteps = entry.getValue();
				}
			}
			return most;
		}
	}
	
	private TileHeatmap heatmap = new TileHeatmap();
	private void unitActions() {

		workersStillAlive.clear();
		for(Unit unit : faction.getUnits()) {
			if(!unit.isBuilder()) {
				continue;
			}
			workersStillAlive.add(unit.id());
			unitManager.checkIfMissionComplete(unit.id());
		}
		unitManager.removeDeadWorkers(workersStillAlive);

		unitManager.countAssignments();
		for(Unit unit : faction.getUnits()) {
			heatmap.steppedOn(unit.getTile());
			if(unit.isBuilder()) {
				handleWorker(unit);
			}
			else if(unit.getType().isCaravan()) {
				if (unit.isIdle()) {
					handleCaravan(unit);
				}
			}
			else {
				if (unit.isIdle()) {
					handleCombatUnit(unit);
				}
			}
		}
	}

	private void handleCombatUnit(Unit unit) {
		if(!unit.isGuarding()) {
			commands.setGuarding(unit, true);
		}
		if (unit.getMaxAttackRange() > 19) {
			attackNearestEnemyBuilding(unit);
		}
		else {
			attackTowardsNearestEnemy(unit);
		}
	}
	
	private void handleWorker(Unit worker) {
//		if(!worker.isAutoBuilding()) {
//			worker.setAutoBuild(true);
//		}
		Mission mission = unitManager.getMissionFor(worker.id());
		if (mission != null) {
//			System.out.println("Worker " + worker + " has mission " + mission);
			mission.attempt(worker);
			return;
//			System.out.println("NON NULL MISSION");
//			if (mission.attempt(worker)) {
//				System.out.println("MISSION SUCCESS");
//				return;
//			}
//			else {
//				System.out.println("MISSION FAILED");
//				unitManager.addMission(mission);
//			}
		}

		WorkerTask task = unitManager.getTaskFor(worker.id());
		boolean result = true;
		switch (task) {
		case FARM:
			if (!handleFarmingWorker(worker)) {
				handleChoppingWorker(worker);
			}
			break;
		case CHOP:
			handleChoppingWorker(worker);
			break;
		case CLOSEFORAGE:
			result = handleForagingWorker(worker, CLOSE_FORAGE_RADIUS);
			if (!result) {
				handleChoppingWorker(worker);
			}
			break;
		case FORAGE:
			result = handleForagingWorker(worker, FAR_FORAGE_RADIUS);
			break;
		case GATHERSTONE:
			handleGatherStoneWorker(worker);
			break;
		case GATHERSILVER:
			result = handleGatherMetalWorker(worker, silverTiles);
			break;
		case GATHERCOPPER:
			result = handleGatherMetalWorker(worker, copperTiles);
			break;
		case GATHERIRON:
			result = handleGatherMetalWorker(worker, ironTiles);
			break;
		case GATHERCOAL:
			result = handleGatherMetalWorker(worker, coalTiles);
			break;
		case GATHERMITHRIL:
			result = handleGatherMetalWorker(worker, mithrilTiles);
			break;
		}
		
		if (!result) {
			System.out.println(worker + "Failed to do task: " + task);
			unitManager.unassign(worker.id());
		}
	}
	

	private boolean handleGatherMetalWorker(Unit worker, List<Tile> tiles) {
		if (tiles.size() == 0) {
			return false;
		}
		Tile tile = tiles.get(worker.id() % tiles.size());
		if(tile == null) {
			return false;
		}
		commands.planAction(worker, PlannedAction.harvestTile(tile), true);
		return true;
	}
	
	private void handleGatherStoneWorker(Unit worker) {
		Tile tile = getTargetTile(worker.getTile(), 0, FAR_FORAGE_RADIUS, e -> {
			return e.getTerrain() == Terrain.ROCK 
					&& (e.getBuilding() == null || !e.getBuilding().getType().isCastle()) ;
		});
		if(tile == null) {
			return;
		}
		commands.planAction(worker, PlannedAction.harvestTile(tile), true);
	}
	
	private Building getRandomCompletedCastle() {
		if (castles.isEmpty()) {
			return castle;
		}
		return castles.get((int)(Math.random()*castles.size()));
	}
	
	private boolean handleCaravan(Unit unit) {
		Building stables = unitManager.getStablesForCaravan(unit, faction.getBuildings());
		if (stables != null) {
			commands.planAction(unit, PlannedAction.takeItemsFrom(stables), true);
			return true;
		}
		Tile tile = getTargetTile(getHomeTile(unit), 0, FAR_FORAGE_RADIUS, e -> {
			return !e.getInventory().isEmpty() && !e.isBlocked(unit);
		});
		if(tile == null) {
			return false;
		}
		commands.planAction(unit, PlannedAction.moveTo(tile), true);
		commands.planAction(unit, PlannedAction.deliver(getRandomCompletedCastle()), false);
		return true;
	}
	private Tile getHomeTile(Unit unit) {
		if (!castles.isEmpty()) {
			return castles.get((int)(Math.random()*castles.size())).getTile();
		}
		if (castle != null) {
			return castle.getTile();
		}
		if (unit != null) {
			return unit.getTile();
		}
		for(Building b : faction.getBuildings()) {
			return b.getTile();
		}
		for (Unit u : faction.getUnits()) {
			return u.getTile();
		}
		return world.getRandomTile();
	}

	private void attackNearestEnemyBuilding(Unit unit) {
		Tile homeTile = getHomeTile(unit);
		
		Building target = null;
		int closestDistance = Integer.MAX_VALUE;
		for (Building building : world.getBuildings()) {
			if (building.getFaction().id() == faction.id()) {
				continue;
			}
			if (building.getType().isRoad()) {
				continue;
			}
			
			int distance = building.getTile().distanceTo(homeTile);
			if (distance < closestDistance) {
				closestDistance = distance;
				target = building;
			}
		}
		if (target == null) {
			return;
		}
		commands.planAction(unit, PlannedAction.attack(target), true);
	}
	private void attackTowardsNearestEnemy(Unit unit) {
		Tile homeTile = getHomeTile(unit);

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

	private boolean handleForagingWorker(Unit unit, int radius) {
		Tile tile = getTargetTile(unit.getTile(), 0, radius, e -> {
			
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
		Tile tile = getTargetTile(worker.getTile(), 0, FAR_FORAGE_RADIUS, e -> {
			return e.getPlant() != null && e.getPlant().getType() == Game.plantTypeMap.get("TREE");
		});
		if(tile == null) {
			return;
		}
		commands.planAction(worker, PlannedAction.harvest(tile.getPlant()), true);
	}
	
	
	private boolean handleFarmingWorker(Unit worker) {
		Building farmNeedsWorker = unitManager.getFarmForWorker(worker, faction.getBuildings());
		if(farmNeedsWorker != null) {
			finishBuildAndHarvest(worker, farmNeedsWorker);
			return true;
		}
		
		Tile homeTile = getHomeTile(worker);
		Tile chosenTile = getTargetTile(homeTile, 1, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild() && e.canPlant();
		});
		if(chosenTile == null) {
			return false;
		}
		Building farm = buildAndHarvest(worker, chosenTile, FARM);
		if (farm == null) {
			return false;
		}
		unitManager.assignWorkerToFarm(worker, farm);
		return true;
	}
	private Building buildAndHarvest(Unit unit, Tile tile, BuildingType type) {
		Building building = checkCostAndBuild(type, unit, tile);
		if(building == null) {
			return null;
		}
		commands.planAction(unit, PlannedAction.harvest(building), false);
		return building;
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
		for (Entry<BuildingType, QuantityReq> buildingReq : phases.get(currentPhase).buildings.entrySet()) {
			BuildingType type = buildingReq.getKey();
			if (buildingQuantities[1][type.id()] < buildingReq.getValue().enough) {
				completed = false;
			}
			if (buildingQuantities[0][type.id()] >= buildingReq.getValue().max) {
				continue;
			}
			
			if (type == Game.buildingTypeMap.get("BRICK_ROAD")) {
				if (faction.getInventory().getItemAmount(ItemType.STONE) < 1000) {
					continue;
				}
			}
			

			Mission build;
			TileSelector selector;
			if (type.isRoad()) {
				selector = (Unit unit) -> heatmap.getMostSteppedOnWithoutRoad();
			}
			else {
				if (type.isCastle()) {
					int MINIMUM_CASTLE_SEPARATION = 10;
					selector = (Unit unit) -> getTargetTile(getHomeTile(unit), MINIMUM_CASTLE_SEPARATION, MAX_BUILD_RADIUS*2, e -> {
						for (Building castle : castles) {
							if (castle.getTile().distanceTo(e) < MINIMUM_CASTLE_SEPARATION) {
								return false;
							}
						}
						return !e.hasBuilding() && e.canBuild();
					});
				}
				else if (type == Game.buildingTypeMap.get("STABLES")) {
					selector = (Unit unit) -> getTargetTile(getHomeTile(unit), 1, MAX_BUILD_RADIUS, e -> {
						return !e.hasBuilding() && e.canBuild() && e.hasUnit(Game.unitTypeMap.get("HORSE"));
					});
				}
				else {
					selector = (Unit unit) -> getTargetTile(getHomeTile(unit), 1, MAX_BUILD_RADIUS, e -> {
						return !e.hasBuilding() && e.canBuild();
					});
				}
			}
			build = new Mission() {
				Building building;
				
				@Override
				public String toString() {
					if (building == null) {
						return "Mission build " + type;
					}
					else {
						return "Mission build " + building;
					}
				}
				@Override
				public boolean isComplete() {
					if (building != null 
							&& building.getType().isRoad()) {
						
						if (building.getTile().hasRoad() && building.getTile().getRoad() != building) {
							System.out.println("isComplete chosen tile already has road!");
						}
						
					}
					return building != null && building.isBuilt();
				}
				
				@Override
				public boolean isStarted() {
					return building != null;
				}
				
				@Override
				public boolean isPossible() {
					return false;
				}
				
				@Override
				public boolean attempt(Unit unit) {
					if (building != null && !building.isBuilt() && building.isDead()) {
						building = null;
					}
					if (building != null ) {
						PlannedAction action = unit.actionQueue.peek();
						if (action != null && action.target == building) {
							return true;
						}
						commands.planAction(unit, PlannedAction.buildOnTile(building.getTile(), building.getType().isRoad()), true);
						return true;
					}
					Tile chosenTile = selector.selectTile(unit);
					if (chosenTile == null) {
						return false;
					}
					if (!faction.canAfford(type.getCost())) {
						return false;
					}
					building = commands.planBuilding(unit, chosenTile, true, type);
					return building != null;
				}
			};
			unitManager.addUnstartedMission(build, type.toString());
		}	
		return completed;
	}
	
	private Building checkCostAndBuild(BuildingType type, Unit builder, Tile tile) {
		if (!faction.canAfford(type.getCost())) {
			return null;
		}
		return commands.planBuilding(builder, tile, true, type);
	}
	HashMap<ItemType, Integer> researchCost;
	private boolean research() {
		researchCost = null;
		boolean allResearchesFinished = true;
//		ListIterator<ResearchType> iter = phases.get(currentPhase).requiredResearches.listIterator();
		for (ResearchType type : phases.get(currentPhase).requiredResearches) {
//		while(iter.hasNext()) {
//			ResearchType type = iter.next();
			if (faction.getResearch(type).isCompleted()) {
//				System.out.println(faction + " research completed " + type);
//				iter.remove();
				continue;
			}
			allResearchesFinished = false;
			if (faction.getResearchTarget() != null) {
				break;
			}
			if(faction.setResearchTarget(type)) {
				System.out.println(faction + " started research " + type);
				break;
			}
			else {
				researchCost = type.cost;
			}
		}
		if (faction.getResearchTarget() == null) {
			for (ResearchType type : phases.get(currentPhase).optionalResearches) {
				if (faction.getResearch(type).isCompleted()) {
					continue;
				}
				if(faction.setResearchTarget(type)) {
					System.out.println(faction + " started research " + type);
					break;
				}
			}
		}
		return allResearchesFinished;
	}
	
	private boolean ensureSaveForResearchCost(HashMap<ItemType, Integer> potentialCost) {
		if (researchCost == null) {
			return true;
		}
		for (Entry<ItemType, Integer> cost : potentialCost.entrySet()) {
			if (!researchCost.containsKey(cost.getKey())) {
				continue;
			}
			int potentialRemaining = faction.getInventory().getItemAmount(cost.getKey()) - cost.getValue();
			if (potentialRemaining < researchCost.get(cost.getKey())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean replentishUnits() {
		boolean completed = true;
		for (Entry<UnitType, QuantityReq> unitReq : phases.get(currentPhase).units.entrySet()) {
			if (unitQuantities[unitReq.getKey().id()] >= unitReq.getValue().max) {
				continue;
			}
			boolean enough = unitQuantities[unitReq.getKey().id()] >= unitReq.getValue().enough;
			if (!enough) {
				completed = false;
			}
			if (enough && !ensureSaveForResearchCost(unitReq.getKey().getCost())) {
				continue;
			}
			attemptToQueueUnit(unitReq.getKey());
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
