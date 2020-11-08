package game;

public class AttackStyle {
	
	private int damage;
	private int range;
	private int minRange;
	private boolean lifesteal;
	private int cooldown;
	
//	private boolean hasProjectile;
//	private String projectileImage;
//	private String projectileGroundModifier;
//	private int explosionRadius;
//	private int projectileSpeed;
	private ProjectileType projectile;
	
	public AttackStyle(int damage, int range, int minRange, boolean lifesteal, int cooldown, ProjectileType projectile) {
		this.damage = damage;
		this.range = range;
		this.minRange = minRange;
		this.lifesteal = lifesteal;
		this.cooldown = cooldown;
		this.projectile = projectile;
	}

	public int getDamage() {
		return damage;
	}

	public int getRange() {
		return range;
	}

	public int getMinRange() {
		return minRange;
	}

	public boolean isLifesteal() {
		return lifesteal;
	}

	public int getCooldown() {
		return cooldown;
	}

//	public boolean isHasProjectile() {
//		return hasProjectile;
//	}
//
//	public String getProjectileImage() {
//		return projectileImage;
//	}
//
//	public String getProjectileGroundModifier() {
//		return projectileGroundModifier;
//	}
//
//	public int getExplosionRadius() {
//		return explosionRadius;
//	}
//
//	public int getProjectileSpeed() {
//		return projectileSpeed;
//	}

	public ProjectileType getProjectile() {
		return projectile;
	}
	
	
	
}
