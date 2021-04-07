package ui.graphics;

import java.awt.*;
import java.awt.image.*;

import game.*;
import ui.view.GameView.*;
import utils.*;
import world.*;

public abstract class Drawer {

	protected volatile BufferedImage terrainImage;
	protected volatile BufferedImage minimapImage;
	protected volatile BufferedImage heightMapImage;
	protected volatile BufferedImage humidityMapImage;
	protected volatile BufferedImage pressureMapImage;
	protected volatile BufferedImage temperatureMapImage;
	protected volatile BufferedImage massMapImage;

	protected GameViewState state;
	protected Game game;

	public Drawer(Game game, GameViewState state) {
		this.state = state;
		this.game = game;
	}
	
	public abstract BufferedImage getImageToDrawMinimap();
	public abstract Position[] getVisibleTileBounds();
	public abstract Component getDrawingCanvas();
	
	/** Converts the on screen pixel to a tile position */
	public abstract Position getWorldCoordOfPixel(Point pixelOnScreen, Position viewOffset, int tileSize);
	public abstract void zoomView(int scroll, int mx, int my);
	public abstract void zoomViewTo(int newTileSize, int mx, int my);
	/**
	 * @param dx pixels moved left-right
	 * @param dy pixels moved up-down
	 */
	public abstract void shiftView(int dx, int dy);

	public void updateTerrainImages() {
		if (game.world != null) {
			BufferedImage[] images = game.world.createTerrainImage(state.faction);
			this.terrainImage = images[0];
			this.minimapImage = images[1];
			this.heightMapImage = images[2];
			this.humidityMapImage = images[3];
			this.pressureMapImage = images[4];
			this.temperatureMapImage = images[5];
		}
	}
}
