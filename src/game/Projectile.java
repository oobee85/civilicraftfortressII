package game;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import utils.*;
import world.*;

public class Projectile implements Externalizable {

	private ProjectileType type;
	private Tile targetTile;
	private double timeToMove;
	private Tile tile;
	
	private Unit source;
	private int damage;
	private int totalDistance;
	private boolean fromGround;
	private int ticksUntilLanding;
	public int currentHeight = 0;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = ProjectileType.values()[in.readByte()];
		targetTile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
		tile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
		damage = in.readInt();
		fromGround = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(type.ordinal());
		targetTile.getLocation().writeExternal(out);
		tile.getLocation().writeExternal(out);
		out.writeInt(damage);
		out.writeBoolean(fromGround);
	}
	
	/** used by Externalizable interface */
	public Projectile() {
		
	}
	
	public Projectile(ProjectileType type, Tile tile, Tile targetTile, Unit source, int damage, boolean fromGround, int ticksUntilLanding) {
		this.type = type;
		this.tile = tile;
		this.targetTile = targetTile;
		this.source = source;
		this.timeToMove = type.getSpeed();
		this.damage = damage;
		this.fromGround = fromGround;
		this.ticksUntilLanding = ticksUntilLanding;
		totalDistance = tile.getLocation().distanceTo(targetTile.getLocation());
		updateCurrentHeight(totalDistance);
	}
	public Projectile(ProjectileType type, Tile tile, Tile targetTile, Unit source, int damage) {
		this(type, tile, targetTile, source, damage, true, 0);
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
		if(ticksUntilLanding > 0) {
			ticksUntilLanding -= 1;
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
		updateCurrentHeight(nextDistance);
	}
	private void updateCurrentHeight(int nextDistance) {
		if(fromGround) {
			currentHeight = (int) (nextDistance * (totalDistance - nextDistance)*type.getSpeed() + ticksUntilLanding*3);
		}
		else {
			currentHeight = nextDistance + ticksUntilLanding*3;
		}
	}
	public boolean reachedTarget() {
		return this.targetTile == this.tile && ticksUntilLanding <= 0;
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
	public boolean getFromGround() {
		return fromGround;
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
	public boolean isLightProjectile() {
		if(this.getType() == ProjectileType.ARROW || this.getType() == ProjectileType.RUNE_ARROW || this.getType() == ProjectileType.BULLET) {
			return true;
		}
		return false;
	}
	public boolean isHeavyProjectile() {
		if(this.getType() == ProjectileType.ROCK_STONE_GOLEM || this.getType() == ProjectileType.ROCK || this.getType() == ProjectileType.FIREBALL_TREBUCHET) {
			return true;
		}
		return false;
	}
	public String toString() {
		return type.toString();
	}
	
	public List<String> getDebugStrings() {
		return new LinkedList<String>(Arrays.asList(
				String.format("TTM=%.0f", getTimeToMove())
				));
	}
}
