package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.MipMap;
import utils.Utils;

public enum GroundModifierType {
	FIRE(new String[] { "Images/ground_modifiers/fire.gif" }, 1, 0.5, new Color[] {new Color(255, 145, 0)}),
//	RAIN(new String[] { "Images/ground_modifiers/rain.png" }, 0, 0),
//	SNOW(new String[] { "Images/ground_modifiers/snow.png" }, 0, 0),
//	SNOW(new String[] { "Images/liquid/snow128.png" }, 1, 0),
	;

	int damage;
	double brightness;
	
	private MipMap mipmap;

	GroundModifierType(String[] s, int damage, double brightness) {
		this.mipmap = new MipMap(s);
		this.damage = damage;
		this.brightness = brightness;
	}
	GroundModifierType(String[] s, int damage, double brightness, Color[] colors) {
		this.mipmap = new MipMap(s, colors);
		this.damage = damage;
		this.brightness = brightness;
	}

	public int getDamage() {
		return damage;
	}
	public double getBrightness() {
		return brightness;
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}
	
	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
	
}
