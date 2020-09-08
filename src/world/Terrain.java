package world;
import java.awt.*;

import utils.*;

public enum Terrain {
		GRASS 		(1, 4, 0, new String[] {"resources/Images/grass/grass16.png", "resources/Images/grass/grass128.png", "resources/Images/grass/grass512.png"}),
		DIRT  		(0.5, 4, 0, new String[] {"resources/Images/dirt/dirt16.png", "resources/Images/dirt/dirt128.png", "resources/Images/dirt/dirt512.png"}),
		VOLCANO 	(4, 12, 0.1, new String[] {"resources/Images/lava/volcano16.png", "resources/Images/lava/volcano128.png", "resources/Images/lava/magma512.png"}),
		ROCK 		(2, 8, 0, new String[] {"resources/Images/mountain/rock16.png", "resources/Images/mountain/rock128.png", "resources/Images/mountain/rock512.png"}),
		SNOW 		(4, 12, 0, new String[] {"resources/Images/mountain/snow16.png", "resources/Images/mountain/snow128.png", "resources/Images/mountain/snow512.png"}),
		SAND 		(2, 6, 0, new String[] {"resources/Images/sand.png"}),
		BURNED_GROUND (4, 10, 0.08, new String[] {"resources/Images/burnedground.png"}),
		;

	private final double movePenalty;
	private final double roadCost;
	private final double brightness;
	private MipMap mipmap;

	Terrain(double roadCost, double speedPenalty, double brightness, String[] s) {
		this.movePenalty = speedPenalty;
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

	public double moveSpeed() {
		return movePenalty;
	}
	
	public double getBrightness() {
		return brightness;
	}

	public boolean isBuildable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.SNOW) {
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
		if (t == Terrain.VOLCANO || t == Terrain.SNOW || t == Terrain.ROCK || t == Terrain.BURNED_GROUND) {
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

//	public boolean isBridgeable(Terrain t) {
//		if (t == Terrain.WATER) {
//			return true;
//		}
//		return false;
//
//	}

	@Override
	public String toString() {
		return Utils.getName(this);
	}

}
