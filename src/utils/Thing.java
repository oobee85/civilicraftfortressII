package utils;
import java.io.*;
import java.util.*;
import java.util.List;

import game.*;
import game.components.*;
import networking.server.*;
import world.*;

public class Thing implements Serializable, Comparable<Thing> {
	
	private transient static int idCounter = 0;
	private int id;

	private int factionID;
	private double maxHealth;
	private double health;
	private boolean isDead;
	private boolean isRemoved;
	private TileLoc tileLoc;
	private transient Tile tile;
	
	private transient HashMap<Class, GameComponent> components;
	private Inventory inventory;

	private transient Faction faction;
	private transient int timeLastDamageTaken = -1000;
	private transient boolean isSelected;
	private transient MipMap mipmap;
	
	private transient Hitsplat[] hitsplats = new Hitsplat[4];
	
	public Thing(double maxHealth, MipMap mipmap, Faction faction, Tile tile, int inventoryStackSize) {
		components = new HashMap<>();
		health = maxHealth;
		this.maxHealth = maxHealth;
		this.mipmap = mipmap;
		setFaction(faction);
		this.id = idCounter++;
		if (inventoryStackSize > 0) {
			this.setInventory(new Inventory(inventoryStackSize));
		}
		ThingMapper.created(this);
		setTile(tile);
		this.isRemoved = false;
	}
	
	public int id() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	
	public Faction getFaction() {
		return faction;
	}
	
	public int getFactionID() {
		return factionID;
	}
	
	public void setFaction(Faction faction) {
		this.faction = faction;
		this.factionID = faction.id();
	}

	public void setDead(boolean state) {
		isDead = state;
	}
	// setting removed doesnt play sound when it dies
	public void setRemoved(boolean state) {
		isRemoved = state;
	}
	
	// if thing is removed, it wont play sound when dying
	public boolean isRemoved() {
		return isRemoved;
	}
	public boolean isDead() {
		return health <= 0 || isDead;
	}
	
	public void addComponent(Class key, GameComponent component) {
		if (components == null) {
			components = new HashMap<>();
		}
		components.put(key, component.instance());
	}
	
	public void replaceComponent(Class key, GameComponent component) {
		if (components == null) {
			components = new HashMap<>();
		}
		components.put(key, component);
	}
	
	public boolean isBuilder() {
		return components.containsKey(Builder.class);
	}
	
	public Set<BuildingType> getBuildableBuildingTypes() {
		Builder builder = (Builder) components.get(Builder.class);
		return builder.getBuildingTypeSet();
	}
	
	public boolean hasInventory() {
		return inventory != null;
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public void setInventory(Inventory i) {
		this.inventory = i;
	}

	public int applyResistance(int damage, DamageType type) {
		if(components.containsKey(DamageResistance.class)) {
			return ((DamageResistance)components.get(DamageResistance.class)).applyResistance(damage, type);
		}
		else {
			return DamageResistance.applyDefaultResistance(damage, type);
		}
	}
	public double applyResistance(double[] danger) {
		if(components.containsKey(DamageResistance.class)) {
			return ((DamageResistance)components.get(DamageResistance.class)).applyResistance(danger);
		}
		else {
			return DamageResistance.applyDefaultResistance(danger);
		}
	}
	
	
	/**
	 * @return true if this is lethal damage, false otherwise
	 */
	public boolean takeDamage(int damage, DamageType type) {
		boolean before = isDead();
		int totalDamage = applyResistance(damage, type);
		health -= totalDamage;
		if(totalDamage != 0) {
			timeLastDamageTaken = World.ticks;
			getFaction().gotAttacked(getTile());
			addHitsplat(totalDamage);
		}
		// Return true if isDead changed from false to true.
		return !before && isDead();
	}
	
	public void takeFakeDamage() {
		timeLastDamageTaken = World.ticks;
	}
	
	public void heal(double healing, boolean hitsplat) {
		if (health == maxHealth) {
			return;
		}
		int oldHealth = (int)health;
		health += healing;
		if (health > maxHealth) {
			health = maxHealth;
		}
		// Check if health increased to the next int
		int amountHealed = (int)health - oldHealth;
		if (amountHealed >= 1) {
			timeLastDamageTaken = World.ticks;
			if(hitsplat == true && amountHealed >= 1) {
				addHitsplat(-amountHealed);
			}
		}
	}
	private void addHitsplat(int damage) {
		int oldest = 0;
		Hitsplat oldestHitsplat = hitsplats[0];
		int offset = (int)(Math.random()*hitsplats.length);
		for(int ii = 0; ii < hitsplats.length; ii++) {
			int i = (ii+offset) % hitsplats.length;
			Hitsplat current = hitsplats[i];
			if(current == null) {
				oldest = i;
				oldestHitsplat = current;
				break;
			}
			if(oldestHitsplat == null || current.getMaxDuration() < oldestHitsplat.getMaxDuration() ) {
				oldest = i;
				oldestHitsplat = current;
			}
		}
		Hitsplat hit = new Hitsplat(damage, oldest, this);
		hitsplats[oldest] = hit;
	}
	public double getHealth() {
		return health;
	}
	public double getMaxHealth() {
		return maxHealth;
	}
	public void setMaxHealth(double maxHealth) {
		this.maxHealth = maxHealth;
		if(health > maxHealth) {
			health = maxHealth;
		}
	}
	public void setHealth(double hp) {
		health = hp;
	}
	public int getTimeLastDamageTaken() {
		return timeLastDamageTaken;
	}
	public void updateHitsplats() {
		for(int i = 0; i < hitsplats.length; i++) {
			if(hitsplats[i] == null) {
				continue;
			}
			if(hitsplats[i].isDead() == true) {
				hitsplats[i] = null;
			}
		}
	}
	public boolean hasHitsplat() {
		for(int i = 0; i < hitsplats.length; i++) {
			if(hitsplats[i] != null) {
				return true;
			}
		}
		return false;
	}
	public Hitsplat[] getHitsplatList() {
		return hitsplats;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public boolean isSelected() {
		return isSelected;
	}
	
	public void setTile(Tile tile) {
		this.tile = tile;
		this.tileLoc = tile.getLocation();
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public TileLoc getTileLocation() {
		return tileLoc;
	}
	
	public MipMap getMipMap() {
		return mipmap;
	}
	
	public void setMipMap(MipMap mipmap) {
		this.mipmap = mipmap;
	}
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(
				String.format("HP=%.0f/%.0f", getHealth(), getMaxHealth())
				));
	}
	@Override
	public int compareTo(Thing o) {
		return this.id - o.id;
	}

}
