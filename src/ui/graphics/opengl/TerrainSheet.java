package ui.graphics.opengl;

import java.util.*;

import ui.graphics.opengl.maths.*;
import utils.*;
import world.*;

public class TerrainSheet {
	
	public static final float FULL_TILE = 1f;
	public static final float TILE_RADIUS = FULL_TILE*2/3;
	public static final float Y_MULTIPLIER = (float) (TILE_RADIUS * Math.sin(Math.toRadians(60)));
	
	
	private HashMap<TileLoc, ArrayList<Vertex>> locToHex;
	
	private Mesh createMeshFromWorld3(World world) {
		locToHex = new HashMap<>();
		ArrayList<Vertex> vertices = new ArrayList<>();
		ArrayList<Integer> indicesList = new ArrayList<>();
		HashMap<Tile, ArrayList<Vertex>> tileToVertex = new HashMap<>();
		HashMap<Vertex, Integer> vertexToIndex = new HashMap<>();
		float radius = TILE_RADIUS*4/5;
		float yoffset = (float) (radius * Math.sin(Math.toRadians(60)));
		Vector3f white = new Vector3f(1, 1, 1);
		for(Tile tile : world.getTiles()) {
			
			Vector2f textureCoord = new Vector2f(
					(tile.getLocation().x() + 0.5f)/world.getWidth(), 
					(tile.getLocation().y() + 0.5f)/world.getHeight());
			
			ArrayList<Vertex> verts = locToHex.getOrDefault(tile.getLocation(), new ArrayList<>());
			Vector3f center = GLDrawer.tileLocTo3dCoords(tile.getLocation(), tile.getHeight());

			verts.add(new Vertex(center, white, null, textureCoord));
			verts.add(new Vertex(center.add(-radius, 0, 0), white, null, textureCoord));
			verts.add(new Vertex(center.add(-radius/2, -yoffset, 0), white, null, textureCoord));
			verts.add(new Vertex(center.add(radius/2, -yoffset, 0), white, null, textureCoord));
			verts.add(new Vertex(center.add(radius, 0, 0), white, null, textureCoord));
			verts.add(new Vertex(center.add(radius/2, yoffset, 0), white, null, textureCoord));
			verts.add(new Vertex(center.add(-radius/2, yoffset, 0), white, null, textureCoord));
			locToHex.put(tile.getLocation(), verts);
			
			int i = vertices.size();
			for(int j = 0; j < verts.size(); j++) {
				vertexToIndex.put(verts.get(j), i+j);
			}
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
			for(Vertex vec : verts) {
				vertices.add(vec);
			}
		}
		for(Tile tile : world.getTiles()) {
			int x = tile.getLocation().x();
			int y = tile.getLocation().y();
			Tile north = world.get(new TileLoc(x, y + 1));
			if(north != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(6)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.get(vertices.size()-1));
				
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(6)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(2)));
				locToHex.get(north.getLocation()).add(vertices.get(vertices.size()-1));
			}

			Tile northeast = world.get(new TileLoc(x+1, y + (x%2)));
			if(northeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.get(vertices.size()-1));

				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(1)));
				locToHex.get(northeast.getLocation()).add(vertices.get(vertices.size()-1));
			}
			
			if(north != null && northeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(5)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(1)));
				locToHex.get(northeast.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(north).get(3)));
				locToHex.get(north.getLocation()).add(vertices.get(vertices.size()-1));
			}
			
			Tile southeast = world.get(new TileLoc(x+1, y - (1 - (x%2))));
			if(southeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(3)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(1)));
				locToHex.get(southeast.getLocation()).add(vertices.get(vertices.size()-1));

				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(1)));
				locToHex.get(southeast.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(6)));
				locToHex.get(southeast.getLocation()).add(vertices.get(vertices.size()-1));
			}
			
			if(northeast != null && southeast != null) {
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(tile).get(4)));
				locToHex.get(tile.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(southeast).get(6)));
				locToHex.get(southeast.getLocation()).add(vertices.get(vertices.size()-1));
				indicesList.add(vertices.size());
				vertices.add(new Vertex(tileToVertex.get(northeast).get(2)));
				locToHex.get(northeast.getLocation()).add(vertices.get(vertices.size()-1));
			}
		}
		
		int[] indices = new int[indicesList.size()];
		for(int i = 0; i < indices.length; i++) {
			indices[i] = indicesList.get(i);
		}
		Vertex[] vertexArray = vertices.toArray(new Vertex[0]);
		return new Mesh(vertexArray, indices);
	}
}
