package game;

import world.Tile;

public class GroundModifier {

	
	private GroundModifierType type;
	private int timeAlive;
	private int maxTime;
	private Tile tile;
	
	public GroundModifier(GroundModifierType type, Tile tile) {
		this.type = type;
		timeAlive = 0;
		this.tile = tile;
		maxTime = (int) (Math.random()*type.getMaxTime());
	}
	
	public GroundModifierType getType() {
		return type;
	}
	
	public int timeLeft() {
		return maxTime - timeAlive;
	}
	public boolean updateTime() {
		timeAlive ++;
		return timeAlive >= maxTime;
	}
	public Tile getTile() {
		return tile;
	}
	
	
}
