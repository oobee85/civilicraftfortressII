package sounds;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import javax.sound.sampled.*;

import utils.*;

public class SoundManager {
	
	private static final String basePath = "sounds/";
	private static final Map<SoundEffect, Future<byte[]>> soundData = new HashMap<>();
	
	private static LinkedBlockingQueue<Sound2> queuedUpSoundEffects = new LinkedBlockingQueue<>();
	private static LinkedList<PlayingSound> readyToPlay = new LinkedList<>();
	private static Set<PlayingSound> currentlyPlayingSounds = new HashSet<>();
	private static Map<SoundEffect, LinkedList<Clip>> previouslyUsedClips = new HashMap<>();
	private static ArrayList<SoundEffect> musicQueue = new ArrayList<>();
	private static int currentMusicIndex = 0;
	public static void queueSoundEffect(SoundEffect soundEffect) {
		queueSoundEffect(soundEffect, null);
	}
	public static void queueSoundEffect(SoundEffect soundEffect, TileLoc sourceLocation) {
		Sound2 sound = new Sound2(soundEffect, sourceLocation);
		queuedUpSoundEffects.add(sound);
	}
	
	private static Semaphore songFinished = new Semaphore(0);
	public static void initializeMusicQueue() {
		for (SoundEffect soundEffect : SoundEffect.values()) {
			if (soundEffect.getIsMusic()) {
				musicQueue.add(soundEffect);
			}
		}
		
		Thread musicThread = new Thread(() -> {
			while(true) {
				try {
					songFinished.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				SoundEffect nextSong = musicQueue.get(currentMusicIndex);
				currentMusicIndex = (currentMusicIndex + 1) % musicQueue.size();
				
				Clip clip = getClipForSoundEffect(nextSong);
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if (event.getType() == LineEvent.Type.STOP) {
							songFinished.release();
						}
					}
				});
				readyToPlay.add(new PlayingSound(nextSong, null, clip));
			}
		});
		musicThread.start();
	}
	
	public static void startMusic() {
		songFinished.release();
	}
	
	public static void transitionToSong(SoundEffect song) {
		// fade out currently playing song
		// remember where in the song it is for later?
		
		// fade in requested song
	}
	
	private static Clip getClipForSoundEffect(SoundEffect soundEffect) {
		if (previouslyUsedClips.containsKey(soundEffect)) {
			LinkedList<Clip> usedClips = previouslyUsedClips.get(soundEffect);
			if (!usedClips.isEmpty()) {
				Clip usedClip = usedClips.removeFirst();
				return usedClip;
			}
		}
		Future<byte[]> futureBytes = soundData.get(soundEffect);
		try {
			byte[] data = futureBytes.get();
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(data));
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			return clip;
		} catch (InterruptedException | ExecutionException | UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

	// one thread to pull sounds off queue and create the clip
	public static void startJukeboxThread(VolumeQueryInterface volume) {
		Thread jukeboxThread = new Thread(() -> {
			while(true) {
				try {
					Sound2 toPlay = queuedUpSoundEffects.take();
					Clip clip = getClipForSoundEffect(toPlay.getSoundEffect());
					if (clip == null) {
						System.err.println("Failed to get clip for " + toPlay.getSoundEffect());
						queuedUpSoundEffects.add(toPlay);
						continue;
					}
					readyToPlay.addLast(new PlayingSound(toPlay.getSoundEffect(), toPlay.getSourceLocation(), clip));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		jukeboxThread.start();
	}
	// another thread to 
	//		- start sound clips
	// 		- update volume based on current camera position & global volume setting
	//		- remove sounds that have finished
	public static void startVolumeUpdateThread(VolumeQueryInterface volume) {
		ArrayList<PlayingSound> finished = new ArrayList<>(20);
		Thread volumeUpdateThread = new Thread(() -> {
			float previousGlobalVolume = -1f;
			TileLoc previousTopLeft = null;
			TileLoc previousBottomRight = null;
			while(true) {
				while(!readyToPlay.isEmpty()) {
					PlayingSound ready = readyToPlay.removeFirst();
					setVolumeOnClip(ready.getClip(), ready.getSourceLocation(), volume);
					ready.getClip().setFramePosition(0);
					ready.getClip().start();
					currentlyPlayingSounds.add(ready);
				}
				float globalVol = volume.getGlobalSoundVolume();
				TileLoc topLeft = volume.getScreenTopLeftLocation();
				TileLoc bottomRight = volume.getScreenBottomRightLocation();
				boolean volumeChanged = (globalVol != previousGlobalVolume) 
						|| !topLeft.equals(previousTopLeft)
						|| !bottomRight.equals(previousBottomRight);
				previousGlobalVolume = globalVol;
				previousTopLeft = topLeft;
				previousBottomRight = bottomRight;
				
				if (volumeChanged) {
					for (PlayingSound playing : currentlyPlayingSounds) {
						setVolumeOnClip(playing.getClip(), playing.getSourceLocation(), volume);
						if (!playing.getClip().isRunning()
								&& playing.getClip().getFramePosition() == playing.getClip().getFrameLength()) {
							finished.add(playing);
							if (!previouslyUsedClips.containsKey(playing.getSoundEffect())) {
								previouslyUsedClips.put(playing.getSoundEffect(), new LinkedList<Clip>());
							}
							previouslyUsedClips.get(playing.getSoundEffect()).addLast(playing.getClip());
						}
					}
					currentlyPlayingSounds.removeAll(finished);
					finished.clear();
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		volumeUpdateThread.start();
	}
	
	// loads a sound
	public static void loadSound(SoundEffect sound) {
		if(soundData.containsKey(sound)) {
			System.err.println("ERROR trying to load the same sound again! " + sound.getFileName());
			return;
		}
		Future<byte[]> futureBytes =  Utils.executorService.submit(() -> {
			try (InputStream stream = SoundManager.class.getClassLoader().getResourceAsStream(basePath + sound.getFileName())) {
//				soundData.put(sound, stream.readAllBytes());
				return stream.readAllBytes();
			} catch (IOException e) {
				System.err.println("Couldnt load sound: " + sound.getFileName());
				e.printStackTrace();
			}
			return new byte[0];
		});
		soundData.put(sound, futureBytes);
	}

	
	private static TileLoc getClosestScreenLoc(TileLoc sourceTile, TileLoc screenTopLeft, TileLoc screenBottomRight) {
    	int closestX = sourceTile.x();
    	if (sourceTile.x() < screenTopLeft.x()) {
    		closestX = screenTopLeft.x();
    	}
    	else if (sourceTile.x() > screenBottomRight.x()) {
    		closestX = screenBottomRight.x();
    	}
    	
    	int closestY = sourceTile.y();
    	if (sourceTile.y() < screenTopLeft.y()) {
    		closestY = screenTopLeft.y();
    	}
    	else if (sourceTile.y() > screenBottomRight.y()) {
    		closestY = screenBottomRight.y();
    	}
    	
    	return new TileLoc(closestX, closestY);
    }
	
	private static void setVolumeOnClip(Clip clip, TileLoc sourceTile, VolumeQueryInterface volume) {
	    if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
	        System.err.println("Volume control not supported for clip: " + clip);
	        return;
	    }
	    
	    float globalVolume = volume.getGlobalSoundVolume();
	    
	    if (globalVolume < 0f || globalVolume > 1f) {
	    	System.err.println("INVALID GLOBAL VOLUME: " + globalVolume);
	    	return;
	    }
	    
	    float distanceVolume = 1f;
	    float zoomLevelMultiplier = 1f;
	    
	    if (sourceTile != null) {
	    	TileLoc screenTopLeft = volume.getScreenTopLeftLocation();
	    	TileLoc screenBottomRight = volume.getScreenBottomRightLocation();
//	    	System.out.println("screen: " + screenTopLeft + ", " + screenBottomRight);
	    	
	    	int screenDiagonal = screenTopLeft.distanceTo(screenBottomRight);
	    	zoomLevelMultiplier = 1f - screenDiagonal/60f;
	    	int maxDistanceToHear = (screenDiagonal / 2);
	    	if (maxDistanceToHear < 1) {
	    		distanceVolume = 0f;
	    	}
	    	else {
		    	TileLoc closestLocOnScreen = getClosestScreenLoc(sourceTile, screenTopLeft, screenBottomRight);
		    	float distanceFromScreen = (float) closestLocOnScreen.distanceTo(sourceTile);
		    	distanceVolume = (maxDistanceToHear - distanceFromScreen) / maxDistanceToHear;
		    	distanceVolume = (distanceVolume < 0) ? 0 : distanceVolume;
//		    	System.out.println("closestLocOnScreen: " + closestLocOnScreen + ", distanceFromScreen: " + distanceFromScreen);
	    	}
	    }

	    FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

	    float mixedVolume = globalVolume * distanceVolume * zoomLevelMultiplier;
	    mixedVolume = (mixedVolume <= 0.0f) ? 0.0001f : ((mixedVolume>1.0f) ? 1.0f : mixedVolume);
	    
	    float db = (float)(20.0 * Math.log(mixedVolume) / Math.log(10.0));
        float minGain = volumeControl.getMinimum();  // Typically around -80 dB
        float maxGain = volumeControl.getMaximum();  // Typically around 6 dB
        db = (db < minGain) ? minGain : ((db > maxGain) ? maxGain : db);
        volumeControl.setValue(db);
	}
}