package utils;
import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ui.*;
import world.*;

public class Thing implements HasImage {
	
	private double maxHealth;
	private double health;
	private int timeLastDamageTaken = Integer.MIN_VALUE;
	private Tile tile;
	
	private HasImage hasImage;
	
	public Thing(double maxHealth, HasImage hasImage) {
		health = maxHealth;
		this.maxHealth = maxHealth;
		this.hasImage = hasImage;
	}
	public Thing(double maxHealth, HasImage hasImage, Tile tile) {
		this(maxHealth, hasImage);
		this.tile = tile;
	}

	public boolean isDead() {
		return health < 0;
	}
	public void takeDamage(double damage) {
		health -= damage;
		timeLastDamageTaken = Game.ticks;
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
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(String.format("HP=%." + Game.NUM_DEBUG_DIGITS + "f", getHealth())));
	}

}
