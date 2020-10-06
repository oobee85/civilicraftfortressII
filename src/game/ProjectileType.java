package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {

	
		ARROW(30, 5, "resources/Images/projectiles/arrow.png", 1, 2),
		ARROW_ARCHER(10, 5, "resources/Images/projectiles/arrow.png", 1, 2),
		RUNE_ARROW(50, 5, "resources/Images/projectiles/rune_arrow.png", 1, 2),
		ROCK_CATAPULT(200, 5, "resources/Images/itemicons/rock.png", 1, 3),
		ROCK_CYCLOPS(80, 5, "resources/Images/itemicons/rock.png", 1, 2),
		FIREBALL_DRAGON(100, 5, "resources/Images/projectiles/fireball.png", 2, 3),
		FIREBALL_TREBUCHET(400, 5, "resources/Images/projectiles/fireball.png", 2, 3),
		
	;
	
	private MipMap mipmap;
	private double damage;
	private double speed;
	private int radius;
	private int minimumRange;
	
	ProjectileType(double damage, double speed, String s, int radius, int minimumRange){
		mipmap = new MipMap(s);
		this.damage = damage;
		this.speed = speed;
		this.radius = radius;
		this.minimumRange = minimumRange;
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
