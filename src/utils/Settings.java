package utils;

import java.util.HashSet;
import java.util.Set;

public class Settings {
	public static boolean DEBUG;
	public static boolean SPAWN_EXTRA;
	public static boolean CINEMATIC;
	public static boolean DISABLE_VOLCANO_ERUPT = true;
	public static boolean DISABLE_ENEMY_SPAWNS = true;
	public static boolean DISABLE_WILDLIFE_SPAWNS = false;

	public static int NUM_AI = 0;
	public static int WORLD_WIDTH = 128;
	public static int WORLD_HEIGHT = 128;

	public static void fromCmdArgs(String[] args) {
		Set<String> flags = new HashSet<>();
		for(int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
			flags.add(args[i].toLowerCase());
		}
		DEBUG = flags.contains("debug");
		SPAWN_EXTRA = flags.contains("spawn_extra");
		CINEMATIC = flags.contains("cinematic");
	}
}
