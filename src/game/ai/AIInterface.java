package game.ai;

import java.util.Arrays;

import game.*;
import ui.CommandInterface;
import world.World;

public abstract class AIInterface {

	protected final CommandInterface commands;
	protected final Faction faction;
	protected final World world;

	protected Building castle;
	protected int[] buildingQuantities;
	protected int[] unitQuantities;
	
	
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
			buildingQuantities = new int[Game.buildingTypeMap.size()];
		}
		if (unitQuantities == null) {
			unitQuantities = new int[Game.unitTypeMap.size()];
		}
		Arrays.fill(buildingQuantities, 0);
		for(Building building : faction.getBuildings()) {
			buildingQuantities[building.getType().id()]++;
			if(castle == null && building.getType().isCastle() && !building.isDead()) {
				castle = building;
			}
		}

		Arrays.fill(unitQuantities, 0);
		for(Unit unit : faction.getUnits()) {
			unitQuantities[unit.getType().id()]++;
		}
	}

	public final void tick() {
		updateUnitAndBuildingCounts();
		aiTickLogic();
	}

	public abstract void aiTickLogic();
}
