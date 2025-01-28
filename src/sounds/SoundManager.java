package sounds;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.*;

import ui.graphics.vanilla.VanillaDrawer;
import utils.TileLoc;

public class SoundManager {

	private static final Map<SoundEffect, Clip> sounds = new HashMap<>();
	private static final String basePath = "sounds/";

	public static LinkedBlockingQueue<Sound> theSoundQueue = new LinkedBlockingQueue<Sound>();
	
	
	
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
	
	public static void playSoundWithEnd(Sound sound) {
		Clip clip = sounds.get(sound.getSoundEffect());
		if(clip == null) {
			System.out.println("playSound() clip is null");
			return;
		}
		
		clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
//                System.out.println("Sound finished playing.");
            	
                clip.close(); // Close the clip when done
            }
        });
		
		// if already playing, stop and then restart, TODO might not want this
		if(clip.isRunning()) {
			clip.stop();
		}
		
		
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
	
	// sound, -5.0f [dB]
	public static void setVolume(Sound sound, float volume) {
	    Clip clip = sounds.get(sound.getSoundEffect());
	    try {
			clip.open();
		} catch (LineUnavailableException e) {
			
			e.printStackTrace();
		}
	    if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
	        FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	        volume = Math.max(volumeControl.getMinimum(), Math.min(volume, volumeControl.getMaximum())); // Clamp value
	        volumeControl.setValue(volume);
	    } else {
	        System.err.println("Volume control not supported for sound: " + sound);
	    }
	}
	
	
//	public void addToQueue() {
//		
//	}

}