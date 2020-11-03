package world;

import java.util.*;

import game.*;
import utils.*;

public class WorldData {

	public LinkedList<Plant> plants = new LinkedList<Plant>();
	public LinkedList<Plant> newPlants = new LinkedList<Plant>();
	public LinkedList<Unit> units = new LinkedList<Unit>();
	public LinkedList<Unit> newUnits = new LinkedList<Unit>();
	public LinkedList<Building> buildings = new LinkedList<Building>();
	public LinkedList<Building> newBuildings = new LinkedList<Building>();
	public LinkedList<Building> plannedBuildings = new LinkedList<Building>();
	
	public LinkedList<Projectile> projectiles = new LinkedList<Projectile>();
	public LinkedList<Projectile> newProjectiles = new LinkedList<Projectile>();
	public LinkedList<GroundModifier> groundModifiers = new LinkedList<GroundModifier>();
	public LinkedList<GroundModifier> newGroundModifiers = new LinkedList<GroundModifier>();
	public LinkedList<WeatherEvent> weatherEvents = new LinkedList<WeatherEvent>();
	public LinkedList<WeatherEvent> newWeatherEvents = new LinkedList<WeatherEvent>();
	
	private LinkedList<Thing> deadThings = new LinkedList<>();
	
	public void addDeadThing(Thing deadThing) {
		synchronized (deadThings) {
			deadThings.add(deadThing);
		}
	}
	public LinkedList<Thing> clearDeadThings() {
		LinkedList<Thing> copy = new LinkedList<>();
		synchronized (deadThings) {
			copy.addAll(deadThings);
			deadThings.clear();
		}
		return copy;
	}
}
