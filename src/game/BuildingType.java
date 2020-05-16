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
	 	BRIDGE (500, "resources/Images/buildings/bridge.png", true)
		;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;

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

	public double getHealth() {
		return health;
	}
	public boolean canMoveThrough() {
		return canMoveThrough;
	}

}
