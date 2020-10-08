package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public enum GroundModifierType implements HasImage{

	FIRE(new String[] { "resources/Images/ground_modifiers/fire.gif" }, 1, 0.5),
	SNOW(new String[] { "resources/Images/ground_modifiers/snow128.png" }, 1, 0),
	;

	int damage;
	double brightness;
	
	private MipMap mipmap;

	GroundModifierType(String[] s, int damage, double brightness) {
		this.mipmap = new MipMap(s);
		this.damage = damage;
		this.brightness = brightness;
	}

	public int getDamage() {
		return damage;
	}
	public double getBrightness() {
		return brightness;
	}
	public boolean isCold(GroundModifierType gmt) {
		if(gmt == GroundModifierType.SNOW) {
			return true;
		}
		return false;
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
