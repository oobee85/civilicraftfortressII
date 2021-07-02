package game.ai;

import java.util.*;
import java.util.function.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class BasicAI implements AIInterface {
	private static final BuildingType IRRIGATION = Game.buildingTypeMap.get("IRRIGATION");
	
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
					else {
						defend(unit);
					}
				}
			}
			
			if(wantsWorker()) {
				commands.produceUnit(state.castle, Game.unitTypeMap.get("WORKER"));
			}
			if(state.barracks != null) {
				if(wantsWarrior() && !wantsMoreIrrigation()) {
					commands.produceUnit(state.barracks, Game.unitTypeMap.get("WARRIOR"));
				}
			}
		}
	}
	
	private void computeTargetAssignments() {
		int[] assignments = new int[WorkerTask.values().length];
		int numWorkers = state.workers.size();
		assignments[WorkerTask.FORAGE.ordinal()] = numWorkers > 2 ? 1 : 0;
		
		int maxWoodAmount = 2000;
		double woodratio = (1.0*maxWoodAmount - faction.getInventory().getItemAmount(ItemType.WOOD))/maxWoodAmount;
		assignments[WorkerTask.CHOP.ordinal()] = Utils.lerp(1, numWorkers/2, woodratio);

		int maxStoneAmount = 1000;
		double stoneratio = (1.0*maxStoneAmount - faction.getInventory().getItemAmount(ItemType.STONE))/maxStoneAmount;
		assignments[WorkerTask.GATHERSTONE.ordinal()] = Utils.lerp(0, numWorkers/3, stoneratio);
		if(faction.getInventory().getItemAmount(ItemType.FOOD) < 50) {
			assignments[WorkerTask.GATHERSTONE.ordinal()] = 0;
		}
		int total = 0;
		for(int amount : assignments) {
			total += amount;
		}
		assignments[WorkerTask.IRRIGATE.ordinal()] = Math.max(0, numWorkers - total);
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
			if(wantsRoad()) {
				if(buildRoad(worker)) {
					return;
				}
			}
			if(faction.getInventory().getItemAmount(ItemType.FOOD) < 500) {
				if(irrigate(worker)) {
					return;
				}
			}
			if(forage(worker)) {
				return;
			}
			chopWood(worker);
		}
		else if(task == WorkerTask.FORAGE) {
			forage(worker);
		}
		else if(task == WorkerTask.CHOP) {
			chopWood(worker);
		}
		else if(task == WorkerTask.GATHERSTONE) {
			gatherStone(worker);
		}
	}
	
	private boolean wantsWorker() {
		return state.unitQuantities[Game.unitTypeMap.get("WORKER").id()] < 16
				&& faction.getInventory().getItemAmount(ItemType.FOOD) > 50 
				&& state.castle.getProducingUnit().isEmpty();
	}
	private boolean wantsWarrior() {
		return state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()] < 10
				&& faction.getInventory().getItemAmount(ItemType.FOOD) > faction.getUnits().size()*5
				&& state.barracks.getProducingUnit().isEmpty()
				&& state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()]*5 < faction.getUnits().size();
	}
	
	private boolean wantsBarracks() {
		int numBarracks = state.buildingQuantities[Game.buildingTypeMap.get("BARRACKS").id()];
		return faction.getInventory().getItemAmount(ItemType.FOOD) > 100 && numBarracks < 1;
	}
	private boolean wantsWall() {
		return faction.getInventory().getItemAmount(ItemType.FOOD) > 100 && state.barracks != null && faction.getInventory().getItemAmount(ItemType.WOOD) > 200;
	}
	private boolean wantsRoad() {
		return faction.getInventory().getItemAmount(ItemType.FOOD) > 500 && state.barracks != null && faction.getInventory().getItemAmount(ItemType.STONE) > 300;
	}
	
	private boolean wantsMoreIrrigation() {
		int numUnits = faction.getUnits().size();
		return faction.getInventory().getItemAmount(ItemType.FOOD) < 100 + numUnits*25;
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
			commands.buildThing(unit, tile, false, true);
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
			commands.buildThing(unit, tile, true, true);
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
		Tile existingIrrigation = getTargetTile(state.castle.getTile(), 2, MAX_BUILD_RADIUS, e -> {
			return e.hasBuilding() && e.getBuilding().getType() == IRRIGATION;
		});
		if(existingIrrigation != null) {
			return null != buildAndHarvest(unit, existingIrrigation, IRRIGATION);
		}
		
		// otherwise build a new irrigation nearby
		Tile tile = getTargetTile(state.castle.getTile(), 2, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild() && e.canPlant();
		});
		if(tile == null) {
			return false;
		}
		Building building = buildAndHarvest(unit, tile, IRRIGATION);
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
			commands.buildThing(unit, tile, false, true);
			clearQueue = false;
		}
		if(building != null) {
			commands.harvestThing(unit, tile.getBuilding(), clearQueue);
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
		Building building = buildAndHarvest(unit, tile, Game.buildingTypeMap.get("MINE"));
		return building != null;
	}
	private boolean chopWood(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			return e.getPlant() != null && e.getPlant().getType() == PlantType.TREE;
		});
		if(tile == null) {
			return false;
		}
		commands.harvestThing(unit, tile.getPlant(), true);
		return true;
	}
	private boolean forage(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			
			return (e.getPlant() != null && e.getPlant().getType() != PlantType.TREE)
					|| e.getInventory().getItemAmount(ItemType.FOOD) > 0;
		});
		if(tile == null) {
			return false;
		}
		if(tile.getPlant() != null && tile.getPlant().getType() != PlantType.TREE) {
			commands.harvestThing(unit, tile.getPlant(), true);
		}
		else {
			commands.moveTo(unit, tile, true);
			commands.deliver(unit, state.castle, false);
		}
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
				commands.attackThing(unit, u, true);
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
