package networking.message;

import java.io.*;
import java.util.*;

import game.*;
import utils.*;
import world.*;

public class WorldInfo implements Externalizable {
	
	private static final HashSet<Hitsplat> sentHitsplats = new HashSet<>();

	private int width;
	private int height;
	private int tick;
	private Tile[] tileInfos;
	private HashSet<Thing> things;
	private LinkedList<Faction> factions;
	private LinkedList<Projectile> projectiles;
	private LinkedList<Hitsplat> hitsplats;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(tick);
		out.writeObject(tileInfos);
		out.writeInt(factions.size());
		for (Faction faction : factions) {
			faction.writeExternal(out);
		}
		out.writeInt(things.size());
		for (Thing thing : things) {
			out.writeObject(thing);
//			thing.writeExternal(out);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		width = in.readInt();
		height = in.readInt();
		tick = in.readInt();
		tileInfos = (Tile[])in.readObject();
		things = new HashSet<>();
		factions = new LinkedList<>();
		projectiles = new LinkedList<>();
		hitsplats = new LinkedList<>();
		int numFactions = in.readInt();
		for (int i = 0; i < numFactions; i++) {
			Faction faction = new Faction();
			faction.readExternal(in);
			factions.add(faction);
		}
		int numThings = in.readInt();
		for (int i = 0; i < numThings; i++) {
			Thing thing = (Thing)in.readObject();
//			SerializeThingTypes type = SerializeThingTypes.values()[in.readInt()];
//			if (type == SerializeThingTypes.Plant) {
//				Plant plant = new Plant();
//				plant.readExternal(in);
//				things.add(plant);
//			}
			things.add(thing);
		}
	}
	
	public WorldInfo() {}

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
