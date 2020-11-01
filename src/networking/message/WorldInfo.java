package networking.message;

import java.io.*;
import java.util.*;

import game.*;
import utils.*;
import world.*;

public class WorldInfo implements Serializable {

	private final int width;
	private final int height;
	private final int tick;
	private final Tile[] tileInfos;
	private final HashSet<Thing> things;
	private final LinkedList<Faction> factions;
	public WorldInfo(int width, int height, int tick, Tile[] tileInfos) {
		this.width = width;
		this.height = height;
		this.tick = tick;
		this.tileInfos = tileInfos;
		things = new HashSet<>();
		factions = new LinkedList<>();
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getTick() {
		return tick;
	}
	public Tile[] getTileInfos() {
		return tileInfos;
	}
	public HashSet<Thing> getThings() {
		return things;
	}
	public LinkedList<Faction> getFactions() {
		return factions;
	}
	@Override
	public String toString() {
		return "WorldInfo (" + width + "," + height + ") " + tileInfos.length + " tile updates";
	}
}
