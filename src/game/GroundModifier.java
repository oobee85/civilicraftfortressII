package game;

import world.Tile;

public class GroundModifier {

	
	private GroundModifierType type;
	private int timeAlive;
	private Tile tile;
	
	public GroundModifier(GroundModifierType type, Tile tile) {
		this.type = type;
		timeAlive = 0;
		this.tile = tile;
	}
	
	public GroundModifierType getType() {
		return type;
	}
	
	public int timeLeft() {
		return type.getMaxTime() - timeAlive;
	}
	public boolean updateTime() {
		timeAlive ++;
		return timeAlive >= type.getMaxTime();
	}
	public Tile getTile() {
		return tile;
	}
	
	
}
