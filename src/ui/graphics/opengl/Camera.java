package ui.graphics.opengl;

import ui.graphics.opengl.maths.*;

public class Camera {
	
	private Vector3f up = new Vector3f(0, 0, 1);
	private Vector3f forwardFlat = new Vector3f();
	private Vector3f forward = new Vector3f();
	private Vector3f side = new Vector3f();
	private float pitch = 0;
	private float theta = 0;
	
	private Vector3f position;
	private static final Matrix4f prerotate = Matrix4f.rotate(-90, new Vector3f(1, 0, 0));
	public static final Matrix4f prerotateInv = Matrix4f.rotate(90, new Vector3f(1, 0, 0));
	
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
	public void shiftView(float dx, float dy) {
		position = position.add(this.forwardFlat.multiply(-dy));
		position = position.add(this.side.multiply(dx));
	}
	public void rotate(float dx, float dy) {
		theta += dx;
		pitch += dy;
		updateDirectionVectors();
	}
	
	private void updateDirectionVectors() {
		theta = theta % 360;
		pitch = ((pitch + 180) % 360) - 180;
		forwardFlat.set((float)Math.sin(Math.toRadians(theta)), (float)Math.cos(Math.toRadians(theta)), 0);
		forwardFlat = forwardFlat.normalize();
		side = forwardFlat.cross(up).normalize();
		forward = Matrix4f.rotate(pitch, side).multiply(forwardFlat, 1f).normalize();
	}
	
	public void zoom(float distance) {
		this.position = this.position.add(up.multiply(-distance));
	}
	public Vector3f getPosition() {
		return position;
	}

	private Matrix4f translation;
	private Matrix4f rotation;
	private Matrix4f view;
	public void updateMatrices() {
		translation = Matrix4f.translate(position.multiply(-1));
		rotation = prerotate.multiply(Matrix4f.rotate(theta, up)).multiply(Matrix4f.rotate(-pitch, side));
		view = rotation.multiply(translation);
	}
	private Matrix4f getTranslationMatrix() {
		return translation;
	}
	public Matrix4f getRotationMatrix() {
		return rotation;
	}
	public Matrix4f getView() {
		return view;
	}
}
