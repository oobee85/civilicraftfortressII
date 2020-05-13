package utils;

public enum Direction {
	NORTH(new TileLoc(0, -1)),
	EAST(new TileLoc(1, 0)),
	SOUTH(new TileLoc(0, 1)),
	WEST(new TileLoc(-1, 0))
	;
	private TileLoc delta;
	private Direction(TileLoc delta) {
		this.delta = delta;
	}
	
	public TileLoc getDelta() {
		return delta;
	}
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public static Direction getDirection(TileLoc from, TileLoc to) {
		if(to.y - from.y == 1) {
			return SOUTH;
		}
		else if(from.y - to.y == 1) {
			return NORTH;
		}
		else if(to.x - from.x == 1) {
			return EAST;
		}
		else if(from.x - to.x == 1) {
			return WEST;
		}
		return null;
	}
}
