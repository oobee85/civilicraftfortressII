package sounds;

import javax.sound.sampled.Clip;

import utils.TileLoc;

public class PlayingSound {
	private final SoundEffect soundEffect;
	private final TileLoc sourceLocation;
	private final Clip clip;

	public PlayingSound(SoundEffect effect, TileLoc sourceLocation, Clip clip) {
		this.soundEffect = effect;
		this.sourceLocation = sourceLocation;
		this.clip = clip;
	}
	
	public SoundEffect getSoundEffect() {
		return soundEffect;
	}

	public TileLoc getSourceLocation() {
		return sourceLocation;
	}

	public Clip getClip() {
		return clip;
	}
}
