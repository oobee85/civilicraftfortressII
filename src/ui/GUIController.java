package ui;

import world.*;

public interface GUIController {
	public void toggleCityView();
	public void selectedWorker(boolean selected);
	public void toggleTileView();
	public void updateGUI();
	public void openRightClickMenu(int mx, int my, Tile tile);
}
