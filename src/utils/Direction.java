package utils;

import java.awt.Image;

public enum Direction {
	
	NORTH		(0, -1, "Images/interfaces/arrow_north.png", "Images/interfaces/flow/north.gif"),
	NORTHEAST	(1, 0.5, "Images/interfaces/arrow_upright.png", "Images/interfaces/flow/upright.gif"),
	NORTHWEST	(-1, 0.5, "Images/interfaces/arrow_upleft.png", "Images/interfaces/flow/upleft.gif"),
	
	SOUTH		(0, 1, "Images/interfaces/arrow_bot.png", "Images/interfaces/flow/south.gif"),
	SOUTHEAST	(1, -0.5, "Images/interfaces/arrow_botright.png", "Images/interfaces/flow/botright.png"),
	SOUTHWEST	(-1, -0.5, "Images/interfaces/arrow_botleft.png", "Images/interfaces/flow/botleft.png"),
	
	UP(0, 0, "Images/interfaces/arrow_up.png", "Images/interfaces/flow/up.png"),
	DOWN(0, 0, "Images/interfaces/arrow_down.png", "Images/interfaces/flow/down.png"),
	NONE(0, 0, "Images/interfaces/arrow_none.png", "Images/interfaces/flow/none.png")
	;
	
	
	public static final String ALL_DIRECTIONS = NORTH.toString() + NORTHEAST.toString() + SOUTHEAST.toString() + SOUTH.toString() + SOUTHWEST.toString() + NORTHWEST.toString();
	
	private double deltax;
	private double deltay;
	private Image image;
	private Image arrowImage;
	private Direction(double deltax, double deltay, String arrowImageString, String imageString) {
		this.deltax = deltax;
		this.deltay = deltay;
		this.arrowImage = Utils.loadImage(arrowImageString);
		this.image = Utils.loadImage(imageString);
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
