package ui.graphics.opengl;

import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

import ui.graphics.opengl.maths.*;
import utils.*;
import world.*;

public class TerrainObject extends GameObject {

	public static final float FULL_TILE = 1f;
	public static final float TILE_RADIUS = FULL_TILE*2/3;
	public static final float Y_MULTIPLIER = (float) (TILE_RADIUS * Math.sin(Math.toRadians(60)));
	Mesh mesh;
	Texture texture;
	
	private HashMap<TileLoc, ArrayList<Integer>> locToHex;
	
	public TerrainObject() {
		super(null);
	}

	public void create(GL3 gl, World world) {
		mesh = createMeshFromWorld3(world);
		mesh.create(gl);
		MeshUtils.hexwall.create(gl);
	}
	
	
	
	public void updateHeights(World world, boolean includeLiquid) {
		for(Tile tile : world.getTiles()) {
			for(Integer vertexIndex : locToHex.get(tile.getLocation())) {
				float effectiveHeight = tile.getHeight();
				if(includeLiquid) {
					effectiveHeight += tile.liquidAmount - 2;
				}
//				mesh.setVertexZ(vertexIndex, GLDrawer.tileHeightTo3dHeight(effectiveHeight));
			}
		}
		mesh.updateNormals();
		mesh.updatePositions();
	}

	private Mesh createMeshFromWorld3(World world) {
		locToHex = new HashMap<>();
		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<Integer> indicesList = new ArrayList<>();
		HashMap<Tile, ArrayList<Vertex>> tileToVertex = new HashMap<>();
		float radius = TILE_RADIUS*4/5;
		float yoffset = (float) (radius * Math.sin(Math.toRadians(60)));
		Vector3f white = new Vector3f(1, 1, 1);
		for(Tile tile : world.getTiles()) {
			
			Vector2f textureCoord = new Vector2f(
					(tile.getLocation().x() + 0.5f)/world.getWidth(), 
					(tile.getLocation().y() + 0.5f)/world.getHeight());
			ArrayList<Vertex> verts = new ArrayList<>();
			ArrayList<Integer> vertIndices = new ArrayList<>();
//			Vector3f center = GLDrawer.tileLocTo3dCoords(tile.getLocation(), tile.getHeight());
//
//			vertices.add(new Vertex(center, white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(-radius, 0, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(-radius/2, -yoffset, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(radius/2, -yoffset, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(radius, 0, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(radius/2, yoffset, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
//			vertices.add(new Vertex(center.add(-radius/2, yoffset, 0), white, textureCoord));
//			verts.add(vertices.get(vertices.size()-1));
//			vertIndices.add(vertices.size()-1);
			
			locToHex.put(tile.getLocation(), vertIndices);
			
			int i = vertices.size();
			tileToVertex.put(tile, verts);
			for(int j = 1; j <= 6; j++) {
				indicesList.add(i);
				indicesList.add(i+j);
				if(j == 6) {
					indicesList.add(i+1);
				}
				else {
					indicesList.add(i+j+1);
				}
			}
		}
		for(Tile tile : world.getTiles()) {
			int x = tile.getLocation().x();
			int y = tile.getLocation().y();
			Tile north = world.get(new TileLoc(x, y + 1));
			if(north != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(6)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.size()-1);
				
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(6)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(2)));
				locToHex.get(north.getLocation()).add(vertices.size()-1);
			}

			Tile northeast = world.get(new TileLoc(x+1, y + (x%2)));
			if(northeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.size()-1);

				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(1)));
				locToHex.get(northeast.getLocation()).add(vertices.size()-1);
			}
			
			if(north != null && northeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(1)));
				locToHex.get(northeast.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.size()-1);
			}
			
			Tile southeast = world.get(new TileLoc(x+1, y - (1 - (x%2))));
			if(southeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(3)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(1)));
				locToHex.get(southeast.getLocation()).add(vertices.size()-1);

				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(1)));
				locToHex.get(southeast.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(6)));
				locToHex.get(southeast.getLocation()).add(vertices.size()-1);
			}
			
			if(northeast != null && southeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(6)));
				locToHex.get(southeast.getLocation()).add(vertices.size()-1);
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.size()-1);
			}
		}
		
		int[] indices = new int[indicesList.size()];
		for(int i = 0; i < indices.length; i++) {
			indices[i] = indicesList.get(i);
		}
		Vertex[] vertexArray = vertices.toArray(new Vertex[0]);
		return new Mesh(vertexArray, indices);
	}
	private Mesh createMeshFromWorld2(World world) {
		Vertex[] vertices = new Vertex[world.getTiles().size()];
		int[][] coordToVertex = new int[world.getHeight()][world.getWidth()];
		Vector3f c0 = new Vector3f(1, 1, 0);
		Vector3f c1 = new Vector3f(0, 1, 0);
		Vector3f c2 = new Vector3f(0, 1, 1);
		Vector3f c3 = new Vector3f(0, 0, 1);
		int index = 0;
		for(Tile tile : world.getTiles()) {
			coordToVertex[tile.getLocation().y()][tile.getLocation().x()] = index;
//			Vector3f pos0 = GLDrawer.tileLocTo3dCoords(tile.getLocation(), tile.getHeight());
//			Vector3f ca = (tile.getLocation().x() % 2 == 0) ? c0 : c1;
//			Vector3f cb = (tile.getLocation().x() % 2 == 0) ? c2 : c3;
//			Vector3f c = (tile.getLocation().y() % 2 == 0) ? ca : cb;
//			Vector2f textureCoord = new Vector2f((float)tile.getLocation().x()/world.getWidth(), (float)tile.getLocation().y()/world.getHeight());
//			vertices[index] = new Vertex(pos0, c, textureCoord);
//			index++;
		}
		ArrayList<Integer> indicesList = new ArrayList<>();
		index = 0;
		for(int y = 1; y < coordToVertex.length; y++) {
			for(int x = 1; x < coordToVertex[y].length; x++) {
				if(x % 2 == 0) {
					indicesList.add(coordToVertex[y][x]);
					indicesList.add(coordToVertex[y-1][x-1]);
					indicesList.add(coordToVertex[y-1][x]);

					indicesList.add(coordToVertex[y][x]);
					indicesList.add(coordToVertex[y][x-1]);
					indicesList.add(coordToVertex[y-1][x-1]);
				}
				else {
					indicesList.add(coordToVertex[y][x]);
					indicesList.add(coordToVertex[y][x-1]);
					indicesList.add(coordToVertex[y-1][x]);

					if(y < coordToVertex.length - 1) {
						indicesList.add(coordToVertex[y][x]);
						indicesList.add(coordToVertex[y+1][x-1]);
						indicesList.add(coordToVertex[y][x-1]);
					}
				}
				index ++;
			}
		}
		int[] indices = new int[indicesList.size()];
		for(int i = 0; i < indices.length; i++) {
			indices[i] = indicesList.get(i);
		}
		return new Mesh(vertices, indices);
	}
	private Mesh createMeshFromWorld(World world) {
		Vertex[] vertices = new Vertex[world.getTiles().size()*4];
		int[] indices = new int[world.getTiles().size()*6];
		int index = 0;
		Vector3f c0 = new Vector3f(1, 1, 0);
		Vector3f c1 = new Vector3f(0, 1, 0);
		Vector3f c2 = new Vector3f(0, 1, 1);
		Vector3f c3 = new Vector3f(0, 0, 1);
		for(Tile tile : world.getTiles()) {
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
}
