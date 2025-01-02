package world;
import java.awt.*;

import utils.*;

public enum Terrain {
		GRASS 		  (1, 0, 0, new Point(10, 20), new String[] {"Images/terrain/grass16_lowcontrast.png"}),
		DIRT  		  (1, 0, 0, new Point(1, 10), new String[] {"Images/terrain/dirt16_lowcontrast.png"}),
		RICHSOIL      (1, 0, 0, new Point(1, 10), new String[] {"Images/terrain/richsoil.png"}),
		VOLCANO 	  (4, 30, 0.1, new Point(0, 20), new String[] {"Images/terrain/volcano16.png", "Images/terrain/volcano128.png", "Images/terrain/magma512.png"}),
		ROCK 		  (2, 10, 0, new Point(0, 20), new String[] {"Images/terrain/rock16_lowcontrast.png"}),
		LOWROCK 	  (2, 25, 0, new Point(0, 20), new String[] {"Images/terrain/rock16.png"}),
		SAND 		  (2, 20, 0, new Point(0, 1), new String[] {"Images/terrain/sand.png"}),
		BURNED_GROUND (4, 10, 0.08, new Point(0, 20), new String[] {"Images/terrain/burnedground.png"}),
		OCEAN		  (4, 10, 0.08, new Point(0, 20), new String[] {"Images/terrain/burnedground.png"}), // placeholder terrain, used for ocean biome
		;

	private final double movePenalty;
	private final double roadCost;
	private final double brightness;
	private MipMap mipmap;
	private Point minMax;

	Terrain(double roadCost, double speedPenalty, double brightness, Point minmax, String[] s) {
		this.movePenalty = speedPenalty;
		this.roadCost = roadCost;
		this.brightness = brightness;
		this.minMax = minmax;
		mipmap = new MipMap(s);
	}
	
	
	public Point getMinMax() {
		return minMax;
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
		if (t == Terrain.VOLCANO) {
			return false;
		}
		return true;

	}

	public boolean isOreable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.ROCK) {
			return true;
		}
		return false;

	}
	public boolean isPlantable(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.ROCK || t == Terrain.BURNED_GROUND || t == Terrain.SAND) {
			return false;
		}
		return true;

	}
	public boolean isCold(Terrain t) {
		if (t == Terrain.VOLCANO || t == Terrain.BURNED_GROUND) {
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


	@Override
	public String toString() {
		return Utils.getName(this);
	}

}
