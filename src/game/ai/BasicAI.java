package game.ai;

import java.util.*;
import java.util.function.*;

import game.*;
import ui.*;
import utils.*;
import world.*;

public class BasicAI implements AIInterface {
	
	private static final int MAX_BUILD_RADIUS = 10;
	private static final int MAX_SEARCH_RADIUS = 30;

	public class State {
		Building castle;
		Building barracks;
		int[] buildingQuantities;
		int[] unitQuantities;
		LinkedList<Unit> plantGatherers = new LinkedList<>();
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
		
		if(state.castle != null) {
			
			for(Unit unit : faction.getUnits()) {
				if(unit.isIdle()) {
					if(unit.getType().isBuilder()) {
						if(wantsMoreIrrigation()) { 
							if(irrigate(unit)) {
								continue;
							}
						}
						if(wantsBarracks()) {
							if(buildBarracks(unit)) {
								continue;
							}
						}
						if(gatherPlants(unit)) {
							continue;
						}
					}
					else {
						if(defend(unit)) {
							continue;
						}
						
					}
				}
			}
			while(wantsMoreIrrigation() && !state.plantGatherers.isEmpty()) {
				Unit unit = state.plantGatherers.getFirst();
				if(irrigate(unit)) {
					state.plantGatherers.remove();
				}
				else {
					break;
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
	
	private boolean wantsWorker() {
		return faction.getInventory().getItemAmount(ItemType.FOOD) > faction.getUnits().size()*20 
				&& state.castle.getProducingUnit().isEmpty();
	}
	private boolean wantsWarrior() {
		return faction.getInventory().getItemAmount(ItemType.FOOD) > faction.getUnits().size()*5
				&& state.barracks.getProducingUnit().isEmpty()
				&& state.unitQuantities[Game.unitTypeMap.get("WARRIOR").id()]*5 < faction.getUnits().size();
	}
	
	private boolean wantsBarracks() {
		int numBarracks = state.buildingQuantities[Game.buildingTypeMap.get("BARRACKS").id()];
		return numBarracks < 1;
	}
	
	private boolean wantsMoreIrrigation() {
		int numIrrigation = state.buildingQuantities[Game.buildingTypeMap.get("IRRIGATION").id()];
		int numUnits = faction.getUnits().size();
		return numIrrigation*2 < numUnits;
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
	
	private Building planBuilding(Unit unit, Tile tile, boolean clearQueue, BuildingType type) {
		if(faction.canAfford(type.getCost())) {
			return commands.planBuilding(unit, tile, clearQueue, type);
		}
		return null;
	}
	
	private boolean irrigate(Unit unit) {
		Tile tile = getTargetTile(state.castle.getTile(), 2, MAX_BUILD_RADIUS, e -> {
			return !e.hasBuilding() && e.canBuild() && e.canPlant();
		});
		if(tile == null) {
			return false;
		}
		Building building = planBuilding(unit, tile, true, Game.buildingTypeMap.get("IRRIGATION"));
		if(building != null) {
			state.buildingQuantities[Game.buildingTypeMap.get("IRRIGATION").id()]++;
			commands.harvestThing(unit, building, false);
		}
		return building != null;
	}
	
	private boolean gatherPlants(Unit unit) {
		Tile tile = getTargetTile(unit.getTile(), MAX_SEARCH_RADIUS, e -> {
			return e.getPlant() != null;
		});
		if(tile == null) {
			return false;
		}
		commands.harvestThing(unit, tile.getPlant(), true);
		state.plantGatherers.add(unit);
		return true;
	}
	
	private boolean defend(Unit unit) {
		if(!unit.isGuarding()) {
			commands.setGuarding(unit, true);
		}
		if(unit.getTile().getLocation().distanceTo(state.castle.getTile().getLocation()) < MAX_BUILD_RADIUS/2) {
			Tile tile = getTargetTile(unit.getTile(), MAX_BUILD_RADIUS/2, MAX_BUILD_RADIUS*2/3, e -> {
				return e.getUnits().isEmpty();
			});
			if(tile == null) {
				return false;
			}
			commands.moveTo(unit, tile, true);
			return true;
		}
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
		for(Unit unit : faction.getUnits()) {
			unitQuantities[unit.getType().id()]++;
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
