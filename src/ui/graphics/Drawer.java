package ui.graphics;

import java.awt.image.*;

import utils.*;

public interface Drawer {

	public void updateTerrainImages();
	public BufferedImage getImageToDrawMinimap();
	public Position[] getVisibleTileBounds();
}
