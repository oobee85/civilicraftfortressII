package ui.graphics.vanilla;

import java.awt.Point;

import world.Tile;

public interface TileRenderStepInterface {
	public void render(RenderingState state, Tile tile, Point drawat);
}
