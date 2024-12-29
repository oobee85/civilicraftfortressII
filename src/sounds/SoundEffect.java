package sounds;

public enum SoundEffect {
	
	UNITCREATION ("UnitCreation.wav"),
	BUILDINGPLANNED ("BuildingPlanned.wav"),
	SMITHYPRODUCE ("smithy_produce.wav"),
	TRAPCOW("trap.wav"),
	
	
	BOMBEXPLODE ("explosion.wav"),
	RESEARCHCOMPLETE ("ResearchComplete.wav"),
	TOODEEP ("TheDwarvesDelved.wav"),
	
	DIRTWALKING ("DirtWalking.wav"),
	MOVEDIRT("melee_move.wav"),
	MOVESTONE("walk_stone.wav"),
	
	
	
	
	PROJECTILEIMPACTGENERIC("arrow_dirt.wav"),
	ARROWLOOSE("arrow_loose.wav"),
	FIREWAVE("fire_wave.wav"),
	PROJECTILELAUNCHHEAVY("projectile_launch_heavy.wav"),
	PROJECTILEIMPACTHEAVY("projectile_impact_heavy.wav"),
	PROJECTILEIMPACTLIGHT("projectile_impact_light.wav"),
	
	MELEEATTACK("melee_attack.wav"),
	CLEAVEMELEEATTACK("cleave_melee_attack.wav"),
	
	DEATH ("Death.wav"),
	PLANTDEATH ("PlantDeath.wav"),
	BUILDINGSTONEDEATH("building_stone_death.wav"),
	BUILDINGWOODDEATH("building_wood_death.wav"),
	
	WHEELEDUNITMOVE("wheeled_unit_move.wav"),
	;
	
	private String name;
	
	
	
	SoundEffect(String name){
		this.name = name;
	}
	
	
	public String getFileName() {
		return name;
	}
	
}



