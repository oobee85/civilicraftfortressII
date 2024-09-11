package game.ai;

import java.util.*;

import game.*;
import ui.CommandInterface;
import utils.*;
import world.*;

public class NeutralAI extends AIInterface {

	public NeutralAI(CommandInterface commands, Faction faction, World world) {
		super(commands, faction, world);
	}

	@Override
	public void aiTickLogic() {
		if (unitQuantities[Game.unitTypeMap.get("SCORPION").id()] < 20) {
			for (Building building : faction.getBuildings()) {
				if (building.getType() != Game.buildingTypeMap.get("SCORPION_DEN")) {
					continue;
				}
				if (!building.getProducingUnit().isEmpty()) {
					continue;
				}
				commands.produceUnit(building, Game.unitTypeMap.get("SCORPION"));
				List<Tile> nearbyTiles = Utils.getTilesInRadius(building.getTile(), world, 5);
				Tile target = nearbyTiles.get((int) (Math.random() * nearbyTiles.size()));
				commands.setBuildingRallyPoint(building, target);
			}
		}
		for (Unit unit : faction.getUnits()) {
			if (unit.getType() != Game.unitTypeMap.get("SCORPION")) {
				continue;
			}
			if (!unit.isIdle()) {
				continue;
			}
			if (buildingQuantities[0][Game.buildingTypeMap.get("SCORPION_DEN").id()] < 10) {
				List<Tile> nearby = Utils.getRingOfTiles(unit.getTile(), world, 10);
				Collections.shuffle(nearby);
				Optional<Tile> validNearby = nearby.stream()
						.filter(tile -> {
					return (tile.getTerrain() == Terrain.SAND) && (tile.getBuilding() == null);
				}).findFirst();
				if (validNearby.isPresent()) {
					commands.planBuilding(unit, validNearby.get(), true, Game.buildingTypeMap.get("SCORPION_DEN"));
					continue;
				}
			}
			if (!unit.isGuarding()) {
				commands.setGuarding(unit, true);
			}
		}
	}
}
