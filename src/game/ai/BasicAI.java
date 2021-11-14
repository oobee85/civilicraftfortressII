package game.ai;

import java.util.*;
import java.util.function.*;

import game.*;
import game.actions.*;
import ui.*;
import utils.*;
import world.*;

public class BasicAI implements AIInterface {
	public static final BuildingType FARM = Game.buildingTypeMap.get("FARM");
	
	private static final int MAX_BUILD_RADIUS = 10;
	private static final int MAX_SEARCH_RADIUS = 40;
	
	private static final boolean DEBUG_AI = false;
	
	public enum WorkerTask {
		IDLE, IRRIGATE, FORAGE, CHOP, GATHERSTONE
	}
	public class State {
		Building castle;
		Building barracks;
		int[] buildingQuantities;
		int[] unitQuantities;
		
		LinkedList<Unit> workers = new LinkedList<>();
		int[] targetAssignments;
		HashMap<WorkerTask, LinkedList<Unit>> workerAssignments = new HashMap<>();
		HashMap<Unit, WorkerTask> taskPerWorker = new HashMap<>();
	}
	
	private CommandInterface commands;
	private Faction faction;
	private World world;
	private State state;

	public BasicAI(CommandInterface commands, Faction faction, World world) {
		this.commands = commands;
		this.faction = faction;
		this.world = world;
		state = new State();
	}
	
	@Override
	public void tick() {
		updateBuildings();
		updateUnits();
		queueResearch();
		computeTargetAssignments();
		
		if(state.castle != null) {
			
			reassignWorkers();
			for(Unit unit : faction.getUnits()) {
				if(unit.isIdle()) {
					if(unit.getType().isBuilder()) {
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
					commands.produceUnit(state.castle, Game.unitTypeMap.get("WORKER"));
				}
				if(wantsCaravan()) {
					commands.produceUnit(state.castle, Game.unitTypeMap.get("CARAVAN"));
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
		return state.unitQuantities[Game.unitTypeMap.get("WORKER").id()] < 20
				&& state.castle.getProducingUnit().isEmpty();
	}
	private boolean wantsWarrior() {
		return state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()] < 10
				&& state.barracks.getProducingUnit().isEmpty()
				&& state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()]*5 < faction.getUnits().size();
	}
	private boolean wantsCaravan() {
		return state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()] > 0
				&& state.castle.getProducingUnit().isEmpty()
				&& state.unitQuantities[Game.unitTypeMap.get("CARAVAN").id()] < 1;
	}
	
	private boolean wantsBarracks() {
		int numBarracks = state.buildingQuantities[Game.buildingTypeMap.get("BARRACKS").id()];
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
		List<TileLoc> candidates = new LinkedList<>();
		for(int radius = minRadius; radius <= maxRadius; radius++) {
			Utils.getRingOfTiles(source.getLocation(), world, radius, candidates);
			Collections.shuffle(candidates);
			while(!candidates.isEmpty()) {
				Tile potential = world.get(candidates.remove(0));
				if(potential == null) {
					continue;
				}
				if(requirement.test(potential)) {
					return potential;
				}
			}
		}
		return null;
	}
	
	private Tile getTargetTile(Tile source, int maxRadius, Predicate<Tile> requirement) {
		return getTargetTile(source, 0, maxRadius, requirement);
	}
	
	private boolean buildBarracks(Unit unit) {
		Tile tile = getTargetTile(state.castle.getTile(), MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild();
		});
		if(tile == null) {
			return false;
		}
		Building building = planBuilding(unit, tile, true, Game.buildingTypeMap.get("BARRACKS"));
		if(building != null) {
			state.buildingQuantities[Game.buildingTypeMap.get("BARRACKS").id()]++;
		}
		return building != null;
	}
	private boolean buildWall(Unit unit) {
		Tile tile = getTargetTile(state.castle.getTile(), MAX_BUILD_RADIUS, MAX_BUILD_RADIUS, e -> {
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
		Tile tile = getTargetTile(state.castle.getTile(), 0, MAX_BUILD_RADIUS, e -> {
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
		Tile existingIrrigation = getTargetTile(state.castle.getTile(), 1, MAX_BUILD_RADIUS, e -> {
			return e.hasBuilding() && e.getBuilding().getType() == FARM;
		});
		if(existingIrrigation != null) {
			return null != buildAndHarvest(unit, existingIrrigation, FARM);
		}
		
		// otherwise build a new irrigation nearby
		Tile tile = getTargetTile(state.castle.getTile(), 1, MAX_BUILD_RADIUS, e -> {
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
			state.buildingQuantities[type.id()]++;
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
			commands.planAction(unit, PlannedAction.deliver(state.castle), false);
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
		commands.planAction(unit, PlannedAction.deliver(state.castle), false);
		return true;
	}
	
	private boolean defend(Unit unit) {
		if(!unit.isGuarding()) {
			commands.setGuarding(unit, true);
		}
		Tile target = getTargetTile(state.castle.getTile(), MAX_SEARCH_RADIUS, e -> {
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
		if(state.castle != null && state.castle.isDead()) {
			state.castle = null;
		}
		if(state.barracks != null && state.barracks.isDead()) {
			state.barracks = null;
		}
		int[] buildingQuantities = new int[Game.buildingTypeMap.size()];
		for(Building building : faction.getBuildings()) {
			buildingQuantities[building.getType().id()]++;
			if(state.castle == null && building.getType().isCastle() && !building.isDead()) {
				state.castle = building;
			}
			if(state.barracks == null && building.getType() == Game.buildingTypeMap.get("BARRACKS") && !building.isDead()) {
				state.barracks = building;
			}
			
		}
		state.buildingQuantities = buildingQuantities;
	}
	private void updateUnits() {
		int[] unitQuantities = new int[Game.unitTypeMap.size()];
		state.workers.clear();
		for(Unit unit : faction.getUnits()) {
			unitQuantities[unit.getType().id()]++;
			if(unit.getType().isBuilder()) {
				state.workers.add(unit);
			}
		}
		state.unitQuantities = unitQuantities;
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
