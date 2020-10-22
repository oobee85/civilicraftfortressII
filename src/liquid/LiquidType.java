package liquid;

import java.awt.*;

import javax.swing.*;

import utils.*;

public enum LiquidType implements HasImage {
		DRY(new String[] {"resources/Images/liquid/lavaanim32.gif"}, 
				0, 1000, 1000, 5, 0.08, 0, false),
		WATER(new String[] {"resources/Images/liquid/watermoving.gif"}, 
				0.2, 0.0001, 0.001, 10, 0.08, 0, true, new Color[] {new Color(50, 70, 250)}),
		LAVA(new String[] {"resources/Images/liquid/lava16.png", "resources/Images/liquid/lavaanim32.gif"}, 
				0.1, 0.0005, 0.01, 100, 0.001, 10, false),
		ICE(new String[] {"resources/Images/liquid/ice.png"}, 
				0, 1000, 1000, 20, 0.05, 0, true),
		SNOW(new String[] {"resources/Images/liquid/snow128.png"}, 
				1, 0.05, 0.1, 20, 0.05, 0, true),
		;
	
	double viscosity;
	double selfSurfaceTension;
	double surfaceTension;
	double damage;
	double minimumDamageAmount;
	double brightness;
	boolean isWater;
	
	private MipMap mipmap;

	LiquidType(String[] s, double viscosity, double selfSurfaceTension, double surfaceTension, double damage, double minimumDamageAmount, double brightness, boolean isWater) {
		this.viscosity = viscosity;
		this.selfSurfaceTension = selfSurfaceTension;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
		this.brightness = brightness;
		this.isWater = isWater;
		this.mipmap = new MipMap(s);
	}
	LiquidType(String[] s, double viscosity, double selfSurfaceTension, double surfaceTension, double damage, double minimumDamageAmount, double brightness, boolean isWater, Color[] colors) {
		this(s, viscosity, selfSurfaceTension, surfaceTension, damage, minimumDamageAmount, brightness, isWater);
		this.mipmap = new MipMap(s, colors);
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
