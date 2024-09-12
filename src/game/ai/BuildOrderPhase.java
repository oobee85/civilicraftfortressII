package game.ai;

import java.util.*;
import java.util.Map.Entry;

import game.*;

public class BuildOrderPhase {
	public static List<BuildOrderPhase> phases = new ArrayList<>();

	public enum WorkerTask {
		CLOSEFORAGE, FORAGE, FARM, CHOP, GATHERSTONE, GATHERSILVER, GATHERCOPPER, GATHERIRON, GATHERCOAL, GATHERMITHRIL
	}
	
	public int order;
	public List<ResearchType> requiredResearches;
	public List<ResearchType> optionalResearches;
	public Map<UnitType, QuantityReq> units;
	public Map<BuildingType, QuantityReq> buildings;
	public Map<WorkerTask, Double> workerAssignments;
	
	public BuildOrderPhase(int order) {
		this.order = order;
		requiredResearches = new LinkedList<>();
		optionalResearches = new LinkedList<>();
		units = new HashMap<>();
		buildings = new HashMap<>();
		workerAssignments = new HashMap<>();
	}
	
	public BuildOrderPhase(BuildOrderPhase copyFrom) {
		this.order = copyFrom.order;
		requiredResearches = new LinkedList<>(copyFrom.requiredResearches);
		optionalResearches = new LinkedList<>(copyFrom.optionalResearches);
		units = copyFrom.units;
		buildings = copyFrom.buildings;
		workerAssignments = copyFrom.workerAssignments;
		finalize();
	}
	
	public void finalize() {
		units = Collections.unmodifiableMap(units);
		buildings = Collections.unmodifiableMap(buildings);
		workerAssignments = Collections.unmodifiableMap(workerAssignments);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("BuildOrderPhase: " + order + " {\n");
		sb.append("researches: ");
		for (ResearchType type : requiredResearches) {
			sb.append(type + ", ");
		}
		sb.append("\n");
		sb.append("units: ");
		for (Entry<UnitType, QuantityReq> entry : units.entrySet()) {
			sb.append(String.format("{%s: %s}, ", entry.getKey(), entry.getValue()));
		}
		sb.append("\n");
		sb.append("buildings: ");
		for (Entry<BuildingType, QuantityReq> entry : buildings.entrySet()) {
			sb.append(String.format("{%s: %s}, ", entry.getKey(), entry.getValue()));
		}
		sb.append("\n");
		sb.append("workerAssignments: ");
		for (Entry<WorkerTask, Double> entry : workerAssignments.entrySet()) {
			sb.append(String.format("{%s: %.1f%%}, ", entry.getKey(), 100 * entry.getValue()));
		}
		sb.append("\n}");
		
		return sb.toString();
	}
}
