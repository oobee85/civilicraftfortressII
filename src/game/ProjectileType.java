package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {


		ARROW_LONGBOWMAN(45, 2, "resources/Images/projectiles/arrow.png", 1, 2, null),
		ARROW_ARCHER(15, 2, "resources/Images/projectiles/arrow.png", 1, 1, null),
		RUNE_ARROW(60, 2, "resources/Images/projectiles/rune_arrow.png", 1, 2, null),
		ROCK_CATAPULT(200, 3, "resources/Images/itemicons/rock.png", 1, 3, null),
		ROCK_CYCLOPS(80, 3, "resources/Images/itemicons/rock.png", 1, 2, null),
		ROCK_STONE_GOLEM(200, 5, "resources/Images/itemicons/rock.png", 2, 3, null),
		FIREBALL_DRAGON(100, 3, "resources/Images/projectiles/fireball2.png", 2, 3, null),
		FIREBALL_TREBUCHET(400, 3, "resources/Images/projectiles/fireball.png", 2, 3, null),
		BULLET(1000, 0, "resources/Images/projectiles/bullet.png", 1, 1, null),
		FIREWAVE(200, 6, "resources/Images/ground_modifiers/fire.gif", 1, 3, GroundModifierType.FIRE),
		
	;
	
	private MipMap mipmap;
	private int damage;
	private double speed;
	private int radius;
	private int minimumRange;
	private GroundModifierType groundModifierType;
	
	ProjectileType(int damage, double speed, String s, int radius, int minimumRange, GroundModifierType groundModifierType){
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
		return (radius > 1);
	}
	public int getDamage() {
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
