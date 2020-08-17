package wildlife;

import game.*;
import world.*;

public class AnimalFactory {

	public static Animal makeAnimal(UnitType type, Tile tile) {
		if(type == UnitType.FLAMELET) {
			return new Flamelet(tile, false);
		}
		else if(type == UnitType.OGRE) {
			return new Ogre(tile, false);
		}
		else if(type == UnitType.PARASITE) {
			return new Parasite(tile, false);
		}
		else if(type == UnitType.WEREWOLF) {
			return new Werewolf(tile, false);
		}
		else if(type == UnitType.WATER_SPIRIT) {
			return new WaterSpirit(tile, false);
		}
		else if(type == UnitType.LAVAGOLEM) {
			return new LavaGolem(tile, false);
		}
		else if(type == UnitType.ENT) {
			return new Ent(tile, false);
		}
		else if(type == UnitType.DRAGON) {
			return new Dragon(tile, false);
		}
		else {
			return new Animal(type, tile, false);
		}
	}
}
