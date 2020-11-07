package networking.message;

import java.io.*;
import java.util.*;

import game.*;
import utils.*;
import world.*;

public class WorldInfo implements Serializable {
	
	private static final HashSet<Hitsplat> sentHitsplats = new HashSet<>();

	private final int width;
	private final int height;
	private final int tick;
	private final Tile[] tileInfos;
	private final HashSet<Thing> things;
	private final LinkedList<Faction> factions;
	private final LinkedList<Projectile> projectiles;
	private final LinkedList<Hitsplat> hitsplats;
	public WorldInfo(int width, int height, int tick, Tile[] tileInfos) {
		this.width = width;
		this.height = height;
		this.tick = tick;
		this.tileInfos = tileInfos;
		things = new HashSet<>();
		factions = new LinkedList<>();
		projectiles = new LinkedList<>();
		hitsplats = new LinkedList<>();
	}
	public void addHitsplats(WorldData worldData) {
		LinkedList<Hitsplat> toadd = new LinkedList<>();
		for(Thing thing : worldData.getPlants()) {
			for(Hitsplat hitsplat : thing.getHitsplatList()) {
				if(hitsplat == null || sentHitsplats.contains(hitsplat) || hitsplat.isDead()) {
					continue;
				}
				toadd.add(hitsplat);
				sentHitsplats.add(hitsplat);
			}
		}
		for(Thing thing : worldData.getBuildings()) {
			for(Hitsplat hitsplat : thing.getHitsplatList()) {
				if(hitsplat == null || sentHitsplats.contains(hitsplat) || hitsplat.isDead()) {
					continue;
				}
				toadd.add(hitsplat);
				sentHitsplats.add(hitsplat);
			}
		}
		for(Thing thing : worldData.getUnits()) {
			for(Hitsplat hitsplat : thing.getHitsplatList()) {
				if(hitsplat == null || sentHitsplats.contains(hitsplat) || hitsplat.isDead()) {
					continue;
				}
				toadd.add(hitsplat);
				sentHitsplats.add(hitsplat);
			}
		}
		LinkedList<Hitsplat> toremove = new LinkedList<>();
		for(Hitsplat hitsplat : sentHitsplats) {
			if(hitsplat.isDead()) {
				toremove.add(hitsplat);
			}
		}
		for(Hitsplat hitsplat : toremove) {
			sentHitsplats.remove(hitsplat);
		}
		for(Hitsplat hitsplat : toadd) {
			hitsplats.add(hitsplat);
			sentHitsplats.add(hitsplat);
		}
	}
	public LinkedList<Hitsplat> getHitsplats() {
		return hitsplats;
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
	public LinkedList<Projectile> getProjectiles() {
		return projectiles;
	}
	@Override
	public String toString() {
		return "WorldInfo (" + width + "," + height + ") " + tileInfos.length + " tiles, " + things.size() + " things, " + factions.size() + " factions, " + projectiles.size() + " projectiles, " + hitsplats.size() + " hitsplats";
	}
}
