package liquid;

import java.awt.*;

public enum LiquidType {
	WATER(0.1, Color.blue, 0.015, 100),
	LAVA(0.05, Color.orange, 0.01, 1000);
	
	double viscosity;
	Color color;
	double surfaceTension;
	double damage;

	LiquidType(double viscosity, Color color, double surfaceTension, double damage) {
		this.viscosity = viscosity;
		this.color = color;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
	}
	public double getDamage() {
		return damage;
	}
	
	public Color getColor() {
		return color;
	}
}
