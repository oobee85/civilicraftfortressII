package ui.graphics.vanilla;

import world.Tile;

/**
 * RenderingStep is a wrapper for potentially 2 different rendering steps.
 * The first is a tile-independent step that can be used to draw lists of things around the world
 * or to set up some state for the second, tile-specific render step.
 */
public class RenderingStep implements RenderStepInterface, TileRenderStepInterface {

	RenderStepInterface renderStep;
	TileRenderStepInterface tileRenderStep;
	public RenderingStep(RenderStepInterface renderStep, TileRenderStepInterface tileRenderStep) {
		this.renderStep = renderStep;
		this.tileRenderStep = tileRenderStep;
	}
	@Override
	public void render(RenderingState state) {
		if (renderStep != null) {
			renderStep.render(state);
		}
	}
	
	@Override
	public void render(RenderingState state, Tile tile, Point4 drawat) {
		if (tileRenderStep != null) {
			tileRenderStep.render(state, tile, drawat);
		}
	}
}
