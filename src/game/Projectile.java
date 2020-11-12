package game;

import java.awt.Color;
import java.awt.Image;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import ui.*;
import utils.*;
import world.*;

public class Projectile implements HasImage, Externalizable {

	private ProjectileType type;
	private Tile targetTile;
	private double timeToMove;
	private Tile tile;
	private HasImage hasImage;
	
	private Unit source;
	private int damage;
	private int totalDistance;
	public int currentHeight = 0;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = ProjectileType.values()[in.readByte()];
		targetTile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
		tile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
		damage = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(type.ordinal());
		targetTile.getLocation().writeExternal(out);
		tile.getLocation().writeExternal(out);
		out.writeInt(damage);
	}
	
	/** used by Externalizable interface */
	public Projectile() {
		
	}
	
	public Projectile(ProjectileType type, Tile tile, Tile targetTile, Unit source, int damage) {
		this.type = type;
		this.tile = tile;
		this.hasImage = type;
		this.targetTile = targetTile;
		this.source = source;
		this.timeToMove = type.getSpeed();
		this.damage = damage;
		totalDistance = tile.getLocation().distanceTo(targetTile.getLocation());
	}
	
	public int getDamage() {
		return damage;
	}
	public int getHeight() {
		return currentHeight;
	}
	public int getMaxHeight() {
		return (int) (totalDistance*totalDistance*type.getSpeed()/4);
	}
	
	public double getExtraSize() {
		int div = (int) (7 - getType().getSpeed());
		div = div < 1 ? 1 : div;
		return currentHeight/div/30.0;
	}
	
	public Unit getSource() {
		return source;
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
		int nextDistance = nextTile.getLocation().distanceTo(this.targetTile.getLocation());
		for(Tile t : tile.getNeighbors()) {
			int dist = t.getLocation().distanceTo(this.targetTile.getLocation());
			if(dist < nextDistance) {
				nextTile = t;
				nextDistance = nextTile.getLocation().distanceTo(this.targetTile.getLocation());
			}
		}
		moveTo(nextTile);
		resetTimeToMove();
		currentHeight = (int) (nextDistance * (totalDistance - nextDistance)*type.getSpeed());
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
	public Image getShadow(int size) {
		return hasImage.getShadow(size);
	}
	@Override
	public Image getHighlight(int size) {
		return hasImage.getHighlight(size);
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
