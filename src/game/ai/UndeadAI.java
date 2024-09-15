package game.ai;

import java.util.*;

import game.*;
import ui.CommandInterface;
import utils.*;
import world.*;

public class UndeadAI extends AIInterface {
	
	private static final int MAX_INFLUENCE_RADIUS = 24;
	private static final int BASE_UNIT_PRODUCTION_DELAY = 300;
	private static final int PER_UNIT_PRODUCTION_DELAY = 30;
	private int nextTickToProduce = World.ticks + BASE_UNIT_PRODUCTION_DELAY;

	public UndeadAI(CommandInterface commands, Faction faction, World world) {
		super(commands, faction, world);
	}

	private void buildWalls(Unit worker) {
		outerloop: for (int layer = 1; layer <= 16; layer++) {
			int radius = layer;
			boolean wallOrRoad = (radius % 2) == 0;
			Tile home = worker.getTile();
			if (castle != null) {
				home = castle.getTile();
			}
			List<Tile> tileRing = Utils.getRingOfTiles(
					home,
					world,
					radius);
			int buildEveryX = Math.max(layer / 4 + 2, 2);
			int skipEveryOther = (radius % 7) % buildEveryX;
			for (Tile tile : tileRing) {
				skipEveryOther = (skipEveryOther + 1) % buildEveryX;
				if (wallOrRoad && skipEveryOther == 0) {
					continue;
				}
				// Don't try to build walls on tiles that already have a building
				if (wallOrRoad && tile.getBuilding() != null) {
					continue;
				}
				// Don't try to build roads on tiles that have a road already,
				// or tiles that have a wall
				if (!wallOrRoad && 
						(tile.getRoad() != null || 
						(tile.getBuilding() != null && tile.getBuilding().getType().blocksMovement()))) {
					continue;
				}
				// Only build on our or neutral territory
				if (tile.getFaction() != faction && !tile.getFaction().isNeutral()) {
					continue;
				}
				// Dont try to build in deep water
				if (tile.computeTileDamage()[DamageType.WATER.ordinal()] > 0) {
					continue;
				}
				BuildingType type = Game.buildingTypeMap.get("WALL_STONE");
				if (wallOrRoad) {
					type = Game.buildingTypeMap.get("WALL_STONE");
				}
				else {
					type = Game.buildingTypeMap.get("STONE_ROAD");
				}
				commands.planBuilding(worker, tile, true, type);
				break outerloop;
			}
		}
	}

	@Override
	public void aiTickLogic() {
		for (Unit unit : faction.getUnits()) {
			if (unit.isBuilder() && unit.isIdle()) {
				buildWalls(unit);
			}
			else if(!unit.isGuarding()) {
				commands.setGuarding(unit, true);
			}
		}

		if (castle != null && castle.getProducingUnit().isEmpty()
			&& World.ticks >= nextTickToProduce) {
			
			int numBuildings = faction.getBuildings().size();
			int highestTier = 2;
			if (numBuildings >= 250) {
				highestTier = 4;
			}
			else if (numBuildings >= 50) {
				highestTier = 3;
			}
			highestTier = Math.min(highestTier, castle.getType().unitsCanProduce().length);
			int random = (int) (Math.random() * (highestTier));
			UnitType type = Game.unitTypeMap.get(castle.getType().unitsCanProduce()[random]);
			nextTickToProduce = 
					World.ticks
					+ BASE_UNIT_PRODUCTION_DELAY
					+ type.getCombatStats().getTicksToBuild()
					+ faction.getUnits().size() * PER_UNIT_PRODUCTION_DELAY;
			commands.produceUnit(castle, type);
			List<Tile> nearbyTiles = Utils.getTilesInRadius(castle.getTile(), world, MAX_INFLUENCE_RADIUS);
			Collections.shuffle(nearbyTiles);
			for (Tile tile : nearbyTiles) {
				if (tile.getFaction() != faction) {
					continue;
				}
				commands.setBuildingRallyPoint(castle, tile);
				break;
			}
		}
	}

}
