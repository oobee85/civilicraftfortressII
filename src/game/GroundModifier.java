package game;

import ui.*;
import world.Tile;

public class GroundModifier {

	
	private GroundModifierType type;
	private int aliveUntil;
	private int duration;
	private Tile tile;
	
	public GroundModifier(GroundModifierType type, Tile tile, int duration) {
		this.type = type;
		this.tile = tile;
		this.aliveUntil = Game.ticks + duration;
		this.duration = duration;
	}
	public void refreshDuration() {
		this.aliveUntil = Game.ticks + duration;
	}
	
	public GroundModifierType getType() {
		return type;
	}
	public boolean isDead() {
		return Game.ticks >= aliveUntil;
	}
	public int timeLeft() {
		return aliveUntil - Game.ticks;
	}
	public boolean updateTime() {
		return isDead();
	}
	public void addDuration(int duration) {
		aliveUntil += duration;
	}
	
	public void finish() {
		aliveUntil = Game.ticks;
	}
	public Tile getTile() {
		return tile;
	}
	
	
}
