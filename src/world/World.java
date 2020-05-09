package world;

import utils.*;

public class World {
	public Tile[][] world;
	public double[][] heightMap;
	
	private int width;
	private int height;
	
	public World(Tile[][] world, double[][] heightMap) {
		this.world = world;
		this.heightMap = heightMap;
		width = world.length;
		height = world[0].length;
	}
	
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	public Tile get(TileLoc loc) {
		return world[loc.x][loc.y];
	}
}
