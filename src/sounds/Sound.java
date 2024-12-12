package sounds;

import game.Faction;

public class Sound {

	private SoundEffect sound;
	private Faction faction;
	

	public Sound(SoundEffect sound, Faction faction) {
		this.sound = sound;
		this.faction = faction;
	}
	
	public Faction getFaction() {
		return faction;
	}
	public SoundEffect getSoundEffect() {
		return sound;
	}
	
}
