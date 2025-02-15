package sounds;

public enum SoundEffect {
	
	UNITCREATION ("UnitCreation.wav", false),
	BUILDINGPLANNED ("BuildingPlanned.wav", false),
	SMITHYPRODUCE ("smithy_produce.wav", false),
	TRAPCOW("trap.wav", false),
	
	EXPLOSION ("explosion.wav", false),
	RESEARCHCOMPLETE ("ResearchComplete.wav", false),
	TOODEEP ("TheDwarvesDelved.wav", false),
	
	DIRTWALKING ("DirtWalking.wav", false),
	MOVE_DIRT("melee_move.wav", false),
	MOVE_STONE("walk_stone.wav", false),
	MOVE_WHEELED("wheeled_unit_move.wav", false),
	
	
	PROJECTILE_LAUNCH_FIRE("projectile_launch_fire.wav", false),
	PROJECTILE_LAUNCH_LIGHT("projectile_launch_light.wav", false),
	PROJECTILE_LAUNCH_HEAVY("projectile_launch_heavy.wav", false),
	PROJECTILE_LAUNCH_BULLET("projectile_launch_bullet.wav", false),
	
	PROJECTILE_IMPACT_GENERIC("projectile_impact_generic.wav", false),
	PROJECTILE_IMPACT_LIGHT("projectile_impact_light.wav", false),
	PROJECTILE_IMPACT_HEAVY("projectile_impact_heavy.wav", false),
	PROJECTILE_IMPACT_BULLET("projectile_impact_bullet.wav", false),
	
	ATTACK_MELEE_GENERIC("attack_melee_generic.wav", false),
	ATTACK_MELEE_HEAVY("attack_melee_heavy.wav", false),
	
	DEATH_UNIT ("death_unit.wav", false),
	DEATH_PLANT ("death_plant.wav", false),
	BUILDING_STONE_DEATH("building_stone_death.wav", false),
	BUILDING_WOOD_DEATH("building_wood_death.wav", false),
	
	explore1 ("music/explore1.wav", true),
	explore2 ("music/explore2.wav", true),
	explore3 ("music/explore3.wav", true),
	explore4 ("music/explore4.wav", true),
	
	tense1 ("music/tense1.wav", true),
	
	COMBAT1 ("music/combat1.wav", true),
	COMBAT2 ("music/combat2.wav", true),
	COMBAT3 ("music/combat3.wav", true),
	COMBAT4 ("music/combat4.wav", true),

	mushroomwizard ("music/mushroomwizard.wav", true),
	
	
	
	
	;
	
	private String name;
	private boolean isMusic;
	
	
	
	SoundEffect(String name, boolean isMusic){
		this.name = name;
		this.isMusic = isMusic;
	}
	
	
	public String getFileName() {
		return name;
	}
	public boolean getIsMusic() {
		return this.isMusic;
	}
	
}



