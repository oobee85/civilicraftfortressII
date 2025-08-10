package sounds;

import utils.TileLoc;

public interface VolumeQueryInterface {
	public TileLoc getScreenTopLeftLocation();
	public TileLoc getScreenBottomRightLocation();
	public float getGlobalSoundVolume();
	public float getMusicSoundVolume();
	public float getEffectsSoundVolume();
}
