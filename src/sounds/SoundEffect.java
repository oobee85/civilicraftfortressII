package sounds;

public enum SoundEffect {
	
	UNITCREATION ("UnitCreation.wav"),
	DEATH ("Death.wav"),
	BUILDINGPLANNED ("BuildingPlanned.wav"),
	BOMBEXPLODE ("BombExplode.wav"),
	DIRTWALKING ("DirtWalking.wav"),
	PLANTDEATH ("PlantDeath.wav"),
	RESEARCHCOMPLETE ("ResearchComplete.wav"),
	TOODEEP ("TheDwarvesDelved.wav"),
	SMITHYPRODUCE ("smithy_produce.wav"),
	MELEEATTACK("melee_attack.wav"),
	MOVEDIRT("melee_move.wav"),
	ARROWDIRT("arrow_dirt.wav"),
	ARROWLOOSE("arrow_loose.wav"),
	FIREWAVE("fire_wave.wav"),
	CLEAVEMELEEATTACK("cleave_melee_attack.wav"),
	;
	
	private String name;
	
	
	
	SoundEffect(String name){
		this.name = name;
	}
	
	
	public String getFileName() {
		return name;
	}
	
}



