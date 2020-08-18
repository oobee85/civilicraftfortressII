package ui;

import game.BuildingType;
import world.*;

public interface GUIController {
	public void selectedBuilding(BuildingType bt, boolean selected);
	public void selectedWorker(boolean selected);
	public void toggleTileView();
	public void updateGUI();
	public void selectedSpawnUnit(boolean selected);
}
