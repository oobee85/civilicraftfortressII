package sounds;

public enum SoundEffect {
	
	UNITCREATION ("UnitCreation.wav"),
	DEATH ("Death.wav"),
	BUILDINGPLANNED ("BuildingPlanned.wav"),
	BOMBEXPLODE ("BombExplode.wav"),
	DIRTWALKING ("DirtWalking.wav"),
	MELEECOMBAT ("MeleeCombat.wav"),
	PLANTDEATH ("PlantDeath.wav"),
	RESEARCHCOMPLETE ("ResearchComplete.wav"),
	TOODEEP ("TheDwarvesDelved.wav"),
	SMITHYPRODUCE ("smithy_produce.wav"),
	;
	
	private String name;
	
	
	
	SoundEffect(String name){
		this.name = name;
	}
	
	
	public String getFileName() {
		return name;
	}
	
}



