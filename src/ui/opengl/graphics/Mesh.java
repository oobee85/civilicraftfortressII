package ui.opengl.graphics;

import java.nio.*;

import com.jogamp.opengl.*;

public class Mesh {
	
	private GL3 gl;
	private Vertex[] vertices;
	private int[] indices;
	private int vao, pbo, ibo, cbo;

	public Mesh(Vertex[] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;
	}
	
	public void create(GL3 gl) {
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
	
}
