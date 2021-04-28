package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {

		ARROW(2, "Images/projectiles/arrow.png", 1, null, 0),
		RUNE_ARROW(2, "Images/projectiles/rune_arrow.png", 1, null, 0),
		ROCK(3, "Images/itemicons/rock.png", 1, null, 0),
		FIREBALL_TREBUCHET(3, "Images/projectiles/fireball.png", 2, null, 0),
		
		ROCK_STONE_GOLEM(5, "Images/itemicons/rock.png", 1, null, 0),
		FIREBALL_DRAGON(3, "Images/projectiles/fireball2.png", 2, null, 0),
		BULLET(0, "Images/projectiles/bullet.png", 1, null, 0),
		FIRE_WAVE(6, "Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE, 100),
		METEOR_WAVE(4, "Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE, 1000),
		METEOR(10, "Images/projectiles/comet.png", 20, null, 1000),
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
	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return mipmap.getShadow(size);
	}
	@Override
	public Image getHighlight(int size) {
		return mipmap.getHighlight(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}

	@Override
	public Color getColor(int size) {
		return mipmap.getColor(size);
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}

}
