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
import game.liquid.*;
import ui.graphics.*;
import ui.graphics.opengl.maths.*;
import ui.view.GameView.*;
import utils.*;
import world.*;

public class GLDrawer extends Drawer implements GLEventListener {
	
	private static final float FOV = 70f;
	private static final float NEAR_CLIP = 0.1f;
	private static final float FAR_CLIP = 1000f;
	
	private final GLCanvas glcanvas;
	private Shader shader;
	private Matrix4f projection;
	
	private Vector3f sunDirection = new Vector3f();
	private Vector3f sunColor = new Vector3f();
	private Vector3f ambientColor = new Vector3f();
	
	private TerrainObject terrainObject;
	private Mesh hoveredTileBox = MeshUtils.getMeshByFileName("models/selection_cube.ply");
	
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
		
		glcanvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateProjectionMatrix();
			}
		});
	}
	
	private void updateProjectionMatrix() {
		projection = Matrix4f.projection(getAspectRatio(), FOV, NEAR_CLIP, FAR_CLIP);
	}
	
	private float getAspectRatio() {
		return (float)glcanvas.getWidth()/glcanvas.getHeight();
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
		
		updateProjectionMatrix();
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
				camera.set(new Vector3f(game.world.getWidth()/2, 0, 100), 0, -45);
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
			sunColor = sunColor.multiply(new Vector3f(multiplier, multiplier*multiplier, multiplier*multiplier));
			if(Game.DISABLE_NIGHT) {
				ambientColor.set(.7f, .7f, .7f);
			}
			else {
				ambientColor.set(multiplier/4, multiplier/4, multiplier/4);
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

		shader.setUniform("isHighlight", 0f);
		terrainObject.mesh.render(gl, shader, terrainObject.texture, new Vector3f(0, 0, 0), terrainObject.getModelMatrix(), new Vector3f(1, 1, 1));
		
		for(Tile tile : game.world.getTiles()) {
			if(tile.liquidType != LiquidType.DRY) {
				float cutoff = 1f;
				float scale = Math.min(1, tile.liquidAmount * tile.liquidAmount / cutoff);
				Vector3f pos = tileLocTo3dCoords(tile.getLocation(), tile.getHeight() + tile.liquidAmount);
				terrainObject.liquid.render(gl, shader, TextureUtils.getTextureByFileName(tile.liquidType.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(scale, scale, 1));
			}
		}
		for(Plant plant : game.world.getPlants()) {
			Vector3f pos = tileTo3dCoords(plant.getTile());
			plant.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(plant.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
		UnitType dragonType = Game.unitTypeMap.get("DRAGON");
		for(Unit unit : game.world.getUnits()) {
			Vector3f pos = tileTo3dCoords(unit.getTile());
			Vector3f scale = new Vector3f(1, 1, 1);
			if(unit.getType() == dragonType) {
				scale = scale.multiply(2);
			}
			unit.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(unit.getTextureFile(), gl), pos, Matrix4f.identity(), scale);
		}
		for(Building building : game.world.getBuildings()) {
			Vector3f pos = tileTo3dCoords(building.getTile());
			building.getMesh().render(gl, shader, TextureUtils.getTextureByFileName(building.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(1.2f, 1.2f, 1.2f));
		}

		for(Projectile projectile : game.world.getData().getProjectiles()) {
			Vector3f pos = tileTo3dCoords(projectile.getTile());
			MeshUtils.star.render(gl, shader, TextureUtils.getTextureByFileName(PlantType.BERRY.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(2, 2, 2));
		}

		if(game.world.get(state.hoveredTile) != null) {
			Vector3f pos = tileTo3dCoords(game.world.get(state.hoveredTile));
			hoveredTileBox.render(gl, shader, TextureUtils.ERROR_TEXTURE, pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}

		shader.setUniform("isHighlight", 1f);
		for (Thing thing : state.selectedThings) {
			Vector3f pos = tileTo3dCoords(thing.getTile());
			hoveredTileBox.render(gl, shader, TextureUtils.ERROR_TEXTURE, pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
	}
	
	private void updateBackgroundColor(GL3 gl, Color background) {
		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 1.0f);
	}

	public Vector3f rayCast(Vector3f start, Vector3f direction, World world) {
		// TODO convert this to newton's method
		Vector3f current = start;
		Vector3f previous = current;
		Vector3f increment = direction.multiply(1);
		for(int i = 0; i < 1000; i++) {
			TileLoc currentTileLoc = new TileLoc((int)current.x, (int)current.y);
			Tile currentTile = world.get(currentTileLoc);
			if((currentTile != null && current.z <= tileHeightTo3dHeight(currentTile.getHeight()))
					|| (current.z < 0)) {
//				return current.add(previous).multiply(0.5f);
				return previous;
			}
			previous = current;
			current = current.add(increment);
		}
		return null;
	}
	
	@Override
	public Position getWorldCoordOfPixel(Point pixelOnScreen, Position viewOffset, int tileSize) {
		
		double halfFOV = Math.toRadians(FOV/2);
		float frustumWidth = (float) (Math.tan(halfFOV) * FAR_CLIP * 2 * 0.9f);
		float frustumHeight = frustumWidth / getAspectRatio();
		
		Vector3f onScreen = new Vector3f(pixelOnScreen.x, glcanvas.getHeight() - pixelOnScreen.y, 0);
		
		float x = -frustumWidth/2 + frustumWidth * onScreen.x / glcanvas.getWidth();
		float y = -frustumHeight/2 + frustumHeight * onScreen.y / glcanvas.getHeight();
		
		Vector3f viewingRay = new Vector3f(x, y, -FAR_CLIP).normalize();
		
		Vector3f viewingRayInWorld = camera.getView().inverse().multiply(viewingRay, 0).normalize();

		Vector3f intersect = rayCast(camera.getPosition(), viewingRayInWorld, game.world);
		if(intersect == null) {
			return new Position(-1, -1);
		}
		return new Position(intersect.x, intersect.y);
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
		float adjust = 0.1f;
		camera.rotate(-dx*adjust, dy*adjust);
	}

	public static Vector3f tileTo3dCoords(Tile tile) {
		return tileLocTo3dCoords(tile.getLocation(), tile.getHeight());
	}
	public static Vector3f tileLocTo3dCoords(TileLoc tileLoc, float height) {
		return new Vector3f(
				tileLoc.x(), 
				(tileLoc.y() + (tileLoc.x() % 2) * 0.5f)*TerrainObject.Y_OFFSET*2, 
				tileHeightTo3dHeight(height));
	}
	public static float tileHeightTo3dHeight(float height) {
		return height/30;
	}
}
