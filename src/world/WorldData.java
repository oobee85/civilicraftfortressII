package world;

import java.util.*;

import game.*;
import networking.server.*;
import utils.*;

public class WorldData {

	public LinkedList<Plant> plants = new LinkedList<Plant>();
	public LinkedList<Plant> newPlants = new LinkedList<Plant>();
	private LinkedList<Unit> units = new LinkedList<Unit>();
	private LinkedList<Unit> newUnits = new LinkedList<Unit>();
	public LinkedList<Building> buildings = new LinkedList<Building>();
	public LinkedList<Building> newBuildings = new LinkedList<Building>();
	
	private LinkedList<Projectile> projectiles = new LinkedList<Projectile>();
	private LinkedList<Projectile> newProjectiles = new LinkedList<Projectile>();
	
	private LinkedList<GroundModifier> groundModifiers = new LinkedList<GroundModifier>();
	private LinkedList<GroundModifier> newGroundModifiers = new LinkedList<GroundModifier>();
	
	public LinkedList<WeatherEvent> weatherEvents = new LinkedList<WeatherEvent>();
	public LinkedList<WeatherEvent> newWeatherEvents = new LinkedList<WeatherEvent>();
	
	// Stuff server keeps track of to send to clients
	private LinkedList<Projectile> projectilesToSend = new LinkedList<>();
	private LinkedList<Thing> deadThings = new LinkedList<>();

	public void addUnit(Unit unit) {
		synchronized(newUnits) {
			newUnits.add(unit);
		}
	}
	public LinkedList<Unit> getUnits() {
		return units;
	}
	public void filterDeadUnits() {
		// UNITS
		LinkedList<Unit> unitsNew = new LinkedList<Unit>();
		for (Unit unit : units) {
			if (unit.isDead() == true) {
				unit.getTile().removeUnit(unit);
				ThingMapper.removed(unit);
				addDeadThing(unit);
			} else {
				unitsNew.add(unit);
			}
		}
		synchronized(newUnits) {
			unitsNew.addAll(newUnits);
			newUnits.clear();
		}
		units = unitsNew;
	}
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

	public void addGroundModifier(GroundModifier gm) {
		synchronized (newGroundModifiers) {
			newGroundModifiers.add(gm);
		}
	}
	
	public void filterDeadGroundModifiers() {
		LinkedList<GroundModifier> groundModifiersNew = new LinkedList<GroundModifier>();
		for(GroundModifier modifier : groundModifiers) {
			Tile tile = modifier.getTile();
			if(modifier.isDead() == false) {
				groundModifiersNew.add(modifier);
			} else {
				tile.setModifier(null);
			}
		}
		synchronized (newGroundModifiers) {
			groundModifiersNew.addAll(newGroundModifiers);
			newGroundModifiers.clear();
		}
		groundModifiers = groundModifiersNew;
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
	
	@Override
	public String toString() {
		return 	"units: " 				+ units.size() + 
				" \tbuildings: " 		+ buildings.size() + 
				" \tplants: " 			+ plants.size() + 
				" \tgroundModifiers: " 	+ groundModifiers.size() + 
				" \tprojectiles: " 		+ getProjectiles().size();
	}
}
