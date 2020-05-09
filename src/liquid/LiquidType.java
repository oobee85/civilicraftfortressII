package liquid;

import java.awt.*;

public enum LiquidType {
	WATER(0.2, Color.blue, 0.0005, 5, 0.06),
	LAVA(0.1, Color.orange, 0.005, 100, 0.001);
	
	double viscosity;
	Color color;
	double surfaceTension;
	double damage;
	double minimumDamageAmount;

	LiquidType(double viscosity, Color color, double surfaceTension, double damage, double minimumDamageAmount) {
		this.viscosity = viscosity;
		this.color = color;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
	}
	public double getDamage() {
		return damage;
	}
	public double getMinimumDamageAmount() {
		return minimumDamageAmount;
	}
	
	public Color getColor() {
		return color;
	}
}
