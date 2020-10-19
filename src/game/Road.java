package game;

import java.awt.*;

import world.*;

public class Road extends Building {
	
	private RoadType roadType;
	
	public Road(RoadType roadType, Tile tile) {
		super(BuildingType.ROAD, tile, World.NEUTRAL_FACTION);
		this.roadType = roadType;
	}
	public RoadType getRoadType() {
		return roadType;
	}
	
	@Override
	public Image getImage(int size) {
		return getTile().getRoadImage();
	}
}
