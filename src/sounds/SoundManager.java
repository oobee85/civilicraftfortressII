package sounds;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.*;

import ui.graphics.vanilla.VanillaDrawer;
import utils.TileLoc;

public class SoundManager {

	private static final Map<SoundEffect, Clip> sounds = new HashMap<>();
	private static final String basePath = "sounds/";

	public static LinkedBlockingQueue<Sound> theSoundQueue = new LinkedBlockingQueue<Sound>();
	public static LinkedBlockingQueue<SoundEffect> theMusicEffectQueue = new LinkedBlockingQueue<SoundEffect>();
	public static LinkedBlockingQueue<Sound> theMusicQueue = new LinkedBlockingQueue<Sound>();
	
	
	// loads a sound
	public static void loadSound(SoundEffect sound) {
		if(sounds.containsKey(sound)) {
			return;
		}
		try (InputStream stream = SoundManager.class.getClassLoader().getResourceAsStream(basePath + sound.getFileName()); 
				BufferedInputStream buf = new BufferedInputStream(stream);
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(buf);
				) {
			
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			sounds.put(sound, clip);
		} catch (UnsupportedAudioFileException | IOException | ExceptionInInitializerError | NullPointerException | LineUnavailableException e) {
			System.err.println("Couldnt load sound: " + sound.getFileName());
			e.printStackTrace();
		}
	}
	public static boolean isSoundRunning(Sound sound) {
		if(sound == null) {
			return false;
		}
		Clip clip = sounds.get(sound.getSoundEffect());
		if(clip == null) {
			System.err.println("isSoundRunning() clip is null");
			return false;
		}
		
		return clip.isRunning();
	}
	public static Clip getClip(Sound sound) {
		return sounds.get(sound.getSoundEffect());
	}
	
	public static void playSoundWithEnd(Sound sound, Semaphore semaphore) {
		Clip clip = sounds.get(sound.getSoundEffect());
		if(clip == null) {
			System.err.println("playSound() clip is null");
			return;
		}
		semaphore.drainPermits();
		clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
//                System.out.println("Sound finished playing.");
            	semaphore.release();
                clip.close(); // Close the clip when done
            }
        });
		
		// if already playing, stop and then restart, TODO might not want this
//		if(clip.isRunning()) {
//			clip.stop();
//		}
		
		
		clip.setFramePosition(0);
		clip.start();
	}
	
	// plays a sound
	public static void playSound(Sound sound) {
		Clip clip = sounds.get(sound.getSoundEffect());
		
		if(clip == null) {
			System.out.println("playSound() clip is null");
			return;
		}
		// if already playing, stop and then restart, TODO might not want this
		if(clip.isRunning()) {
			clip.stop();
		}
		
		
		clip.setFramePosition(0);
		clip.start();
	}
	
	// sound, -5.0f [dB]	// 0 - 1
	public static void setVolume(Sound sound, float volume) {
	    Clip clip = sounds.get(sound.getSoundEffect());
	    try {
			clip.open();
		} catch (LineUnavailableException e) {
			
			e.printStackTrace();
		}
//	    float volumedB = (float) (20 * Math.log(volume/0.5));
//	    volumedB *= 2;
	    
	    
	    
	    
	    if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
	        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	        
	     // Convert linear scale (0-10) to dB
	        double minGain = volumeControl.getMinimum();  // Typically around -80 dB
	        double maxGain = volumeControl.getMaximum();  // Typically around 6 dB
	        double volumedB;

	        if (volume <= 0) {
	            volumedB = minGain; // Mute if volume is 0
	        } else {
	            double normalizedVolume = volume / 10.0; // Convert to 0 - 10 range
	            volumedB = 20.0 * Math.log10(normalizedVolume);
	            volumedB = Math.max(minGain, Math.min(volumedB, maxGain)); // Ensure it's within valid dB range
	        }
	        
	        volumedB = Math.max(volumeControl.getMinimum(), Math.min(volumedB, volumeControl.getMaximum())); // Clamp value
	        
	        volumeControl.setValue((float)volumedB);
	    } else {
	        System.err.println("Volume control not supported for sound: " + sound);
	    }
	}
	
	
//	public void addToQueue() {
//		
//	}

}