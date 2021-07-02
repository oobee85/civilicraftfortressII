package ui.graphics.opengl;

import java.nio.*;
import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

import ui.graphics.opengl.maths.*;

public class Mesh {
	public static final LinkedList<Mesh> allMeshes = new LinkedList<>();
	public static void initAllMeshes(GL3 gl) {
		for(Mesh mesh : allMeshes) {
			mesh.create(gl);
		}
	}
	
	
	private GL3 gl;
	private int[] indices;
	private int vao, pbo, ibo, cbo, nbo, tbo;
	private FloatBuffer positionBuffer;
	private FloatBuffer normalBuffer;
	private FloatBuffer colorBuffer;
	private FloatBuffer textureCoordBuffer;

	public Mesh(Vertex[] vertices, int[] indices) {
		this.indices = indices;
		allMeshes.add(this);

		initializeDataBuffers(vertices, indices);
	}
	public void setVertexZ(int vertexIndex, float z) {
		positionBuffer.array()[vertexIndex*3 + 2] = z;
	}
	private void updateNormal(int v0, int v1, int v2) {
		float[] arr = positionBuffer.array();
		float onex = arr[v0+0] - arr[v1+0];
		float oney = arr[v0+1] - arr[v1+1];
		float onez = arr[v0+2] - arr[v1+2];
		float twox = arr[v0+0] - arr[v2+0];
		float twoy = arr[v0+1] - arr[v2+1];
		float twoz = arr[v0+2] - arr[v2+2];
		
		float crossx = oney*twoz - onez*twoy;
		float crossy = -(onex*twoz - onez*twox);
		float crossz = onex*twoy - oney*twox;

		float[] normals = normalBuffer.array();
		normals[v0 + 0] += crossx;
		normals[v0 + 1] += crossy;
		normals[v0 + 2] += crossz;
		
		normals[v1 + 0] += crossx;
		normals[v1 + 1] += crossy;
		normals[v1 + 2] += crossz;
		
		normals[v2 + 0] += crossx;
		normals[v2 + 1] += crossy;
		normals[v2 + 2] += crossz;
	}
	public void updateNormals() {
		float[] arr = normalBuffer.array();
		for(int i = 0; i < arr.length; i++) {
			arr[i] = 0;
		}
		for(int triangle = 0; triangle < indices.length; triangle+=3) {
			int i0 = indices[triangle+0];
			int i1 = indices[triangle+1];
			int i2 = indices[triangle+2];
			updateNormal(i0*3, i1*3, i2*3);
		}
		for(int vertex = 0; vertex < normalBuffer.array().length; vertex+=3) {
			float sum = arr[vertex] + arr[vertex+1] + arr[vertex+2];
			arr[vertex] /= sum;
			arr[vertex+1] /= sum;
			arr[vertex+2] /= sum;
		}
	}
	
	public void renderSkybox(GL3 gl, Shader shader, Texture texture) {
//		GL30.glBindVertexArray(mesh.getVAO());
		gl.glBindVertexArray(this.getVAO());
//		GL30.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnableVertexAttribArray(2);
		gl.glEnableVertexAttribArray(3);

		gl.glActiveTexture(GL3.GL_TEXTURE0);
		texture.enable(gl);
		texture.bind(gl); 
		shader.setUniform("textureSampler", 0);
//		shader.setUniform("model", Matrix4f.getModelMatrix(position, rotation, scale));
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, this.getIBO());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		gl.glDrawElements(GL2.GL_TRIANGLES, this.getIndices().length, GL2.GL_UNSIGNED_INT, 0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

		gl.glBindTexture(texture.getTarget(), 0);
		texture.disable(gl);
		
//		GL30.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDisableVertexAttribArray(2);
		gl.glDisableVertexAttribArray(3);
//		GL30.glBindVertexArray(0);
		gl.glBindVertexArray(0);
	}
	public void render(GL3 gl, Shader shader, Texture texture, Vector3f position, Matrix4f rotation, Vector3f scale) {
//		GL30.glBindVertexArray(mesh.getVAO());
		gl.glBindVertexArray(this.getVAO());
//		GL30.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnableVertexAttribArray(2);
		gl.glEnableVertexAttribArray(3);

		gl.glActiveTexture(GL3.GL_TEXTURE0);
		texture.enable(gl);
		texture.bind(gl); 
		shader.setUniform("textureSampler", 0);
		shader.setUniform("useTexture", 1f);
		
		shader.setUniform("model", Matrix4f.getModelMatrix(position, rotation, scale));
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, this.getIBO());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		gl.glDrawElements(GL2.GL_TRIANGLES, this.getIndices().length, GL2.GL_UNSIGNED_INT, 0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

		gl.glBindTexture(texture.getTarget(), 0);
		texture.disable(gl);
		
//		GL30.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDisableVertexAttribArray(2);
		gl.glDisableVertexAttribArray(3);
//		GL30.glBindVertexArray(0);
		gl.glBindVertexArray(0);
	}
	
	private void initializeDataBuffers(Vertex[] vertices, int[] indices) {

		positionBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] positionData = new float[vertices.length * 3];
		for(int i = 0; i < vertices.length; i++) {
			positionData[i*3    ] = vertices[i].getPosition().x;
			positionData[i*3 + 1] = vertices[i].getPosition().y;
			positionData[i*3 + 2] = vertices[i].getPosition().z;
		}
		positionBuffer.put(positionData).flip();

		colorBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] colorData = new float[vertices.length * 3];
		for(int i = 0; i < vertices.length; i++) {
			colorData[i*3    ] = vertices[i].getColor().x;
			colorData[i*3 + 1] = vertices[i].getColor().y;
			colorData[i*3 + 2] = vertices[i].getColor().z;
		}
		colorBuffer.put(colorData).flip();
		
		normalBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] normalData = new float[vertices.length * 3];
		normalBuffer.put(normalData).flip();
		updateNormals();
		
		textureCoordBuffer = FloatBuffer.allocate(vertices.length * 2);
		float[] textureCoordData = new float[vertices.length * 2];
		for(int i = 0; i < vertices.length; i++) {
			textureCoordData[i*2    ] = vertices[i].getTextureCoord().x;
			textureCoordData[i*2 + 1] = vertices[i].getTextureCoord().y;
		}
		textureCoordBuffer.put(textureCoordData).flip();
	}
	
	public void updatePositions() {
		gl.glBindVertexArray(vao);
		updateData(positionBuffer, 0, 3, pbo);
		updateData(normalBuffer, 2, 3, nbo);
	}
	
	public void create(GL3 gl) {
		this.gl = gl;
		
		
		IntBuffer vertexArray = IntBuffer.allocate(1);
		gl.glGenVertexArrays(1, vertexArray);
		vao = vertexArray.get(0);
		
		gl.glBindVertexArray(vao);
		
		pbo = createDataBuffer();
		cbo = createDataBuffer();
		nbo = createDataBuffer();
		tbo = createDataBuffer();


		updateData(positionBuffer, 0, 3, pbo);
		updateData(colorBuffer, 1, 3, cbo);
		updateData(normalBuffer, 2, 3, nbo);
		updateData(textureCoordBuffer, 3, 2, tbo);
		
		
		IntBuffer indicesBuffer = IntBuffer.allocate(indices.length);
		indicesBuffer.put(indices).flip();
		
		IntBuffer temp = IntBuffer.allocate(1);
		gl.glGenBuffers(1, temp);
		ibo = temp.get(0);
		
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ibo);
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, 4 * indicesBuffer.capacity(), indicesBuffer, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private void updateData(FloatBuffer buffer, int index, int size, int bufferID) {
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferID);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4 * buffer.capacity(), buffer, GL2.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(index, size, GL2.GL_FLOAT, false, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}
	private int createDataBuffer() {
		IntBuffer temp = IntBuffer.allocate(1);
		gl.glGenBuffers(1, temp);
		int bufferID = temp.get(0);
		return bufferID;
	}
	
	public void destroy() {
		IntBuffer buffers = IntBuffer.wrap(new int[]{pbo, cbo, ibo});
		gl.glDeleteBuffers(buffers.capacity(), buffers);
		IntBuffer vaoBuffer = IntBuffer.wrap(new int[]{vao});
		gl.glDeleteVertexArrays(1, vaoBuffer);
	}
	
	public void normalize(boolean zeroZ) {
		float[] verticesArr = positionBuffer.array();
		Vector3f min = new Vector3f(verticesArr[0], verticesArr[1], verticesArr[2]);
		Vector3f max = new Vector3f(min);
		for (int vertex = 0; vertex < verticesArr.length; vertex += 3) {
			min.x = Math.min(verticesArr[vertex + 0], min.x);
			min.y = Math.min(verticesArr[vertex + 1], min.y);
			min.z = Math.min(verticesArr[vertex + 2], min.z);
			max.x = Math.max(verticesArr[vertex + 0], max.x);
			max.y = Math.max(verticesArr[vertex + 1], max.y);
			max.z = Math.max(verticesArr[vertex + 2], max.z);
		}

		Vector3f range = max.subtract(min);
		float maximumRange = Math.max(range.y, range.x);
		float scale = 1f / maximumRange;
		Vector3f offset = new Vector3f(-range.x * scale * 0.5f, -range.y * scale * 0.5f, 0);
		if (zeroZ) {
			offset.z = -range.z * scale * 0.5f;
		}
		for (int vertex = 0; vertex < verticesArr.length; vertex += 3) {
			verticesArr[vertex + 0] = (verticesArr[vertex + 0] - min.x) * scale + offset.x;
			verticesArr[vertex + 1] = (verticesArr[vertex + 1] - min.y) * scale + offset.y;
			verticesArr[vertex + 2] = (verticesArr[vertex + 2] - min.z) * scale + offset.z;
		}
	}

	public int[] getIndices() {
		return indices;
	}

	public int getVAO() {
		return vao;
	}

	public int getIBO() {
		return ibo;
	}
}
