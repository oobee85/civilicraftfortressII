package liquid;

import java.awt.*;

import javax.swing.*;

import utils.*;

public enum LiquidType implements HasImage {
		DRY(new String[] {"resources/Images/lava/lavaanim32.gif"}, 0, 1000, 5, 0.08),
		WATER(new String[] {"resources/Images/water/water16.png", "resources/Images/water/water128.png", "resources/Images/water/water512.png", "resources/Images/water/water1024.jpg"}, 0.2, 0.0005, 5, 0.08),
		LAVA(new String[] {"resources/Images/lava/lava16.png", "resources/Images/lava/lavaanim32.gif"}, 0.1, 0.005, 100, 0.001)
		;
	
	double viscosity;
	double surfaceTension;
	double damage;
	double minimumDamageAmount;
	
	private MipMap mipmap;

	LiquidType(String[] s, double viscosity, double surfaceTension, double damage, double minimumDamageAmount) {
		this.viscosity = viscosity;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
        this.mipmap = new MipMap(s);
	}
	public double getDamage() {
		return damage;
	}
	public double getMinimumDamageAmount() {
		return minimumDamageAmount;
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
}
