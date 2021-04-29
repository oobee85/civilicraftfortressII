package utils;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.jogamp.opengl.util.texture.*;

import game.*;
import networking.server.*;
import ui.graphics.*;
import ui.graphics.opengl.*;
import world.*;

public class Thing implements HasImage, HasMesh, Serializable {
	
	private transient static int idCounter = 0;
	private int id;

	private transient Faction faction;
	private int factionID;
	private double maxHealth;
	private double health;
	private boolean isDead;
	private transient int timeLastDamageTaken = -1000;
	private Tile tile;
	private transient boolean isSelected;
	
	private transient HasImage hasImage;
	private transient Mesh mesh;
	private transient String textureFile;
	
	private transient Hitsplat[] hitsplats = new Hitsplat[4];
	
	public Thing(double maxHealth, HasImage hasImage, HasMesh hasMesh, Faction faction) {
		health = maxHealth;
		this.maxHealth = maxHealth;
		this.hasImage = hasImage;
		this.mesh = hasMesh.getMesh();
		this.textureFile = hasMesh.getTextureFile();
		setFaction(faction);
		this.id = idCounter++;
		ThingMapper.created(this);
	}
	public Thing(double maxHealth, HasImage hasImage, HasMesh hasMesh, Faction faction, Tile tile) {
		this(maxHealth, hasImage, hasMesh, faction);
		this.tile = tile;
	}
	
	@Override
	public Mesh getMesh() {
		return mesh;
	}
	@Override
	public String getTextureFile() {
		return textureFile;
	}
	
	public int id() {
		return id;
	}
	public void setID(int id) {
		this.id = id;
	}
	
	public void setImage(HasImage hasImage) {
		this.hasImage = hasImage;
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
	
	public boolean isDead() {
		return health <= 0 || isDead;
	}
	/**
	 * @return true if this is lethal damage, false otherwise
	 */
	public boolean takeDamage(int damage) {
		boolean before = isDead();
		health -= damage;
		if(damage != 0) {
			timeLastDamageTaken = World.ticks;
			getFaction().gotAttacked(getTile());
		}
		addHitsplat(damage);
		// Return true if isDead changed from false to true.
		return !before && isDead();
	}
	
	public void takeFakeDamage() {
		timeLastDamageTaken = World.ticks;
	}
	
	public void heal(double healing, boolean hitsplat) {
		int roundedHealing = (int)Math.ceil(healing);
		double tempHealing = roundedHealing;
		if((health + healing) > this.maxHealth) {
			tempHealing = this.maxHealth - health;
		}
		int finalHealing = (int)tempHealing;
		health += finalHealing;
		if(finalHealing != 0) {
			timeLastDamageTaken = World.ticks;
		}
		if(hitsplat == true && finalHealing > 0) {
			addHitsplat(-finalHealing);
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
	}
	
	public Tile getTile() {
		return tile;
	}
	
	@Override
	public Image getImage(int size) {
		return hasImage.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return hasImage.getShadow(size);
	}
	@Override
	public Image getHighlight(int size) {
		return hasImage.getHighlight(size);
	}
	@Override
	public ImageIcon getImageIcon(int size) {
		return hasImage.getImageIcon(size);
	}
	@Override
	public Color getColor(int size) {
		return hasImage.getColor(size);
	}
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(
				String.format("HP=%.0f/%.0f", getHealth(), getMaxHealth())
				));
	}

}
