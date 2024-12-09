package game;

public class EnemySpawns {
	private static UnitType[] easyTypes = {
			Game.unitTypeMap.get("WATER_SPIRIT"),
			Game.unitTypeMap.get("FIREFLY"),
			Game.unitTypeMap.get("FLAMELET"),
			
			Game.unitTypeMap.get("WARRIOR"),
			Game.unitTypeMap.get("SKELETON"),
			Game.unitTypeMap.get("WOLF"),
	};

	private static UnitType[] mediumTypes = {
			Game.unitTypeMap.get("TERMITE"),
			Game.unitTypeMap.get("ENT"),
			Game.unitTypeMap.get("OGRE"),
			Game.unitTypeMap.get("ROC"),
			Game.unitTypeMap.get("STONE_GOLEM"),
			Game.unitTypeMap.get("VAMPIRE"),
			Game.unitTypeMap.get("BOMB"),
			
	};

	private static UnitType[] hardTypes = {
			Game.unitTypeMap.get("WEREWOLF"),
			Game.unitTypeMap.get("LAVAGOLEM"),
			Game.unitTypeMap.get("ICE_GIANT"),
			
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
