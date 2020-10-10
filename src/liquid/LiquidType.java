package liquid;

import java.awt.*;

import javax.swing.*;

import utils.*;

public enum LiquidType implements HasImage {
		DRY(new String[] {"resources/Images/liquid/lavaanim32.gif"}, 
				0, 1000, 5, 0.08, 0),
		WATER(new String[] {"resources/Images/liquid/watermoving.gif"}, 
						0.2, 0.0005, 10, 0.08, 0, new Color[] {new Color(50, 70, 250)}),
		LAVA(new String[] {"resources/Images/liquid/lava16.png", "resources/Images/liquid/lavaanim32.gif"}, 
				0.1, 0.005, 100, 0.001, 10),
		ICE(new String[] {"resources/Images/liquid/ice.png"}, 
				0, 0.005, 0, 1, 0),
		FOG(new String[] {"resources/Images/liquid/lavaanim32.gif"}, 
				0.5, 0.0005, 0, 1000, 0),
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
	LiquidType(String[] s, double viscosity, double surfaceTension, double damage, double minimumDamageAmount, double brightness, Color[] colors) {
		this(s, viscosity, surfaceTension, damage, minimumDamageAmount, brightness);
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
