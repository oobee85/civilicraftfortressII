package game.components;

import game.*;

public class DamageResistance extends GameComponent {
	
	public static final int BASE_RESISTANCE = 100;
	public static final int[] DEFAULT_RESISTANCE = getDefaultResistance();

	private int[] resistance;
	public DamageResistance(int[] resistance) {
		this.resistance = resistance;
	}
	
	public GameComponent instance() {
		return new DamageResistance(resistance.clone());
	}
	
	public boolean isVulnerableTo(DamageType type) {
		return resistance[type.ordinal()] > 0;
	}
	
	public int applyResistance(int damage, DamageType type) {
		return damage * resistance[type.ordinal()] / BASE_RESISTANCE;
	}
	public double applyResistance(double[] danger) {
		double totalDamage = 0;
		for(int i = 0; i < danger.length; i++) {
			totalDamage += danger[i] * resistance[i] / BASE_RESISTANCE;
		}
		return totalDamage;
	}
	
	public static final int applyDefaultResistance(int damage, DamageType type) {
		return damage * DEFAULT_RESISTANCE[type.ordinal()] / BASE_RESISTANCE;
	}
	public static final double applyDefaultResistance(double[] danger) {
		double totalDamage = 0;
		for(int i = 0; i < danger.length; i++) {
			totalDamage += danger[i] * DEFAULT_RESISTANCE[i] / BASE_RESISTANCE;
		}
		return totalDamage;
	}
	
	public static final int[] getDefaultResistance() {
		int[] resistance = new int[DamageType.values().length];
		for(int i = 0; i < resistance.length; i++) {
			resistance[i] = BASE_RESISTANCE;
		}
		resistance[DamageType.DRY.ordinal()] = 0;
		return resistance;
	}
}
