package game.ai;

import java.util.*;
import java.util.function.*;

import game.*;
import game.actions.*;
import ui.*;
import utils.*;
import world.*;

public class BasicAI extends AIInterface {
	public static final BuildingType FARM = Game.buildingTypeMap.get("FARM");
	public static final BuildingType MINE = Game.buildingTypeMap.get("MINE");
	public static final BuildingType LAB = Game.buildingTypeMap.get("RESEARCH_LAB");
	
	private static final int MAX_BUILD_RADIUS = 10;
	private static final int MAX_SEARCH_RADIUS = 40;
	
	private static final boolean DEBUG_AI = false;
	
	public enum WorkerTask {
		IDLE, IRRIGATE, FORAGE, CHOP, GATHERSTONE
	}
	public class State {
		Building barracks;
		
		LinkedList<Unit> workers = new LinkedList<>();
		int[] targetAssignments;
		HashMap<WorkerTask, LinkedList<Unit>> workerAssignments = new HashMap<>();
		HashMap<Unit, WorkerTask> taskPerWorker = new HashMap<>();
	}
	
	private State state;

	public BasicAI(CommandInterface commands, Faction faction, World world) {
		super(commands, faction, world);
		state = new State();
	}
	
	@Override
	public void aiTickLogic() {
		updateBuildings();
		updateUnits();
		queueResearch();
		computeTargetAssignments();
		
		if(castle != null) {
			
			reassignWorkers();
			for(Unit unit : faction.getUnits()) {
				if(unit.isIdle()) {
					if(unit.isBuilder()) {
						handleWorker(unit);
					}
					else if(unit.getType().isCaravan()) {
						pickupResources(unit);
					}
					else {
						defend(unit);
					}
				}
			}
			
			if(amountOfFoodMissing() == 0) {
				if(wantsWorker()) {
					if (Math.random() < 0.5) {
						commands.produceUnit(castle, Game.unitTypeMap.get("WORKER"));
					}
					else {
						commands.produceUnit(castle, Game.unitTypeMap.get("MASON"));
					}
				}
				if(wantsCaravan()) {
					commands.produceUnit(castle, Game.unitTypeMap.get("CARAVAN"));
				}
				if(state.barracks != null) {
					if(wantsWarrior()) {
						commands.produceUnit(state.barracks, Game.unitTypeMap.get("WARRIOR"));
					}
				}
			}
		}
	}

	/** TOTAL ASSIGNMENTS MUST SUM TO THE NUMBER OF WORKERS!!!*/
	private void computeTargetAssignments() {
		int[] assignments = new int[WorkerTask.values().length];
		int numWorkers = state.workers.size();
		assignments[WorkerTask.FORAGE.ordinal()] = 1;
		numWorkers -= assignments[WorkerTask.FORAGE.ordinal()];
		
		int numToAssignToFarming = amountOfFoodMissing() / 25 + 1;
		assignments[WorkerTask.IRRIGATE.ordinal()] = Math.min(numToAssignToFarming, numWorkers);
		numWorkers -= assignments[WorkerTask.IRRIGATE.ordinal()];
		

		assignments[WorkerTask.GATHERSTONE.ordinal()] = numWorkers/3;
		numWorkers -= assignments[WorkerTask.GATHERSTONE.ordinal()];
		assignments[WorkerTask.CHOP.ordinal()] = numWorkers;
		
		state.targetAssignments = assignments;
		if(DEBUG_AI) {
			System.out.println("assignments: " + printArray(state.targetAssignments));
		}
	}
	public static String printArray(int[] arr) {
		StringBuilder sb = new StringBuilder();
		for(int value : arr) {
			sb.append(value + ", ");
		}
		return sb.substring(0, sb.length()-2);
	}
	
	private LinkedList<Unit> getWorkersOnTask(WorkerTask task) {
		if(!state.workerAssignments.containsKey(task) ) {
			state.workerAssignments.put(task, new LinkedList<>());
		}
		return state.workerAssignments.get(task);
	}
	private void assignTask(Unit worker, WorkerTask task) {
		worker.clearPlannedActions();
		state.taskPerWorker.put(worker, task);
		getWorkersOnTask(task).add(worker);
	}
	private void reassignWorkers() {
		for(Unit worker : state.workers) {
			if(!state.taskPerWorker.containsKey(worker)) {
				assignTask(worker, WorkerTask.IDLE);
			}
		}
		for(WorkerTask task : WorkerTask.values()) {
			if(task == WorkerTask.IDLE) {
				continue;
			}
			while(getWorkersOnTask(task).size() > state.targetAssignments[task.ordinal()]) {
				assignTask(getWorkersOnTask(task).removeLast(), WorkerTask.IDLE);
			}
		}
		for(WorkerTask task : WorkerTask.values()) {
			if(task == WorkerTask.IDLE) {
				continue;
			}
			while(getWorkersOnTask(task).size() < state.targetAssignments[task.ordinal()]) {
				assignTask(getWorkersOnTask(WorkerTask.IDLE).removeLast(), task);
			}
		}
	}
	
	private void handleWorker(Unit worker) {
		if(!worker.isAutoBuilding()) {
			// TODO need to add autobuild to command interface
			worker.setAutoBuild(true);
		}
		
		WorkerTask task = state.taskPerWorker.get(worker);
		if(task == WorkerTask.IRRIGATE) {
			// before making a farm, see if there are any berry bushes nearby
			if(forage(worker, 3)) {
				return;
			}
			if(amountOfFoodMissing() > 0) {
				if(irrigate(worker)) {
					return;
				}
			}
			if(forage(worker, MAX_SEARCH_RADIUS)) {
				return;
			}
		}
		else if(task == WorkerTask.FORAGE) {
			forage(worker, MAX_SEARCH_RADIUS);
		}
		else if(task == WorkerTask.CHOP) {
			if(wantsBarracks()) {
				if(buildBarracks(worker)) {
					return;
				}
			}
			if(wantsWall()) {
				if(buildWall(worker)) {
					return;
				}
			}
			chopWood(worker);
		}
		else if(task == WorkerTask.GATHERSTONE) {
			gatherStone(worker);
			if(wantsRoad()) {
				if(buildRoad(worker)) {
					return;
				}
			}
		}
	}
	
	private boolean wantsWorker() {
		return unitQuantities[Game.unitTypeMap.get("WORKER").id()] < 20
				&& castle.getProducingUnit().isEmpty();
	}
	private boolean wantsWarrior() {
		return unitQuantities[Game.unitTypeMap.get("WARRIOR").id()] < 10
				&& state.barracks.getProducingUnit().isEmpty()
				&& unitQuantities[Game.unitTypeMap.get("WARRIOR").id()]*4 < faction.getUnits().size();
	}
	private boolean wantsCaravan() {
		return unitQuantities[Game.unitTypeMap.get("WARRIOR").id()] > 0
				&& castle.getProducingUnit().isEmpty()
				&& unitQuantities[Game.unitTypeMap.get("CARAVAN").id()] < 1;
	}
	
	private boolean wantsBarracks() {
		int numBarracks = buildingQuantities[0][Game.buildingTypeMap.get("BARRACKS").id()];
		return numBarracks < 1;
	}
	private boolean wantsWall() {
		return state.barracks != null && faction.getInventory().getItemAmount(ItemType.WOOD) > 200;
	}
	private boolean wantsRoad() {
		return state.barracks != null && faction.getInventory().getItemAmount(ItemType.STONE) > 300;
	}
	
	private int amountOfFoodMissing() {
		int targetFoodAmount = faction.getUnits().size() * 50;
		int needFood = targetFoodAmount - faction.getInventory().getItemAmount(ItemType.FOOD);
		return Math.max(0, needFood);
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
	
	private Tile getTargetTile(Tile source, int maxRadius, Predicate<Tile> requirement) {
		return getTargetTile(source, 0, maxRadius, requirement);
	}
	
	private boolean buildBarracks(Unit unit) {
		Tile tile = getTargetTile(castle.getTile(), MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild();
		});
		if(tile == null) {
			return false;
		}
		Building building = planBuilding(unit, tile, true, Game.buildingTypeMap.get("BARRACKS"));
		if(building != null) {
			buildingQuantities[0][Game.buildingTypeMap.get("BARRACKS").id()]++;
		}
		return building != null;
	}
	private boolean buildWall(Unit unit) {
		Tile tile = getTargetTile(castle.getTile(), MAX_BUILD_RADIUS, MAX_BUILD_RADIUS, e -> {
			return (!e.hasBuilding() && e.canBuild()) || (e.hasBuilding() && !e.getBuilding().isBuilt());
		});
		if(tile == null) {
			return false;
		}
		Building building = tile.getBuilding();
		if(building == null) {
			building = planBuilding(unit, tile, true, Game.buildingTypeMap.get("WALL_WOOD"));
		}
		else if(!building.isBuilt()) {
			commands.planAction(unit, PlannedAction.buildOnTile(tile, false), true);
		}
		return building != null;
	}
	private boolean buildRoad(Unit unit) {
		Tile tile = getTargetTile(castle.getTile(), 0, MAX_BUILD_RADIUS, e -> {
			return (!e.hasRoad() && e.canBuild()) || (e.hasRoad() && !e.getRoad().isBuilt());
		});
		if(tile == null) {
			return false;
		}
		Building road = tile.getRoad();
		if(road == null) {
			road = planBuilding(unit, tile, true, Game.buildingTypeMap.get("STONE_ROAD"));
		}
		else if(!road.isBuilt()) {
			commands.planAction(unit, PlannedAction.buildOnTile(tile, true), true);
		}
		return road != null;
	}
	
	private Building planBuilding(Unit unit, Tile tile, boolean clearQueue, BuildingType type) {
		if(faction.canAfford(type.getCost())) {
			return commands.planBuilding(unit, tile, clearQueue, type);
		}
		return null;
	}
	
	private boolean irrigate(Unit unit) {
		// look for already built irrigations first
		Tile existingIrrigation = getTargetTile(castle.getTile(), 1, MAX_BUILD_RADIUS, e -> {
			boolean hasOtherWorker = false;
			for (Unit u : e.getUnits()) {
				if (u.getFaction() == faction
						&& u.isBuilder()) {
					hasOtherWorker = true;
				}
			}
			return e.hasBuilding() 
					&& e.getBuilding().getType() == FARM
					&& !hasOtherWorker;
		});
		if(existingIrrigation != null) {
			return null != buildAndHarvest(unit, existingIrrigation, FARM);
		}
		
		// otherwise build a new irrigation nearby
		Tile tile = getTargetTile(castle.getTile(), 1, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild() && e.canPlant();
		});
		if(tile == null) {
			return false;
		}
		Building building = buildAndHarvest(unit, tile, FARM);
		return building != null;
	}
	private Building buildAndHarvest(Unit unit, Tile tile, BuildingType type) {
		Building building = tile.getBuilding();
		boolean clearQueue = true;
		if(building == null) {
			building = planBuilding(unit, tile, true, type);
			buildingQuantities[0][type.id()]++;
			clearQueue = false;
		}
		else if(!building.isBuilt()) {
			commands.planAction(unit, PlannedAction.buildOnTile(tile, false), true);
			clearQueue = false;
		}
		if(building != null) {
			commands.planAction(unit, PlannedAction.harvest(tile.getBuilding()), clearQueue);
		}
		return building;
	}
	
	private boolean gatherStone(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			return e.getTerrain() == Terrain.ROCK;
		});
		if(tile == null) {
			return false;
		}
		commands.planAction(unit, PlannedAction.harvestTile(tile), true);
		return true;
	}
	private boolean chopWood(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			return e.getPlant() != null && e.getPlant().getType() == Game.plantTypeMap.get("TREE");
		});
		if(tile == null) {
			return false;
		}
		commands.planAction(unit, PlannedAction.harvest(tile.getPlant()), true);
		return true;
	}
	private boolean forage(Unit unit, int searchRadius) {
		Tile tile = getTargetTile(unit.getTile(), searchRadius, e -> {
			
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
	
	private boolean pickupResources(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			return !e.getInventory().isEmpty() && !e.isBlocked(unit);
		});
		if(tile == null) {
			return false;
		}
		commands.planAction(unit, PlannedAction.moveTo(tile), true);
		commands.planAction(unit, PlannedAction.deliver(castle), false);
		return true;
	}
	
	private boolean defend(Unit unit) {
		Tile target = getTargetTile(castle.getTile(), MAX_SEARCH_RADIUS, e -> {
			for(Unit u : e.getUnits()) {
				if(u.getFactionID() != faction.id()) {
					return true;
				}
			}
			return false;
		});
		if(target == null) {
			return false;
		}
		for(Unit u : target.getUnits()) {
			if(u.getFactionID() != faction.id()) {
				commands.planAction(unit, PlannedAction.attack(u), true);
			}
		}
		
//		if(unit.getTile().getLocation().distanceTo(state.castle.getTile().getLocation()) < MAX_BUILD_RADIUS/2) {
//			Tile tile = getTargetTile(unit.getTile(), MAX_BUILD_RADIUS/2, MAX_BUILD_RADIUS*2/3, e -> {
//				return e.getUnits().isEmpty();
//			});
//			if(tile == null) {
//				return false;
//			}
//			commands.moveTo(unit, tile, true);
//			return true;
//		}
		return false;
	}
	
	private void updateBuildings() {
		if(state.barracks != null && state.barracks.isDead()) {
			state.barracks = null;
		}
		for(Building building : faction.getBuildings()) {
			if(state.barracks == null && building.getType() == Game.buildingTypeMap.get("BARRACKS") && !building.isDead()) {
				state.barracks = building;
			}
		}
	}
	private void updateUnits() {
		state.workers.clear();
		for(Unit unit : faction.getUnits()) {
			if(unit.isBuilder()) {
				state.workers.add(unit);
			}
		}
	}
	
	private void queueResearch() {
		if(faction.getResearchTarget() != null) {
			return;
		}
		for(ResearchType type : Game.researchTypeList) {
			faction.setResearchTarget(type);
			if(faction.getResearchTarget() != null) {
				return;
			}
		}
		
	}
}
