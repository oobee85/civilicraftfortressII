package ui;

import game.*;

public interface GUIController {
	public void selectedBuilding(Building building, boolean selected);
	public void selectedWorker(boolean selected);
	public void toggleTileView();
	public void updateGUI();
	public void selectedSpawnUnit(boolean selected);
}
