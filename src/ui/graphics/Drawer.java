package ui.graphics;

import java.awt.*;
import java.awt.image.*;

import game.*;
import ui.*;
import ui.view.GameView.*;
import utils.*;
import world.*;

public abstract class Drawer {

	protected volatile BufferedImage[] mapImages = new BufferedImage[MapMode.values().length];

	protected GameViewState state;
	protected Game game;

	public Drawer(Game game, GameViewState state) {
		this.state = state;
		this.game = game;
	}

	public void updateTerrainImages() {
		if (game.world != null) {
			mapImages = game.world.createTerrainImage(state.faction);
		}
	}
	public BufferedImage getImageToDrawMinimap() {
		if(state.mapMode == MapMode.TERRAIN) {
			return mapImages[MapMode.MINIMAP.ordinal()];
		}
		else {
			return mapImages[state.mapMode.ordinal()];
		}
	}
	public abstract Position[] getVisibleTileBounds();
	public abstract Component getDrawingCanvas();
	
	/** Converts the on screen pixel to a tile position */
	public abstract Position getWorldCoordOfPixel(Point pixelOnScreen, Position viewOffset, int tileSize);
	public abstract Point getPixelOfWorldCoord(Position worldCoord, int tileSize);
	public abstract void zoomView(int scroll, int mx, int my);
	public abstract void zoomViewTo(int newTileSize, int mx, int my);
	/**
	 * @param dx pixels moved left-right
	 * @param dy pixels moved up-down
	 */
	public abstract void shiftView(int dx, int dy);
	public abstract void rotateView(int dx, int dy);
}
