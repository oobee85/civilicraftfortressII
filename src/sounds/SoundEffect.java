package sounds;

public enum SoundEffect {
	
	UNITCREATION ("UnitCreation.wav"),
	BUILDINGPLANNED ("BuildingPlanned.wav"),
	SMITHYPRODUCE ("smithy_produce.wav"),
	TRAPCOW("trap.wav"),
	
	EXPLOSION ("explosion.wav"),
	RESEARCHCOMPLETE ("ResearchComplete.wav"),
	TOODEEP ("TheDwarvesDelved.wav"),
	
	DIRTWALKING ("DirtWalking.wav"),
	MOVE_DIRT("melee_move.wav"),
	MOVE_STONE("walk_stone.wav"),
	MOVE_WHEELED("wheeled_unit_move.wav"),
	
	
	PROJECTILE_LAUNCH_FIRE("projectile_launch_fire.wav"),
	PROJECTILE_LAUNCH_LIGHT("projectile_launch_light.wav"),
	PROJECTILE_LAUNCH_HEAVY("projectile_launch_heavy.wav"),
	PROJECTILE_LAUNCH_BULLET("projectile_launch_bullet.wav"),
	
	PROJECTILE_IMPACT_GENERIC("projectile_impact_generic.wav"),
	PROJECTILE_IMPACT_LIGHT("projectile_impact_light.wav"),
	PROJECTILE_IMPACT_HEAVY("projectile_impact_heavy.wav"),
	PROJECTILE_IMPACT_BULLET("projectile_impact_bullet.wav"),
	
	ATTACK_MELEE_GENERIC("attack_melee_generic.wav"),
	ATTACK_MELEE_HEAVY("attack_melee_heavy.wav"),
	
	DEATH_UNIT ("death_unit.wav"),
	DEATH_PLANT ("death_plant.wav"),
	BUILDING_STONE_DEATH("building_stone_death.wav"),
	BUILDING_WOOD_DEATH("building_wood_death.wav"),
	
	COMBAT1 ("music/combat1.wav"),
	COMBAT2 ("music/combat2.wav"),
	COMBAT3 ("music/combat3.wav"),
	COMBAT4 ("music/combat4.wav"),
	
	explore1 ("music/explore1.wav"),
	explore2 ("music/explore2.wav"),
	explore3 ("music/explore3.wav"),
	explore4 ("music/explore4.wav"),
	
	tense1 ("music/tense1.wav"),
	
	
	;
	
	private String name;
	
	
	
	SoundEffect(String name){
		this.name = name;
	}
	
	
	public String getFileName() {
		return name;
	}
	
}



