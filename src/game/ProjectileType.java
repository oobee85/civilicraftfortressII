package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {

	
		ARROW(30, 1, "resources/Images/projectiles/arrow.png", 1, 2, null),
		ARROW_ARCHER(10, 1, "resources/Images/projectiles/arrow.png", 1, 2, null),
		RUNE_ARROW(50, 1, "resources/Images/projectiles/rune_arrow.png", 1, 2, null),
		ROCK_CATAPULT(200, 2, "resources/Images/itemicons/rock.png", 1, 3, null),
		ROCK_CYCLOPS(80, 2, "resources/Images/itemicons/rock.png", 1, 2, null),
		FIREBALL_DRAGON(100, 2, "resources/Images/projectiles/fireball2.png", 2, 3, null),
		FIREBALL_TREBUCHET(400, 2, "resources/Images/projectiles/fireball.png", 2, 3, null),
		FIREWAVE(200, 5, "resources/Images/ground_modifiers/fire.gif", 1, 3, GroundModifierType.FIRE),
		
	;
	
	private MipMap mipmap;
	private double damage;
	private double speed;
	private int radius;
	private int minimumRange;
	private GroundModifierType groundModifierType;
	
	ProjectileType(double damage, double speed, String s, int radius, int minimumRange, GroundModifierType groundModifierType){
		mipmap = new MipMap(s);
		this.damage = damage;
		this.speed = speed;
		this.radius = radius;
		this.minimumRange = minimumRange;
		this.groundModifierType = groundModifierType;
	}
	public GroundModifierType getGroundModifierType() {
		return groundModifierType;
	}

	public boolean isExplosive() {
		return radius > 1;
	}
	public double getDamage() {
		return damage;
	}
	public int getRadius() {
		return radius;
	}
	public int getMinimumRange() {
		return minimumRange;
	}
	public double getSpeed() {
		return speed;
	}
	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
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
