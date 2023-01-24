package game;

public class EnemySpawns {
	private static UnitType[] easyTypes = {
			Game.unitTypeMap.get("WATER_SPIRIT"),
			Game.unitTypeMap.get("FLAMELET"),
			Game.unitTypeMap.get("FIREFLY")
	};

	private static UnitType[] mediumTypes = {
			Game.unitTypeMap.get("SKELETON"),
			Game.unitTypeMap.get("ENT"),
			Game.unitTypeMap.get("ROC"),
			Game.unitTypeMap.get("VAMPIRE"),
	};

	private static UnitType[] hardTypes = {
			Game.unitTypeMap.get("WEREWOLF"),
			Game.unitTypeMap.get("OGRE"),
			Game.unitTypeMap.get("TERMITE"),
			Game.unitTypeMap.get("BOMB"),
			Game.unitTypeMap.get("LAVAGOLEM"),
			Game.unitTypeMap.get("ICE_GIANT"),
			Game.unitTypeMap.get("STONE_GOLEM"),
	};
	
	public static UnitType getRandomEasyType() {
		return easyTypes[(int)(Math.random() * easyTypes.length)];
	}

	public static UnitType getRandomMediumType() {
		return mediumTypes[(int)(Math.random() * mediumTypes.length)];
	}
	
	public static UnitType getRandomHardType() {
		return hardTypes[(int)(Math.random() * hardTypes.length)];
	}
	
}
