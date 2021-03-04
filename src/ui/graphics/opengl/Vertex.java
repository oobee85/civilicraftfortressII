package ui.graphics.opengl;

import ui.graphics.opengl.maths.*;

public class Vertex {

	private Vector3f position;
	private Vector3f color;

	public Vertex(Vector3f position, Vector3f color) {
		this.position = position;
		this.color = color;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getColor() {
		return color;
	}
}
