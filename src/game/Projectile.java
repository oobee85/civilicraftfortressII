package game;

import java.awt.Color;
import java.awt.Image;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import pathfinding.Pathfinding;
import utils.*;
import world.Terrain;
import world.Tile;

public class Projectile implements HasImage {

	private ProjectileType type;
	private Tile targetTile;
	private double timeToMove;
	private Tile tile;
	private HasImage hasImage;
	
	private int damageBuff;
	private Unit source;
	
	public Projectile(ProjectileType type, Tile tile, Tile targetTile, Unit source) {
		this.type = type;
		this.tile = tile;
		this.hasImage = type;
		this.targetTile = targetTile;
		this.damageBuff = 0;
		this.source = source;
		this.timeToMove = type.getSpeed();
	}
	
	public Unit getSource() {
		return source;
	}
	
	public void setDamageBuff(int damageBuff) {
		this.damageBuff = damageBuff;
	}
	
	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
	}
	
	private void moveTo(Tile t) {
		getTile().removeProjectile(this);
		t.addProjectile(this);
		this.setTile(t);
	}
	public void moveToTarget() {
		if(!readyToMove()) {
			return;
		}
		if(this.getTargetTile() == null) {
			return;
		}
		Tile nextTile = this.tile;
		for(Tile t : tile.getNeighbors()) {
			if(t.getLocation().distanceTo(this.targetTile.getLocation()) < nextTile.getLocation().distanceTo(this.targetTile.getLocation())) {
				nextTile = t;
			}
		}
		moveTo(nextTile);
		resetTimeToMove();
	}
	public boolean reachedTarget() {
		return this.targetTile == this.tile;
	}
	public void setTile(Tile t) {
		this.tile = t;
	}
	public void setTargetTile(Tile target) {
		this.targetTile = target;
	}
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	public double getTimeToMove() {
		return timeToMove;
	}
	public void resetTimeToMove() {
		timeToMove = type.getSpeed();
	}
	public Tile getTargetTile() {
		return targetTile;
	}
	public Tile getTile() {
		return tile;
	}
	public ProjectileType getType() {
		return type;
	}
	
	public String toString() {
		return type.toString();
	}
	public Image getImage(int size) {
		return hasImage.getImage(size);
	}
	public ImageIcon getImageIcon(int size) {
		return hasImage.getImageIcon(size);
	}
	public Color getColor(int size) {
		return hasImage.getColor(size);
	}
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(
				String.format("TTM=%.0f", getTimeToMove())
				));
	}
	
	
	
}
