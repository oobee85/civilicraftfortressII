package sounds;

import game.Faction;
import utils.TileLoc;
import world.Tile;

public class Sound {

	private SoundEffect sound;
	private Faction faction;
	private Tile tile;
	private boolean isMusic;
	private float baseVolume;

	public Sound(SoundEffect sound, Faction faction, float baseVolume) {
		this.sound = sound;
		this.faction = faction;
		this.tile = null;
		this.isMusic = sound.getIsMusic();
		this.baseVolume = baseVolume;
	}
	public Sound(SoundEffect sound, Faction faction, Tile tile, float baseVolume) {
		this.sound = sound;
		this.faction = faction;
		this.tile = tile;
		this.baseVolume = baseVolume;
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
	public boolean getIsMusic() {
		return this.isMusic;
	}
	
	public void updateVolume(float newBaseVolume) {
		this.baseVolume = newBaseVolume;
	}
	
	public float getVolume() {
		return this.baseVolume;
	}
}
