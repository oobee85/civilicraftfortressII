package game;

import java.awt.*;
import javax.swing.*;
import utils.*;

public enum BuildingType implements HasImage {
	 	
	 	WALL_WOOD (250, "resources/Images/buildings/wall_wood.png", 0, false, ResearchType.WOODCUTTING),
	 	WALL_STONE (1000, "resources/Images/buildings/wall_stone.png", 0, false, ResearchType.MASONRY),
	 	WALL_BRICK (5000, "resources/Images/buildings/wall_brick.png", 0, false, null),
	 	BRIDGE (500, "resources/Images/buildings/bridge.png", 0, true, null),
	 	MINE (500, "resources/Images/buildings/mine256.png", 0, true, null),
	 	IRRIGATION (100, "resources/Images/buildings/irrigation.png", 0, true, null),
	 	
	 	WINDMILL (1000, "resources/Images/buildings/ancientwindmill.png", 0.05, true, ResearchType.CONSTRUCTION),
		SAWMILL (1000, "resources/Images/buildings/sawmill.png", 0.05, true, null),
	 	FARM (500, "resources/Images/buildings/farm.png", 0.05, true, ResearchType.FARMING),
	 	GRANARY (500, "resources/Images/buildings/granary.png", 0.05, true, null),
	 	CASTLE (5000, "resources/Images/buildings/castle256.png", 1, true, null),
		BARRACKS (1000, "resources/Images/buildings/barracks256.png", 0.1, true, ResearchType.WARRIOR_CODE)
	 	;

	private final double health;
	private MipMap mipmap;
	private boolean canMoveThrough;
	private ResearchType researchRequirement;
	public double cultureRate;
	
	BuildingType(double hp, String s, double cultureRate, boolean canMoveThrough, ResearchType requirement) {
		this.researchRequirement = requirement;
		this.health = hp;
		this.cultureRate = cultureRate;
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
