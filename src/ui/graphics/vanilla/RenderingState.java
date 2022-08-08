package ui.graphics.vanilla;

import java.awt.Graphics2D;

import game.Faction;
import ui.MapMode;
import ui.view.GameView.GameViewState;
import world.*;

public class RenderingState {
	double highHeight = Double.MIN_VALUE;
	double lowHeight = Double.MAX_VALUE;
	double highPressure = Double.MIN_VALUE;
	double lowPressure = Double.MAX_VALUE;
	double highTemp = Double.MIN_VALUE;
	double lowTemp = Double.MAX_VALUE;
	double highHumidity = Double.MIN_VALUE;
	double lowHumidity = Double.MAX_VALUE;
	
	Graphics2D g;
	int tileSize;
	Faction faction;
	MapMode mapMode;
	GameViewState gameViewState;
	World world;
	
	/** TODO: need to deprecate draww and drawh and just use tileSize */
	int draww;
	/** TODO: need to deprecate draww and drawh and just use tileSize */
	int drawh;

	int lowerX;
	int lowerY;
	int upperX;
	int upperY;

}
