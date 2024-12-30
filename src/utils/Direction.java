package utils;

import java.awt.Image;

public enum Direction {
	
	NORTH		(0, -1, 0x2, "Images/interfaces/flow/arrow_north.png", "Images/interfaces/flow/up2.gif"),
	NORTHEAST	(1, -0.5, 0x4, "Images/interfaces/flow/arrow_upright.png", "Images/interfaces/flow/upright6.gif"),
	NORTHWEST	(-1, -0.5, 0x40, "Images/interfaces/flow/arrow_upleft.png", "Images/interfaces/flow/upleft3.gif"),
	
	SOUTH		(0, 1, 0x10, "Images/interfaces/flow/arrow_bot.png", "Images/interfaces/flow/down2.gif"),
	SOUTHEAST	(1, 0.5, 0x8, "Images/interfaces/flow/arrow_botright.png", "Images/interfaces/flow/botright3.gif"),
	SOUTHWEST	(-1, 0.5, 0x20, "Images/interfaces/flow/arrow_botleft.png", "Images/interfaces/flow/botleft2.gif"),
	
	UP(0, 0, 0x0, "Images/interfaces/flow/arrow_up.png", "Images/interfaces/flow/up.gif"),
	DOWN(0, 0, 0x0, "Images/interfaces/flow/arrow_down.png", "Images/interfaces/flow/down.gif"),
	NONE(0, 0, 0x1, "Images/interfaces/flow/arrow_none.png", "Images/interfaces/flow/none.png")
	
	;
	
	
	public static final String ALL_DIRECTIONS = NORTH.toString() + NORTHEAST.toString() + SOUTHEAST.toString() + SOUTH.toString() + SOUTHWEST.toString() + NORTHWEST.toString();
	
	private double deltax;
	private double deltay;
	private Image image;
	private Image arrowImage;
	public int tilingBit;
	private Direction(double deltax, double deltay, int tilingBit, String arrowImageString, String imageString) {
		this.deltax = deltax;
		this.deltay = deltay;
		this.arrowImage = Utils.loadImage(arrowImageString);
		this.image = Utils.loadImage(imageString);
//		this.image = Utils.loadImage("Images/interfaces/flow/upright6.gif");
		this.tilingBit = tilingBit;
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
		else {
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
		return null;
	}
}
