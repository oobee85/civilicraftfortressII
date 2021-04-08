package ui.graphics.opengl;

import java.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.*;

import ui.graphics.opengl.maths.*;
import world.*;

public class TerrainObject extends GameObject {
	
	Mesh mesh;
	Texture texture;
	
	public TerrainObject() {
		super(null);
	}

	public void create(GL3 gl, World world) {
		mesh = createMeshFromWorld2(world);
		mesh.create(gl);
	}

	private Mesh createMeshFromWorld2(World world) {
		float xoffset = (float)(world.getWidth() + 1)/2;
		float zoffset = (float)(world.getHeight() + 1)/2;
		Vertex[] vertices = new Vertex[world.getTiles().size()];
		int[][] coordToVertex = new int[world.getHeight()][world.getWidth()];
		Vector3f c0 = new Vector3f(1, 1, 0);
		Vector3f c1 = new Vector3f(0, 1, 0);
		Vector3f c2 = new Vector3f(0, 1, 1);
		Vector3f c3 = new Vector3f(0, 0, 1);
		int index = 0;
		for(Tile tile : world.getTiles()) {
			coordToVertex[tile.getLocation().y()][tile.getLocation().x()] = index;
			float y = tile.getLocation().y() + (tile.getLocation().x() % 2) * 0.5f;
			Vector3f pos0 = new Vector3f(tile.getLocation().x() - xoffset, y - zoffset, tile.getHeight()/15);
			Vector3f ca = (tile.getLocation().x() % 2 == 0) ? c0 : c1;
			Vector3f cb = (tile.getLocation().x() % 2 == 0) ? c2 : c3;
			Vector3f c = (tile.getLocation().y() % 2 == 0) ? ca : cb;
			Vector2f textureCoord = new Vector2f((float)tile.getLocation().x()/world.getWidth(), (float)tile.getLocation().y()/world.getHeight());
			vertices[index] = new Vertex(pos0, c, null, textureCoord);
//			indices[index*6+0] = index*4+3;
//			indices[index*6+1] = index*4+0;
//			indices[index*6+2] = index*4+1;
			index++;
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
