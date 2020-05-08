package liquid;

import java.awt.*;

public enum LiquidType {
	WATER(0.2, Color.blue, 0.0005, 10),
	LAVA(0.1, Color.orange, 0.05, 100);
	
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
