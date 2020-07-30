package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import utils.HasImage;
import utils.MipMap;
import utils.Utils;

public enum GroundModifierType implements HasImage{

	FIRE(new String[] { "resources/Images/ground_modifiers/fire.gif" }, 100),

	;

	int maxTime;
	
	
	private MipMap mipmap;

	GroundModifierType(String[] s, int maxTime) {
		this.mipmap = new MipMap(s);
		this.maxTime = maxTime;
	}

	public int getMaxTime() {
		return maxTime;
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
