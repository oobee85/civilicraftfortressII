package world;
import java.awt.*;

import utils.*;

public enum Terrain {
		GRASS (1, 10, 0, new String[] {"resources/Images/grass/grass16.png", "resources/Images/grass/grass128.png", "resources/Images/grass/grass512.png"}),
		DIRT  (0.5, 8, 0, new String[] {"resources/Images/dirt/dirt16.png", "resources/Images/dirt/dirt128.png", "resources/Images/dirt/dirt512.png"}),
		LAVA  (10000, 0, 1, new String[] {"resources/Images/lava/lava16.png", "resources/Images/lava/lavaanim32.gif", "resources/Images/lava/lava128.gif", "resources/Images/lava/lava512.png"}),
		VOLCANO (4, 1, 0.1, new String[] {"resources/Images/lava/volcano16.png", "resources/Images/lava/volcano128.png", "resources/Images/lava/magma512.png"}),
		ROCK (2, 3, 0, new String[] {"resources/Images/mountain/rock16.png", "resources/Images/mountain/rock128.png", "resources/Images/mountain/rock512.png"}),
		SNOW (4, 1, 0, new String[] {"resources/Images/mountain/snow16.png", "resources/Images/mountain/snow128.png", "resources/Images/mountain/snow512.png"}),
		WATER (10000, 0, 0, new String[] {"resources/Images/water/water16.png", "resources/Images/water/water128.png", "resources/Images/water/water512.png"}),
		;

	private final int moveSpeed;
	private final double roadCost;
	private final double brightness;
	private MipMap mipmap;

	Terrain(double roadCost, int speed, double brightness, String[] s) {
		this.moveSpeed = speed;
		this.roadCost = roadCost;
		this.brightness = brightness;
		mipmap = new MipMap(s);
	}
	
	public double getRoadCost() {
		return roadCost;
	}

	public Image getImage(int size) {
		return mipmap.getImage(size);
	}

	private int moveSpeed() {
		return moveSpeed;
	}
	
	public double getBrightness() {
		return brightness;
	}

	public boolean isBuildable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.LAVA || t == Terrain.SNOW || t == Terrain.WATER) {
			return false;
		}
		return true;

	}

	public boolean isOreable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.SNOW || t == Terrain.ROCK) {
			return true;
		}
		return false;

	}
	public boolean isPlantable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.LAVA || t == Terrain.SNOW || t == Terrain.WATER || t == Terrain.ROCK) {
			return false;
		}
		return true;

	}
	public boolean canSupportRare(Terrain t) {
		if (t == Terrain.VOLCANO) {
			return true;
		}
		return false;
	}

	public boolean isBridgeable(Terrain t) {
		if (t == Terrain.WATER) {
			return true;
		}
		return false;

	}

}
