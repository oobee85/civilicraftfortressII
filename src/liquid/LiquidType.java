package liquid;

import java.awt.*;

public enum LiquidType {
	WATER(0.3, Color.blue, 0.01),
	LAVA(0.05, Color.orange, 0.01);
	
	double viscosity;
	Color color;
	double surfaceTension;

	LiquidType(double viscosity, Color color, double surfaceTension) {
		this.viscosity = viscosity;
		this.color = color;
		this.surfaceTension = surfaceTension;
	}
	
	public Color getColor() {
		return color;
	}
}
