package game;

public enum DamageType {
	PHYSICAL,
	FIRE,
	WATER,
	DRY,
	HUNGER,
	SPIRIT,
	;
	
	public static final int[] getZeroDamageArray() {
		return new int[DamageType.values().length];
	}
	
	public static final int[] makeDamageArray(int damage, DamageType type) {
		int[] arr = getZeroDamageArray();
		arr[type.ordinal()] = damage;
		return arr;
	}
}
