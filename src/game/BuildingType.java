package game;

import java.awt.*;
import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (100, "resources/Images/buildings/wall_wood.png"),
	 	WALL_STONE (1000, "resources/Images/buildings/wall_stone.png"),
	 	WALL_BRICK (100, "resources/Images/buildings/wall_brick.png"),
	 	
	 	MINE (100, "resources/Images/buildings/mine256.png"),
	 	IRRIGATION (50, "resources/Images/buildings/irrigation.png"),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png")
		;

	private final double health;
	private MipMap mipmap;

	BuildingType(double hp, String s) {
		this.health = hp;
		mipmap = new MipMap(s);
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

	public double getHealth() {
		return health;
	}

}
