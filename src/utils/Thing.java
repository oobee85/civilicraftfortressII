package utils;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import game.*;
import ui.*;
import world.*;

public class Thing implements HasImage {

	private Faction faction;
	private double maxHealth;
	private double health;
	private boolean isDead;
	private int timeLastDamageTaken = -1000;
	private Tile tile;
	private boolean isSelected;
	
	private HasImage hasImage;
//	private LinkedList<Hitsplat> hitsplats = new LinkedList<Hitsplat>();
	private Hitsplat[] hitsplats = new Hitsplat[4];
	
	private String name;
	
	public Thing(double maxHealth, HasImage hasImage, Faction faction) {
		health = maxHealth;
		this.maxHealth = maxHealth;
		this.hasImage = hasImage;
		this.faction = faction;
	}
	public Thing(double maxHealth, HasImage hasImage, Faction faction, Tile tile) {
		this(maxHealth, hasImage, faction);
		this.tile = tile;
	}
	
	public void setImage(HasImage hasImage) {
		this.hasImage = hasImage;
	}
	
	public Faction getFaction() {
		return faction;
	}
	
	public void setFaction(Faction faction) {
		this.faction = faction;
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
	public boolean takeDamage(double damage) {
		boolean before = isDead();
		health -= damage;
		if(damage != 0) {
			timeLastDamageTaken = Game.ticks;
			getFaction().gotAttacked(getTile());
		}
		addHitsplat((int)(Math.ceil(damage)));
		// Return true if isDead changed from false to true.
		return !before && isDead();
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
			timeLastDamageTaken = Game.ticks;
		}
		if(hitsplat == true && finalHealing > 2) {
			addHitsplat(-finalHealing);
		}
	}
	private void addHitsplat(int damage) {
		int oldest = 0;
		for(int i = 0; i < hitsplats.length; i++) {
			if(hitsplats[i] == null) {
				oldest = i;
				break;
			}
			oldest = hitsplats[i].getMaxDuration() < hitsplats[oldest].getMaxDuration() ? i : oldest;
		}
		Hitsplat hit = new Hitsplat(damage, oldest);
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
	public double getHitsplatDamage() {
		for(int i = 0; i < hitsplats.length; i++) {
			if(hitsplats[i] != null) {
				return hitsplats[i].getDamage();
			}
		}
		return 0;
	}
	public Hitsplat[] getHitsplatList() {
		return hitsplats;
	}
	public void setIsSelected(boolean select) {
		isSelected = select;
	}
	public boolean getIsSelected() {
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
