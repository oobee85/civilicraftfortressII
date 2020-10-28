package game;

import ui.*;
import world.*;

public class GroundModifier {

	
	private GroundModifierType type;
	private int aliveUntil;
	private int duration;
	private Tile tile;
	
	public GroundModifier(GroundModifierType type, Tile tile, int duration) {
		this.type = type;
		this.tile = tile;
		this.aliveUntil = World.ticks + duration;
		this.duration = duration;
	}
	public void refreshDuration() {
		this.aliveUntil = World.ticks + duration;
	}
	
	public GroundModifierType getType() {
		return type;
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
	
	
}
