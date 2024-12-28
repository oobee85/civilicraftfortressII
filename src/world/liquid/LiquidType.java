package world.liquid;

import java.awt.*;
import game.DamageType;
import utils.*;

public enum LiquidType {
		DRY(new String[] {"Images/liquid/lavaanim32.gif"}, "Images/liquid/lavaanim32.gif",
				0f, 1000f, 1000f, 5f, 0.08f, 0f, false, DamageType.DRY),
		WATER(new String[] {"Images/liquid/watermoving.gif"}, "Images/liquid/watermoving.gif",
				0.1f, 0.1f, 0.8f, 0.05f, 4f, 0f, true, DamageType.WATER, new Color[] {new Color(50, 70, 250)}),
		LAVA(new String[] {"Images/liquid/lava16.png", "Images/liquid/lavaanim32.gif"}, "Images/liquid/lava16.png",
				0.1f, 0.0005f, 0.005f, 1f, 1f, 0.1f, false, DamageType.HEAT),
		ICE(new String[] {"Images/liquid/ice.png"}, "Images/liquid/ice.png",
				0f, 10000f, 10000f, 0.05f, 6f, 0f, true, DamageType.COLD),
		SNOW(new String[] {"Images/liquid/snow.png"}, "Images/liquid/snow.png",
				0f, 1f, 1f, 0.05f, 6f, 0f, true, DamageType.COLD),
//				0.1f, 0.9f, 1f, 1f, 1f, 0.1f, true, DamageType.COLD),
		
		;
	
	float viscosity;
	float selfSurfaceTension;
	float surfaceTension;
	float damage;
	float minimumDamageAmount;
	float brightness;
	boolean isWater;
	DamageType damageType;
	
	private MipMap mipmap;
	private String textureFile;

	LiquidType(String[] s, String textureFile, float viscosity, float selfSurfaceTension, float surfaceTension, float damage, float minimumDamageAmount, float brightness, boolean isWater, DamageType damageType) {
		this.viscosity = viscosity;
		this.selfSurfaceTension = selfSurfaceTension;
		this.surfaceTension = surfaceTension;
		this.damage = damage;
		this.minimumDamageAmount = minimumDamageAmount;
		this.brightness = brightness;
		this.isWater = isWater;
		this.damageType = damageType;
		this.mipmap = new MipMap(s);
		this.textureFile = textureFile;
	}
	LiquidType(String[] s, String textureFile, float viscosity, float selfSurfaceTension, float surfaceTension, float damage, float minimumDamageAmount, float brightness, boolean isWater, DamageType damageType, Color[] colors) {
		this(s, textureFile, viscosity, selfSurfaceTension, surfaceTension, damage, minimumDamageAmount, brightness, isWater, damageType);
		this.mipmap = new MipMap(s, colors);
	}
	public double getDamage() {
		return damage;
	}
	public boolean isWater() {
		return isWater;
	}
	public double getMinimumDamageAmount() {
		return minimumDamageAmount;
	}
	public double getBrightness() {
		return brightness;
	}
	public DamageType getDamageType() {
		return damageType;
	}

	public MipMap getMipMap() {
		return mipmap;
	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}
	
	public String getTextureFile() {
		return textureFile;
	}
}
