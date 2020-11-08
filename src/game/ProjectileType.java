package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {


		ARROW_LONGBOWMAN(2, "resources/Images/projectiles/arrow.png", 1, null),
		ARROW_CHARIOT(2, "resources/Images/projectiles/arrow.png", 1, null),
		ARROW_ARCHER(2, "resources/Images/projectiles/arrow.png", 1, null),
		RUNE_ARROW(2, "resources/Images/projectiles/rune_arrow.png", 1, null),
		ROCK_CATAPULT(3, "resources/Images/itemicons/rock.png", 1, null),
		FIREBALL_TREBUCHET(3, "resources/Images/projectiles/fireball.png", 2, null),
		
		ROCK_CYCLOPS(3, "resources/Images/itemicons/rock.png", 1, null),
		ROCK_STONE_GOLEM(5, "resources/Images/itemicons/rock.png", 1, null),
		FIREBALL_DRAGON(3, "resources/Images/projectiles/fireball2.png", 2, null),
		BULLET(0, "resources/Images/projectiles/bullet.png", 1, null),
		FIREWAVE(6, "resources/Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE),
		METEOR_WAVE(4, "resources/Images/ground_modifiers/fire.gif", 1, GroundModifierType.FIRE),
	;
	
	private MipMap mipmap;
	private double speed;
	private int radius;
	private GroundModifierType groundModifierType;
	
	ProjectileType(double speed, String s, int radius, GroundModifierType groundModifierType){
		mipmap = new MipMap(s);
		this.speed = speed;
		this.radius = radius;
		this.groundModifierType = groundModifierType;
	}
	public GroundModifierType getGroundModifierType() {
		return groundModifierType;
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
