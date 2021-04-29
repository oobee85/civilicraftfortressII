package game;

import java.awt.Color;
import java.awt.Image;
import java.io.*;

import javax.swing.ImageIcon;

import game.liquid.*;
import utils.*;
import world.*;

public class WeatherEvent implements Externalizable {

	
	private double strength;
	private int aliveUntil;
	private Tile tile;
	private Tile targetTile;
	private double timeToMove;
	private int speed;
	private LiquidType liquidType;
	private boolean isCold;

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		tile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
		targetTile = new Tile(TileLoc.readFromExternal(in), Terrain.DIRT);
//		aliveUntil = in.readInt();
		strength = in.readDouble();
		liquidType = LiquidType.values()[in.readByte()];
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		tile.getLocation().writeExternal(out);
		targetTile.getLocation().writeExternal(out);
//		out.writeInt(aliveUntil);
		out.writeDouble(strength);
		out.writeByte(liquidType.ordinal());
	}
	
	/** used only by externalizable interface */
	public WeatherEvent() { }
	
	public WeatherEvent(Tile tile, Tile targetTile, double strength, LiquidType liquidType) {
		this.targetTile = targetTile;
		this.tile = tile;
//		this.aliveUntil = World.ticks + duration;
		this.strength = strength;
		this.liquidType = liquidType;
		this.isCold = false;
		this.speed = WeatherEventType.RAIN.getSpeed();
		
	}
	public int getSpeed() {
		return speed;
	}
	public double getStrength() {
		return strength;
	}
	public void addStrength(double added) {
		strength += added;
	}
	public LiquidType getLiquidType() {
		return liquidType;
	}
	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
		updateColdness();
	}
	private void updateColdness() {
//		isCold = tile.airTemperature();
//		if(isCold == true) {
//			this.mipmap = WeatherEventType.SNOW;
//			liquidType = LiquidType.SNOW;
//			strength = 0.0003;
//		}else {
//			this.mipmap = WeatherEventType.RAIN;
//			liquidType = LiquidType.WATER;
//			strength = 0.0002;
//		}
	}
	
	private void moveTo(Tile t) {
		getTile().removeWeather();
		t.setWeather(this);
		this.setTile(t);
		updateColdness();
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
			if(t.hasWeather() == true) {
				continue;
			}
			int dist = t.getLocation().distanceTo(this.targetTile.getLocation());
			if(dist < nextDistance) {
				nextTile = t;
				nextDistance = nextTile.getLocation().distanceTo(this.targetTile.getLocation());
			}
		}
		moveTo(nextTile);
		resetTimeToMove();
	}
	public boolean readyToMove() {
		return timeToMove <= 0;
	}
	public double getTimeToMove() {
		return timeToMove;
	}
	public void resetTimeToMove() {
		timeToMove = speed;
	}
	
	public boolean isDead() {
		return strength <= 0;
//		return World.ticks >= aliveUntil;
	}
	public int timeLeft() {
		return aliveUntil - World.ticks;
	}
	
	public Tile getTile() {
		return tile;
	}
	public void setTile(Tile t) {
		this.tile = t;
	}
	public Tile getTargetTile() {
		return targetTile;
	}
	public void setTargetTile(Tile t) {
		this.targetTile = t;
	}
}
