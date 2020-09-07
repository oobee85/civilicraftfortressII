package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;



public enum ProjectileType implements HasImage {

	
		ARROW(30, 5, "resources/Images/projectiles/arrow.png"),
		ROCK(200, 5, "resources/Images/itemicons/rock.png"),
		FIREBALL(30, 5, "resources/Images/projectiles/fireball.png"),
	;
	
	private MipMap mipmap;
	private double damage;
	private double speed;
	
	ProjectileType(double damage, double speed, String s){
		mipmap = new MipMap(s);
		this.damage = damage;
		this.speed = speed;
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
