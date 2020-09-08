package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {

	
		ARROW(30, 5, "resources/Images/projectiles/arrow.png", false),
		ROCK_CATAPULT(200, 5, "resources/Images/itemicons/rock.png", false),
		FIREBALL_DRAGON(100, 5, "resources/Images/projectiles/fireball.png", true),
		FIREBALL_TREBUCHET(400, 5, "resources/Images/projectiles/fireball.png", true),
	;
	
	private MipMap mipmap;
	private double damage;
	private double speed;
	private boolean isExplosive;
	
	ProjectileType(double damage, double speed, String s, boolean isExplosive){
		mipmap = new MipMap(s);
		this.damage = damage;
		this.speed = speed;
		this.isExplosive = isExplosive;
	}

	public boolean isExplosive() {
		return isExplosive;
	}
	public double getDamage() {
		return damage;
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
