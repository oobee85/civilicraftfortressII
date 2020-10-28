package networking.message;

import java.io.*;

public class WorldInfo implements Serializable {

	private final int width;
	private final int height;
	private final TileInfo[] tileInfos;
	public WorldInfo(int width, int height, TileInfo[] tileInfos) {
		this.width = width;
		this.height = height;
		this.tileInfos = tileInfos;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public TileInfo[] getTileInfos() {
		return tileInfos;
	}
}
