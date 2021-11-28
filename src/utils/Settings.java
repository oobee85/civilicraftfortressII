package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
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
		DEBUG |= flags.contains("debug");
		SPAWN_EXTRA |= flags.contains("spawn_extra");
		CINEMATIC |= flags.contains("cinematic");
	}
	
	public static void toFile() {
		try (PrintWriter pw = new PrintWriter(new FileWriter(new File("settings.ini")))){
			for(Field f : Settings.class.getFields()) {
				try {
					pw.println(f.getName() + "=" + f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void fromFile() {
		HashMap<String, String> values = new HashMap<>();
		try(BufferedReader reader = new BufferedReader(new FileReader(new File("settings.ini")))) {
			reader.lines().forEach(line -> {
				String[] spl = line.split("=");
				String name = spl[0].trim();
				String value = spl[1].trim();
				values.put(name, value);
			});
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(Entry<String, String> entry : values.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			try {
				Field f = Settings.class.getField(name);
				if(f.getType() == boolean.class) {
					f.set(null, Boolean.parseBoolean(value));
				}
				else if(f.getType() == int.class) {
					f.set(null, Integer.parseInt(value));
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}