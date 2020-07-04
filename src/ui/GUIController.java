package ui;

import world.*;

public interface GUIController {
	public void selectedBuilding(boolean selected);
	public void selectedWorker(boolean selected);
	public void toggleTileView();
	public void updateGUI();
	public void openRightClickMenu(int mx, int my, Tile tile);
	public void selectedSpawnUnit(boolean selected);
}
