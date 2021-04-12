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
	private Vertex[] vertices;
	private int[] indices;
	private int vao, pbo, ibo, cbo, nbo;

	public Mesh(Vertex[] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;
		allMeshes.add(this);
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
	
	private void generateNormals() {
		Vector3f[] normals = new Vector3f[vertices.length];
		for(int i = 0; i < normals.length; i++) {
			normals[i] = new Vector3f();
		}
		for(int i = 0; i < indices.length; i+=3) {
			int i0 = indices[i+0];
			int i1 = indices[i+1];
			int i2 = indices[i+2];
			Vector3f v0 = vertices[i0].getPosition();
			Vector3f v1 = vertices[i1].getPosition();
			Vector3f v2 = vertices[i2].getPosition();
			Vector3f one = v0.subtract(v1);
			Vector3f two = v0.subtract(v2);
			Vector3f normal = one.cross(two);
			normals[i0] = normals[i0].add(normal);
			normals[i1] = normals[i1].add(normal);
			normals[i2] = normals[i2].add(normal);
		}
		for(int i = 0; i < normals.length; i++) {
			vertices[i].setNormal(normals[i].normalize());
		}
	}
	
	public void create(GL3 gl) {
		generateNormals();
		this.gl = gl;
//		vao = GL30.glGenVertexArrays();
		IntBuffer vertexArray = IntBuffer.allocate(1);
		gl.glGenVertexArrays(1, vertexArray);
		vao = vertexArray.get(0);
		
//		GL30.glBindVertexArray(vao);
		gl.glBindVertexArray(vao);
		
//		FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
		FloatBuffer positionBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] positionData = new float[vertices.length * 3];
		for(int i = 0; i < vertices.length; i++) {
			positionData[i*3    ] = vertices[i].getPosition().x;
			positionData[i*3 + 1] = vertices[i].getPosition().y;
			positionData[i*3 + 2] = vertices[i].getPosition().z;
		}
		positionBuffer.put(positionData).flip();
		pbo = storeData(positionBuffer, 0, 3);

//		FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
		FloatBuffer colorBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] colorData = new float[vertices.length * 3];
		for(int i = 0; i < vertices.length; i++) {
			colorData[i*3    ] = vertices[i].getColor().x;
			colorData[i*3 + 1] = vertices[i].getColor().y;
			colorData[i*3 + 2] = vertices[i].getColor().z;
		}
//		colorBuffer.put(colorData);
		colorBuffer.put(colorData).flip();
		cbo = storeData(colorBuffer, 1, 3);

		FloatBuffer normalBuffer = FloatBuffer.allocate(vertices.length * 3);
		float[] normalData = new float[vertices.length * 3];
		for(int i = 0; i < vertices.length; i++) {
			normalData[i*3    ] = vertices[i].getNormal().x;
			normalData[i*3 + 1] = vertices[i].getNormal().y;
			normalData[i*3 + 2] = vertices[i].getNormal().z;
		}
		normalBuffer.put(normalData).flip();
		nbo = storeData(normalBuffer, 2, 3);
		

		FloatBuffer textureCoordBuffer = FloatBuffer.allocate(vertices.length * 2);
		float[] textureCoordData = new float[vertices.length * 2];
		for(int i = 0; i < vertices.length; i++) {
			textureCoordData[i*2    ] = vertices[i].getTextureCoord().x;
			textureCoordData[i*2 + 1] = vertices[i].getTextureCoord().y;
		}
		textureCoordBuffer.put(textureCoordData).flip();
		nbo = storeData(textureCoordBuffer, 3, 2);
		
		
//		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
		IntBuffer indicesBuffer = IntBuffer.allocate(indices.length);
		indicesBuffer.put(indices).flip();
		
//		ibo = GL15.glGenBuffers();
		IntBuffer temp = IntBuffer.allocate(1);
		gl.glGenBuffers(1, temp);
		ibo = temp.get(0);
		
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, ibo);
//		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, 4 * indicesBuffer.capacity(), indicesBuffer, GL2.GL_STATIC_DRAW);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private int storeData(FloatBuffer buffer, int index, int size) {
//		int bufferID = GL15.glGenBuffers();
		IntBuffer temp = IntBuffer.allocate(1);
		gl.glGenBuffers(1, temp);
		int bufferID = temp.get(0);
		
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferID);
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4 * buffer.capacity(), buffer, GL2.GL_STATIC_DRAW);
//		GL20.glVertexAttribPointer(index, size, GL11.GL_FLOAT, false, 0, 0);
		gl.glVertexAttribPointer(index, size, GL2.GL_FLOAT, false, 0, 0);
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
		return bufferID;
	}
	
	public void destroy() {
//		gl.glDeleteBuffers(pbo);
//		gl.glDeleteBuffers(cbo);
//		gl.glDeleteBuffers(ibo);
		IntBuffer buffers = IntBuffer.wrap(new int[]{pbo, cbo, ibo});
		gl.glDeleteBuffers(buffers.capacity(), buffers);
//		gl.glDeleteVertexArrays(vao);
		IntBuffer vaoBuffer = IntBuffer.wrap(new int[]{vao});
		gl.glDeleteVertexArrays(1, vaoBuffer);
	}

	public Vertex[] getVertices() {
		return vertices;
	}

	public int[] getIndices() {
		return indices;
	}

	public int getVAO() {
		return vao;
	}

	public int getPBO() {
		return pbo;
	}

	public int getIBO() {
		return ibo;
	}
	
	public int getCBO() {
		return cbo;
	}
	
	public int getNBO() {
		return nbo;
	}
	
}
