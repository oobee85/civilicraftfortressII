package ui;

import world.*;

public interface GUIController {
	public void toggleCityView();
	public void toggleTileView();
	public void updateGUI();
	public void setWorkerView(boolean visible);
	public void openRightClickMenu(int mx, int my, Tile tile);
}
