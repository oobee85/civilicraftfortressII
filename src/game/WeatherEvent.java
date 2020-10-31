package game;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;

import liquid.LiquidType;
import utils.HasImage;
import utils.MipMap;
import world.Tile;
import world.World;

public class WeatherEvent implements HasImage{

	
	private double strength;
	private int aliveUntil;
	private Tile tile;
	private Tile targetTile;
	private int duration;
	private double timeToMove;
	private int speed = 2;
	private HasImage hasImage;
	private LiquidType liquidType;
	private MipMap mipmap;
	private boolean isCold;
	
	
	public WeatherEvent(Tile tile, Tile targetTile, int duration, double strength, LiquidType liquidType) {
		this.targetTile = targetTile;
		this.tile = tile;
		this.aliveUntil = World.ticks + duration;
		this.strength = strength;
		this.duration = duration;
		this.liquidType = liquidType;
		this.hasImage = WeatherEventType.RAIN;
		this.isCold = false;
		
	}
	public int getSpeed() {
		return speed;
	}
	public double getStrength() {
		return strength;
	}
	public LiquidType getLiquidType() {
		return liquidType;
	}
	public void tick() {
		if(timeToMove > 0) {
			timeToMove -= 1;
		}
		
		isCold = tile.isCold();
		if(isCold == true) {
			this.hasImage = WeatherEventType.SNOW;
			liquidType = LiquidType.SNOW;
			strength = 0.0005;
		}else {
			this.hasImage = WeatherEventType.RAIN;
			liquidType = LiquidType.WATER;
			strength = 0.00002;
		}
	}
	
	private void moveTo(Tile t) {
		getTile().removeWeather();
		t.setWeather(this);
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
	public void refreshDuration() {
		this.aliveUntil = World.ticks + duration;
	}
	
	public boolean isDead() {
		return World.ticks >= aliveUntil;
	}
	public int timeLeft() {
		return aliveUntil - World.ticks;
	}
	public boolean updateTime() {
		return isDead();
	}
	public void addDuration(int duration) {
		aliveUntil += duration;
	}
	
	public void finish() {
		aliveUntil = World.ticks;
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
	public Image getImage(int size) {
		return hasImage.getImage(size);
	}
	@Override
	public Image getShadow(int size) {
		return null;
	}
	@Override
	public ImageIcon getImageIcon(int size) {
		return hasImage.getImageIcon(size);
	}
	@Override
	public Color getColor(int size) {
		return hasImage.getColor(size);
	}
}
