package game;

import java.awt.*;
import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (250, "resources/Images/buildings/wall_wood.png", false, ResearchType.WOODCUTTING),
	 	WALL_STONE (1000, "resources/Images/buildings/wall_stone.png", false, ResearchType.MASONRY),
	 	WALL_BRICK (5000, "resources/Images/buildings/wall_brick.png", false, null),
	 	
	 	MINE (500, "resources/Images/buildings/mine256.png", true, null),
	 	IRRIGATION (100, "resources/Images/buildings/irrigation.png", true, null),
	 	WINDMILL (1000, "resources/Images/buildings/ancientwindmill.png", true, ResearchType.CONSTRUCTION),
		SAWMILL (1000, "resources/Images/buildings/sawmill.png", true, null),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png", true, null),
	 	FARM (500, "resources/Images/buildings/farm.png", true, ResearchType.FARMING),
	 	GRANARY (500, "resources/Images/buildings/granary.png", true, null),
	 	;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private ResearchType researchRequirement;
	
	BuildingType(double hp, String s, boolean canMoveThrough, ResearchType requirement) {
		this.researchRequirement = requirement;
		this.health = hp;
		mipmap = new MipMap(s);
		this.canMoveThrough = canMoveThrough;
	}
	public ResearchType getResearchRequirement() {
		return researchRequirement;
	}

	@Override
	public Image getImage(int size) {
		return mipmap.getImage(size);
	}

	@Override
	public ImageIcon getImageIcon(int size) {
		return mipmap.getImageIcon(size);
	}
	
	public double getHealth() {
		return health;
	}
	public boolean canMoveThrough() {
		return canMoveThrough;
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
