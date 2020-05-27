package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;

public enum RoadType implements HasImage{
	ROAD_STONE (4, "resources/Images/buildings/castle256.png"),
	;
	
	private final double speed;
	private MipMap mipmap;
	
	// 	move penalty = penalty/speed
	RoadType(double speed, String s) {
		this.speed = speed;
		mipmap = new MipMap(s);

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

	public double getSpeed() {
		return speed;
	}

}
