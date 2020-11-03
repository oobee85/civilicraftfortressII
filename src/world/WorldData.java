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
	
	private LinkedList<Projectile> projectiles = new LinkedList<Projectile>();
	private LinkedList<Projectile> newProjectiles = new LinkedList<Projectile>();
	
	public LinkedList<GroundModifier> groundModifiers = new LinkedList<GroundModifier>();
	public LinkedList<GroundModifier> newGroundModifiers = new LinkedList<GroundModifier>();
	public LinkedList<WeatherEvent> weatherEvents = new LinkedList<WeatherEvent>();
	public LinkedList<WeatherEvent> newWeatherEvents = new LinkedList<WeatherEvent>();
	
	// Stuff server keeps track of to send to clients
	private LinkedList<Projectile> projectilesToSend = new LinkedList<>();
	private LinkedList<Thing> deadThings = new LinkedList<>();

	public void addProjectile(Projectile newProjectile) {
		synchronized(newProjectiles) {
			newProjectiles.add(newProjectile);
		}
	}
	public LinkedList<Projectile> getProjectiles() {
		return projectiles;
	}
	public void filterDeadProjectiles() {
		// PROJECTILES
		LinkedList<Projectile> projectilesNew = new LinkedList<Projectile>();
		for(Projectile projectile : projectiles) {
			if(projectile.reachedTarget()) {
				projectile.getTile().removeProjectile(projectile);
			} else {
				projectilesNew.add(projectile);
			}
		}
		synchronized(newProjectiles) {
			synchronized(projectilesToSend) {
				projectilesToSend.addAll(newProjectiles);
			}
			projectilesNew.addAll(newProjectiles);
			newProjectiles.clear();
		}
		projectiles = projectilesNew;
	}
	public LinkedList<Projectile> clearProjectilesToSend() {
		LinkedList<Projectile> copy = new LinkedList<>();
		synchronized (projectilesToSend) {
			copy.addAll(projectilesToSend);
			projectilesToSend.clear();
		}
		return copy;
	}
	
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
