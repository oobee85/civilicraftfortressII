package game.components;

import game.*;

public class DamageResistance extends Component {
	
	public static final int BASE_RESISTANCE = 100;
	public static final int[] DEFAULT_RESISTANCE = getDefaultResistance();

	private int[] resistance;
	public DamageResistance(int[] resistance) {
		this.resistance = resistance;
	}
	
	public int computeDamage(int[] damage) {
		int totalDamage = 0;
		for(int i = 0; i < damage.length; i++) {
			totalDamage += damage[i] * resistance[i] / BASE_RESISTANCE;
		}
		return totalDamage;
	}
	public double computeDamage(double[] danger) {
		double totalDamage = 0;
		for(int i = 0; i < danger.length; i++) {
			totalDamage += danger[i] * resistance[i] / BASE_RESISTANCE;
		}
		return totalDamage;
	}
	
	public static final int computeDamageDefault(int[] damage) {
		int totalDamage = 0;
		for(int i = 0; i < damage.length; i++) {
			totalDamage += damage[i] * DEFAULT_RESISTANCE[i] / BASE_RESISTANCE;
		}
		return totalDamage;
	}
	public static final double computeDamageDefault(double[] danger) {
		double totalDamage = 0;
		for(int i = 0; i < danger.length; i++) {
			totalDamage += danger[i];
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
