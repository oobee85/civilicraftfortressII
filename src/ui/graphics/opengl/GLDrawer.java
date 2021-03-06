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
	private TerrainObject terrainObject;
	
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
		
		if(terrainObject == null && game.world != null) {
			terrainObject = new TerrainObject();
			terrainObject.create(gl, game.world);
			camera.set(new Vector3f(0, 160, game.world.getHeight()/2), 0, -60);
//			camera.set(new Vector3f(game.world.getWidth()/2, 100, game.world.getHeight()/2), 0, -90);
		}
		if(terrainObject != null) {
//			terrainObject.rotate(Matrix4f.rotate(1, new Vector3f(0, 1, 0)));
			int dayOffset = World.getCurrentDayOffset();
			float ratio = (float)dayOffset / (World.DAY_DURATION + World.NIGHT_DURATION);
			Matrix4f rot = Matrix4f.rotate(ratio * 360, new Vector3f(0, 0, -1));
			Vector3f initPosition = new Vector3f(-1, 0, 0);
			Vector3f result = rot.multiply(initPosition, 1);
			sunDirection = result.multiply(-1);
			sunColor.set(1f, 1f, 0.95f);
			float multiplier = (float)World.getDaylight();
			sunColor = sunColor.multiply(new Vector3f(multiplier, multiplier*multiplier, multiplier*multiplier));
			renderStuff(gl, shader);
			terrainObject.render(gl, shader);
		}
		shader.unbind(gl);
		
		gl.glFlush();
	}

	public void renderStuff(GL3 gl, Shader shader) {
		shader.setUniform("projection", projection);
		shader.setUniform("view", camera.getView());
		shader.setUniform("sunDirection", sunDirection);
		shader.setUniform("sunColor", sunColor);
		terrainObject.render(gl, shader);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
	}
	
	private void updateBackgroundColor(GL3 gl, Color background) {
		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1.0f);
	}
}
