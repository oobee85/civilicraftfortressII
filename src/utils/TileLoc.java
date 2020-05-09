package utils;

public class TileLoc {
	public final int x, y;
	public TileLoc(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString() {
		return String.format("(%d, %d)", x, y);
	}
	public TileLoc add(TileLoc other) {
		return new TileLoc(x + other.x, y + other.y);
	}
	public static void main(String[] args ) {
		TileLoc a = new TileLoc(1, 2);
		TileLoc b = new TileLoc(3, 4);
		TileLoc c = a + b;
		System.out.println(a + " + " + b + " = " + c);
	}
}
