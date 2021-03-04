package ui.graphics.opengl;

import java.awt.*;
import java.awt.image.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;

import game.*;
import ui.graphics.*;
import ui.graphics.opengl.maths.*;
import ui.view.GameView.*;
import utils.*;

public class GLDrawer implements Drawer, GLEventListener {
	private long startTime = System.currentTimeMillis();
	private final GLCanvas glcanvas;
	private Shader shader;
	private Matrix4f projection;
	public Mesh mesh = new Mesh(new Vertex[] {
			new Vertex(new Vector3f(-0.5f,  0.5f, -6f), new Vector3f(0, 0, 0)),
			new Vertex(new Vector3f(-0.5f, -0.5f, -6f), new Vector3f(1, 1, 0)),
			new Vertex(new Vector3f( 0.5f, -0.5f, -6f), new Vector3f(1, 0, 1)),
			new Vertex(new Vector3f( 0.5f,  0.5f, -6f), new Vector3f(0, 1, 1))
		}, new int[] {
			0, 1, 2,
			0, 3, 2
		});
	
	private final Game game;
	private final GameViewState state;
	
	public GLDrawer(Game game, GameViewState state) {
		this.game = game;
		this.state = state;
		// getting the capabilities object of GL2 profile
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);

		// The canvas
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener(this);
		glcanvas.setSize(640, 640);
	}
	
	public Component getDrawingCanvas() {
		return glcanvas;
	}

	public void updateTerrainImages() {
	}

	public BufferedImage getImageToDrawMinimap() {
		return new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
	}
	public Position[] getVisibleTileBounds() {
		return null;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glClearDepthf(10.0f);
		updateBackgroundColor(gl, Color.black);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		

		shader = new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl");
		shader.create(gl);
		mesh.create(gl);
		projection = Matrix4f.projection((float)glcanvas.getWidth()/glcanvas.getHeight(), 70f, 0.1f, 1000);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		updateBackgroundColor(gl, game.getBackgroundColor());
		gl.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_COLOR_BUFFER_BIT);

		shader.bind(gl);
		renderMesh(gl, mesh);
		shader.unbind(gl);
		
		gl.glFlush();
	}
	
	public void renderMesh(GL3 gl, Mesh mesh) {
//		GL30.glBindVertexArray(mesh.getVAO());
		gl.glBindVertexArray(mesh.getVAO());
//		GL30.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		long deltaTime = System.currentTimeMillis() - startTime;
		Vector3f off = new Vector3f(
				(float)(0.5*Math.sin(Math.toRadians((double)deltaTime/13))),
				(float)(0.5*Math.cos(Math.toRadians((double)deltaTime/17))),
				(float)(5*Math.sin(Math.toRadians((double)deltaTime/19)))
				);
		shader.setUniform("off", off);
		shader.setUniform("projection", projection);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		gl.glDrawElements(GL2.GL_TRIANGLES, mesh.getIndices().length, GL2.GL_UNSIGNED_INT, 0);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
//		GL30.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
//		GL30.glBindVertexArray(0);
		gl.glBindVertexArray(0);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
	}
	
	private void updateBackgroundColor(GL3 gl, Color background) {
		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1.0f);
	}
}
