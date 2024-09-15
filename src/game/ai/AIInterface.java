package game.ai;

import java.util.*;

import game.*;
import ui.CommandInterface;
import world.World;

public abstract class AIInterface {

	protected final CommandInterface commands;
	protected final Faction faction;
	protected final World world;

	protected Building castle;
	protected List<Building> castles = new ArrayList<>();
	protected int[][] buildingQuantities;
	protected int[] unitQuantities;
	
	protected List<Unit> units = new ArrayList<>();
	protected List<Building> buildings = new ArrayList<>();
	
	
	public AIInterface(CommandInterface commands, Faction faction, World world) {
		this.commands = commands;
		this.faction = faction;
		this.world = world;
	}

	public final void updateUnitAndBuildingCounts() {
		if(castle != null && castle.isDead()) {
			castle = null;
		}
		if (buildingQuantities == null) {
			buildingQuantities = new int[2][Game.buildingTypeMap.size()];
		}
		if (unitQuantities == null) {
			unitQuantities = new int[Game.unitTypeMap.size()];
		}
		castles.clear();
		units.clear();
		buildings.clear();
		Arrays.fill(buildingQuantities[0], 0);
		Arrays.fill(buildingQuantities[1], 0);
		for(Building building : faction.getBuildings()) {
			buildings.add(building);
			buildingQuantities[0][building.getType().id()]++;
			if (building.isBuilt()) {
				buildingQuantities[1][building.getType().id()]++;
			}
			if (building.getType().isCastle() && !building.isDead()) {
				if (building.isBuilt()) {
					castles.add(building);
				}
				if (castle == null || !castle.isBuilt()) {
					castle = building;
				}
			}
		}

		Arrays.fill(unitQuantities, 0);
		for(Unit unit : faction.getUnits()) {
			units.add(unit);
			unitQuantities[unit.getType().id()]++;
		}
	}

	public final void tick() {
		updateUnitAndBuildingCounts();
		aiTickLogic();
	}

	public abstract void aiTickLogic();
}
