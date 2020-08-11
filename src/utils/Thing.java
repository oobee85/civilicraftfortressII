package utils;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import game.*;
import ui.*;
import world.*;

public class Thing implements HasImage {
	
	private double maxHealth;
	private double health;
	private int timeLastDamageTaken = -1000;
	private Tile tile;
	private Tile targetTile;
	private boolean isSelected;
	
	private HasImage hasImage;
	private boolean sideHealthBar;
//	private LinkedList<Hitsplat> hitsplats = new LinkedList<Hitsplat>();
	private Hitsplat[] hitsplats = new Hitsplat[4];
	
	private String name;
	
	public Thing(double maxHealth, HasImage hasImage) {
		health = maxHealth;
		this.maxHealth = maxHealth;
		this.hasImage = hasImage;
		sideHealthBar = false;
		if(hasImage instanceof UnitType) {
			UnitType t = (UnitType)hasImage;
			if(t == UnitType.ARCHER || t == UnitType.HORSEMAN || t == UnitType.SWORDSMAN || t == UnitType.SPEARMAN || t == UnitType.WORKER) {
				sideHealthBar = true;
			}
		}
	}
	public Thing(double maxHealth, HasImage hasImage, Tile tile) {
		this(maxHealth, hasImage);
		this.tile = tile;
	}
	
	public boolean isSideHealthBar() {
		return sideHealthBar;
	}

	public boolean isDead() {
		return health <= 0;
	}
	public void takeDamage(double damage) {
		int roundedDamage = (int)Math.ceil(damage);
		health -= roundedDamage;
		if(damage != 0) {
			timeLastDamageTaken = Game.ticks;
		}
		addHitsplat(roundedDamage);
	}
	public void heal(double healing) {
		int roundedHealing = (int)Math.ceil(healing);
		health += roundedHealing;
		if(roundedHealing != 0) {
			timeLastDamageTaken = Game.ticks;
		}
		if(roundedHealing > 2) {
			addHitsplat(-roundedHealing);
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
		if(targetTile == getTile() ) {
			targetTile = null;
		}
	}
	
	public Tile getTile() {
		return tile;
	}
	
	public Tile getTargetTile() {
		return targetTile;
	}
	public void setTargetTile(Tile t) {
		if(t != getTile()) {
			targetTile = t;
		}
	}
	@Override
	public Image getImage(int size) {
		return hasImage.getImage(size);
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
		return new LinkedList<String>(Arrays.asList(String.format("HP=%." + Game.NUM_DEBUG_DIGITS + "f", getHealth())));
	}

}
