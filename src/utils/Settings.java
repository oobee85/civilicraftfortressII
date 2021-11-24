package utils;

import java.util.HashSet;
import java.util.Set;

public class Settings {
	public static boolean CMD_ARG_DEBUG;
	public static boolean CMD_ARG_SPAWN_EXTRA;
	public static boolean CMD_ARG_CINEMATIC;
	
	public static void fromCmdArgs(String[] args) {
		Set<String> flags = new HashSet<>();
		for(int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
			flags.add(args[i].toLowerCase());
		}
		CMD_ARG_DEBUG = flags.contains("debug");
		CMD_ARG_SPAWN_EXTRA = flags.contains("spawn_extra");
		CMD_ARG_CINEMATIC = flags.contains("cinematic");
	}
}
