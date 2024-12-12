package sounds;

public enum SoundEffect {
	
	CASTLEUNITCREATION ("CastleUnitCreation.wav"),
	DEATH ("Death.wav"),
	BUILDINGPLANNED ("BuildingPlanned.wav"),
	BOMBEXPLODE ("BombExplode.wav"),
	DIRTWALKING ("DirtWalking.wav"),
	MELEECOMBAT ("MeleeCombat.wav"),
	TREEDEATH ("TreeDeath.wav"),
	RESEARCHCOMPLETE ("ResearchComplete.wav"),
	
	;
	
	private String name;
	
	
	
	SoundEffect(String name){
		this.name = name;
	}
	
	
	public String getFileName() {
		return name;
	}
	
}



