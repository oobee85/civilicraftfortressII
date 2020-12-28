package utils;

public enum Direction {
	NORTH(0, -1),
	NORTHEAST(1, 0.5),
	SOUTHEAST(1, -0.5),
	SOUTH(0, 1),
	SOUTHWEST(-1, -0.5),
	NORTHWEST(-1, 0.5)
	;
	public static final String ALL_DIRECTIONS = NORTH.toString() + NORTHEAST.toString() + SOUTHEAST.toString() + SOUTH.toString() + SOUTHWEST.toString() + NORTHWEST.toString();
	
	private double deltax;
	private double deltay;
	private Direction(double deltax, double deltay) {
		this.deltax = deltax;
		this.deltay = deltay;
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
