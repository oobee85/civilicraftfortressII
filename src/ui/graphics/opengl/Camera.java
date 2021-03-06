package ui.graphics.opengl;

import ui.graphics.opengl.maths.*;

public class Camera {
	
	private Vector3f up = new Vector3f(0, 1f, 0);
	private Vector3f forwardFlat = new Vector3f(0, 0, -1f);
	private Vector3f forward = new Vector3f(0, 0, -1f);
	private Vector3f side = new Vector3f(-1f, 0, 0);
	private float pitch = 0;
	private float theta = 0;
	
	private Vector3f position;

	public Camera(Vector3f position, float theta, float pitch) {
		this.position = position;
		this.theta = theta;
		this.pitch = pitch;
		updateDirectionVectors();
	}
	
	public void set(Vector3f position, float theta, float pitch) {
		this.position = position;
		this.theta = theta;
		this.pitch = pitch;
		updateDirectionVectors();
	}
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	private void updateDirectionVectors() {
		forwardFlat.set((float)Math.sin(Math.toRadians(theta)), 0, -(float)Math.cos(Math.toRadians(theta)));
		side = forwardFlat.cross(up);
		forward = Matrix4f.rotate(pitch, side).multiply(forwardFlat, 1f);
	}

	public Vector3f getPosition() {
		return position;
	}

	private Matrix4f getTranslationMatrix() {
		return Matrix4f.translate(position.multiply(-1));
	}
	private Matrix4f getRotationMatrix() {
		return Matrix4f.rotate(theta, up).multiply(Matrix4f.rotate(-pitch, side));
	}
	public Matrix4f getView() {
		return getRotationMatrix().multiply(getTranslationMatrix());
	}
	
	
}
