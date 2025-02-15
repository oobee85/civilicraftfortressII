package sounds;

import utils.TileLoc;

public class Sound2 {

	private final SoundEffect soundEffect;
	private final TileLoc sourceLocation;
	
	public Sound2(SoundEffect soundEffect, TileLoc sourceLocation) {
		this.soundEffect = soundEffect;
		this.sourceLocation = sourceLocation;
	}
	
	public SoundEffect getSoundEffect() {
		return soundEffect;
	}
	
	public TileLoc getSourceLocation() {
		return sourceLocation;
	}
}
