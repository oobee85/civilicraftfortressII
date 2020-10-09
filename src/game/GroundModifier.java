package game;

import world.Tile;

public class GroundModifier {

	
	private GroundModifierType type;
	private int timeAlive;
	private int maxTime;
	private Tile tile;
	
	public GroundModifier(GroundModifierType type, Tile tile, int duration) {
		this.type = type;
		this.timeAlive = 0;
		this.tile = tile;
		this.maxTime = duration;
	}
	public void refreshDuration() {
		timeAlive = 0;
	}
	
	public GroundModifierType getType() {
		return type;
	}
	public boolean isDead() {
		return timeAlive >= maxTime;
	}
	public int timeLeft() {
		return maxTime - timeAlive;
	}
	public boolean updateTime() {
		timeAlive ++;
		return timeAlive >= maxTime;
	}
	public void addDuration(int duration) {
		timeAlive -= duration;
	}
	
	public void finish() {
		timeAlive = maxTime;
	}
	public Tile getTile() {
		return tile;
	}
	
	
}
