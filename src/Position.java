
	
public class Position {
	
	public double x, y;

	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public int getIntX() {
		return (int)x;
	}
	public int getIntY() {
		return (int)y;
	}
	
	public Position add(Position other) {
		return new Position(x + other.x, y + other.y);
	}
	public Position subtract(Position other) {
		return new Position(x - other.x, y - other.y);
	}
	
	
	
	
	public Position add(double value) {
		return new Position(x + value, y + value);
	}
	
	public Position divide(double value) {
		return new Position(x / value, y / value);
	}
	public Position multiply(double value) {
		return new Position(x * value, y * value);
	}
	
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
