package ui;

import game.*;
import ui.infopanels.*;

public interface GUIController {
	public void selectedBuilding(Building building, boolean selected);
	public void selectedUnit(Unit unit, boolean selected);
	public void toggleTileView();
	public void updateGUI();
	public void selectedSpawnUnit(boolean selected);
	public void changedFaction(Faction faction);

	public void pushInfoPanel(InfoPanel infoPanel);
	public void popInfoPanel();
	public void switchInfoPanel(InfoPanel infoPanel);
	public void pressedSelectedUnitPortrait(Unit unit);
	public void tryToCraftItem(ItemType type, int amount);
}
