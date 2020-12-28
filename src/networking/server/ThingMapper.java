package networking.server;

import java.util.*;

import utils.*;

public class ThingMapper {
	
	public static boolean ACTIVE = false;

	private transient static HashMap<Integer, Thing> thingMap = new HashMap<>();
	public static void removed(Thing thing) {
		if(ACTIVE) {
			thingMap.remove(thing.id());
		}
	}
	public static void created(Thing thing) {
		if(ACTIVE) {
			thingMap.put(thing.id(), thing);
		}
	}
	
	public static Thing get(int id) {
		return thingMap.get(id);
	}
}
