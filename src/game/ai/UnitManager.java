package game.ai;

import java.util.*;
import java.util.Map.Entry;

import game.*;
import game.ai.BuildOrderPhase.WorkerTask;

public class UnitManager {

	public static final BuildingType FARM = Game.buildingTypeMap.get("FARM");
	public static final BuildingType STABLES = Game.buildingTypeMap.get("STABLES");

	private Map<WorkerTask, Double> targetWorkerAssignmentRatios;
	
	private HashMap<Integer, Mission> specialMissions = new HashMap<>();

	private HashMap<Integer, WorkerTask> unitidToTask = new HashMap<>(); // key is a worker Unit ID
	private HashMap<WorkerTask, Integer> taskToCount = new HashMap<>();

	private Map<Building, Unit> farmToWorker = new HashMap<>();
	private Map<Unit, Building> workerToFarm = new HashMap<>();
	
	private Map<Building, Unit> stablesToCaravan = new HashMap<>();
	private Map<Unit, Building> caravanToStables = new HashMap<>();
	
	
	public UnitManager(Map<WorkerTask, Double> initialTargetWorkerAssignmentRatios) {
		this.targetWorkerAssignmentRatios = initialTargetWorkerAssignmentRatios;
	}

	private LinkedList<Mission> missions = new LinkedList<>();
	private HashMap<String, Mission> unstartedMissions = new HashMap<>();
	public void addUnstartedMission(Mission mission, String key) {
		if (unstartedMissions.containsKey(key)) {
			if (!unstartedMissions.get(key).isStarted()) {
				return;
			}
		}
		missions.addLast(mission);
		unstartedMissions.put(key, mission);
		
	}
	
	public Mission getMissionFor(int id) {
		if (!specialMissions.containsKey(id)) {
			if (missions.isEmpty()) {
				return null;
			}
			specialMissions.put(id, missions.removeFirst());
//			System.out.println("Assigned worker " + id + " to " + specialMissions.get(id));
		}
		return specialMissions.get(id);
	}
	
	public void checkIfMissionComplete(int id) {
		if (!specialMissions.containsKey(id)) {
			return;
		}
		Mission mission = specialMissions.get(id);
		if (mission.isComplete()) {
			specialMissions.remove(id);
		}
	}

	private int getCount(WorkerTask task) {
		if (!taskToCount.containsKey(task)) {
			return 0;
		}
		return taskToCount.get(task);
	}

	private void increment(WorkerTask task, int amount) {
		taskToCount.put(task, amount + getCount(task));
	}
	
	public void countAssignments() {
		taskToCount.clear();
		for (Entry<Integer, WorkerTask> preassigned : unitidToTask.entrySet()) {
			if (!taskToCount.containsKey(preassigned.getValue())) {
				taskToCount.put(preassigned.getValue(), 0);
			}
			taskToCount.put(preassigned.getValue(), 1 + taskToCount.get(preassigned.getValue()));
		}
	}
	
	public void unassign(int unitid) {
		WorkerTask task = unitidToTask.remove(unitid);
		increment(task, -1);
		if (specialMissions.containsKey(unitid)) {
			missions.add(specialMissions.remove(unitid));
		}
	}
	
	WorkerTask getTaskFor(int id) {
		if (unitidToTask.containsKey(id)) {
			return unitidToTask.get(id);
		}

		WorkerTask task = chooseAssignment();
		unitidToTask.put(id, task);
		increment(task, 1);
		return task;
	}
	
	WorkerTask chooseAssignment() {
		WorkerTask bestTask = WorkerTask.CHOP;
		double highestDifference = 0;
		for (Entry<WorkerTask, Double> entry : targetWorkerAssignmentRatios.entrySet()) {
			double currentRatio = 0;
			if (unitidToTask.size() > 0) {
				currentRatio = (double)getCount(entry.getKey()) / unitidToTask.size();
			}
//			System.out.println(String.format("Desired ratio for %s: %.1f%%, Current: %.1f%%",
//					entry.getKey(), 100*entry.getValue(), 100*currentRatio));
			double difference = entry.getValue() - currentRatio;
			if (difference >= highestDifference) {
				highestDifference = difference;
				bestTask = entry.getKey();
			}
		}
//		System.out.println("Assigning worker to " + bestTask);
		return bestTask;
	}
	public void newPhase(Map<WorkerTask, Double> targetWorkerAssignmentRatios) {
		this.targetWorkerAssignmentRatios = targetWorkerAssignmentRatios;
	}
	void removeDeadWorkers(Set<Integer> aliveWorkers) {
		Set<Integer> deadWorkersToRemove = new HashSet<>();
		for (Integer id : unitidToTask.keySet()) {
			if (!aliveWorkers.contains(id)) {
				deadWorkersToRemove.add(id);
			}
		}
		for (Integer id : specialMissions.keySet()) {
			if (!aliveWorkers.contains(id)) {
				deadWorkersToRemove.add(id);
			}
		}
		for (Integer id : deadWorkersToRemove) {
			unassign(id);
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Worker assignments: ");
		for (Entry<WorkerTask, Integer> entry : taskToCount.entrySet()) {
			if (entry.getValue() == 0) {
				continue;
			}
			sb.append(String.format("%s: %d, ", entry.getKey(), entry.getValue()));
		}
		return sb.toString();
	}

	

	public void assignCaravanToStables(Unit caravan, Building stables) {
		stablesToCaravan.put(stables, caravan);
		caravanToStables.put(caravan, stables);
	}

	public Building getStablesForCaravan(Unit caravan, Set<Building> buildings) {
		if (caravanToStables.containsKey(caravan)) {
			Building stables = caravanToStables.get(caravan);
			if (stables.isDead()) {
				caravanToStables.remove(caravan);
				stablesToCaravan.remove(stables);
			}
			else {
				return stables;
			}
		}
		
		for (Building building : buildings) {
			if (building.getType() != STABLES) {
				continue;
			}
			Building stables = building;
			if (stablesToCaravan.containsKey(stables)) {
				Unit existingCaravan = stablesToCaravan.get(stables);
				if (!existingCaravan.isDead()) {
					continue;
				}
				stablesToCaravan.remove(stables);
				caravanToStables.remove(existingCaravan);
			}

			assignCaravanToStables(caravan, stables);
			return stables;
		}
		return null;
	}
	

	
	public void assignWorkerToFarm(Unit worker, Building farm) {
		farmToWorker.put(farm, worker);
		workerToFarm.put(worker, farm);
	}

	public Building getFarmForWorker(Unit worker, Set<Building> buildings) {
		if (workerToFarm.containsKey(worker)) {
			Building farm = workerToFarm.get(worker);
			if (farm.isDead()) {
				workerToFarm.remove(worker);
				farmToWorker.remove(farm);
			}
			else {
				return farm;
			}
		}
		
		for (Building building : buildings) {
			if (building.getType() != FARM) {
				continue;
			}
			Building farm = building;
			if (farmToWorker.containsKey(farm)) {
				Unit existingWorker = farmToWorker.get(farm);
				if (!existingWorker.isDead()) {
					continue;
				}
				farmToWorker.remove(farm);
				workerToFarm.remove(existingWorker);
			}

			assignWorkerToFarm(worker, farm);
			return farm;
		}
		return null;
	}
	

}
