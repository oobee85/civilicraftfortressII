package liquid;

import java.awt.*;

import javax.swing.*;

import utils.*;

public enum LiquidType implements HasImage {
		DRY(new String[] {"resources/Images/lava/lavaanim32.gif"}, 
				0, 1000, 5, 0.08, 0),
		WATER(new String[] {"resources/Images/water/water16.png", "resources/Images/water/water128.png", "resources/Images/water/water512.png", "resources/Images/water/water1024.jpg"}, 
				0.2, 0.0005, 10, 0.08, 0),
		LAVA(new String[] {"resources/Images/lava/lava16.png", "resources/Images/lava/lavaanim32.gif"}, 
				0.1, 0.005, 100, 0.001, 10)
		;
	
	double viscosity;
	double surfaceTension;
	double damage;
	double minimumDamageAmount;
	double brightness;
	
	private MipMap mipmap;

	LiquidType(String[] s, double viscosity, double surfaceTension, double damage, double minimumDamageAmount, double brightness) {
		this.viscosity = viscosity;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
		this.brightness = brightness;
		this.mipmap = new MipMap(s);
	}
	public double getDamage() {
		return damage;
	}
	public double getMinimumDamageAmount() {
		return minimumDamageAmount;
	}
	public double getBrightness() {
		return brightness;
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
