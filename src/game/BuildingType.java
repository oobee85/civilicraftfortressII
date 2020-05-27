package game;

import java.awt.*;
import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (100, "resources/Images/buildings/wall_wood.png", false),
	 	WALL_STONE (1000, "resources/Images/buildings/wall_stone.png", false),
	 	WALL_BRICK (5000, "resources/Images/buildings/wall_brick.png", false),
	 	
	 	MINE (200, "resources/Images/buildings/mine256.png", true),
	 	IRRIGATION (100, "resources/Images/buildings/irrigation.png", true),
	 	WINDMILL (200, "resources/Images/buildings/ancientwindmill.png", true),
		SAWMILL (200, "resources/Images/buildings/sawmill2.png", true),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png", true),
	 	FARM (100, "resources/Images/buildings/farm.png", true)
		;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private String name;
	
	BuildingType(double hp, String s, boolean canMoveThrough) {
		this.health = hp;
		mipmap = new MipMap(s);
		this.canMoveThrough = canMoveThrough;
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
		return name;
	}
	
	public double getHealth() {
		return health;
	}
	public boolean canMoveThrough() {
		return canMoveThrough;
	}
	

}
