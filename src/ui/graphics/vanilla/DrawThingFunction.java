package ui.graphics.vanilla;

import utils.Thing;

public interface DrawThingFunction {
	public void draw(RenderingState state, int x, int y, int size, Thing thing);
}
