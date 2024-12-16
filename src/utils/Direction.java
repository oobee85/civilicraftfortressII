package utils;

import java.awt.Image;

public enum Direction {
	
	NONE		(0, 0, "C", "Images/interfaces/flow/arrow_none.png", "Images/interfaces/flow/none.png"),
	NORTH		(0, -1, "N", "Images/interfaces/flow/arrow_north.png", "Images/interfaces/flow/up2.gif"),
	NORTHEAST	(1, -0.5, "NE", "Images/interfaces/flow/arrow_upright.png", "Images/interfaces/flow/upright6.gif"),
	SOUTHEAST	(1, 0.5, "SE", "Images/interfaces/flow/arrow_botright.png", "Images/interfaces/flow/botright3.gif"),
	SOUTH		(0, 1, "S", "Images/interfaces/flow/arrow_bot.png", "Images/interfaces/flow/down2.gif"),
	SOUTHWEST	(-1, 0.5, "SW", "Images/interfaces/flow/arrow_botleft.png", "Images/interfaces/flow/botleft2.gif"),
	NORTHWEST	(-1, -0.5, "NW", "Images/interfaces/flow/arrow_upleft.png", "Images/interfaces/flow/upleft3.gif"),
	;
	
	public static final String ALL_DIRECTIONS = NORTH.toString() + NORTHEAST.toString() + SOUTHEAST.toString() + SOUTH.toString() + SOUTHWEST.toString() + NORTHWEST.toString();
	
	private double deltax;
	private double deltay;
	private String shortname;
	private Image image;
	private Image arrowImage;
	private Direction(double deltax, double deltay, String shortname, String arrowImageString, String imageString) {
		this.deltax = deltax;
		this.deltay = deltay;
		this.shortname = shortname;
		this.arrowImage = Utils.loadImage(arrowImageString);
		this.image = Utils.loadImage(imageString);
//		this.image = Utils.loadImage("Images/interfaces/flow/upright6.gif");
	}
	public Image getArrowImage() {
		return this.arrowImage;
	}
	public Image getImage() {
		return this.image;
	}
	public double deltax() {
		return deltax;
	}
	public double deltay() {
		return deltay;
	}
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public String getShortName() {
		return shortname;
	}
	
	public static Direction getDirection(TileLoc from, TileLoc to) {
		if(from.x() == to.x()) {
			if(to.y() - from.y() == 1) {
				return SOUTH;
			}
			else if(from.y() - to.y() == 1) {
				return NORTH;
			}
		}
		else if(from.x() % 2 == 0){
			if(to.x() - from.x() == 1) {
				if(to.y() == from.y()) {
					return NORTHEAST;
				}
				else {
					return SOUTHEAST;
				}
			}
			else if(from.x() - to.x() == 1) {
				if(to.y() == from.y()) {
					return NORTHWEST;
				}
				else {
					return SOUTHWEST;
				}
			}
		}
		else {
			if(to.x() - from.x() == 1) {
				if(to.y() == from.y()) {
					return SOUTHEAST;
				}
				else {
					return NORTHEAST;
				}
			}
			else if(from.x() - to.x() == 1) {
				if(to.y() == from.y()) {
					return SOUTHWEST;
				}
				else {
					return NORTHWEST;
				}
			}
		}
		return null;
	}
}
