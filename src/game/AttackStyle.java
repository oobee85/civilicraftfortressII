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
	
	public void addDamage(int damage) {
		this.damage += damage;
	}
	public void addRange(int range) {
		this.range += range;
	}
	public void addMinRange(int minRange) {
		this.minRange += minRange;
	}
	public void addCooldown(int cooldown) {
		this.cooldown += cooldown;
	}
	
	public void setDamage(int damage) {
		this.damage = damage;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public void setMinRange(int minRange) {
		this.minRange = minRange;
	}
	public void setLifesteal(boolean lifesteal) {
		this.lifesteal = lifesteal;
	}
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	public void mergeAttackStyle(AttackStyle other) {
		this.damage += other.getDamage();
		this.range += other.getRange();
		this.minRange += other.getMinRange();
		this.lifesteal = this.lifesteal || other.lifesteal;
		this.cooldown += other.getCooldown();
	}

	public ProjectileType getProjectile() {
		return projectile;
	}
	
	
	
}
