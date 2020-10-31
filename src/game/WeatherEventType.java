package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public enum WeatherEventType implements HasImage{

	RAIN("resources/Images/ground_modifiers/rain.png"),
	SNOW("resources/Images/ground_modifiers/snow.png"),
	;
	
	private MipMap mipmap;
	
	WeatherEventType(String s){
		mipmap = new MipMap(s);
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
