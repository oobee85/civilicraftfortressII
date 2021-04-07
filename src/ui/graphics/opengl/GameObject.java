package ui.graphics.opengl;

import com.jogamp.opengl.*;

import ui.graphics.opengl.maths.*;

public class GameObject {

	private Vector3f position;
	private Matrix4f rotation;
	private Vector3f scale;
	
	private Mesh mesh;

	public GameObject(Mesh mesh) {
		this.position = new Vector3f();
		this.rotation = Matrix4f.identity();
		this.scale = new Vector3f(1, 1, 1);
		this.mesh = mesh;
	}
	
	public void rotate(Matrix4f r) {
		rotation = r.multiply(rotation);
	}

	public void render(GL3 gl, Shader shader) {
		render(gl, shader, position);
	}
	public void render(GL3 gl, Shader shader, Vector3f customPosition) {
//		GL30.glBindVertexArray(mesh.getVAO());
		gl.glBindVertexArray(mesh.getVAO());
//		GL30.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnableVertexAttribArray(2);
		shader.setUniform("model", MeshUtils.getModelMatrix(customPosition, rotation, scale));
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		gl.glDrawElements(GL2.GL_TRIANGLES, mesh.getIndices().length, GL2.GL_UNSIGNED_INT, 0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
//		GL30.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDisableVertexAttribArray(2);
//		GL30.glBindVertexArray(0);
		gl.glBindVertexArray(0);
	}
	
	public Matrix4f getModelMatrix() {
//		return Matrix4f.multiply(Matrix4f.translate(position), rotation).multiply(Matrix4f.scale(scale));
		return Matrix4f.multiply(Matrix4f.translate(position), Matrix4f.multiply(rotation, Matrix4f.scale(scale)));
	}
}
