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
import world.*;

public class GLDrawer implements Drawer, GLEventListener {
	private final GLCanvas glcanvas;
	private Shader shader;
	private Matrix4f projection;
	
	private Vector3f sunDirection = new Vector3f();
	private Vector3f sunColor = new Vector3f();
	public Mesh mesh = null;
	
	/*= new Mesh(new Vertex[] {
			new Vertex(new Vector3f(-0.5f, -0.5f, -6f), new Vector3f(1, 1, 0)), 1 -> 0
			new Vertex(new Vector3f( 0.5f, -0.5f, -6f), new Vector3f(1, 0, 1)), 2 -> 1
			new Vertex(new Vector3f( 0.5f,  0.5f, -6f), new Vector3f(0, 1, 1))  3 -> 2
			new Vertex(new Vector3f(-0.5f,  0.5f, -6f), new Vector3f(0, 0, 0)), 0 -> 3
		}, new int[] {
			0, 1, 2, -> 3, 0, 1
			0, 3, 2 ->  3, 2, 1
		});*/
	
	private final Game game;
	private final GameViewState state;
	
	private final Camera camera;
	
	public GLDrawer(Game game, GameViewState state) {
		this.game = game;
		this.state = state;
		camera = new Camera(new Vector3f(), 0, 90);
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
		
		if(mesh == null && game.world != null) {
			mesh = createMeshFromWorld2();
			mesh.create(gl);
			camera.set(new Vector3f(0, 160, game.world.getHeight()), 45, -60);
//			camera.set(new Vector3f(game.world.getWidth()/2, 100, game.world.getHeight()/2), 0, -90);
		}
		if(mesh != null) {
			sunDirection.set(0, -1, 0);
			sunColor.set(1f, 1f, 0.95f);
			float multiplier = (float)game.getBackgroundColor().getRed() / 255f;
			sunColor = sunColor.multiply(new Vector3f(multiplier, multiplier*multiplier, multiplier*multiplier));
			renderMesh(gl, mesh);
		}
		shader.unbind(gl);
		
		gl.glFlush();
	}

	private Mesh createMeshFromWorld2() {
		Vertex[] vertices = new Vertex[game.world.getTiles().size()];
		int[][] coordToVertex = new int[game.world.getHeight()][game.world.getWidth()];
		int numIndices = (coordToVertex.length) * (coordToVertex[0].length) * 6;
		int[] indices = new int[numIndices];
		Vector3f c0 = new Vector3f(1, 1, 0);
		Vector3f c1 = new Vector3f(0, 1, 0);
		Vector3f c2 = new Vector3f(0, 1, 1);
		Vector3f c3 = new Vector3f(0, 0, 1);
		int index = 0;
		for(Tile tile : game.world.getTiles()) {
			coordToVertex[tile.getLocation().y()][tile.getLocation().x()] = index;
			float y = tile.getLocation().y() + (tile.getLocation().x() % 2) * 0.5f;
			Vector3f pos0 = new Vector3f(tile.getLocation().x(), tile.getHeight()/10, y);
			Vector3f ca = (tile.getLocation().x() % 2 == 0) ? c0 : c1;
			Vector3f cb = (tile.getLocation().x() % 2 == 0) ? c2 : c3;
			Vector3f c = (tile.getLocation().y() % 2 == 0) ? ca : cb;
			vertices[index] = new Vertex(pos0, c);
//			indices[index*6+0] = index*4+3;
//			indices[index*6+1] = index*4+0;
//			indices[index*6+2] = index*4+1;
			index++;
		}
		index = 0;
		for(int y = 1; y < coordToVertex.length; y++) {
			for(int x = 1; x < coordToVertex[y].length; x++) {
				if(x % 2 == 0) {
					indices[index*6+0] = coordToVertex[y][x];
					indices[index*6+1] = coordToVertex[y-1][x];
					indices[index*6+2] = coordToVertex[y-1][x-1];

					indices[index*6+3] = coordToVertex[y][x];
					indices[index*6+4] = coordToVertex[y-1][x-1];
					indices[index*6+5] = coordToVertex[y][x-1];
				}
				else {
					indices[index*6+0] = coordToVertex[y][x];
					indices[index*6+1] = coordToVertex[y-1][x];
					indices[index*6+2] = coordToVertex[y][x-1];

					if(y < coordToVertex.length - 1) {
						indices[index*6+3] = coordToVertex[y][x];
						indices[index*6+4] = coordToVertex[y][x-1];
						indices[index*6+5] = coordToVertex[y+1][x-1];
					}
				}
				index ++;
			}
		}
		return new Mesh(vertices, indices);
	}
	private Mesh createMeshFromWorld() {
		Vertex[] vertices = new Vertex[game.world.getTiles().size()*4];
		int[] indices = new int[game.world.getTiles().size()*6];
		int index = 0;
		Vector3f c0 = new Vector3f(1, 1, 0);
		Vector3f c1 = new Vector3f(0, 1, 0);
		Vector3f c2 = new Vector3f(0, 1, 1);
		Vector3f c3 = new Vector3f(0, 0, 1);
		for(Tile tile : game.world.getTiles()) {
			float y = tile.getLocation().y() + (tile.getLocation().x() % 2) * 0.5f;
			Vector3f pos0 = new Vector3f(tile.getLocation().x(), tile.getHeight()/10, y);
			Vector3f pos1 = new Vector3f(pos0.x + 1, pos0.y, pos0.z);
			Vector3f pos2 = new Vector3f(pos0.x + 1, pos0.y, pos0.z + 1);
			Vector3f pos3 = new Vector3f(pos0.x, pos0.y, pos0.z + 1);
			vertices[index*4+0] = new Vertex(pos0, c0);
			vertices[index*4+1] = new Vertex(pos1, c1);
			vertices[index*4+2] = new Vertex(pos2, c2);
			vertices[index*4+3] = new Vertex(pos3, c3);
			
			indices[index*6+0] = index*4+3;
			indices[index*6+1] = index*4+0;
			indices[index*6+2] = index*4+1;
			
			indices[index*6+3] = index*4+3;
			indices[index*6+4] = index*4+2;
			indices[index*6+5] = index*4+1;
			index++;
		}
		return new Mesh(vertices, indices);
	}
	
	public void renderMesh(GL3 gl, Mesh mesh) {
//		GL30.glBindVertexArray(mesh.getVAO());
		gl.glBindVertexArray(mesh.getVAO());
//		GL30.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnableVertexAttribArray(2);
		shader.setUniform("projection", projection);
		shader.setUniform("view", camera.getView());
		shader.setUniform("sunDirection", sunDirection);
		shader.setUniform("sunColor", sunColor);
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

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
	}
	
	private void updateBackgroundColor(GL3 gl, Color background) {
		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1.0f);
	}
}
