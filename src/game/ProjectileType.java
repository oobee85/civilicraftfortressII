package game;

import utils.MipMap;
import utils.Utils;

public enum ProjectileType {

		ARROW(1, "Images/projectiles/arrow.png", 1, null, 0),
		RUNE_ARROW(1, "Images/projectiles/rune_arrow.png", 1, null, 0),
		ROCK(3, "Images/itemicons/rock.png", 1, null, 0),
		FIREBALL_TREBUCHET(3, "Images/projectiles/fireball.png", 2, null, 0),
		
		ROCK_STONE_GOLEM(5, "Images/itemicons/rock.png", 1, null, 0),
		FIREBALL_DRAGON(3, "Images/projectiles/fireball2.png", 2, null, 0),
		BULLET(0, "Images/projectiles/bullet.png", 1, null, 0),
		FIRE_WAVE(6, "Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE, 100),
		METEOR_WAVE(4, "Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE, 1000),
		METEOR(10, "Images/projectiles/comet.png", 20, null, 1000),
		LAVA_BALL(1, "Images/liquid/lavaanim32.gif", 1, null, 0),
		WIZARD_BALL(2, "Images/projectiles/fireball2.png", 4, null, 100),
		FIREBREATH(3, "Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE, 10),
	;
	
	private MipMap mipmap;
	private double speed;
	private int radius;
	private GroundModifierType groundModifierType;
	private int groundModifierDuration;
	
	ProjectileType(double speed, String s, int radius, GroundModifierType groundModifierType, int groundModifierDuration){
		mipmap = new MipMap(s);
		this.speed = speed;
		this.radius = radius;
		this.groundModifierType = groundModifierType;
		this.groundModifierDuration = groundModifierDuration;
	}
	public GroundModifierType getGroundModifierType() {
		return groundModifierType;
	}
	public int getGroundModifierDuration() {
		return groundModifierDuration;
	}

	public boolean isExplosive() {
		return (radius > 1);
	}
	public int getRadius() {
		return radius;
	}
	public double getSpeed() {
		return speed;
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}

}
