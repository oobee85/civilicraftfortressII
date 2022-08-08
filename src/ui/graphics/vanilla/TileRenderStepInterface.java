package ui.graphics.vanilla;

import world.Tile;

public interface TileRenderStepInterface {
	public void render(RenderingState state, Tile tile, Point4 drawat);
}
