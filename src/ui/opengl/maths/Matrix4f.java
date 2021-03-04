package ui.opengl.maths;

import java.util.*;

public class Matrix4f {

	public static final int SIZE = 4;
	
	private float[] elements = new float[SIZE*SIZE];
	
	public static Matrix4f identity() {
		Matrix4f matrix = new Matrix4f();
		matrix.set(0, 0, 1);
		matrix.set(1, 1, 1);
		matrix.set(2, 2, 1);
		matrix.set(3, 3, 1);
		return matrix;
	}
	
	public static Matrix4f translate(Vector3f translate) {
		Matrix4f matrix = identity();
		matrix.set(0, 3, translate.x);
		matrix.set(1, 3, translate.y);
		matrix.set(2, 3, translate.z);
		return matrix;
	}
	
	public static Matrix4f rotate(float angle, Vector3f axis) {
		Matrix4f matrix = identity();
		
		float cos = (float)Math.cos(Math.toRadians(angle));
		float sin = (float)Math.sin(Math.toRadians(angle));
		float minuscos = 1 - cos;
		
		float x = axis.x;
		float y = axis.y;
		float z = axis.z;
		
		matrix.set(0, 0, cos + x*x * minuscos);
		matrix.set(0, 1, x * y * minuscos - z * sin);
		matrix.set(0, 2, x * z * minuscos + y * sin);
		
		matrix.set(1, 0, y * x * minuscos + z * sin);
		matrix.set(1, 1, cos + y*y * minuscos);
		matrix.set(1, 2, y * z * minuscos - x * sin);
		
		matrix.set(2, 0, z * x * minuscos - y * sin);
		matrix.set(2, 1, z * y * minuscos + x * sin);
		matrix.set(2, 2, cos + z*z * minuscos);
		
		return matrix;
	}
	
	public static Matrix4f scale(Vector3f scale) {
		Matrix4f matrix = identity();
		matrix.set(0, 0, scale.x);
		matrix.set(1, 1, scale.y);
		matrix.set(2, 2, scale.z);
		return matrix;
	}
	
	public static Matrix4f transform(Vector3f position, Vector3f rotation, Vector3f scale) {
		Matrix4f translate = Matrix4f.translate(position);
		Matrix4f rotateX = Matrix4f.rotate(rotation.x, new Vector3f(1, 0, 0));
		Matrix4f rotateY = Matrix4f.rotate(rotation.y, new Vector3f(0, 1, 0));
		Matrix4f rotateZ = Matrix4f.rotate(rotation.z, new Vector3f(0, 0, 1));
		Matrix4f scaleMat = Matrix4f.scale(scale);
		
		Matrix4f fullRotation = rotateX.multiply(rotateY).multiply(rotateZ);
		return translate.multiply(fullRotation).multiply(scaleMat);
	}
	
	public static Matrix4f projection(float aspect, float fov, float near, float far) {
		Matrix4f result = new Matrix4f();
		float tanFOV = (float)Math.toRadians(fov);
		result.set(0, 0, (float) (1 / (aspect * tanFOV/2)));
		result.set(1, 1, (float) (1 / (tanFOV/2)));
		result.set(2, 2, -1 * (far + near) / (far - near));
		
		result.set(2, 3, -2 * far * near / (far - near));
		result.set(3, 2, -1f);
		result.set(3, 3, 0);
		return result;
	}
	
	public Matrix4f multiply(Matrix4f right) {
		Matrix4f matrix = Matrix4f.identity();
		
		for(int r = 0; r < Matrix4f.SIZE; r++) {
			for(int c = 0; c < Matrix4f.SIZE; c++) {
				matrix.set(r, c, 
						this.get(r, 0) * right.get(0, c) + 
						this.get(r, 1) * right.get(1, c) + 
						this.get(r, 2) * right.get(2, c) + 
						this.get(r, 3) * right.get(3, c));
			}
		}
		return matrix;
	}
	public Vector3f multiply(Vector3f right, float w) {
		Vector3f result = new Vector3f();
		result.set(
					this.get(0, 0) * right.x + 
					this.get(0, 1) * right.y + 
					this.get(0, 2) * right.z + 
					this.get(0, 3) * w,
					this.get(1, 0) * right.x + 
					this.get(1, 1) * right.y + 
					this.get(1, 2) * right.z + 
					this.get(1, 3) * w,
					this.get(2, 0) * right.x + 
					this.get(2, 1) * right.y + 
					this.get(2, 2) * right.z + 
					this.get(2, 3) * w
				);
		return result;
	}
	public static Matrix4f multiply(Matrix4f left, Matrix4f right) {
		return left.multiply(right);
	}
	
	public float get(int row, int col) {
		return elements[row*SIZE + col];
	}
	
	public void set(int row, int col, float value) {
		elements[row*SIZE + col] = value;
	}
	
	public float[] getAll() {
		return elements;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(elements);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix4f other = (Matrix4f) obj;
		if (!Arrays.equals(elements, other.elements))
			return false;
		return true;
	}
}
