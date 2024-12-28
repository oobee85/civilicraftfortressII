package sounds;

import game.Faction;
import utils.TileLoc;
import world.Tile;

public class Sound {

	private SoundEffect sound;
	private Faction faction;
	private Tile tile;
	

	public Sound(SoundEffect sound, Faction faction) {
		this.sound = sound;
		this.faction = faction;
	}
	public Sound(SoundEffect sound, Faction faction, Tile tile) {
		this.sound = sound;
		this.faction = faction;
		this.tile = tile;
	}
	
	public Faction getFaction() {
		return faction;
	}
	public SoundEffect getSoundEffect() {
		return sound;
	}
	public Tile getTile() {
		return tile;
	}
	
}
