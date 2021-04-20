package ui.graphics.opengl.maths;

public class Vector3f {
	public float x, y, z;

	public Vector3f() {
		this(0, 0, 0);
	}
	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector3f(Vector3f other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public void set(float x, float y, float z) {
		this.x = x; this.y = y; this.z = z;
	}
	public void set(Vector3f other) {
		this.x = other.x; this.y = other.y; this.z = other.z;
	}
	
	public Vector3f add(Vector3f other) {
		return new Vector3f(x + other.x, y + other.y, z + other.z);
	}
	public Vector3f subtract(Vector3f other) {
		return new Vector3f(x - other.x, y - other.y, z - other.z);
	}
	public Vector3f multiply(Vector3f other) {
		return new Vector3f(x * other.x, y * other.y, z * other.z);
	}
	public Vector3f multiply(float scalar) {
		return new Vector3f(x * scalar, y * scalar, z * scalar);
	}
	/** Element-wise division */
	public Vector3f divide(Vector3f other) {
		return new Vector3f(x / other.x, y / other.y, z / other.z);
	}
	public float dot(Vector3f other) {
		return x * other.x + y * other.y + z * other.z;
	}
	public float magnitude() {
		return (float)Math.sqrt(x*x + y*y + z*z);
	}
	public Vector3f normalize() {
		float mag = magnitude();
		return new Vector3f(x / mag, y / mag, z / mag);
	}
	public Vector3f cross(Vector3f other) {
		return new Vector3f(
				this.y*other.z - this.z*other.y,
				-(this.x*other.z - this.z*other.x),
				this.x*other.y - this.y*other.x);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
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
		Vector3f other = (Vector3f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "<" + x + "," + y + "," + z + ">";
	}
	
	public static Vector3f xAxis() {
		return new Vector3f(1, 0, 0);
	}
	public static Vector3f yAxis() {
		return new Vector3f(0, 1, 0);
	}
	public static Vector3f zAxis() {
		return new Vector3f(0, 0, 1);
	}
}
