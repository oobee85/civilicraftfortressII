package game;

import ui.*;
import world.*;

public class AttackedNotification {
	private static final int DURATION = 20;

	private final int expires;
	public final Tile tile;

	public AttackedNotification(Tile tile) {
		this.expires = World.ticks + DURATION;
		this.tile = tile;
	}
	public boolean isExpired() {
		return World.ticks >= expires;
	}
	
}
