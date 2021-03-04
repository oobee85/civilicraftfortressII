package ui.graphics.opengl;

import java.nio.*;
import java.nio.charset.*;

import com.jogamp.opengl.*;

import ui.graphics.opengl.maths.*;
import utils.*;

public class Shader {

	private GL3 gl;
	private String vertexFile, fragmentFile;
	private int vertexID, fragmentID, programID;

	public Shader(String vertexPath, String fragmentPath) {
		this.vertexFile = Utils.loadFileAsString(vertexPath);
		this.fragmentFile = Utils.loadFileAsString(fragmentPath);
	}
	
	public void create(GL3 gl) {
		this.gl = gl;
		IntBuffer statusBuffer = IntBuffer.allocate(1);
		
//		programID = GL20.glCreateProgram();
		programID = gl.glCreateProgram();
//		vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		vertexID = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		
//		GL20.glShaderSource(vertexID, vertexFile);
		gl.glShaderSource(vertexID, 1, new String[] {vertexFile}, null);
//		GL20.glCompileShader(vertexID);
		gl.glCompileShader(vertexID);
		
//		if(GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
		gl.glGetShaderiv(vertexID, GL2.GL_COMPILE_STATUS, statusBuffer);
		if(statusBuffer.get(0) == GL2.GL_FALSE) {
//			System.err.println("ERROR Vertex Shader: " + GL20.glGetShaderInfoLog(vertexID));
			gl.glGetShaderiv(vertexID, GL2.GL_INFO_LOG_LENGTH, statusBuffer);
			ByteBuffer errorBuffer = ByteBuffer.allocate(statusBuffer.get(0));
			System.out.println("error buffer size = " + errorBuffer.capacity());
			gl.glGetShaderInfoLog(vertexID, errorBuffer.capacity(), null, errorBuffer);
			System.err.println("ERROR Vertex Shader: " + StandardCharsets.UTF_8.decode(errorBuffer).toString());
			return;
		}

//		fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		fragmentID = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
//		GL20.glShaderSource(fragmentID, fragmentFile);
		gl.glShaderSource(fragmentID, 1, new String[] {fragmentFile}, null);
//		GL20.glCompileShader(fragmentID);
		gl.glCompileShader(fragmentID);

//		if(GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
		gl.glGetShaderiv(fragmentID, GL2.GL_COMPILE_STATUS, statusBuffer);
		if(statusBuffer.get(0) == GL2.GL_FALSE) {
//			System.err.println("ERROR Fragment Shader: " + GL20.glGetShaderInfoLog(fragmentID));
			gl.glGetShaderiv(fragmentID, GL2.GL_INFO_LOG_LENGTH, statusBuffer);
			ByteBuffer errorBuffer = ByteBuffer.allocate(statusBuffer.get(0));
			gl.glGetShaderInfoLog(fragmentID, errorBuffer.capacity(), null, errorBuffer);
			System.err.println("ERROR Fragment Shader: " + StandardCharsets.UTF_8.decode(errorBuffer).toString());
			return;
		}
		
//		GL20.glAttachShader(programID, vertexID);
		gl.glAttachShader(programID, vertexID);
//		GL20.glAttachShader(programID, fragmentID);
		gl.glAttachShader(programID, fragmentID);
		
//		GL20.glLinkProgram(programID);
		gl.glLinkProgram(programID);
//		if(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
		gl.glGetShaderiv(programID, GL2.GL_LINK_STATUS, statusBuffer);
		if(statusBuffer.get(0) == GL2.GL_FALSE) {
//			System.err.println("ERROR Program Linking: " + GL20.glGetProgramInfoLog(programID));
			gl.glGetShaderiv(programID, GL2.GL_INFO_LOG_LENGTH, statusBuffer);
			ByteBuffer errorBuffer = ByteBuffer.allocate(statusBuffer.get(0));
			gl.glGetShaderInfoLog(programID, errorBuffer.capacity(), null, errorBuffer);
			System.err.println("ERROR Program Linking: " + StandardCharsets.UTF_8.decode(errorBuffer).toString());
		}

//		GL20.glValidateProgram(programID);
		gl.glValidateProgram(programID);
//		if(GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == GL20.GL_FALSE) {
		gl.glGetShaderiv(programID, GL2.GL_VALIDATE_STATUS, statusBuffer);
		if(statusBuffer.get(0) == GL2.GL_FALSE) {
//			System.err.println("ERROR Validation: " + GL20.glGetProgramInfoLog(programID));
			gl.glGetShaderiv(programID, GL2.GL_INFO_LOG_LENGTH, statusBuffer);
			ByteBuffer errorBuffer = ByteBuffer.allocate(statusBuffer.get(0));
			gl.glGetShaderInfoLog(programID, errorBuffer.capacity(), null, errorBuffer);
			System.err.println("ERROR Validation: " + StandardCharsets.UTF_8.decode(errorBuffer).toString());
		}
	}
	
	public int getUniformLocation(String name) {
		return gl.glGetUniformLocation(programID, name);
	}
	
	public void setUniform(String name, float value) {
		gl.glUniform1f(getUniformLocation(name), value);
	}
	public void setUniform(String name, int value) {
		gl.glUniform1i(getUniformLocation(name), value);
	}
	public void setUniform(String name, boolean value) {
		gl.glUniform1i(getUniformLocation(name), value ? 1 : 0);
	}
//	public void setUniform(String name, Vector2f value) {
//		GL20.glUniform2f(getUniformLocation(name), value.getX(), value.getY());
//	}
	public void setUniform(String name, Vector3f value) {
		gl.glUniform3f(getUniformLocation(name), value.x, value.y, value.z);
	}
	public void setUniform(String name, Matrix4f value) {
		FloatBuffer matrix = FloatBuffer.allocate(Matrix4f.SIZE * Matrix4f.SIZE);
		matrix.put(value.getAll()).flip();
		gl.glUniformMatrix4fv(getUniformLocation(name), 1, true, matrix);
	}
	
	public void bind(GL3 gl) {
		gl.glUseProgram(programID);
	}
	
	public void unbind(GL3 gl) {
		gl.glUseProgram(0);
	}
	
	public void destroy(GL3 gl) {
		gl.glDetachShader(programID, vertexID);
		gl.glDetachShader(programID, fragmentID);
		gl.glDeleteShader(vertexID);
		gl.glDeleteShader(fragmentID);
		gl.glDeleteProgram(programID);
	}
	
}
