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

	protected GameViewState state;
	protected Game game;

	public Drawer(Game game, GameViewState state) {
		this.state = state;
		this.game = game;
	}
	
	public abstract BufferedImage getImageToDrawMinimap();
	public abstract Position[] getVisibleTileBounds();
	public abstract Component getDrawingCanvas();

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
