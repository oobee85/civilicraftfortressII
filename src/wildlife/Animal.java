package wildlife;

import world.*;

public class Animal {
	private Tile tile;
	public Animal() {
	}
	
	public void setTile(Tile tile) {
		this.tile = tile;
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public double getMoveChance() {
		return 0.01;
	}
}
