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
	
	private HasImage hasImage;
	private boolean sideHealthBar;
	
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
		return health < 0;
	}
	public void takeDamage(double damage) {
		health -= damage;
		if(damage != 0) {
			timeLastDamageTaken = Game.ticks;
		}
	}
	public double getHealth() {
		return health;
	}
	public double getMaxHealth() {
		return maxHealth;
	}
	public int getTimeLastDamageTaken() {
		return timeLastDamageTaken;
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
