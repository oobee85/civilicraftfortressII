package sounds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.*;

public class SoundManager {

	private static final Map<SoundEffect, Clip> sounds = new HashMap<>();
	private static final String basePath = "sounds/";

	public static LinkedBlockingQueue<SoundEffect> theSoundQueue = new LinkedBlockingQueue<SoundEffect>();
	
	// theoretically loads a sound
	public static void LoadSound(SoundEffect sound) {
		if(sounds.containsKey(sound)) {
			return;
		}
		try (InputStream stream = SoundManager.class.getClassLoader().getResourceAsStream(basePath + sound.getFileName()); 
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(stream);
				) {
			
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			sounds.put(sound, clip);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.out.println("Couldnt load sound: " + sound.getFileName());
			e.printStackTrace();
		}
	}
	
	// plays a sound
	public static void PlaySound (SoundEffect sound) {
		Clip clip = sounds.get(sound);
		
		if(clip == null) {
			System.out.println("playSound() clip is null");
			return;
		}
		// if already playing, stop and then restart, TODO might not want this
//		if(clip.isRunning()) {
//			clip.stop();
//		}
		clip.setFramePosition(0);
		clip.start();
	}
//	public void addToQueue() {
//		
//	}

}