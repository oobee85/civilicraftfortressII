package ui.opengl.maths;

public class Vector2f {
	
	private float x, y;

	public Vector2f() {
		this(0, 0);
	}
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}
	public Vector2f(Vector2f other) {
		this.x = other.x;
		this.y = other.y;
	}

	public Vector2f add(Vector2f other) {
		return new Vector2f(x + other.x, y + other.y);
	}
	public Vector2f subtract(Vector2f other) {
		return new Vector2f(x - other.x, y - other.y);
	}
	public Vector2f multiply(Vector2f other) {
		return new Vector2f(x * other.x, y * other.y);
	}
	public float dot(Vector2f other) {
		return x * other.x + y * other.y;
	}
	public float magnitude() {
		return (float)Math.sqrt(x*x + y*y);
	}
	public Vector2f normalize() {
		float mag = magnitude();
		return new Vector2f(x / mag, y / mag);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector2f other = (Vector2f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "<" + x + "," + y + ">";
	}
}
