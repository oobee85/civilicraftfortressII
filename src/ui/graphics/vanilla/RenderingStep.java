package ui.graphics.vanilla;

public class RenderingStep implements RenderStepInterface {

	boolean perTile = true;
	RenderStepInterface renderStepInterface;
	public RenderingStep(boolean perTile, RenderStepInterface renderStepInterface) {
		this.perTile = perTile;
		this.renderStepInterface = renderStepInterface;
	}
	@Override
	public void render(RenderingState state) {
		renderStepInterface.render(state);
	}
}
