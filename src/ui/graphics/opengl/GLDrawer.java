package ui.graphics.opengl;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.util.texture.*;

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
	private Shader liquidShader;
	private Shader skyboxShader;
	private Matrix4f projection;
	
	private Vector3f sunDirection = new Vector3f();
	private Vector3f sunColor = new Vector3f();
	private Vector3f ambientColor = new Vector3f();
	private long startTime = System.currentTimeMillis();
	
	private TerrainObject terrainObject;
	private Mesh hoveredTileBox = MeshUtils.getMeshByFileName("models/selection_cube.ply");
	private Texture skybox;
	
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

	private BufferedImage[] getskyboxImages() {
		String[] filenames = new String[] {
				"textures/sh_ft.png",
				"textures/sh_bk.png",
				"textures/sh_up.png",
				"textures/sh_dn.png",
				"textures/sh_rt.png",
				"textures/sh_lf.png",
		};
		BufferedImage[] images = new BufferedImage[6];
		for(int i = 0; i < images.length; i++) {
			images[i] = Utils.toBufferedImage(Utils.loadImage(filenames[i]));
		}
		return images;
	}
	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		gl.glEnable(GL3.GL_CULL_FACE);
		gl.glCullFace(GL3.GL_BACK);
		TextureUtils.initDefaultTextures(gl);
		
		skybox = TextureUtils.cubeMapFromImages(gl, getskyboxImages());
		MeshUtils.getMeshByFileName("models/fire.ply");
		Mesh.initAllMeshes(gl);
		
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glClearDepthf(1);
		
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); 
		gl.glEnable(GL.GL_BLEND);  
		updateBackgroundColor(gl, Color.black);
		

		shader = new Shader("/shaders/mainVertex.glsl", "/shaders/mainFragment.glsl");
		shader.create(gl);
		
		liquidShader = new Shader("/shaders/liquidVertex.glsl", "/shaders/liquidFragment.glsl");
		liquidShader.create(gl);

		skyboxShader = new Shader("/shaders/skyboxVertex.glsl", "/shaders/skyboxFragment.glsl");
		skyboxShader.create(gl);
		
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
			if(Game.DISABLE_NIGHT) {
				dayOffset = World.DAY_DURATION/2;
			}
			float ratio = (float)dayOffset / (World.DAY_DURATION + World.NIGHT_DURATION);
			Matrix4f rot = Matrix4f.rotate(ratio * 360, new Vector3f(0, 1, 0));
			Vector3f initPosition = new Vector3f(-1, 0, 0);
			Vector3f result = rot.multiply(initPosition, 1);
			sunDirection = result.multiply(-1).normalize();
			sunColor.set(1f, 1f, 0.95f);
			float multiplier = (float)World.getDaylight();
			if(Game.DISABLE_NIGHT) {
				multiplier = Math.min(1, multiplier);
			}
			sunColor = sunColor.multiply(new Vector3f(multiplier, multiplier*multiplier, multiplier*multiplier));
			float ambient = multiplier/2;
			ambientColor.set(ambient, ambient, ambient);

			renderSkybox(gl, skyboxShader, multiplier);
			shader.bind(gl);
			renderStuff(gl, shader);
			shader.unbind(gl);
			renderLiquids(gl, liquidShader);
		}
		
		gl.glFlush();
	}
	
	public void renderSkybox(GL3 gl, Shader shader, float daylight) {
		gl.glDisable(GL2.GL_DEPTH_TEST);
		skyboxShader.bind(gl);
		skyboxShader.setUniform("projection", projection);
		skyboxShader.setUniform("view", camera.getRotationMatrix().multiply(Camera.prerotateInv));
		skyboxShader.setUniform("daylight", daylight);
		MeshUtils.skybox.renderSkybox(gl, 
				skyboxShader, 
				skybox);
		skyboxShader.unbind(gl);
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}
	
	public void renderLiquids(GL3 gl, Shader shader) {
		shader.bind(gl);
		shader.setUniform("projection", projection);
		shader.setUniform("view", camera.getView());
		shader.setUniform("sunDirection", sunDirection);
		shader.setUniform("sunColor", sunColor);
		shader.setUniform("ambientColor", ambientColor);
		shader.setUniform("waveOffset", (float)(System.currentTimeMillis() - startTime));
		
		gl.glBindVertexArray(terrainObject.liquid.getVAO());
		gl.glEnableVertexAttribArray(0);
		gl.glEnableVertexAttribArray(1);
		gl.glEnableVertexAttribArray(2);
		gl.glEnableVertexAttribArray(3);
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, terrainObject.liquid.getIBO());
		for(Tile tile : game.world.getTiles()) {
			float bright = Math.min(1, (float) tile.getBrightness(state.faction));
			Vector3f ambientColorWithBrightness = ambientColor.add(bright, bright, bright);
			shader.setUniform("ambientColor", ambientColorWithBrightness);
			if(tile.liquidType != LiquidType.DRY) {
				float cutoff = 1f;
				float scale = Math.min(1, tile.liquidAmount * tile.liquidAmount / cutoff);
				Vector3f pos = tileLocTo3dCoords(tile.getLocation(), tile.getHeight() + tile.liquidAmount);
				gl.glActiveTexture(GL3.GL_TEXTURE0);
				Texture texture = TextureUtils.getTextureByFileName(tile.liquidType.getTextureFile(), gl);
				texture.enable(gl);
				texture.bind(gl); 
				gl.glActiveTexture(GL3.GL_TEXTURE1);
				skybox.enable(gl);
				skybox.bind(gl);
				shader.setUniform("textureSampler", 0);
				shader.setUniform("cubeMap", 1);
				shader.setUniform("useTexture", 1f);
				shader.setUniform("model", Matrix4f.getModelMatrix(pos, Matrix4f.identity(), new Vector3f(scale, scale, 1)));
				gl.glDrawElements(GL2.GL_TRIANGLES, terrainObject.liquid.getIndices().length, GL2.GL_UNSIGNED_INT, 0);

				gl.glBindTexture(skybox.getTarget(), 0);
				skybox.disable(gl);
				gl.glBindTexture(texture.getTarget(), 0);
				texture.disable(gl);
//				terrainObject.liquid.render(gl, shader, TextureUtils.getTextureByFileName(tile.liquidType.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(scale, scale, 1));
			}
		}
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDisableVertexAttribArray(2);
		gl.glDisableVertexAttribArray(3);
		gl.glBindVertexArray(0);
		
		for(Tile tile : game.world.getTiles()) {
			float bright = Math.min(1, (float) tile.getBrightness(state.faction));
			Vector3f ambientColorWithBrightness = ambientColor.add(bright, bright, bright);
			shader.setUniform("ambientColor", ambientColorWithBrightness);
			if(tile.getModifier() != null) {
				Vector3f pos = tileTo3dCoords(tile);
				float scale = 1.0f + (float)Math.random()*0.1f;
				MeshUtils.getMeshByFileName("models/fire.ply").render(gl, shader, TextureUtils.getTextureByFileName("Images/ground_modifiers/fire.png", gl), pos, Matrix4f.identity(), new Vector3f(scale, scale, scale));
			}
		}
		shader.unbind(gl);
	}
	
	class RenderObject {
		Texture texture;
		Matrix4f model;
		public RenderObject(Texture texture, Matrix4f model) {
			this.texture = texture;
			this.model = model;
		}
	}
	HashMap<Mesh, List<RenderObject>> torender = new HashMap<>();
	private void clearToRender() {
		for(Entry<Mesh, List<RenderObject>> entry : torender.entrySet()) {
			entry.getValue().clear();
		}
	}
	private void addToRender(Mesh mesh, RenderObject obj) {
		if(!torender.containsKey(mesh)) {
			torender.put(mesh, new ArrayList<>());
		}
		torender.get(mesh).add(obj);
	}
	
	public void renderStuff(GL3 gl, Shader shader) {
		clearToRender();
		shader.setUniform("projection", projection);
		shader.setUniform("view", camera.getView());
		shader.setUniform("sunDirection", sunDirection);
		shader.setUniform("sunColor", sunColor);
		shader.setUniform("ambientColor", ambientColor);
		shader.setUniform("isHighlight", 0f);
		
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

		terrainObject.mesh.render(gl, shader, terrainObject.texture, new Vector3f(0, 0, 0), terrainObject.getModelMatrix(), new Vector3f(1, 1, 1));
		
//		shader.unbind(gl);
//		liquidShader.bind(gl);
//		liquidShader.setUniform("projection", projection);
//		liquidShader.setUniform("view", camera.getView());
//		liquidShader.setUniform("sunDirection", sunDirection);
//		liquidShader.setUniform("sunColor", sunColor);
//		liquidShader.setUniform("ambientColor", ambientColor);
//		liquidShader.setUniform("waveOffset", (float)(System.currentTimeMillis() - startTime));
//		for(Tile tile : game.world.getTiles()) {
//			float bright = Math.min(1, (float) tile.getBrightness(state.faction));
//			Vector3f ambientColorWithBrightness = ambientColor.add(bright, bright, bright);
//			shader.setUniform("ambientColor", ambientColorWithBrightness);
//			if(tile.liquidType != LiquidType.DRY) {
//				float cutoff = 1f;
//				float scale = Math.min(1, tile.liquidAmount * tile.liquidAmount / cutoff);
//				Vector3f pos = tileLocTo3dCoords(tile.getLocation(), tile.getHeight() + tile.liquidAmount);
//				terrainObject.liquid.render(gl, shader, TextureUtils.getTextureByFileName(tile.liquidType.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(scale, scale, 1));
//			}
//			if(tile.getModifier() != null) {
//				Vector3f pos = tileTo3dCoords(tile);
//				float scale = 1.0f + (float)Math.random()*0.1f;
//				MeshUtils.getMeshByFileName("models/fire.ply").render(gl, shader, TextureUtils.getTextureByFileName("Images/ground_modifiers/fire.png", gl), pos, Matrix4f.identity(), new Vector3f(scale, scale, scale));
//			}
//		}
//		liquidShader.unbind(gl);
//		shader.bind(gl);
		shader.setUniform("ambientColor", ambientColor);
		for(Plant plant : game.world.getPlants()) {
			Vector3f pos = tileTo3dCoords(plant.getTile());
			Vector3f scale = new Vector3f(1, 1, 1);
			if(plant.getType() == PlantType.FOREST1) {
				scale.x = scale.x * (1f + 0.15f*(plant.getTile().getLocation().x()%3) + 0.15f*(plant.getTile().getLocation().y()%5));
				scale.y = scale.x;
				scale.z = scale.z * (2f + 0.1f*(plant.getTile().getLocation().x()%7) + 0.1f*(plant.getTile().getLocation().y()%13));
			}
			addToRender(plant.getMesh(), new RenderObject(
					TextureUtils.getTextureByFileName(plant.getTextureFile(), gl), 
					Matrix4f.getModelMatrix(pos, Matrix4f.identity(), scale)));
		}
		UnitType dragonType = Game.unitTypeMap.get("DRAGON");
		for(Unit unit : game.world.getUnits()) {
			float height = unit.getTile().getHeight();
			if(unit.getUnitType().isFlying()) {
				height = unit.getTile().getHeight() + unit.getTile().liquidAmount;
			}
			Vector3f pos = tileLocTo3dCoords(unit.getTile().getLocation(), height);
			Vector3f scale = new Vector3f(1, 1, 1);
			if(unit.getType() == dragonType) {
				scale = scale.multiply(2);
			}
			if(unit.getType().isFlying()) {
				pos.z += 3;
			}
			addToRender(unit.getMesh(), new RenderObject(
					TextureUtils.getTextureByFileName(unit.getTextureFile(), gl), 
					Matrix4f.getModelMatrix(pos, Matrix4f.identity(), scale)));
		}
		for(Building building : game.world.getBuildings()) {
			if(building.getType().blocksMovement()) {
				Vector3f pos = tileLocTo3dCoords(building.getTile().getLocation(), building.getTile().getHeight() + Liquid.WALL_HEIGHT);
				addToRender(terrainObject.liquid, new RenderObject(
						TextureUtils.getTextureByFileName(building.getTextureFile(), gl), 
						Matrix4f.getModelMatrix(pos, Matrix4f.identity(), new Vector3f(1, 1, 1))));
			}
			else {
				Vector3f pos = tileTo3dCoords(building.getTile());
				addToRender(building.getMesh(), new RenderObject(
						TextureUtils.getTextureByFileName(building.getTextureFile(), gl), 
						Matrix4f.getModelMatrix(pos, Matrix4f.identity(), new Vector3f(1.2f, 1.2f, 1.2f))));
			}
		}

		for(Projectile projectile : game.world.getData().getProjectiles()) {
			Vector3f pos = tileTo3dCoords(projectile.getTile());
			MeshUtils.getMeshByFileName("models/bomb.ply").render(gl, shader, TextureUtils.getTextureByFileName(PlantType.BERRY.getTextureFile(), gl), pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}

		if(!state.fpMode && game.world.get(state.hoveredTile) != null) {
			Vector3f pos = tileTo3dCoords(game.world.get(state.hoveredTile));
			hoveredTileBox.render(gl, shader, TextureUtils.ERROR_TEXTURE, pos, Matrix4f.identity(), new Vector3f(1, 1, 1));
		}
		
		// Matrix4f.getModelMatrix(position, rotation, scale)
//		System.out.println(torender.size());
		for(Entry<Mesh, List<RenderObject>> entry : torender.entrySet()) {
			Mesh mesh = entry.getKey();
			List<RenderObject> list = entry.getValue();
			gl.glBindVertexArray(mesh.getVAO());
			gl.glEnableVertexAttribArray(0);
			gl.glEnableVertexAttribArray(1);
			gl.glEnableVertexAttribArray(2);
			gl.glEnableVertexAttribArray(3);
			gl.glActiveTexture(GL3.GL_TEXTURE0);
			for(RenderObject obj : list) {

				obj.texture.enable(gl);
				obj.texture.bind(gl); 
				shader.setUniform("textureSampler", 0);
				shader.setUniform("useTexture", 1f);
				Matrix4f scaledModel = obj.model.multiply(Matrix4f.scale(new Vector3f(TerrainObject.FULL_TILE, TerrainObject.FULL_TILE, TerrainObject.FULL_TILE)));
				shader.setUniform("model", scaledModel);
				gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());
				gl.glDrawElements(GL2.GL_TRIANGLES, mesh.getIndices().length, GL2.GL_UNSIGNED_INT, 0);
				gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

				gl.glBindTexture(obj.texture.getTarget(), 0);
				obj.texture.disable(gl);
			}
			gl.glDisableVertexAttribArray(0);
			gl.glDisableVertexAttribArray(1);
			gl.glDisableVertexAttribArray(2);
			gl.glDisableVertexAttribArray(3);
			gl.glBindVertexArray(0);
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
//		gl.glClearColor(background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 0.0f);
		gl.glClearColor(0, 0, 0, 0);
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
		if(state.fpMode) {
			float adjust = 0.6f;
			camera.shiftView(dx*adjust, -dy*adjust);
			Tile tile = game.world.get(coordsToTile(camera.getPosition()));
			if(tile != null) {
				float height = tile.getHeight();
				if(tile.liquidType == LiquidType.ICE) {
					height += tile.liquidAmount;
				}
				camera.getPosition().z = tileHeightTo3dHeight(height) + 4;
			}
		}
		else {
			float adjust = 0.2f;
			camera.shiftView(dx*adjust, dy*adjust);
		}
	}
	
	@Override
	public void rotateView(int dx, int dy) {
		float adjust = 0.1f;
		camera.rotate(-dx*adjust, dy*adjust);
	}

	public static TileLoc coordsToTile(Vector3f coords) {
		int x = (int)((coords.x + TerrainObject.FULL_TILE/2) / TerrainObject.FULL_TILE);
		int y = (int)((coords.y + TerrainObject.Y_MULTIPLIER)/(TerrainObject.Y_MULTIPLIER*2));
		return new TileLoc(x, y);
	}
	public static Vector3f tileTo3dCoords(Tile tile) {
		return tileLocTo3dCoords(tile.getLocation(), tile.getHeight());
	}
	public static Vector3f tileLocTo3dCoords(TileLoc tileLoc, float height) {
		return new Vector3f(
				TerrainObject.FULL_TILE*tileLoc.x(), 
				2 * TerrainObject.Y_MULTIPLIER * (tileLoc.y() + (tileLoc.x() % 2) * 0.5f), 
				tileHeightTo3dHeight(height));
	}
	public static float tileHeightTo3dHeight(float height) {
		return height*height/(20000/TerrainObject.FULL_TILE);
	}
}
