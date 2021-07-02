package ui.graphics.opengl;

import ui.graphics.opengl.maths.*;

public class Vertex {

	private Vector3f position;
	private Vector3f color;
	private Vector2f textureCoord;

	public Vertex(Vertex other) {
		this(new Vector3f(other.position), 
				new Vector3f(other.getColor()), 
				new Vector2f(other.getTextureCoord()));
	}
	
	public Vertex(Vector3f position, Vector3f color) {
		this(position, color, null);
	}
	
	public Vertex(Vector3f position, Vector3f color, Vector2f textureCoord) {
		this.position = position;
		this.color = color;
		this.textureCoord = textureCoord;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public Vector2f getTextureCoord() {
		return textureCoord;
	}
}
