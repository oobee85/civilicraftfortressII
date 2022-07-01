package world;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.MipMap;
import utils.Utils;

public enum WeatherEventType {

	RAIN("Images/weather/rain.gif", 4),
	SNOW("Images/weather/snow.gif", 4),
//	WIND("Images/weather/wind.png", 4),
//	RAIN("Images/weather/hail.png", 4),
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

	public MipMap getMipMap() {
		return mipmap;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
}
