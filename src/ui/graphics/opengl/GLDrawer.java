package ui.graphics.opengl;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import org.smurn.jply.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;

import game.*;
import ui.graphics.*;
import ui.graphics.opengl.maths.*;
import ui.view.GameView.*;
import utils.*;
import world.*;

public class GLDrawer extends Drawer implements GLEventListener {
	private final GLCanvas glcanvas;
	private Shader shader;
	private Matrix4f projection;
	
	private Vector3f sunDirection = new Vector3f();
	private Vector3f sunColor = new Vector3f();
	private Vector3f ambientColor = new Vector3f();
	
	private TerrainObject terrainObject;
	private Mesh hoveredTileBox = MeshUtils.cube;
	
	private final Camera camera;
	
	public GLDrawer(Game game, GameViewState state) {
		super(game, state);
		camera = new Camera(new Vector3f(), 0, 0);
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

	public Position[] getVisibleTileBounds() {
		return null;
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glEnable(GL3.GL_CULL_FACE);
		gl.glCullFace(GL3.GL_BACK);
		TextureUtils.initDefaultTextures(gl);
		Mesh.initAllMeshes(gl);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glClearDepthf(1);
		updateBackgroundColor(gl, Color.black);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		

		shader = new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl");
		shader.create(gl);
		projection = Matrix4f.projection((float)glcanvas.getWidth()/glcanvas.getHeight(), 70f, 0.1f, 1000);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		System.err.println("DISPLOSING GL");
		TextureUtils.dispose(drawable.getGL().getGL3());
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		updateBackgroundColor(gl, game.getBackgroundColor());
		gl.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_COLOR_BUFFER_BIT);

		if(game.world != null) {
		
			shader.bind(gl);
			
			// Initialize terrainObject
			if(terrainObject == null) {
				terrainObject = new TerrainObject();
				terrainObject.create(gl, game.world);
				camera.set(new Vector3f(0, -game.world.getHeight()/2, 100), 0, -45);
				this.updateTerrainImages();
			}
			if(this.terrainImage != null) {
				if(terrainObject.texture != null) {
					terrainObject.texture.destroy(gl);
				}
				terrainObject.texture = TextureUtils.textureFromImage(gl, this.terrainImage);
			}
			
//			terrainObject.rotate(Matrix4f.rotate(1, new Vector3f(0, 1, 0)));
			int dayOffset = World.getCurrentDayOffset();
			float ratio = (float)dayOffset / (World.DAY_DURATION + World.NIGHT_DURATION);
			Matrix4f rot = Matrix4f.rotate(ratio * 360, new Vector3f(0, 1, 0));
			Vector3f initPosition = new Vector3f(-1, 0, 0);
			Vector3f result = rot.multiply(initPosition, 1);
			sunDirection = result.multiply(-1);
			sunColor.set(1f, 1f, 0.95f);
			float multiplier = (float)World.getDaylight();
//			sunColor = sunColor.multiply(new Vector3f(multiplier, multiplier*multiplier, multiplier*multiplier));
			if(Game.DISABLE_NIGHT) {
				ambientColor.set(.7f, .7f, .7f);
			}
			else {
				ambientColor.set(multiplier/5, multiplier/5, multiplier/5);
			}
			renderStuff(gl, shader);
			shader.unbind(gl);
		}
		
		gl.glFlush();
	}

	public void renderStuff(GL3 gl, Shader shader) {
		shader.setUniform("projection", projection);
		shader.setUniform("view", camera.getView());
		shader.setUniform("sunDirection", sunDirection);
		shader.setUniform("sunColor", sunColor);
		shader.setUniform("ambientColor", ambientColor);
		
		MeshUtils.x.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(-20, 0, 0), 
				Matrix4f.rotate(90, new Vector3f(0, 0, 1)), 
				new Vector3f(.1f, .1f, .1f));
		MeshUtils.x.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(5, 0, 0), 
				Matrix4f.rotate(90, new Vector3f(0, 0, 1)), 
				new Vector3f(.1f, .1f, .1f));
		UnitType pig = Game.unitTypeMap.get("PIG");
		MeshUtils.y.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(0, -20, 0), 
				Matrix4f.identity(), 
				new Vector3f(.1f, .1f, .1f));
		MeshUtils.y.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(0, 5, 0), 
				Matrix4f.identity(), 
				new Vector3f(.1f, .1f, .1f));
		MeshUtils.z.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(0, 0, -20), 
				Matrix4f.rotate(90, new Vector3f(0, 1, 0)), 
				new Vector3f(.1f, .1f, .1f));
		MeshUtils.z.render(gl, shader, 
				TextureUtils.getTextureByFileName(PlantType.FOREST1.getTextureFile(), gl), 
				new Vector3f(0, 0, 5), 
				Matrix4f.rotate(90, new Vector3f(0, 1, 0)), 
				new Vector3f(.1f, .1f, .1f));

		terrainObject.mesh.render(gl, shader, terrainObject.texture, new Vector3f(0, 0, 0), terrainObject.getModelMatrix(), new Vector3f(1, 1, 1));
		
		float xoffset = (float)game.world.getWidth()/2;
		float zoffset = (float)game.world.getHeight()/2;
		for(Plant plant : game.world.getPlants()) {
			float y = plant.getTile().getLocation().y() + (plant.getTile().getLocation().x() % 2) * 0.5f;
			Vector3f pos = new Vector3f(
					plant.getTile().getLocation().x() - xoffset, 
					y - zoffset,
					plant.getTile().getHeight()/15);
			plant.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(plant.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
		for(Unit unit : game.world.getUnits()) {
			float y = unit.getTile().getLocation().y() + (unit.getTile().getLocation().x() % 2) * 0.5f;
			Vector3f pos = new Vector3f(
					unit.getTile().getLocation().x() - xoffset, 
					y - zoffset, 
					unit.getTile().getHeight()/15);
			unit.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(unit.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
		for(Building building : game.world.getBuildings()) {
			float y = building.getTile().getLocation().y() + (building.getTile().getLocation().x() % 2) * 0.5f;
			Vector3f pos = new Vector3f(
					building.getTile().getLocation().x() - xoffset, 
					y - zoffset, 
					building.getTile().getHeight()/15);
			building.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(building.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(2, 2, 2));
		}

		for(Projectile projectile : game.world.getData().getProjectiles()) {
			float y = projectile.getTile().getLocation().y() + (projectile.getTile().getLocation().x() % 2) * 0.5f;
			Vector3f pos = new Vector3f(
					projectile.getTile().getLocation().x() - xoffset, 
					y - zoffset, 
					projectile.getTile().getHeight()/15 + projectile.getHeight()/15);
			MeshUtils.star.render(gl, shader, TextureUtils.getTextureByFileName(PlantType.BERRY.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(2, 2, 2));
		}

		if(game.world.get(state.hoveredTile) != null) {
			float y = state.hoveredTile.y() + (state.hoveredTile.x() % 2) * 0.5f;
			Vector3f pos = new Vector3f(
					state.hoveredTile.x() - xoffset,
					y - zoffset, 
					game.world.get(state.hoveredTile).getHeight()/15);
			hoveredTileBox.render(gl, shader, TextureUtils.ERROR_TEXTURE, pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
	}
	
	private void updateBackgroundColor(GL3 gl, Color background) {
		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1.0f);
	}

	@Override
	public Position getWorldCoordOfPixel(Point pixelOnScreen, Position viewOffset, int tileSize) {
//		Vector3f onScreen = new Vector3f(pixelOnScreen.x, pixelOnScreen.y, 0);
		// TODO need to implement Matrix.inverse();
		// Vector3f onView = projection.inverse().multiply(onScreen, 1);
		// Vector3f viewingRay = onView.subtract(onScreen).normalize();
		// Vector3f intersectWithCloseCuttingPlane = viewingRay*closeDistance + cameraPos;
		// Vector3f intersectWithFarCuttingPlane = viewingRay*farDistance + cameraPos;
		// closeIntersectWorld = view.inverse() * intersectWithCloseCuttingPlane;
		// farIntersectWorld = view.inverse() * intersectWithFarCuttingPlane;
		// TODO implement ray-cast to find where closeIntersectWorld  to farIntersectWorld
		// 		intersects with the world mesh
		
		// The little trick make the game vaguely playable :P
		return new Position(game.world.getWidth() * pixelOnScreen.x / glcanvas.getWidth(), 
				game.world.getHeight() * (glcanvas.getHeight() - pixelOnScreen.y) / glcanvas.getHeight());
	}

	@Override
	public void zoomView(int scroll, int mx, int my) {
		camera.zoom(-4*scroll);
	}

	@Override
	public void zoomViewTo(int newTileSize, int mx, int my) {
	}

	@Override
	public void shiftView(int dx, int dy) {
		float adjust = 0.2f;
		camera.shiftView(dx*adjust, dy*adjust);
	}
	
	@Override
	public void rotateView(int dx, int dy) {
		float adjust = 0.05f;
		camera.rotate(dx*adjust, -dy*adjust);
	}
}
