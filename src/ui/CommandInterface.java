package ui;

import game.*;
import game.actions.*;
import world.*;

public interface CommandInterface {
	public void setBuildingRallyPoint(Building building, Tile rallyPoint);
	public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType);
	public void stop(Unit unit);
	public void research(Faction faction, ResearchType researchType);
	public void craftItem(Faction faction, ItemType itemType, int amount);
	public void produceUnit(Building building, UnitType unitType);
	
	public void planAction(Unit unit, PlannedAction plan, boolean clearQueue);
}
