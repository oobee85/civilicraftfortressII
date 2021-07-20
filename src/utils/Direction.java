package utils;

import java.awt.Image;

public enum Direction {
	
	NORTH		(0, -1, "Images/interfaces/arrow_north.png"),
	NORTHEAST	(1, 0.5, "Images/interfaces/arrow_upright.png"),
	NORTHWEST	(-1, 0.5, "Images/interfaces/arrow_upleft.png"),
	
	SOUTH		(0, 1, "Images/interfaces/arrow_bot.png"),
	SOUTHEAST	(1, -0.5, "Images/interfaces/arrow_botright.png"),
	SOUTHWEST	(-1, -0.5, "Images/interfaces/arrow_botleft.png"),
	
	UP(0, 0, "Images/interfaces/arrow_up.png"),
	DOWN(0, 0, "Images/interfaces/arrow_down.png"),
	NONE(0, 0, "Images/interfaces/arrow_none.png")
	;
	
	
	public static final String ALL_DIRECTIONS = NORTH.toString() + NORTHEAST.toString() + SOUTHEAST.toString() + SOUTH.toString() + SOUTHWEST.toString() + NORTHWEST.toString();
	
	private double deltax;
	private double deltay;
	private Image image;
	private Direction(double deltax, double deltay, String string) {
		this.deltax = deltax;
		this.deltay = deltay;
		this.image = Utils.loadImage(string);
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
