package ui;

import game.*;
import utils.*;
import world.*;

public interface CommandInterface {
	public void setBuildingRallyPoint(Building building, Tile rallyPoint);
	public void moveTo(Unit unit, Tile target, boolean clearQueue);
	public void attackThing(Unit unit, Thing target, boolean clearQueue);
	public void buildThing(Unit unit, Thing target, boolean clearQueue);
	public Building planBuilding(Unit unit, Tile target, boolean clearQueue, BuildingType buildingType);
	public void stop(Unit unit);
	public void research(Faction faction, ResearchType researchType);
}
