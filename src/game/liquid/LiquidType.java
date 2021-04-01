package game.liquid;

import java.awt.*;

import javax.swing.*;

import utils.*;

public enum LiquidType implements HasImage {
		DRY(new String[] {"Images/liquid/lavaanim32.gif"},
				0f, 1000f, 1000f, 5f, 0.08f, 0f, false),
		WATER(new String[] {"Images/liquid/watermoving.gif"}, 
				0.2f, 0.8f, 0.8f, 0.1f, 2f, 0f, true, new Color[] {new Color(50, 70, 250)}),
		LAVA(new String[] {"Images/liquid/lava16.png", "Images/liquid/lavaanim32.gif"},
				0.1f, 0.0005f, 0.005f, 1f, 1f, 10f, false),
		ICE(new String[] {"Images/liquid/ice.png"},
				0f, 1000f, 1000f, 20f, 0.05f, 0f, true),
		SNOW(new String[] {"Images/liquid/snow128.png"}, 
				1f, 0.05f, 0.1f, 20f, 0.05f, 0f, true),
		;
	
	float viscosity;
	float selfSurfaceTension;
	float surfaceTension;
	float damage;
	float minimumDamageAmount;
	float brightness;
	boolean isWater;
	
	private MipMap mipmap;

	LiquidType(String[] s, float viscosity, float selfSurfaceTension, float surfaceTension, float damage, float minimumDamageAmount, float brightness, boolean isWater) {
		this.viscosity = viscosity;
		this.selfSurfaceTension = selfSurfaceTension;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
		this.brightness = brightness;
		this.isWater = isWater;
		this.mipmap = new MipMap(s);
	}
	LiquidType(String[] s, float viscosity, float selfSurfaceTension, float surfaceTension, float damage, float minimumDamageAmount, float brightness, boolean isWater, Color[] colors) {
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
