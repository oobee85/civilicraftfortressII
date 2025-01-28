package ui;

import game.*;
import ui.infopanels.*;
import world.*;

public interface GUIController {
	public void selectedBuilding(Building building, boolean selected);
	public void selectedUnit(Unit unit, boolean selected);
	public void selectedPlant(Plant plant, boolean selected);
	public void updateGUI();
	public void changedFaction(Faction faction);

	public void pushInfoPanel(InfoPanel infoPanel);
	public void popInfoPanel();
	public void switchInfoPanel(InfoPanel infoPanel);
	public void pressedSelectedUnitPortrait(Unit unit);
	public void tryToCraftItem(ItemType type, int amount);
	public void research(ResearchType researchType);

	public void setFastForwarding(boolean enabled);
	public void setPauseGame(boolean enabled);
}
