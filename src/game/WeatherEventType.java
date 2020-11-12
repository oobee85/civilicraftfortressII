package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public enum WeatherEventType implements HasImage{

	RAIN("resources/Images/weather/rain.png", 4),
	SNOW("resources/Images/weather/snow.png", 4),
	WIND("resources/Images/weather/wind.png", 4),
	HAIL("resources/Images/weather/hail.png", 4),
	;
	
	private MipMap mipmap;
	private int speed;
	
	WeatherEventType(String s, int speed){
		mipmap = new MipMap(s);
		this.speed = speed;
	}
	public int getSpeed() {
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
