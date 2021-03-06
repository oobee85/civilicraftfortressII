package ui.graphics.opengl;

import ui.graphics.opengl.maths.*;

public class Vertex {

	private Vector3f position;
	private Vector3f color;
	private Vector3f normal;

	public Vertex(Vector3f position, Vector3f color) {
		this(position, color, null);
	}
	
	public Vertex(Vector3f position, Vector3f color, Vector3f normal) {
		this.position = position;
		this.color = color;
		this.normal = normal;
	}
	
	public void setNormal(Vector3f normal) {
		this.normal = normal;
	}
	
	public Vector3f getNormal() {
		return normal;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getColor() {
		return color;
	}
}
